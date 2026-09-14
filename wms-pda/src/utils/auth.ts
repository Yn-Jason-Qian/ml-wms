/**
 * Token 管理 — 基于 uni.storage（复用 wms-web 的 auth.ts 模式）
 */

const TOKEN_KEY = 'wms_token'

/**
 * Pinia 持久化 key。
 * 必须和 wms-web 的 `auth` 区分开——两个项目跑在同一端口时（Vite 端口冲突会自动 +1），
 * 同源同名会互相覆盖，PDA 会读到 PC 端那份带旧 token 的状态。
 */
export const AUTH_STORE_KEY = 'wms_pda_auth'

export function getToken(): string | null {
  return uni.getStorageSync(TOKEN_KEY) ?? null
}

export function setToken(token: string): void {
  uni.setStorageSync(TOKEN_KEY, token)
}

export function removeToken(): void {
  uni.removeStorageSync(TOKEN_KEY)
}
