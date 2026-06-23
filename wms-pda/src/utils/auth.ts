/**
 * Token 管理 — 基于 uni.storage（复用 wms-web 的 auth.ts 模式）
 */

const TOKEN_KEY = 'wms_token'

export function getToken(): string | null {
  return uni.getStorageSync(TOKEN_KEY) ?? null
}

export function setToken(token: string): void {
  uni.setStorageSync(TOKEN_KEY, token)
}

export function removeToken(): void {
  uni.removeStorageSync(TOKEN_KEY)
}
