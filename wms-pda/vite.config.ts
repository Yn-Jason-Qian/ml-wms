import { defineConfig, loadEnv, type Plugin } from 'vite'
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

/**
 * 修补 DCloud 版 Vue 运行时的 slots 只读 bug
 *
 * DCloud 的 uni-h5-vue / uni-app-vue 是从上游 Vue fork 出来的旧实现：
 *
 *   instance.slots = toRaw(children)
 *   def(children, "_", type)        // @vue/shared 的 def 第 4 参默认 false
 *
 * 上游 3.5 已经改成 assignSlots(slots, children) + def(slots, "_", type, true)。
 * `_` 是编译产物上的 slot flag 标记，父组件每次重渲染时 updateSlots() 都会用
 * Object.assign(slots, children) 再覆盖一次，目标属性只读就会抛：
 *
 *   TypeError: Cannot assign to read only property '_' of object
 *
 * 后果是整次重渲染中断（页面不切换、状态更新不生效），控制台每轮刷一次。
 * 这里只把调用补成上游写法（显式 writable: true），其余逻辑一律不动。
 */
function dcloudVueSlotsCompatPlugin(): Plugin {
  const VUE_RUNTIME_RE =
    /[\\/]@dcloudio[\\/]uni-[\w-]*-vue[\\/]dist[\w-]*[\\/]vue\.runtime\.(?:esm|cjs)[\w.-]*\.js$/
  const NEEDLE = ', "_", type)'

  return {
    name: 'wms-dcloud-vue-slots-compat',
    enforce: 'pre',
    transform(code, id) {
      const file = id.split('?')[0]
      if (!VUE_RUNTIME_RE.test(file) || !code.includes(NEEDLE)) {
        return null
      }
      return { code: code.replace(/, "_", type\)/g, ', "_", type, true)'), map: null }
    }
  }
}
export default defineConfig(({ mode }) => {
  // 开发期后端地址：默认 localhost:8080，用 .env.local 覆盖（.env.local 已在 .gitignore 中）
  const devProxyTarget =
    loadEnv(mode, __dirname, '').VITE_DEV_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [
      wxsCompatPlugin(),
      dcloudVueSlotsCompatPlugin(),
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
          target: devProxyTarget,
          changeOrigin: true
        },
        // PDA 实时推送（STOMP over WebSocket）
        '/ws-stomp': {
          target: devProxyTarget,
          changeOrigin: true,
          ws: true
        }
      }
    }
  }
})
