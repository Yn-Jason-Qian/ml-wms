#!/bin/sh
# ── WMS 部署脚本公用函数（被 deploy.sh / rollback.sh source）──
#
# 刻意保持 POSIX sh 语法，不依赖服务器一定装有 bash。
# 调用方需先设置：WMS_HOME / COMPOSE_PROJECT / STATE_DIR / HISTORY / WMS_LOG_PREFIX

wms_log() {
  echo "[${WMS_LOG_PREFIX:-deploy}] $*"
}

# 读取部署目录下的 .env（deploy.sh、rollback.sh 与 docker compose 读的是同一个文件）
wms_load_env() {
  if [ -f "$WMS_HOME/.env" ]; then
    # shellcheck source=/dev/null
    . "$WMS_HOME/.env"
  fi
}

# ── compose ──

# 选择 compose 命令（兼容只有 docker-compose 的老服务器）
wms_init_compose() {
  if docker compose version >/dev/null 2>&1; then
    WMS_COMPOSE="docker compose"
  else
    WMS_COMPOSE="docker-compose"
  fi
}

# 执行 compose，自动带上 -p / --env-file / -f
#
# 编排文件默认取部署目录里的那份；WMS_COMPOSE_FILE 指向别处时以它为准 ——
# 回滚会指向"该发布当时快照下来的那份编排"，让镜像和编排一起回到当时的状态。
wms_compose() {
  _wms_args="-p $COMPOSE_PROJECT"
  if [ -f "$WMS_HOME/.env" ]; then
    _wms_args="$_wms_args --env-file $WMS_HOME/.env"
  fi
  # shellcheck disable=SC2086
  $WMS_COMPOSE $_wms_args -f "${WMS_COMPOSE_FILE:-$WMS_HOME/docker-compose.yml}" "$@"
}

wms_ps() {
  wms_compose ps || true
}

# ── 健康检查 ──

# 健康检查相关的默认值，可在 .env 中覆盖
wms_init_health() {
  WMS_SERVER_HEALTH_URL="${WMS_SERVER_HEALTH_URL:-http://127.0.0.1:8080/doc.html}"
  WMS_WEB_HEALTH_URL="${WMS_WEB_HEALTH_URL:-http://127.0.0.1/}"
  WMS_HEALTH_TRIES="${WMS_HEALTH_TRIES:-40}"
  WMS_HEALTH_INTERVAL="${WMS_HEALTH_INTERVAL:-3}"
}

# 单次 HTTP 探测：wget 优先，退化为 curl；两者都没有时视为失败
wms_http_ok() {
  if command -v wget >/dev/null 2>&1; then
    wget -q -O /dev/null "$1" >/dev/null 2>&1
  elif command -v curl >/dev/null 2>&1; then
    curl -fsS -o /dev/null "$1" >/dev/null 2>&1
  else
    return 1
  fi
}

# 轮询等待 URL 可用；成功返回 0，超时返回 1
wms_wait_http() {
  _wms_url="$1"
  _wms_label="$2"
  _wms_i=0
  while [ "$_wms_i" -lt "$WMS_HEALTH_TRIES" ]; do
    if wms_http_ok "$_wms_url"; then
      wms_log "✅ ${_wms_label} 可达: $_wms_url"
      return 0
    fi
    _wms_i=$((_wms_i + 1))
    if [ "$_wms_i" -lt "$WMS_HEALTH_TRIES" ]; then
      sleep "$WMS_HEALTH_INTERVAL"
    fi
  done
  wms_log "❌ ${_wms_label} 探测失败（已等待 $((WMS_HEALTH_TRIES * WMS_HEALTH_INTERVAL)) 秒）: $_wms_url"
  return 1
}

# ── 发布状态 ──

# 读状态文件里的一个键
wms_state_value() {
  _wms_file="$1"
  _wms_key="$2"
  [ -f "$_wms_file" ] || return 1
  sed -n "s/^${_wms_key}=//p" "$_wms_file" | head -n 1 | tr -d '\r'
}

# 当前发布标识（history 第一行）
wms_current_release() {
  [ -f "$HISTORY" ] || return 1
  head -n 1 "$HISTORY" | tr -d '\r' | tr -d ' '
}

# history 中仍被引用的镜像 tag（清理镜像时的保护名单）；latest 别名永远保留
wms_protected_tags() {
  printf '%s\n' "wms-server:latest" "wms-web:latest"
  [ -f "$HISTORY" ] || return 0
  while IFS= read -r _wms_r; do
    _wms_r=$(printf '%s' "$_wms_r" | tr -d '\r ')
    [ -n "$_wms_r" ] || continue
    _wms_sf="$STATE_DIR/$_wms_r.env"
    [ -f "$_wms_sf" ] || continue
    sed -n 's/^WMS_SERVER_TAG=/wms-server:/p; s/^WMS_WEB_TAG=/wms-web:/p' "$_wms_sf"
  done < "$HISTORY"
}

