import axios, { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { getToken, removeToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 —— 注入 JWT
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 —— 统一错误处理
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        removeToken()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    // 401: 未认证（token 过期/无效），403: 禁止访问（token 过期后 Spring Security 兜底）
    if (status === 401 || status === 403) {
      removeToken()
      window.location.href = '/login'
      return Promise.reject(error)
    }
    // 优先展示后端返回的错误消息，比 Axios 默认的 "Request failed with status code xxx" 更友好
    const serverMsg = error.response?.data?.message
    ElMessage.error(serverMsg || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
