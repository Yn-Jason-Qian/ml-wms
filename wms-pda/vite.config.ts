import { defineConfig, type Plugin } from 'vite'
import uniDefault from '@dcloudio/vite-plugin-uni'
import path from 'path'

// CJS → ESM interop: vite-plugin-uni uses CJS default export
const uni = (uniDefault as unknown as { default?: () => any }).default || uniDefault

/**
 * Vite 插件：处理 uView Plus 的 .wxs 文件在 H5/App 平台的兼容问题
 * wxs（WeiXin Script）仅用于小程序，其他平台需要 fallback 为空的 export
 */
function wxsCompatPlugin(): Plugin {
  return {
    name: 'wms-wxs-compat',
    enforce: 'pre',
    resolveId(id) {
      if (id.endsWith('.wxs')) {
        return id + '?wxs-compat'
      }
      return null
    },
    load(id) {
      if (id.endsWith('.wxs') || id.endsWith('.wxs?wxs-compat')) {
        return 'export default {}'
      }
      return null
    }
  }
}

/**
 * Vite 插件：替换 @dcloudio/uni-app 的 ESM dist 为修补版本
 *
 * UniApp 3.0.0-alpha-5010320260611001 的 uni-app.es.js 从 'vue' 导入了
 * isInSSRComponentSetup 和 injectHook，这两个是 @vue/runtime-core 内部 API，
 * 未在 Vue 公共包中公开导出。
 *
 * 此插件用项目内的修补文件替换该模块。
 */
function uniAppPatchPlugin(): Plugin {
  const PATCHED_FILE = path.resolve(__dirname, 'src/patches/uni-app-es.patch.js')
  return {
    name: 'wms-uni-app-patch',
    enforce: 'pre',
    resolveId(id) {
      // 拦截所有对 @dcloudio/uni-app 的引用，替换为修补版本
      if (
        id === '@dcloudio/uni-app' ||
        id.startsWith('@dcloudio/uni-app/') ||
        id.includes('@dcloudio/uni-app/dist/uni-app.es.js') ||
        id.includes('@dcloudio/uni-app/dist/uni-app.cjs.js')
      ) {
        return PATCHED_FILE
      }
      return null
    },
    load(id) {
      if (id === PATCHED_FILE) {
        // Load has higher priority, return null to let Vite read the file normally
        return null
      }
      return null
    }
  }
}

export default defineConfig({
  plugins: [
    wxsCompatPlugin(),
    uniAppPatchPlugin(),
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
