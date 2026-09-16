#!/bin/sh
# ── WMS 部署脚本（在目标服务器上执行）──
# 由 Jenkins 通过 Publish over SSH 传输文件后调用，也可以手工执行：
#   sh /opt/wms/deploy.sh
#   sh /opt/wms/deploy.sh --release b123-abc1234 --components server,web
#   sh /opt/wms/deploy.sh --no-rollback
#
# 发布模型：
#   - 每次发布给镜像打独立 tag（server / web 各一个，未重建的服务沿用上一版的 tag），
#     旧镜像保留在本地 ⇒ 出问题可以秒级回滚（deploy/rollback.sh）。
#   - 按 --components 只重建有改动的服务，另一个服务不动（避免无谓重启）。
#   - 健康检查失败时返回非 0（Jenkins 会红灯），并自动回滚到上一个发布。
#   - 发布状态记录在 /opt/wms/.releases/ 下（history 为发布顺序，新 → 旧）。
#
# 服务器目录结构：
#   /opt/wms/docker-compose.yml
#   /opt/wms/deploy.sh / rollback.sh / lib.sh
#   /opt/wms/init.sql                       （首次建库用）
#   /opt/wms/release.env                    （可选，Jenkins 写入的发布元信息）
#   /opt/wms/.env                           （环境配置，模板见 deploy/.env.example）
#   /opt/wms/.releases/                     （发布状态，脚本自己维护）
#   /opt/wms/server/Dockerfile + app.jar + wms-web-<ver>.jar
#   /opt/wms/web/Dockerfile + nginx.conf + dist/
#
# 依赖的基础设施（由服务器上已有的 mysql/redis compose 管理，本脚本只复用、不接管）。
# 容器名、库名等环境相关的值都有默认值，可在 /opt/wms/.env 中覆盖，见 deploy/.env.example。
set -eu

WMS_HOME="${WMS_HOME:-/opt/wms}"
COMPOSE_PROJECT="${COMPOSE_PROJECT:-wms}"
STATE_DIR="$WMS_HOME/.releases"
HISTORY="$STATE_DIR/history"
WMS_LOG_PREFIX=deploy

usage() {
  cat <<'EOF'
用法: sh deploy.sh [选项]

  --release <id>        发布标识（写进镜像 tag 与发布记录）。默认 manual-<时间戳>
  --components <列表>   要重建的服务，逗号分隔：server / web / all（默认 all）
  --no-rollback         健康检查失败时不自动回滚（默认会回滚）
  -h, --help            显示本帮助

对应的环境变量（可在 /opt/wms/.env 中覆盖）：
  WMS_ROLLBACK_ON_FAILURE=0   等价于 --no-rollback
  WMS_KEEP_JARS=5             本地保留的 wms-web-*.jar 份数
  WMS_KEEP_RELEASES=10        发布记录保留条数（同时也是可回滚深度上限）
EOF
}

# ───── 命令行参数 ─────
RELEASE=""
RELEASE_FROM_CLI=0
COMPONENTS="all"
ROLLBACK_ON_FAILURE="${WMS_ROLLBACK_ON_FAILURE:-1}"

while [ $# -gt 0 ]; do
  case "$1" in
    --release)
      [ $# -ge 2 ] || { echo "[deploy] ❌ --release 缺少参数" >&2; exit 2; }
      RELEASE="$2"; RELEASE_FROM_CLI=1; shift 2 ;;
    --release=*)     RELEASE="${1#*=}"; RELEASE_FROM_CLI=1; shift ;;
    --components)
      [ $# -ge 2 ] || { echo "[deploy] ❌ --components 缺少参数" >&2; exit 2; }
      COMPONENTS="$2"; shift 2 ;;
    --components=*)  COMPONENTS="${1#*=}"; shift ;;
    --no-rollback)   ROLLBACK_ON_FAILURE=0; shift ;;
    -h|--help)       usage; exit 0 ;;
    *) echo "[deploy] ❌ 未知参数: $1" >&2; usage >&2; exit 2 ;;
  esac
done

if [ ! -f "$WMS_HOME/lib.sh" ]; then
  echo "[deploy] ❌ 缺少 $WMS_HOME/lib.sh，请重新执行一次完整发布（或从仓库 deploy/ 目录拷贝）" >&2
  exit 1
fi
# shellcheck source=/dev/null
. "$WMS_HOME/lib.sh"

cd "$WMS_HOME"

# 先加载 /opt/wms/.env（deploy.sh 与 docker compose 读的是同一个文件）
wms_load_env