# 只保留 history 仍引用的发布记录文件（状态文件 + 编排快照）
wms_prune_releases() {
  [ -d "$STATE_DIR" ] || return 0
  for _wms_sf in "$STATE_DIR"/*.env "$STATE_DIR"/*.compose.yml; do
    [ -f "$_wms_sf" ] || continue
    case "$_wms_sf" in
      *.compose.yml) _wms_r=$(basename "$_wms_sf" .compose.yml) ;;
      *)             _wms_r=$(basename "$_wms_sf" .env) ;;
    esac
    if ! grep -qxF "$_wms_r" "$HISTORY" 2>/dev/null; then
      rm -f "$_wms_sf"
      wms_log "清理历史发布记录: $(basename "$_wms_sf")"
    fi
  done
}

# 删除不再被任何发布引用的本项目镜像 tag（只动 wms-server / wms-web，
# 不动 daemon 上其他项目的镜像；层被多个 tag 共享时 docker rmi 只解引用）
wms_prune_images() {
  _wms_keep=" $(wms_protected_tags | sort -u | tr '\n' ' ') "
  for _wms_img in $(docker image ls --format '{{.Repository}}:{{.Tag}}' 2>/dev/null | grep -E '^wms-(server|web):' || true); do
    case "$_wms_keep" in
      *" $_wms_img "*) continue ;;
    esac
    wms_log "清理旧镜像: $_wms_img"
    docker rmi "$_wms_img" >/dev/null 2>&1 || true
  done
}

# 校验镜像是否存在于本地（回滚前用）
wms_require_images() {
  for _wms_t in "$@"; do
    if ! docker image inspect "$_wms_t" >/dev/null 2>&1; then
      wms_log "❌ 本地不存在镜像 $_wms_t（可能已被清理）"
      return 1
    fi
  done
  return 0
}

# ── docker 网络 ──

# ── 后端连接参数 ──
#
# deploy.sh 与 rollback.sh 必须派生同一套连接参数：这些值不在 .env 里（.env 里只有
# WMS_DB_CONTAINER / WMS_REDIS_CONTAINER），而是由脚本从容器名推导后导出给 compose。
# 漏掉任何一项，compose 就会用默认值（mysql8 / redis7 / root）渲染出与当前运行容器
# 不同的配置 —— 轻则把没改动的服务也重建一遍，重则让回滚后的实例连错库。
#
# 依赖调用方已设置 DB_CONTAINER / REDIS_CONTAINER。
wms_init_connection_env() {
  DB_NAME="${WMS_DB_NAME:-ml_wms}"
  export WMS_DB_NAME="$DB_NAME"
  export WMS_DB_HOST="${WMS_DB_HOST:-$DB_CONTAINER}"
  export WMS_REDIS_HOST="${WMS_REDIS_HOST:-$REDIS_CONTAINER}"
  export WMS_DB_PORT="${WMS_DB_PORT:-3306}"
  export WMS_REDIS_PORT="${WMS_REDIS_PORT:-6379}"
  export MYSQL_USER="${MYSQL_USER:-root}"
  export MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"
}

# 探测 mysql / redis 所在网络；.env 中显式设置了 WMS_NET 时直接沿用
# 依赖调用方已设置 DB_CONTAINER / REDIS_CONTAINER
wms_init_network() {
  if [ -n "${WMS_NET:-}" ]; then
    if ! docker network inspect "$WMS_NET" >/dev/null 2>&1; then
      wms_log "❌ .env 指定的 Docker 网络 $WMS_NET 不存在"
      exit 1
    fi
    wms_log "使用 .env 指定的 Docker 网络: $WMS_NET"
    return 0
  fi

  _wms_net=""
  for _wms_c in "$DB_CONTAINER" "$REDIS_CONTAINER"; do
    _wms_n="$(docker inspect -f '{{range $k,$v := .NetworkSettings.Networks}}{{println $k}}{{end}}' "$_wms_c" 2>/dev/null | head -1 || true)"
    if [ -n "$_wms_n" ] && [ "$_wms_n" != "bridge" ] && [ "$_wms_n" != "host" ]; then
      _wms_net="$_wms_n"
      break
    fi
  done

  if [ -z "$_wms_net" ]; then
    _wms_net="backend"
    docker network inspect "$_wms_net" >/dev/null 2>&1 || docker network create "$_wms_net"
    for _wms_c in "$DB_CONTAINER" "$REDIS_CONTAINER"; do
      docker network connect "$_wms_net" "$_wms_c" 2>/dev/null || true
    done
  fi

  WMS_NET="$_wms_net"
  export WMS_NET
  wms_log "复用 Docker 网络: $WMS_NET"
}
