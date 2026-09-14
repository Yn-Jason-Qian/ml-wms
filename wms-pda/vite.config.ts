import { defineConfig, type Plugin } from 'vite'
import uniDefault from '@dcloudio/vite-plugin-uni'
import path from 'path'

// CJS → ESM interop: vite-plugin-uni uses CJS default export
const uni = (uniDefault as unknown as { default?: () => any }).default || uniDefault

/**
 * Vite 插件：处理 uView Plus 的 .wxs 文件在 H5/App 平台的兼容问题
 * wxs（WeiXin Script）仅用于小程序，其他平台需要 fallback 为空的 export
 *
 * 注意：小程序平台原生支持 wxs，必须原样透传给编译器，
 * 否则 u-swipe-action-item / u-slider / u-scroll-list 的交互脚本会被清空。
 */
function wxsCompatPlugin(): Plugin {
  const isMiniProgram = (process.env.UNI_PLATFORM || '').startsWith('mp-')
  return {
    name: 'wms-wxs-compat',
    enforce: 'pre',
    resolveId(id) {
      if (isMiniProgram) {
        return null
      }
      if (id.endsWith('.wxs')) {
        return id + '?wxs-compat'
      }
      return null
    },
    load(id) {
      if (isMiniProgram) {
        return null
      }
      if (id.endsWith('.wxs') || id.endsWith('.wxs?wxs-compat')) {
        return 'export default {}'
      }
      return null
    }
  }
}

/**
 * 说明：这里不需要为 @dcloudio/uni-app 引用 Vue 私有 API 做任何补丁。
 * uni-app 会按平台把 `vue` 指向自家运行时（h5 → @dcloudio/uni-h5-vue，
 * 小程序 → @dcloudio/uni-mp-vue），它们都导出了 isInSSRComponentSetup / injectHook。
 * 只有当平台插件没被加载、`vue` 落到 npm 包时，才会报 "is not exported by vue"。
 */
export default defineConfig({
  plugins: [
    wxsCompatPlugin(),
    ...(typeof uni === 'function' ? uni() : [])
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // uView Plus 组件内部使用的 mixin + 变量需要全局注入
        additionalData: `@import "uview-plus/theme.scss";@import "uview-plus/libs/css/mixin.scss";`
      }
    }
  },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