# ───── 以下均为默认值，可在 /opt/wms/.env 中覆盖 ─────
# 已有的基础设施容器名（本部署只复用，不接管）
DB_CONTAINER="${WMS_DB_CONTAINER:-mysql8}"
REDIS_CONTAINER="${WMS_REDIS_CONTAINER:-redis7}"

# 后端连接用的主机名默认取容器名（同一个 docker 网络内可直接解析容器名）；
# 数据库在宿主机或别处时，用 WMS_DB_HOST / WMS_REDIS_HOST 显式指定。
# 这段派生逻辑放在 lib.sh 里，rollback.sh 走同一份 —— 两边不一致会导致回滚后连错库。
wms_init_connection_env

KEEP_JARS="${WMS_KEEP_JARS:-5}"
KEEP_RELEASES="${WMS_KEEP_RELEASES:-10}"

wms_init_health
wms_init_compose

# ───── 1) 发布标识与本次要重建的服务 ─────
# 手工执行时给一个时间戳标识，保证每次发布都有独立的 tag / 记录
if [ -z "$RELEASE" ]; then
  RELEASE="manual-$(date +%Y%m%d-%H%M%S)"
fi
# 收敛成安全的镜像 tag（docker tag 只允许 [A-Za-z0-9._-]）
RELEASE="$(printf '%s' "$RELEASE" | tr -c 'A-Za-z0-9._-' '-')"

DO_SERVER=0
DO_WEB=0
case "$COMPONENTS" in
  ''|all) DO_SERVER=1; DO_WEB=1 ;;
  *)
    _old_ifs="$IFS"; IFS=','
    for _c in $COMPONENTS; do
      case "$_c" in
        server) DO_SERVER=1 ;;
        web)    DO_WEB=1 ;;
        '')     ;;
        *) echo "[deploy] ❌ 未知的 components 项: $_c（可选 server / web / all）" >&2; IFS="$_old_ifs"; exit 2 ;;
      esac
    done
    IFS="$_old_ifs"
    ;;
esac
if [ "$DO_SERVER" = 0 ] && [ "$DO_WEB" = 0 ]; then
  echo "[deploy] ❌ --components 为空，没有需要重建的服务" >&2
  exit 2
fi

COMPONENTS_LIST=""
[ "$DO_SERVER" = 1 ] && COMPONENTS_LIST="server"
[ "$DO_WEB" = 1 ] && COMPONENTS_LIST="${COMPONENTS_LIST:+$COMPONENTS_LIST,}web"

# 发布元信息。release.env 是 Jenkins 这次发布写进来的，只在「由 CI 指定发布标识」时采用；
# 手工执行时不能用它 —— 否则会把上一次 CI 的 commit 记到这次手工发布头上。
GIT_SHA=""
BUILD_NUMBER=""
if [ "$RELEASE_FROM_CLI" = 1 ] && [ -f "$WMS_HOME/release.env" ]; then
  GIT_SHA="$(wms_state_value "$WMS_HOME/release.env" WMS_GIT_SHA || true)"
  BUILD_NUMBER="$(wms_state_value "$WMS_HOME/release.env" WMS_BUILD_NUMBER || true)"
fi

# ───── 2) 读取当前发布（未重建的服务沿用它的 tag；健康检查失败时回滚到它）─────
mkdir -p "$STATE_DIR"
CUR_RELEASE="$(wms_current_release || true)"
CUR_SERVER_TAG=""
CUR_WEB_TAG=""
if [ -n "$CUR_RELEASE" ] && [ -f "$STATE_DIR/$CUR_RELEASE.env" ]; then
  CUR_SERVER_TAG="$(wms_state_value "$STATE_DIR/$CUR_RELEASE.env" WMS_SERVER_TAG || true)"
  CUR_WEB_TAG="$(wms_state_value "$STATE_DIR/$CUR_RELEASE.env" WMS_WEB_TAG || true)"
fi

# 还没有任何发布记录（第一次用这套发布机制）时没有可信的历史 tag 可沿用，
# 而 latest 是会移动的别名（后面每次发布都会把它指向新镜像），沿用它会导致
# 「回滚到这一次发布，却拿到另一个版本的镜像」。所以这一次强制全量发布，把两个 tag 钉死。
if [ -z "$CUR_RELEASE" ]; then
  if [ "$DO_SERVER" = 0 ] || [ "$DO_WEB" = 0 ]; then
    echo "[deploy] 首次建立发布记录：本次按全量发布处理（server + web 都重建）"
    DO_SERVER=1
    DO_WEB=1
    COMPONENTS_LIST="server,web"
  fi
