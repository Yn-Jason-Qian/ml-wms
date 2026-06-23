/**
 * HTTP 请求封装 — 基于 uni.request，类 axios API
 * 复用 wms-web 的 request.ts 模式（拦截器 + JWT + 统一错误处理）
 */

import { getToken, removeToken } from './auth'

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
const BASE_URL = '/api/v1'
const TIMEOUT = 30000

// ── 内部请求方法 ──
function request<T = unknown>(
  method: 'GET' | 'POST' | 'PUT' | 'DELETE',
  url: string,
  data?: Record<string, unknown> | null,
  params?: Record<string, unknown>
): Promise<ApiResponse<T>> {
  return new Promise((resolve, reject) => {
    // 构建完整 URL
    let fullUrl = BASE_URL + url
    if (params) {
      const query = Object.entries(params)
        .filter(([, v]) => v !== undefined && v !== null)
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
        .join('&')
      if (query) fullUrl += `?${query}`
    }

    const token = getToken()

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
        const body = res.data as ApiResponse<T>
        if (res.statusCode === 200 && body.code === 200) {
          resolve(body)
        } else if (res.statusCode === 401 || body.code === 401) {
          removeToken()
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
        uni.showToast({
          title: '网络连接失败，请检查网络',
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
