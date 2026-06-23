/**
 * 认证状态管理 — Pinia + 持久化（uni.storage）
 * 复用 wms-web stores/auth.ts 模式，适配 UniApp 环境
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import request, { type ApiResponse } from '@/utils/request'
import { setToken, removeToken } from '@/utils/auth'

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  username: string
  realName: string
  token: string
  tenantId: number
  tenantName: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const username = ref('')
  const realName = ref('')
  const tenantId = ref<number>(0)
  const tenantName = ref('')
  const warehouseId = ref<number>(0)
  const warehouseName = ref('')

  /** 登录 */
  async function login(params: LoginParams): Promise<void> {
    const res = await request.post<UserInfo>('/auth/login', params as unknown as Record<string, unknown>)
    const user = res.data
    token.value = user.token
    username.value = user.username
    realName.value = user.realName
    tenantId.value = user.tenantId
    tenantName.value = user.tenantName
    setToken(user.token)
  }

  /** 退出 */
  function logout(): void {
    token.value = ''
    username.value = ''
    realName.value = ''
    tenantId.value = 0
    tenantName.value = ''
    removeToken()
    uni.reLaunch({ url: '/pages/login/index' })
  }

  /** 恢复会话（App 启动时） */
  function restoreSession(): void {
    // Pinia persist 已恢复 token 等字段，无需额外操作
    // 如果 token 存在但已过期，由 request 拦截器处理 401
  }

  /** 是否已登录 */
  const isLoggedIn = () => !!token.value

  /** 切换仓库 */
  function switchWarehouse(id: number, name: string): void {
    warehouseId.value = id
    warehouseName.value = name
  }

  return {
    token, username, realName, tenantId, tenantName,
    warehouseId, warehouseName,
    login, logout, restoreSession, isLoggedIn, switchWarehouse
  }
}, {
  persist: {
    // Pinia persistedstate 使用 uni.storage（main.ts 已配置）
    paths: ['token', 'username', 'realName', 'tenantId', 'tenantName', 'warehouseId', 'warehouseName']
  }
})
