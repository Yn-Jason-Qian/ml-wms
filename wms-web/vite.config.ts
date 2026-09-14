import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // 开发期后端地址：默认 localhost:8080，用 .env.local 覆盖（.env.local 已在 .gitignore 中）
  const devProxyTarget =
    loadEnv(mode, __dirname, '').VITE_DEV_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: devProxyTarget,
          changeOrigin: true
        },
        // Dashboard 实时推送（STOMP over WebSocket）
        '/ws-stomp': {
          target: devProxyTarget,
          changeOrigin: true,
          ws: true
        }
      }
    }
  }
})
