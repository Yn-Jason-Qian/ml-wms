/**
 * API 模块统一导出
 * 按业务域拆分，各模块引用 request 封装
 */

export { default as request } from '@/utils/request'
export type { ApiResponse, PageResponse } from '@/utils/request'

// 后续业务模块将在此导出：
// export * from './modules/auth'
// export * from './modules/inbound'
// export * from './modules/outbound'
// export * from './modules/inventory'
// export * from './modules/task'
