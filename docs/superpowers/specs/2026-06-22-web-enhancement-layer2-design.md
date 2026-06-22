# Web 前端增强 — 第二层：页签导航 + 全局搜索 + 列自定义

> 设计文档 | 2026-06-22 | 状态：已确认

## 一、页签导航（TagsView）

### 1.1 效果

```
┌──────────────────────────────────────────────────────┐
│  Header（logo + collapse + breadcrumb + user）        │
├──────────────────────────────────────────────────────┤
│  🏠首页 │ 📋仓库 │ 📥ASN │ 🧠策略 │  ×关闭           │  ← TagsView
├──────────────────────────────────────────────────────┤
│  <router-view>  (keep-alive 缓存)                    │
└──────────────────────────────────────────────────────┘
```

### 1.2 交互规则

| 操作 | 行为 |
|------|------|
| 点击侧边栏菜单 | 打开新 tab（已存在则激活） |
| 点击 tab | 切换页面，`<keep-alive>` 保持状态 |
| 右键 tab | 关闭当前 / 关闭其他 / 关闭所有 |
| 关闭含未保存表单的 tab | `ElMessageBox.confirm` 确认 |
| 刷新页面 | 恢复 tabs（localStorage 持久化） |

### 1.3 技术方案

| 文件 | 说明 |
|------|------|
| `src/stores/tabs.ts` | `visitedViews` + `cachedViews` 状态管理，持久化到 localStorage |
| `src/layout/components/TagsView.vue` | tabs 栏组件（右键菜单、滚动箭头） |
| `src/layout/index.vue` | 嵌入 TagsView + `<keep-alive :include="cachedViews">` |
| `src/router/index.ts` | 路由 meta 增加 `noCache: true`（如登录页不缓存）|

---

## 二、全局搜索

### 2.1 交互

- 快捷键 `Ctrl+K` 聚焦搜索框（header 右侧）
- 输入 ≥2 字符，防抖 300ms，调用搜索 API
- 下拉分组展示结果，点击跳转详情页（新 tab）

### 2.2 后端 API

```
GET /api/v1/search?q={keyword}&types=asn,order,sku,location,owner,wave
```

返回：
```json
{
  "results": [
    {"type":"ASN","id":1,"title":"ASN-20260622001","subtitle":"状态:已创建","url":"/inbound/asn"}
  ]
}
```

### 2.3 前端文件

| 文件 | 说明 |
|------|------|
| `src/components/common/GlobalSearch.vue` | 搜索输入框 + 下拉结果面板 |
| `src/layout/index.vue` | header 嵌入 GlobalSearch |
| `src/api/modules/search.ts` | 搜索 API |

---

## 三、表格列自定义

### 3.1 交互

每个 `el-table` 右上角 ⚙️ 图标按钮 → 弹出列选择面板：

```
┌─────────────────────────┐
│ ☑ 仓库编码  ☑ 仓库名称   │
│ ☑ 仓库类型  ☐ 地址      │
│ ☑ 联系人    ☐ 联系电话   │
│                [重置]   │
└─────────────────────────┘
```

- 勾选/取消即时生效
- 配置按路由 path 存 localStorage
- 重置恢复默认列

### 3.2 前端文件

| 文件 | 说明 |
|------|------|
| `src/components/common/ColumnSelector.vue` | 列选择面板组件 |
| 各管理页面 | 引入 ColumnSelector，替换硬编码列 |

ColumnSelector 接口：
```ts
defineProps<{
  columns: { prop: string; label: string; visible: boolean }[]
}>()
// emit('update:columns', newColumns)
```

---

## 四、实施顺序

```
1. TagsView — stores/tabs.ts + TagsView.vue + layout 修改
2. GlobalSearch — 后端搜索API + GlobalSearch.vue + layout 集成
3. ColumnSelector — 组件封装 + 逐页面接入
```
