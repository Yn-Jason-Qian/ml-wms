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
 * Vite 插件：兼容 @dcloudio/uni-app 对 Vue 私有内部 API 的引用
 *
 * uni-app 的 Vue 3 发布线只有 3.0.0-alpha-* 预发布版本，其 dist 文件顶部固定写着：
 *
 *   import { shallowRef, ref, getCurrentInstance, isInSSRComponentSetup, injectHook } from 'vue'
 *
 * 其中 isInSSRComponentSetup / injectHook 是 Vue runtime-core 的私有内部 API。
 * 上游 vue 包（3.4 / 3.5 全系）从未公开导出过这两个名字，只有 DCloud 自家 vendored 的
 * Vue（@dcloudio/uni-cli-shared/lib/vapor/@vue）才导出，因此直接用 npm 上的 vue 构建会报
 * "isInSSRComponentSetup is not exported by vue"。
 *
 * 处理方式：构建期只把这一行 import 里的两个名字摘掉，并在模块尾部内联等价实现。
 * 这里刻意不复制整份 dist 文件——那样 uni-app 一升级，构建就会静默运行旧版本代码。
 */
function buildVueInternalsShim(getCurrentInstanceRef: string): string {
  return `
// ── 由 vite.config.ts 的 wms-uni-app-vue-internals 插件注入 ──
// isInSSRComponentSetup：PDA 是纯客户端，不存在 SSR 组件上下文，恒为 false
var isInSSRComponentSetup = false;
// injectHook：等价于 Vue runtime-core 内部的同名函数，只依赖公开的 getCurrentInstance
function injectHook(type, hook, target) {
  target = target || ${getCurrentInstanceRef};
  if (!target) {
    return;
  }
  var hooks = target[type] || (target[type] = []);
  var wrappedHook = hook.__weh || (hook.__weh = function () {
    var args = [];
    for (var i = 0; i < arguments.length; i++) {
      args[i] = arguments[i];
    }
    try {
      return hook.apply(void 0, args);
    } catch (e) {
      console.error('[wms] uni-app 生命周期钩子执行失败:', type, e);
    }
  });
  hooks.push(wrappedHook);
}
`
}

function uniAppVueInternalsPlugin(): Plugin {
  const targets = [
    // ESM：Rollup / Vite 构建走的就是这一份
    { re: /[\\/]@dcloudio[\\/]uni-app[\\/]dist[\\/]uni-app\.es\.js$/, cjs: false },
    // CJS：Node 侧（编译器、CLI）可能加载这一份
    { re: /[\\/]@dcloudio[\\/]uni-app[\\/]dist[\\/]uni-app\.cjs\.js$/, cjs: true }
  ]
  return {
    name: 'wms-uni-app-vue-internals',
    enforce: 'pre',
    transform(code, id) {
      const file = id.split('?')[0]
      const target = targets.find((t) => t.re.test(file))
      if (!target || !code.includes('isInSSRComponentSetup')) {
        return null
      }

      const patched = target.cjs
        ? code
            .replace(/\bvue\.isInSSRComponentSetup\b/g, 'isInSSRComponentSetup')
            .replace(/\bvue\.injectHook\b/g, 'injectHook')
        : code.replace(
            /\bimport\s*\{([^}]*)\}\s*from\s*(['"])vue\2/,
            (statement: string, names: string) => {
              const kept = names
                .split(',')
                .map((name) => name.trim())
                .filter((name) => name && name !== 'isInSSRComponentSetup' && name !== 'injectHook')
              return kept.length ? `import { ${kept.join(', ')} } from 'vue'` : ''
            }
          )

      if (patched === code) {
        return null
      }
      return {
        code: `${patched}\n${buildVueInternalsShim(
          target.cjs ? 'vue.getCurrentInstance()' : 'getCurrentInstance()'
        )}`,
        map: null
      }
    }
  }
}

export default defineConfig({
  plugins: [
    wxsCompatPlugin(),
    uniAppVueInternalsPlugin(),
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
