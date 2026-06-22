# Web 前端增强 — 第一层：驾驶舱 + 报表 + 批量导入

> 设计文档 | 2026-06-22 | 状态：已确认

## 一、运营驾驶舱

### 1.1 技术选型

- 安装 `echarts` + `vue-echarts`（Vue 3 官方封装）
- 按需引入：`LineChart`, `BarChart`, `PieChart`
- 封装 `src/composables/useChart.ts`：处理 resize、theme、dispose

### 1.2 布局

```
┌──────────────┬──────────────┬──────────────┬──────────────┐
│  待收货 N     │  待上架 N     │  待拣货 N     │  待发货 N     │  ← 4卡片(已有,API接入)
└──────────────┴──────────────┴──────────────┴──────────────┘
┌─────────────────────────────────┬────────────────────────┐
│  近30天出入库趋势 (折线+柱状)     │  库龄分布 (饼图)         │
│  (60%宽)                        │  (40%宽)                │
├─────────────────────────────────┼────────────────────────┤
│  今日作业效率 (柱状)              │  效期预警 Top5 (表格)     │
└─────────────────────────────────┴────────────────────────┘
```

### 1.3 新增后端 API

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/dashboard/stats` | 今日4维数量 + 昨日对比 |
| POST | `/api/v1/dashboard/trend` | `{days:30}` → 每日入/出库量 |
| GET | `/api/v1/dashboard/expiry-alert` | 效期预警 Top5 |

### 1.4 前端文件

| 文件 | 变更 |
|------|------|
| `src/views/dashboard/index.vue` | 重写：4卡片 + 4图表区 |
| `src/composables/useChart.ts` | 新增：ECharts 初始化/resize |
| `src/api/modules/dashboard.ts` | 新增：stats/trend/expiry API |

---

## 二、报表导出

### 2.1 报表中心页面

新增 `/report` 路由，菜单图标 `Document`。三个 Tab 分组：

| Tab | 报表 | 筛选条件 |
|-----|------|---------|
| 库存报表 | 收发存汇总、库龄分析、效期预警 | 仓库、日期范围 |
| 作业报表 | 收货明细、拣货明细、发货明细 | 仓库、日期范围 |
| 单据报表 | ASN明细、订单明细、盘点差异 | 仓库、日期范围 |

### 2.2 导出交互

- 每行报表：筛选项 + `[导出Excel]` 按钮
- 封装 `src/composables/useExport.ts`：`exportReport(url, params)` → Blob 下载
- 加载状态：按钮 loading + `ElMessage` 提示

### 2.3 后端新增（9 端点）

全部 `POST`，参数 `{warehouseId, dateFrom, dateTo}`，返回 Excel 二进制流：

```
POST /api/v1/report/inventory-summary
POST /api/v1/report/inventory-aging
POST /api/v1/report/inventory-expiry
POST /api/v1/report/receive-detail
POST /api/v1/report/pick-detail
POST /api/v1/report/ship-detail
POST /api/v1/report/asn-detail
POST /api/v1/report/order-detail
POST /api/v1/report/stocktake-diff
```

### 2.4 前端文件

| 文件 | 变更 |
|------|------|
| `src/views/report/index.vue` | 新增：报表中心页面 |
| `src/composables/useExport.ts` | 新增：导出下载逻辑 |
| `src/router/index.ts` | 新增 `/report` 路由 |

---

## 三、批量导入

### 3.1 交互模式

每个管理页面 toolbar 增加两个按钮：

```
[新增XX] [批量导入] [下载模板]
```

- **批量导入**：`<el-upload>` 选 `.xlsx` → 前端预览（可选）→ 点确认 → 后端解析并批量写入
- **下载模板**：调用 `GET /api/v1/template/{entity}` 下载预置表头 Excel

### 3.2 后端新增

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/v1/import/warehouses` | 仓库批量导入 |
| POST | `/api/v1/import/locations` | 库位批量导入 |
| POST | `/api/v1/import/owners` | 货主批量导入 |
| POST | `/api/v1/import/skus` | SKU 批量导入 |
| POST | `/api/v1/import/asns` | ASN 批量导入 |
| POST | `/api/v1/import/orders` | 订单批量导入 |
| GET | `/api/v1/template/warehouses` | 仓库模板 |
| GET | `/api/v1/template/locations` | 库位模板 |
| GET | `/api/v1/template/owners` | 货主模板 |
| GET | `/api/v1/template/skus` | SKU 模板 |
| GET | `/api/v1/template/asns` | ASN 模板 |
| GET | `/api/v1/template/orders` | 订单模板 |

后端复用 `EasyExcelUtil.read()` 解析上传文件，校验后批量入库。

### 3.3 涉及修改的页面

| 现有页面 | 增加 |
|---------|------|
| `masterdata/warehouse/index.vue` | 导入按钮 + 上传弹窗 |
| `masterdata/location/index.vue` | 导入按钮 + 上传弹窗 |
| `masterdata/owner/index.vue` | 导入按钮 + 上传弹窗 |
| `masterdata/sku/index.vue` | 导入按钮 + 上传弹窗 |
| `inbound/asn/index.vue` | 导入按钮 + 上传弹窗 |
| `outbound/order/index.vue` | 导入按钮 + 上传弹窗 |

### 3.4 前端文件

| 文件 | 变更 |
|------|------|
| `src/components/common/ImportButton.vue` | 新增：导入按钮 + upload 弹窗封装 |
| `src/api/modules/import.ts` | 新增：导入/模板下载 API |
| 6 个管理页面 | 增加 `ImportButton` 组件 |

---

## 四、实施顺序

```
1. 后端: Dashboard 统计API (3个)
2. 前端: ECharts + Dashboard 重写
3. 后端: Report 导出API (9个)
4. 前端: useExport composable + Report 页面
5. 后端: Import + Template API (12个)
6. 前端: ImportButton 组件 + 6页面集成
```