fi

NEW_SERVER_TAG="$CUR_SERVER_TAG"
NEW_WEB_TAG="$CUR_WEB_TAG"
[ "$DO_SERVER" = 1 ] && NEW_SERVER_TAG="$RELEASE"
[ "$DO_WEB" = 1 ] && NEW_WEB_TAG="$RELEASE"
# 兜底（正常不会走到这里）：既没有历史 tag 又没重建该服务
if [ -z "$NEW_SERVER_TAG" ]; then
  NEW_SERVER_TAG="latest"
  echo "[deploy] ⚠️ 缺少后端历史 tag，本次使用 latest"
fi
if [ -z "$NEW_WEB_TAG" ]; then
  NEW_WEB_TAG="latest"
  echo "[deploy] ⚠️ 缺少前端历史 tag，本次使用 latest"
fi
export WMS_SERVER_TAG="$NEW_SERVER_TAG" WMS_WEB_TAG="$NEW_WEB_TAG"

echo "[deploy] ============== WMS 部署开始 =============="
echo "[deploy] 发布标识: $RELEASE （重建: $COMPONENTS_LIST）"
echo "[deploy] 镜像: wms-server:$NEW_SERVER_TAG / wms-web:$NEW_WEB_TAG"
if [ -n "$CUR_RELEASE" ]; then
  echo "[deploy] 当前版本: $CUR_RELEASE （失败时回滚目标）"
fi

# ───── 3) 后端取最近一次上传的 jar 作为运行包（避免版本号变化后 Dockerfile 失效）─────
if [ "$DO_SERVER" = 1 ]; then
  NEWEST_JAR=""
  for f in "$WMS_HOME"/server/wms-web-*.jar; do
    [ -f "$f" ] || continue
    if [ -z "$NEWEST_JAR" ] || [ "$f" -nt "$NEWEST_JAR" ]; then
      NEWEST_JAR="$f"
    fi
  done

  if [ -n "$NEWEST_JAR" ]; then
    cp -f "$NEWEST_JAR" "$WMS_HOME/server/app.jar"
    echo "[deploy] 构建产物: $(basename "$NEWEST_JAR")"
  elif [ ! -f "$WMS_HOME/server/app.jar" ]; then
    echo "[deploy] ❌ 既没有 wms-web-*.jar 也没有 server/app.jar，无法构建后端镜像" >&2
    exit 1
  else
    echo "[deploy] ⚠️ 未找到新的 wms-web-*.jar，沿用已有 server/app.jar"
  fi
fi

# ───── 4) 选择 compose 命令 + 探测 docker 网络 ─────
wms_init_network

# ───── 5) 数据库：不存在则导入 init.sql；存在但结构不完整（失败导入留下的半成品）则明确失败，
#          避免"库里没有表却每次都跳过初始化"这种静默故障。
#          另外 mysql 官方镜像里 mysql 客户端默认字符集是 latin1（容器无 LANG 环境变量），
#          不指定 utf8mb4 会把中文写成双重编码（乱码），必须显式指定。
if docker inspect "$DB_CONTAINER" >/dev/null 2>&1; then
  # 口令不放命令行（同机器上任何用户 ps 都能看到）：写进 DB 容器里的临时 option 文件，
  # 内容经 stdin 传入、权限 600，脚本退出时删除。写不进去才退回命令行传参。
  MYSQL_CNF="/tmp/.wms-deploy-my.cnf"
  MYSQL_OPTS="-u$MYSQL_USER"
  if docker exec -i "$DB_CONTAINER" sh -c "umask 077; cat > $MYSQL_CNF" <<EOF
