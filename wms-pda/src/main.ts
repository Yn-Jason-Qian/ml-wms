import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'
import uviewPlus from 'uview-plus'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)

  const pinia = createPinia()
  pinia.use(createPersistedState({
    storage: {
      getItem(key: string): string | null {
        return uni.getStorageSync(key) ?? null
      },
      setItem(key: string, value: string): void {
        uni.setStorageSync(key, value)
      }
    }
  }))

  app.use(pinia)
  app.use(uviewPlus)

  return { app }
}
