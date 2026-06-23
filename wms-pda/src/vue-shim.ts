/**
 * Vue shim — 补齐 UniApp alpha 需要的 Vue 内部 API
 *
 * UniApp 3.0.0-alpha-5010320260611001 从 'vue' 导入了 isInSSRComponentSetup，
 * 但该 API 是 @vue/runtime-core 的内部模块级变量，未在任何地方公开导出。
 * PDA 运行在纯客户端环境，SSR 上下文始终为 false。
 */

export * from 'vue'

// isInSSRComponentSetup 是 runtime-core 内部的模块级布尔变量，
// 在客户端环境下始终为 false。UniApp 用它来判断是否在 SSR setup 中
// 以便决定生命周期钩子的注册方式。
export const isInSSRComponentSetup = false