[client]
user=$MYSQL_USER
password=$MYSQL_PASSWORD
EOF
  then
    # --defaults-extra-file 必须是第一个参数
    MYSQL_OPTS="--defaults-extra-file=$MYSQL_CNF -u$MYSQL_USER"
    trap 'docker exec "$DB_CONTAINER" rm -f "$MYSQL_CNF" >/dev/null 2>&1 || true' EXIT
  else
    echo "[deploy] ⚠️ 无法写入临时口令文件，本次退回命令行传口令（ps 可见）"
    MYSQL_OPTS="-u$MYSQL_USER -p$MYSQL_PASSWORD"
  fi

  EXISTS="$(docker exec "$DB_CONTAINER" mysql $MYSQL_OPTS --default-character-set=utf8mb4 -N -B -e "SHOW DATABASES LIKE '$DB_NAME'" 2>/dev/null || true)"

  if [ -z "$EXISTS" ]; then
    if [ -f "$WMS_HOME/init.sql" ]; then
      echo "[deploy] 数据库 $DB_NAME 不存在，导入 init.sql ..."
      docker exec -i "$DB_CONTAINER" mysql $MYSQL_OPTS --default-character-set=utf8mb4 < "$WMS_HOME/init.sql"
      echo "[deploy] 数据库初始化完成"
    else
      echo "[deploy] ⚠️ 未找到 init.sql，跳过数据库初始化"
    fi
  else
    # 用建表顺序里最后一张表判断结构是否完整
    COMPLETE="$(docker exec "$DB_CONTAINER" mysql $MYSQL_OPTS --default-character-set=utf8mb4 -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME' AND table_name='wms_print_record'" 2>/dev/null || echo 0)"
    if [ "$COMPLETE" = "1" ]; then
      echo "[deploy] 数据库 $DB_NAME 已存在且结构完整，跳过初始化"
    else
      echo "[deploy] ❌ 数据库 $DB_NAME 存在但结构不完整（缺少 wms_print_record 等表）"
      echo "[deploy]    通常是一次失败导入留下的半成品库。确认可以重建后执行："
      echo "[deploy]      docker exec $DB_CONTAINER mysql -u$MYSQL_USER -p****** -e 'DROP DATABASE $DB_NAME'"
      echo "[deploy]    再重新部署即可自动重建。"
      exit 1
    fi
  fi
else
  echo "[deploy] ⚠️ 未找到容器 $DB_CONTAINER，跳过数据库检查"
  echo "[deploy]    容器名不对的话，请在 /opt/wms/.env 里设置 WMS_DB_CONTAINER / WMS_REDIS_CONTAINER"
fi

# ───── 6) 清理不属于本 compose 项目的同名旧容器（避免 container name 冲突）─────
for c in wms-server wms-web; do
  if docker inspect "$c" >/dev/null 2>&1; then
    proj="$(docker inspect -f '{{index .Config.Labels "com.docker.compose.project"}}' "$c" 2>/dev/null || true)"
    if [ "$proj" != "$COMPOSE_PROJECT" ]; then
      echo "[deploy] 移除旧的同名容器: $c (project=${proj:-none})"
      docker rm -f "$c" >/dev/null 2>&1 || true
    fi
  fi
done

# ───── 7) 构建镜像并启动（只影响本次重建的服务，不触碰 mysql、redis）─────
if [ "$DO_SERVER" = 1 ] && [ "$DO_WEB" = 1 ]; then
  wms_compose up -d --build
elif [ "$DO_SERVER" = 1 ]; then
  wms_compose up -d --build --no-deps server
else
  wms_compose up -d --build --no-deps web
fi

# ───── 8) 健康检查：失败即视为发布失败（Jenkins 红灯），并尝试自动回滚 ─────
HEALTH_FAILED=0
if [ "$DO_SERVER" = 1 ]; then
  wms_wait_http "$WMS_SERVER_HEALTH_URL" "后端" || HEALTH_FAILED=1
fi
if [ "$DO_WEB" = 1 ]; then
  wms_wait_http "$WMS_WEB_HEALTH_URL" "前端" || HEALTH_FAILED=1
fi

# 本次没重建的服务只提示状态，不影响本次成败
if [ "$DO_SERVER" = 0 ] && ! wms_http_ok "$WMS_SERVER_HEALTH_URL"; then
  echo "[deploy] ⚠️ 后端当前不可达（本次未重建后端）"
fi
if [ "$DO_WEB" = 0 ] && ! wms_http_ok "$WMS_WEB_HEALTH_URL"; then
  echo "[deploy] ⚠️ 前端当前不可达（本次未重建前端）"
fi

