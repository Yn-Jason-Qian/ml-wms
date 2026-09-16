#!/bin/sh
# ── WMS 回滚脚本（在目标服务器上执行）──
#
# 回滚不重新构建镜像：只是把 server / web 切回某个历史发布记录的镜像 tag 并重启容器，
# 所以通常几秒内完成（前提是该版本的镜像还在本地；deploy.sh 默认保留最近若干次发布）。
#
# 用法:
#   sh /opt/wms/rollback.sh                 # 回退到上一个发布
#   sh /opt/wms/rollback.sh --to <发布标识>  # 回退到指定发布
#   sh /opt/wms/rollback.sh --list           # 列出可回滚的发布（新 → 旧）
#
# 发布记录由 deploy.sh 维护在 $WMS_HOME/.releases/ 下，本脚本只读取 + 裁剪顺序。
set -eu

WMS_HOME="${WMS_HOME:-/opt/wms}"
COMPOSE_PROJECT="${COMPOSE_PROJECT:-wms}"
STATE_DIR="$WMS_HOME/.releases"
HISTORY="$STATE_DIR/history"
WMS_LOG_PREFIX=rollback

usage() {
  cat <<'EOF'
用法: sh rollback.sh [选项]

  --to <发布标识>   回退到指定发布（用 --list 查看可选值）
  --list            列出可回滚的发布（新 → 旧）
  -h, --help        显示本帮助

不带参数时回退到"当前版本的上一版"。
EOF
}

TARGET=""
DO_LIST=0
while [ $# -gt 0 ]; do
  case "$1" in
    --to)
      [ $# -ge 2 ] || { echo "[rollback] ❌ --to 缺少参数" >&2; exit 2; }
      TARGET="$2"; shift 2 ;;
    --to=*)  TARGET="${1#*=}"; shift ;;
    --list)  DO_LIST=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "[rollback] ❌ 未知参数: $1" >&2; usage >&2; exit 2 ;;
  esac
done

if [ ! -f "$WMS_HOME/lib.sh" ]; then
  echo "[rollback] ❌ 缺少 $WMS_HOME/lib.sh，请重新执行一次完整发布（或从仓库 deploy/ 目录拷贝）" >&2
  exit 1
fi
# shellcheck source=/dev/null
. "$WMS_HOME/lib.sh"

cd "$WMS_HOME"
wms_load_env

# 与 deploy.sh 保持完全一致的派生逻辑：容器名 → 连接参数 + docker 网络。
# 少了这一步，compose 会回落到默认值渲染出不同的容器配置，回滚就会连错库/无谓重启。
DB_CONTAINER="${WMS_DB_CONTAINER:-mysql8}"
REDIS_CONTAINER="${WMS_REDIS_CONTAINER:-redis7}"

wms_init_connection_env
wms_init_health
wms_init_compose

if [ ! -f "$HISTORY" ]; then
  wms_log "❌ 没有发布记录（$HISTORY 不存在），无法回滚"
  exit 1
fi

if [ "$DO_LIST" = 1 ]; then
  wms_log "可回滚的发布（新 → 旧，* 为当前版本）:"
  _i=0
  while IFS= read -r _r; do
    _r=$(printf '%s' "$_r" | tr -d '\r ')
    [ -n "$_r" ] || continue
    _i=$((_i + 1))
    _sf="$STATE_DIR/$_r.env"
    if [ -f "$_sf" ]; then
      _at="$(wms_state_value "$_sf" WMS_DEPLOYED_AT || true)"
      _sha="$(wms_state_value "$_sf" WMS_GIT_SHA || true)"
      _st="$(wms_state_value "$_sf" WMS_SERVER_TAG || true)"
      _wt="$(wms_state_value "$_sf" WMS_WEB_TAG || true)"
      [ -n "$_sha" ] || _sha="-"
      _mark=" "
      [ "$_i" = 1 ] && _mark="*"
      printf '%s %-26s %s  sha=%s  server=%s  web=%s\n' "$_mark" "$_r" "${_at:-未知时间}" "$_sha" "$_st" "$_wt"
    else
      printf '  %-26s (状态文件缺失，不可回滚)\n' "$_r"
    fi
  done < "$HISTORY"
  exit 0
fi

CURRENT="$(wms_current_release || true)"

# 默认目标：当前版本的"下一行"（即上一个发布）
if [ -z "$TARGET" ]; then
  TARGET="$(sed -n '2p' "$HISTORY" | tr -d '\r ' || true)"
  if [ -z "$TARGET" ]; then
    wms_log "❌ 只有当前版本 $CURRENT 一条记录，没有可回退的版本"
    wms_log "   重跑一次历史版本的构建，或者 sh $WMS_HOME/deploy.sh"
    exit 1
  fi
fi

if ! grep -qxF "$TARGET" "$HISTORY" 2>/dev/null; then
  wms_log "❌ 发布记录里找不到 $TARGET（可用 --list 查看）"
  exit 1
fi

SF="$STATE_DIR/$TARGET.env"
if [ ! -f "$SF" ]; then
  wms_log "❌ 缺少发布记录文件 $SF，无法回滚"
  exit 1
fi

S_TAG="$(wms_state_value "$SF" WMS_SERVER_TAG || true)"
W_TAG="$(wms_state_value "$SF" WMS_WEB_TAG || true)"
[ -n "$S_TAG" ] || S_TAG="latest"
[ -n "$W_TAG" ] || W_TAG="latest"

if ! wms_require_images "wms-server:$S_TAG" "wms-web:$W_TAG"; then
  wms_log "❌ 该版本的镜像已不在本地，无法回滚（需要重新走一次部署）"
  exit 1
fi

wms_init_network

wms_log "↩️ 回滚 $CURRENT → $TARGET (wms-server:$S_TAG / wms-web:$W_TAG)"
export WMS_SERVER_TAG="$S_TAG" WMS_WEB_TAG="$W_TAG"

# 先裁剪发布顺序：把 TARGET 及其之后的版本保留，TARGET 之前的（含刚才的当前版本）移除，
# 这样连按两次 rollback 会继续往回退，而不是来回横跳。
_tmp="$HISTORY.tmp.$$"
awk -v t="$TARGET" '
  { line=$0; gsub(/\r/,"",line); sub(/[ \t]+$/,"",line) }
  !keep && line == t { keep=1 }
  keep { print line }
' "$HISTORY" > "$_tmp"
mv "$_tmp" "$HISTORY"

# 不带 --build：直接用本地已有的镜像重建容器
wms_compose up -d --no-deps server web

FAILED=0
wms_wait_http "$WMS_SERVER_HEALTH_URL" "后端" || FAILED=1
wms_wait_http "$WMS_WEB_HEALTH_URL" "前端" || FAILED=1

echo "[rollback] 当前容器状态:"
wms_ps

if [ "$FAILED" = 1 ]; then
  wms_log "❌ 回滚后健康检查仍未通过，请人工介入（发布顺序已切到 $TARGET）"
  docker logs wms-server --tail 50 2>&1 || true
  exit 1
fi

wms_log "✅ 回滚完成，当前版本: $TARGET"
