import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/index.scss'

const app = createApp(App)

const pinia = createPinia()
pinia.use(createPersistedState())

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局 Vue 错误处理器
app.config.errorHandler = (err, instance, info) => {
  console.error('[GlobalError]', err, 'Component:', instance, 'Info:', info)
  // 生产环境可上报到监控系统
}

// 全局 Loading (路由切换时)
router.beforeEach((_to, _from, next) => {
  const appEl = document.getElementById('app')
  if (appEl) {
    const loader = document.createElement('div')
    loader.id = 'global-loader'
    loader.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:3px;z-index:9999;background:linear-gradient(90deg,#1677ff,#69b1ff);animation:loading-bar 1.5s ease infinite;'
    appEl.appendChild(loader)
    setTimeout(() => loader.remove(), 300)
  }
  next()
})

app.mount('#app')
