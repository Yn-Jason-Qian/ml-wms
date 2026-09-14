/**
 * HTTP 请求封装 — 基于 uni.request，类 axios API
 * 复用 wms-web 的 request.ts 模式（拦截器 + JWT + 统一错误处理）
 */

import { AUTH_STORE_KEY, getToken, removeToken } from './auth'
import { API_BASE } from './env'

// ── 基础类型 ──
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  traceId?: string
}

export interface PageResponse<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

// ── 配置 ──
const TIMEOUT = 30000

/** 确保 url 以 / 开头 */
function normalizeUrl(url: string): string {
  return url.startsWith('/') ? url : '/' + url
}

/**
 * 匿名接口：不带 Authorization。
 * 否则一旦本地残留了过期 token，后端 JWT 过滤器会在进入登录逻辑之前
 * 直接把请求判成 401，导致「拿着旧 token 就永远登不进来」。
 */
const ANONYMOUS_PATHS = ['/auth/login']

function isAnonymous(url: string): boolean {
  return ANONYMOUS_PATHS.includes(normalizeUrl(url))
}

/**
 * 清理登录态。
 * 除了删除请求层用的 token，还要清掉 Pinia 的持久化状态——
 * 否则 App 下次启动时 restoreSession() 会把旧 token 又写回 uni.storage，
 * 变成「清掉了但刷新后复活」。
 */
function clearSession(): void {
  removeToken()
  uni.removeStorageSync(AUTH_STORE_KEY)
}

// ── 内部请求方法 ──
function request<T = unknown>(
  method: 'GET' | 'POST' | 'PUT' | 'DELETE',
  url: string,
  data?: Record<string, unknown> | null,
  params?: Record<string, unknown>
): Promise<ApiResponse<T>> {
  return new Promise((resolve, reject) => {
    // 构建完整 URL
    let fullUrl = API_BASE + normalizeUrl(url)
    if (params) {
      const query = Object.entries(params)
        .filter(([, v]) => v !== undefined && v !== null)
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
        .join('&')
      if (query) fullUrl += `?${query}`
    }

    const token = isAnonymous(url) ? null : getToken()

    uni.request({
      url: fullUrl,
      method,
      data: data ?? undefined,
      timeout: TIMEOUT,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        // 空响应体守卫
        if (!res.data) {
          reject(new Error('Empty response body'))
          return
        }
        const body = res.data as ApiResponse<T>
        const isSuccess = res.statusCode >= 200 && res.statusCode < 300 && body.code === 200
        if (isSuccess) {
          resolve(body)
        } else if (res.statusCode === 401 || (body && body.code === 401)) {
          clearSession()
          uni.reLaunch({ url: '/pages/login/index' })
          reject(new Error(body.message || '登录已过期'))
        } else {
          uni.showToast({
            title: body.message || '请求失败',
            icon: 'none',
            duration: 2500
          })
          reject(new Error(body.message || '请求失败'))
        }
      },
      fail: (err) => {
        const errMsg = err.errMsg || '网络连接失败'
        uni.showToast({
          title: errMsg,
          icon: 'none',
          duration: 2500
        })
        reject(err)
      }
    })
  })
}

// ── 对外 API ──
export default {
  get<T = unknown>(url: string, params?: Record<string, unknown>): Promise<ApiResponse<T>> {
    return request<T>('GET', url, null, params)
  },
  post<T = unknown>(url: string, data?: Record<string, unknown>): Promise<ApiResponse<T>> {
    return request<T>('POST', url, data)
  },
  put<T = unknown>(url: string, data?: Record<string, unknown>): Promise<ApiResponse<T>> {
    return request<T>('PUT', url, data)
  },
  delete<T = unknown>(url: string, params?: Record<string, unknown>): Promise<ApiResponse<T>> {
    return request<T>('DELETE', url, null, params)
  }
}
