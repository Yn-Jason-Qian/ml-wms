# SDD Progress Ledger

## Web Frontend Enhancement (2026-06-22)

### ✅ Task 1: Install ECharts + vue-echarts (commits b9d8f00..)
- Installed echarts ^6.1.0, vue-echarts ^8.0.1
- 0 vulnerabilities

### ✅ Task 2: Backend Dashboard API (commits b9d8f00..)
- DashboardController: GET /stats, POST /trend, GET /expiry-alert
- DTOs: DashboardStatsDTO, TrendRequest, TrendItemDTO

### ✅ Task 3: Frontend useChart + Dashboard API
- src/composables/useChart.ts
- src/api/modules/dashboard.ts

### ✅ Task 4: Dashboard Page Rewrite
- 4 stat cards (today vs yesterday with trend arrows)
- 3 ECharts: trend (bar+line), aging (pie), efficiency (bar)
- Expiry alert top 5 table
- useExport composable

### 🔄 Task 5: Report Export APIs - PENDING
### 🔄 Task 6: Report Center Page - PENDING
### 🔄 Task 7: Import APIs - PENDING
### 🔄 Task 8: ImportButton Component - PENDING
### 🔄 Task 9: TagsView Navigation - PENDING
### 🔄 Task 10: Global Search - PENDING
### 🔄 Task 11: ColumnSelector - PENDING
