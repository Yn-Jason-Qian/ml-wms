/**
 * 运行时地址配置
 *
 * 为什么需要单独抽出来：
 * - H5 可以用相对路径（dev server 代理 / nginx 反代），同源就没有跨域问题
 * - 小程序和 App 没有「当前域名」的概念，uni.request 必须拿到完整地址，
 *   相对路径会直接失败。所以这两个平台必须通过 VITE_API_BASE_URL 指定后端地址。
 */

/** 后端地址（不含 /api/v1）。H5 下留空表示同源 */
function resolveApiOrigin(): string {
  const configured = String(import.meta.env.VITE_API_BASE_URL || '')
    .trim()
    .replace(/\/+$/, '')

  // #ifndef H5
  if (!configured) {
    console.warn(
      '[PDA] 未配置 VITE_API_BASE_URL：小程序 / App 需要完整的后端地址（H5 可留空走同源代理）'
    )
  }
  // #endif

  return configured
}

/** WebSocket 端点。未显式配置时，H5 按同源推导 */
function resolveWebSocketUrl(): string {
  const configured = String(import.meta.env.VITE_WS_BASE_URL || '').trim()
  if (configured) {
    return configured
  }

  let url = ''
  // #ifdef H5
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  url = `${protocol}//${location.host}/ws-stomp`
  // #endif
  return url
}

export const API_ORIGIN = resolveApiOrigin()
export const API_BASE = `${API_ORIGIN}/api/v1`
export const WS_URL = resolveWebSocketUrl()
