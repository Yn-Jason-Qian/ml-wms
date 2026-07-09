# wms-web 前端目录结构

## 技术栈

Vue 3 + Vite + Element Plus + Pinia + TypeScript + Axios

## 目录结构

```
wms-web/src/
├── main.ts / App.vue                 # 应用入口
│
├── api/                              # 服务调用层（领域子目录 + barrel 统一导出）
│   ├── request.ts                    # Axios 实例、JWT 拦截器、统一错误处理
│   ├── auth.ts / dashboard.ts        # 横切：登录、仪表盘
│   ├── search.ts / import.ts         # 横切：全局搜索、批量导入
│   ├── masterdata/  (warehouse  area  location  owner  sku  dict)
│   ├── inbound/     (asn  receive  qc  putaway)
│   ├── outbound/    (order  wave  pick  ship)
│   ├── inventory/   (stock  move  stocktake  freeze)
│   ├── strategy/    (config + rule)
│   └── task/        (claim  start  complete  cancel)
│
├── views/                            # 页面视图（与 api 领域一一对应）
│   ├── login/   dashboard/
│   ├── masterdata/  (warehouse  area  location  owner  sku  dict)
│   ├── inbound/     (asn  receive  qc  putaway)
│   ├── outbound/    (order  wave  pick  ship)
│   ├── inventory/   (stock  move  stocktake  freeze)
│   ├── strategy/  (含 RuleEditor 子组件)
│   ├── task/      report/      print/（预留）
│
├── router/index.ts                   # Vue Router
├── stores/  (auth  app)              # Pinia 状态管理
├── composables/  (useChart  useExport  useWebSocket)
├── components/common/  (GlobalSearch  ImportButton  ColumnSelector)
├── layout/index.vue                  # 布局组件
├── styles/index.scss                 # 全局样式
├── utils/auth.ts                     # JWT 工具
└── assets/                           # 静态资源
```

## API 层约定

### 导入方式

视图通过领域 barrel 文件导入，禁止直接引用 `request.ts`：

```ts
// ✅ 正确：按领域导入
import { getWarehouseList, getWarehousePage } from '@/api/masterdata'
import { pageAsns, createAsn, getAsn } from '@/api/inbound'
import { pageOrders, createOrder } from '@/api/outbound'
import { pageStocks } from '@/api/inventory'

// ❌ 错误：视图直接引用 request
import request from '@/api/request'
```

### 命名约定

| 前缀 | HTTP 方法 | 示例 |
|------|-----------|------|
| `page*` | POST /xxx/page | `pageAsns(params)` |
| `get*` | GET /xxx/:id | `getAsn(id)`, `getDictItems(typeCode)` |
| `create*` | POST /xxx | `createAsn(data)` |
| `update*` | PUT /xxx/:id | `updateWarehouse(id, data)` |
| `delete*` | DELETE /xxx/:id | `deleteSku(id)` |
| `enable*` / `disable*` | POST /xxx/:id/enable | `enableArea(id)` |
| `submit*` | POST /xxx/submit | `submitQc(data)` |

### 文件粒度

- 每个 API 文件只负责一个实体（如 `warehouse.ts`、`order.ts`）
- 每个领域子目录含 `index.ts` barrel 文件，`export * from './xxx'` 统一导出
- 横切功能（auth/dashboard/search/import）放在 `api/` 顶层，不建子目录

## 代码格式

- 禁止压缩/混淆代码（`node_modules`、`dist` 除外）
- `<template>` `<script>` `<style>` 必须分行缩进，禁止单行堆砌
- `import` 每条独立一行

## 视图层约定

- 视图目录结构与 `api/` 领域目录一一对应
- 每个视图目录下 `index.vue` 为主页面
- 视图内部子组件（如 `RuleEditor.vue`）放在同一目录下
- 跨视图复用的组件放在 `components/common/`

## 状态管理

- `stores/auth.ts`：JWT 令牌、登录用户信息、登录/登出方法
- `stores/app.ts`：侧边栏折叠、面包屑等全局 UI 状态
- 列表页数据用组件内 `ref/reactive` 管理，不放入 store

## 构建命令

```bash
npm run dev            # 开发服务器 (localhost:5173)
npm run build          # 生产构建
npm run preview        # 预览构建产物
npx vue-tsc --noEmit   # TypeScript 类型检查
```
