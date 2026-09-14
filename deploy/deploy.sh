#!/bin/sh
# ── WMS 部署脚本（在目标服务器上执行）──
# 由 Jenkins 通过 Publish over SSH 传输文件后调用: sh /opt/wms/deploy.sh
# 刻意使用 POSIX sh 语法，不依赖服务器一定装有 bash。
#
# 服务器目录结构：
#   /opt/wms/docker-compose.yml
#   /opt/wms/deploy.sh
#   /opt/wms/init.sql                       （首次建库用）
#   /opt/wms/server/Dockerfile + app.jar + wms-web-<ver>.jar
#   /opt/wms/web/Dockerfile + nginx.conf + dist/
#
# 依赖的基础设施（由服务器上已有的 mysql/redis compose 管理，本脚本只复用、不接管）。
# 容器名、库名等环境相关的值都有默认值，可在 /opt/wms/.env 中覆盖，见 deploy/.env.example。
set -eu

WMS_HOME="${WMS_HOME:-/opt/wms}"
COMPOSE_PROJECT="${COMPOSE_PROJECT:-wms}"

# mysql 官方镜像里 mysql 客户端默认字符集是 latin1（容器无 LANG 环境变量）。
# 不带这个参数导入 init.sql 会把中文写成双重编码（乱码），必须显式指定。
MYSQL_CHARSET=--default-character-set=utf8mb4

cd "$WMS_HOME"

# 先加载 /opt/wms/.env（deploy.sh 与 docker compose 读的是同一个文件）
if [ -f "$WMS_HOME/.env" ]; then
  . "$WMS_HOME/.env"
fi

# ───── 以下均为默认值，可在 /opt/wms/.env 中覆盖 ─────
# 已有的基础设施容器名（本部署只复用，不接管）
DB_CONTAINER="${WMS_DB_CONTAINER:-mysql8}"
REDIS_CONTAINER="${WMS_REDIS_CONTAINER:-redis7}"
DB_NAME="${WMS_DB_NAME:-ml_wms}"

MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"
export MYSQL_USER MYSQL_PASSWORD

# 后端连接用的主机名默认取容器名（同一个 docker 网络内可直接解析容器名）；
# 数据库在宿主机或别处时，用 WMS_DB_HOST / WMS_REDIS_HOST 显式指定。
export WMS_DB_HOST="${WMS_DB_HOST:-$DB_CONTAINER}"
export WMS_REDIS_HOST="${WMS_REDIS_HOST:-$REDIS_CONTAINER}"
export WMS_DB_PORT="${WMS_DB_PORT:-3306}"
export WMS_REDIS_PORT="${WMS_REDIS_PORT:-6379}"
export WMS_DB_NAME="$DB_NAME"

echo "[deploy] ============== WMS 部署开始 =============="

# 1) 取最近一次上传的 jar 作为运行包（避免版本号变化后 Dockerfile 失效）
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
else
  echo "[deploy] 未找到 wms-web-*.jar，沿用已有 server/app.jar"
fi

# 2) 探测 mysql / redis 所在网络并复用
NET=""
for c in "$DB_CONTAINER" "$REDIS_CONTAINER"; do
  n="$(docker inspect -f '{{range $k,$v := .NetworkSettings.Networks}}{{println $k}}{{end}}' "$c" 2>/dev/null | head -1 || true)"
  if [ -n "$n" ] && [ "$n" != "bridge" ] && [ "$n" != "host" ]; then
    NET="$n"
    break
  fi
done

if [ -z "$NET" ]; then
  NET="backend"
  docker network inspect "$NET" >/dev/null 2>&1 || docker network create "$NET"
  for c in "$DB_CONTAINER" "$REDIS_CONTAINER"; do
    docker network connect "$NET" "$c" 2>/dev/null || true
  done
fi

WMS_NET="$NET"
export WMS_NET
echo "[deploy] 复用 Docker 网络: $WMS_NET"

# 3) 选择 compose 命令（新旧版本兼容）
if docker compose version >/dev/null 2>&1; then
  COMPOSE="docker compose"
else
  COMPOSE="docker-compose"
fi

# 4) 数据库：不存在则导入 init.sql；存在但结构不完整（失败导入留下的半成品）则明确失败，
#    避免"库里没有表却每次都跳过初始化"这种静默故障。
if docker inspect "$DB_CONTAINER" >/dev/null 2>&1; then
  EXISTS="$(docker exec "$DB_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" $MYSQL_CHARSET -N -B -e "SHOW DATABASES LIKE '$DB_NAME'" 2>/dev/null || true)"

  if [ -z "$EXISTS" ]; then
    if [ -f "$WMS_HOME/init.sql" ]; then
      echo "[deploy] 数据库 $DB_NAME 不存在，导入 init.sql ..."
      docker exec -i "$DB_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" $MYSQL_CHARSET < "$WMS_HOME/init.sql"
      echo "[deploy] 数据库初始化完成"
    else
      echo "[deploy] ⚠️ 未找到 init.sql，跳过数据库初始化"
    fi
  else
    # 用建表顺序里最后一张表判断结构是否完整
    COMPLETE="$(docker exec "$DB_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" $MYSQL_CHARSET -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME' AND table_name='wms_print_record'" 2>/dev/null || echo 0)"
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

# 5) 清理不属于本 compose 项目的同名旧容器（避免 container name 冲突）
for c in wms-server wms-web; do
  if docker inspect "$c" >/dev/null 2>&1; then
    proj="$(docker inspect -f '{{index .Config.Labels "com.docker.compose.project"}}' "$c" 2>/dev/null || true)"
    if [ "$proj" != "$COMPOSE_PROJECT" ]; then
      echo "[deploy] 移除旧的同名容器: $c (project=${proj:-none})"
      docker rm -f "$c" >/dev/null 2>&1 || true
    fi
  fi
done

# 6) 构建镜像并启动（只影响 server / web，不触碰 mysql、redis）
$COMPOSE -p "$COMPOSE_PROJECT" -f "$WMS_HOME/docker-compose.yml" up -d --build

# 7) 清理悬空镜像，避免磁盘堆积
docker image prune -f >/dev/null 2>&1 || true

echo "[deploy] 当前容器状态:"
$COMPOSE -p "$COMPOSE_PROJECT" -f "$WMS_HOME/docker-compose.yml" ps

# 8) 健康检查
echo "[deploy] 等待后端就绪..."
i=0
while [ "$i" -lt 20 ]; do
  i=$((i + 1))
  sleep 3
  if wget -q -O - http://localhost:8080/doc.html >/dev/null 2>&1; then
    break
  fi
done

if wget -q -O - http://localhost:8080/doc.html >/dev/null 2>&1; then
  echo "[deploy] ✅ 后端 8080 可达"
else
  echo "[deploy] ⚠️ 后端 8080 未响应，请查看: docker logs wms-server --tail 100"
fi

if wget -q -O - http://localhost/ >/dev/null 2>&1; then
  echo "[deploy] ✅ 前端 80 可达"
else
  echo "[deploy] ⚠️ 前端 80 未响应，请查看: docker logs wms-web --tail 50"
fi

echo "[deploy] ============== 部署完成 =============="
