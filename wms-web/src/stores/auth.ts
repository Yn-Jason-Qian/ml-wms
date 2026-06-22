import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, type LoginParams } from '@/api/modules/auth'
import { setToken, removeToken } from '@/utils/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const username = ref('')
  const realName = ref('')
  const tenantId = ref<number>(0)
  const tenantName = ref('')

  async function login(params: LoginParams) {
    const res = await loginApi(params)
    const user = res.data
    token.value = user.token
    username.value = user.username
    realName.value = user.realName
    tenantId.value = user.tenantId
    tenantName.value = user.tenantName
    setToken(user.token)
  }

  function logout() {
    token.value = ''
    username.value = ''
    realName.value = ''
    removeToken()
    router.push('/login')
  }

  return { token, username, realName, tenantId, tenantName, login, logout }
}, {
  persist: true
})