if [ "$HEALTH_FAILED" = 1 ]; then
  echo "[deploy] ❌ 新版本健康检查未通过，发布失败: $RELEASE"
  wms_ps
  echo "[deploy] ---- wms-server 日志尾部 ----"
  docker logs wms-server --tail 50 2>&1 || true
  echo "[deploy] ----------------------------"

  if [ "$ROLLBACK_ON_FAILURE" != 1 ]; then
    echo "[deploy] ⚠️ 已按 --no-rollback 跳过自动回滚，请人工介入"
  elif [ -z "$CUR_RELEASE" ]; then
    echo "[deploy] ⚠️ 没有可回滚的历史版本（这是首次发布），请人工介入"
  elif ! wms_require_images "wms-server:$CUR_SERVER_TAG" "wms-web:$CUR_WEB_TAG"; then
    echo "[deploy] ⚠️ 上一个版本的镜像已被清理，无法自动回滚，请人工介入"
  else
    echo "[deploy] ↩️ 自动回滚到上一个发布: $CUR_RELEASE (wms-server:$CUR_SERVER_TAG / wms-web:$CUR_WEB_TAG)"
    export WMS_SERVER_TAG="$CUR_SERVER_TAG" WMS_WEB_TAG="$CUR_WEB_TAG"
    # 编排文件也一起回退（若该发布留有快照），避免用新编排去跑旧镜像
    PREV_SNAP="$(wms_state_value "$STATE_DIR/$CUR_RELEASE.env" WMS_COMPOSE_SNAPSHOT || true)"
    if [ -n "$PREV_SNAP" ] && [ -f "$PREV_SNAP" ]; then
      WMS_COMPOSE_FILE="$PREV_SNAP"
      export WMS_COMPOSE_FILE
      echo "[deploy] 使用 $CUR_RELEASE 当时的编排快照"
    fi
    wms_compose up -d --no-deps server web
    if wms_wait_http "$WMS_SERVER_HEALTH_URL" "后端(回滚后)"; then
      echo "[deploy] ✅ 已回滚到 $CUR_RELEASE，服务恢复"
    else
      echo "[deploy] ❌ 回滚后后端仍不可达，请人工介入: docker logs wms-server --tail 100"
    fi
  fi
  exit 1
fi

# ───── 9) 记录发布状态（新版本已在运行，这一步失败不影响线上）─────
# 快照本次的编排文件：回滚时用它，让「镜像 + 编排」一起回到当时的状态。
# 注意快照里的 build.context 是相对路径，只用于 up/回滚，不要拿它去 build。
COMPOSE_SNAPSHOT="$STATE_DIR/$RELEASE.compose.yml"
cp -f "$WMS_HOME/docker-compose.yml" "$COMPOSE_SNAPSHOT"

cat > "$STATE_DIR/$RELEASE.env" <<EOF
# 由 deploy.sh 生成：本次发布实际使用的镜像 tag（rollback.sh 依赖此文件）
WMS_RELEASE=$RELEASE
WMS_SERVER_TAG=$NEW_SERVER_TAG
WMS_WEB_TAG=$NEW_WEB_TAG
WMS_COMPONENTS=$COMPONENTS_LIST
WMS_COMPOSE_SNAPSHOT=$COMPOSE_SNAPSHOT
WMS_GIT_SHA=$GIT_SHA
WMS_BUILD_NUMBER=$BUILD_NUMBER
WMS_DEPLOYED_AT=$(date '+%Y-%m-%d %H:%M:%S')
EOF

# history：最新发布在最前；同标识去重；只保留最近 KEEP_RELEASES 条（决定可回滚深度）
_tmp="$HISTORY.tmp.$$"
{
  printf '%s\n' "$RELEASE"
  if [ -f "$HISTORY" ]; then cat "$HISTORY"; fi
} | awk 'NF && !seen[$0]++' | head -n "$KEEP_RELEASES" > "$_tmp"
mv "$_tmp" "$HISTORY"

# latest 别名指向当前发布：手工执行 docker compose 时不必依赖状态文件
if [ "$NEW_SERVER_TAG" != "latest" ]; then
  docker tag "wms-server:$NEW_SERVER_TAG" wms-server:latest >/dev/null 2>&1 || true
fi
if [ "$NEW_WEB_TAG" != "latest" ]; then
  docker tag "wms-web:$NEW_WEB_TAG" wms-web:latest >/dev/null 2>&1 || true
fi

# ───── 10) 清理：不再被任何发布引用的镜像 tag、超出保留份数的 jar、过期的发布记录 ─────
wms_prune_images
wms_prune_releases

_jar_i=0
for _jar_f in $(ls -1t "$WMS_HOME"/server/wms-web-*.jar 2>/dev/null || true); do
  _jar_i=$((_jar_i + 1))
  if [ "$_jar_i" -gt "$KEEP_JARS" ]; then
    rm -f "$_jar_f"
    echo "[deploy] 清理旧 jar: $(basename "$_jar_f")"
  fi
done

echo "[deploy] 当前容器状态:"
wms_ps

echo "[deploy] ============== 部署完成 =============="
echo "[deploy] 发布标识: $RELEASE"
echo "[deploy] 镜像: wms-server:$NEW_SERVER_TAG / wms-web:$NEW_WEB_TAG"
echo "[deploy] 回滚方式: sh $WMS_HOME/rollback.sh  （列出可回滚版本: sh $WMS_HOME/rollback.sh --list）"
