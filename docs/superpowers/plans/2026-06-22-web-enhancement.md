# Web 前端增强实施计划

> **For agentic workers:** 使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 来逐任务实现本计划。步骤使用 `- [ ]` 复选框语法进行跟踪。

**Goal:** 为 WMS Web 前端添加运营驾驶舱(ECharts)、报表导出(Excel)、批量导入、页签导航、全局搜索和表格列自定义

**Architecture:** 后端新增 25 个 API 端点（3 Dashboard + 9 Report + 12 Import/Template + 1 Search），前端新增 14 个文件 + 修改 8 个现有页面。分层推进：先面板+报表+导入，再页签+搜索+列自定义。

**Tech Stack:** Vue 3 + Element Plus + Pinia + ECharts + vue-echarts；Java 21 + Spring Boot + MyBatis-Plus + EasyExcel

## Global Constraints

- 所有 API 返回 `ApiResponse<T>` 统一响应体
- 前端 API 调用统一使用 `src/api/request.ts`（JWT 注入 + 错误拦截）
- 页面组件使用 `<script setup lang="ts">` 语法
- 样式使用 scoped SCSS + Element Plus 组件
- 导出文件格式统一为 `.xlsx`（application/vnd.openxmlformats）
- 页签持久化到 localStorage

---

### Task 1: 安装 ECharts 依赖

**Files:**
- Modify: `wms-web/package.json`

**Interfaces:**
- Produces: `echarts` 和 `vue-echarts` 包可供 import

- [ ] **Step 1: 安装依赖**

```bash
cd wms-web && npm install echarts vue-echarts
```

- [ ] **Step 2: 验证安装**

```bash
node -e "require('echarts'); require('vue-echarts'); console.log('OK')"
```
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add wms-web/package.json wms-web/package-lock.json
git commit -m "feat: add echarts and vue-echarts dependencies"
```

---

### Task 2: 后端 Dashboard 统计 API（3 个端点）

**Files:**
- Create: `wms-server/wms-web/src/main/java/com/wms/web/controller/DashboardController.java`
- Create: `wms-server/wms-web/src/main/java/com/wms/web/dto/DashboardStatsDTO.java`
- Create: `wms-server/wms-web/src/main/java/com/wms/web/dto/TrendRequest.java`
- Create: `wms-server/wms-web/src/main/java/com/wms/web/dto/TrendItemDTO.java`

**Interfaces:**
- Produces:
  - `GET /api/v1/dashboard/stats` → `ApiResponse<DashboardStatsDTO>`: `{todayReceive, todayPutaway, todayPick, todayShip, yesterdayReceive, yesterdayPutaway, yesterdayPick, yesterdayShip}`
  - `POST /api/v1/dashboard/trend` body `{days: 30}` → `ApiResponse<List<TrendItemDTO>>`: `[{date, inboundQty, outboundQty}]`
  - `GET /api/v1/dashboard/expiry-alert` → `ApiResponse<List<Map>>`: `[{skuCode, skuName, locationCode, batchNo, expiryDate, qtyOnHand, daysLeft}]`

- [ ] **Step 1: 创建 DashboardStatsDTO**

```java
// wms-server/wms-web/src/main/java/com/wms/web/dto/DashboardStatsDTO.java
package com.wms.web.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private long todayReceive;
    private long todayPutaway;
    private long todayPick;
    private long todayShip;
    private long yesterdayReceive;
    private long yesterdayPutaway;
    private long yesterdayPick;
    private long yesterdayShip;
}
```

- [ ] **Step 2: 创建 TrendRequest 和 TrendItemDTO**

```java
// TrendRequest.java
package com.wms.web.dto;
import lombok.Data;
@Data
public class TrendRequest { private int days = 30; }

// TrendItemDTO.java
package com.wms.web.dto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class TrendItemDTO {
    private String date;
    private BigDecimal inboundQty;
    private BigDecimal outboundQty;
}
```

- [ ] **Step 3: 创建 DashboardController**

```java
// wms-server/wms-web/src/main/java/com/wms/web/controller/DashboardController.java
package com.wms.web.controller;

import com.wms.common.base.ApiResponse;
import com.wms.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final JdbcTemplate jdbc;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsDTO> stats() {
        DashboardStatsDTO d = new DashboardStatsDTO();
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();
        d.setTodayReceive(count("wms_inbound_receive_header", today));
        d.setYesterdayReceive(count("wms_inbound_receive_header", yesterday));
        d.setTodayPutaway(count("wms_inbound_putaway_header", today));
        d.setYesterdayPutaway(count("wms_inbound_putaway_header", yesterday));
        d.setTodayPick(count("wms_outbound_pick_header", today));
        d.setYesterdayPick(count("wms_outbound_pick_header", yesterday));
        d.setTodayShip(count("wms_outbound_ship_header", today));
        d.setYesterdayShip(count("wms_outbound_ship_header", yesterday));
        return ApiResponse.ok(d);
    }

    private long count(String table, String date) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE DATE(created_at) = ? AND is_deleted = 0";
        Long c = jdbc.queryForObject(sql, Long.class, date);
        return c != null ? c : 0;
    }

    @PostMapping("/trend")
    public ApiResponse<List<TrendItemDTO>> trend(@RequestBody TrendRequest req) {
        List<TrendItemDTO> list = new ArrayList<>();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(req.getDays() - 1);
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            TrendItemDTO item = new TrendItemDTO();
            item.setDate(d.toString());
            item.setInboundQty(jdbc.queryForObject(
                "SELECT COALESCE(SUM(rl.receive_qty),0) FROM wms_inbound_receive_line rl " +
                "JOIN wms_inbound_receive_header rh ON rl.receive_header_id=rh.id " +
                "WHERE DATE(rh.created_at)=? AND rl.is_deleted=0", java.math.BigDecimal.class, d.toString()));
            item.setOutboundQty(jdbc.queryForObject(
                "SELECT COALESCE(SUM(sl.ship_qty),0) FROM wms_outbound_ship_line sl " +
                "JOIN wms_outbound_ship_header sh ON sl.ship_header_id=sh.id " +
                "WHERE DATE(sh.created_at)=? AND sl.is_deleted=0", java.math.BigDecimal.class, d.toString()));
            list.add(item);
        }
        return ApiResponse.ok(list);
    }

    @GetMapping("/expiry-alert")
    public ApiResponse<List<Map<String, Object>>> expiryAlert() {
        String sql = "SELECT sku_code, sku_name, batch_no, location_id, expiry_date, qty_on_hand, " +
            "DATEDIFF(expiry_date, NOW()) AS days_left FROM wms_inventory_stock " +
            "WHERE expiry_date IS NOT NULL AND expiry_date <= DATE_ADD(NOW(), INTERVAL 30 DAY) " +
            "AND qty_on_hand > 0 AND is_deleted = 0 ORDER BY expiry_date ASC LIMIT 5";
        return ApiResponse.ok(jdbc.queryForList(sql));
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
cd wms-server && mvn compile -pl wms-web
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add wms-server/wms-web/src/main/java/com/wms/web/controller/DashboardController.java
git add wms-server/wms-web/src/main/java/com/wms/web/dto/
git commit -m "feat: add dashboard stats/trend/expiry-alert APIs"
```

---

### Task 3: 前端 useChart composable + Dashboard API 模块

**Files:**
- Create: `wms-web/src/composables/useChart.ts`
- Create: `wms-web/src/api/modules/dashboard.ts`

**Interfaces:**
- Consumes: `echarts`, `vue-echarts` (Task 1)
- Produces:
  - `useChart()` → `{ chartRef, initChart, disposeChart }`
  - `getDashboardStats()`, `getDashboardTrend(days)`, `getExpiryAlert()`

- [ ] **Step 1: 创建 useChart.ts**

```typescript
// wms-web/src/composables/useChart.ts
import { ref, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

export function useChart() {
  const chartRef = ref<HTMLElement>()
  let chartInstance: echarts.ECharts | null = null

  function initChart() {
    if (chartRef.value) {
      chartInstance = echarts.init(chartRef.value)
      window.addEventListener('resize', () => chartInstance?.resize())
    }
    return chartInstance
  }

  function disposeChart() {
    window.removeEventListener('resize', () => chartInstance?.resize())
    chartInstance?.dispose()
    chartInstance = null
  }

  onBeforeUnmount(disposeChart)

  return { chartRef, initChart, disposeChart }
}
```

- [ ] **Step 2: 创建 dashboard.ts API 模块**

```typescript
// wms-web/src/api/modules/dashboard.ts
import request from '../request'

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

export function getDashboardTrend(days: number = 30) {
  return request.post('/dashboard/trend', { days })
}

export function getExpiryAlert() {
  return request.get('/dashboard/expiry-alert')
}
```

- [ ] **Step 3: Commit**

```bash
git add wms-web/src/composables/useChart.ts wms-web/src/api/modules/dashboard.ts
git commit -m "feat: add useChart composable and dashboard API module"
```

---

### Task 4: 重写 Dashboard 页面

**Files:**
- Modify: `wms-web/src/views/dashboard/index.vue`

**Interfaces:**
- Consumes: `useChart` (Task 3), `getDashboardStats`, `getDashboardTrend`, `getExpiryAlert` (Task 3)

- [ ] **Step 1: 重写 dashboard/index.vue**

```vue
<template>
  <div class="dashboard">
    <!-- 4 stat cards -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="s in stats" :key="s.label">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon" :style="{background:s.color}">
              <el-icon :size="24"><component :is="s.icon"/></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ s.today }}</div>
              <div class="stat-label">{{ s.label }}
                <span :class="s.trend>=0?'trend-up':'trend-down'">
                  {{ s.trend>=0?'↑':'↓' }}{{ Math.abs(s.trend) }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts row 1 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="15">
        <el-card><template #header>📈 近30天出入库趋势</template>
          <div ref="trendChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card><template #header>🍩 库龄分布</template>
          <div ref="agingChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts row 2 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="15">
        <el-card><template #header>⚡ 今日作业效率</template>
          <div ref="efficiencyChartRef" style="height:250px"></div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card><template #header>⚠️ 效期预警 Top5</template>
          <el-table :data="expiryList" size="small" v-loading="expLoading">
            <el-table-column prop="sku_code" label="SKU" width="120"/>
            <el-table-column prop="batch_no" label="批次" width="100"/>
            <el-table-column prop="days_left" label="剩余天数" width="80">
              <template #default="{row}">
                <el-tag size="small" :type="row.days_left<=7?'danger':row.days_left<=15?'warning':'info'">
                  {{ row.days_left }}天
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="qty_on_hand" label="库存" width="70"/>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, getDashboardTrend, getExpiryAlert } from '@/api/modules/dashboard'

const stats = ref([
  { label:'待收货', today:0, yesterday:0, trend:0, color:'#409eff', icon:'Download' },
  { label:'待上架', today:0, yesterday:0, trend:0, color:'#e6a23c', icon:'Place' },
  { label:'待拣货', today:0, yesterday:0, trend:0, color:'#67c23a', icon:'Upload' },
  { label:'待发货', today:0, yesterday:0, trend:0, color:'#f56c6c', icon:'Van' }
])

const trendChartRef = ref<HTMLElement>()
const agingChartRef = ref<HTMLElement>()
const efficiencyChartRef = ref<HTMLElement>()
const expiryList = ref<any[]>([])
const expLoading = ref(false)

onMounted(async () => {
  // Stats
  try {
    const r = await getDashboardStats()
    const d = (r as any).data
    stats.value[0].today = d.todayReceive; stats.value[0].yesterday = d.yesterdayReceive
    stats.value[1].today = d.todayPutaway; stats.value[1].yesterday = d.yesterdayPutaway
    stats.value[2].today = d.todayPick; stats.value[2].yesterday = d.yesterdayPick
    stats.value[3].today = d.todayShip; stats.value[3].yesterday = d.yesterdayShip
    stats.value.forEach(s => s.trend = s.today - s.yesterday)
  } catch {}

  // Trend chart
  try {
    const r = await getDashboardTrend(30)
    const data = (r as any).data || []
    const chart = echarts.init(trendChartRef.value!)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map((i:any)=>i.date?.slice(5)) },
      yAxis: { type: 'value' },
      series: [
        { name:'入库', type:'bar', data: data.map((i:any)=>i.inboundQty), itemStyle:{color:'#409eff'} },
        { name:'出库', type:'line', data: data.map((i:any)=>i.outboundQty), itemStyle:{color:'#67c23a'} }
      ]
    })
    window.addEventListener('resize', () => chart.resize())
  } catch {}

  // Aging pie (mock data until real API)
  try {
    const chart = echarts.init(agingChartRef.value!)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type:'pie', radius:['40%','70%'],
        data: [{value:335,name:'0-30天'},{value:234,name:'30-60天'},{value:120,name:'60-90天'},{value:56,name:'90天以上'}]
      }]
    })
  } catch {}

  // Efficiency bar (mock data)
  try {
    const chart = echarts.init(efficiencyChartRef.value!)
    chart.setOption({
      tooltip:{trigger:'axis'}, xAxis:{type:'category',data:['收货','拣货','上架','复核','发货']},
      yAxis:{type:'value'}, series:[{type:'bar',data:[45,82,30,28,15],itemStyle:{color:'#409eff'}}]
    })
  } catch {}

  // Expiry
  expLoading.value = true
  try {
    const r = await getExpiryAlert()
    expiryList.value = (r as any).data || []
  } finally { expLoading.value = false }
})
</script>

<style scoped>
.stat-card { display:flex; align-items:center; gap:12px; }
.stat-icon { width:48px; height:48px; border-radius:10px; display:flex; align-items:center; justify-content:center; color:#fff; }
.stat-value { font-size:28px; font-weight:bold; }
.stat-label { font-size:13px; color:#999; }
.trend-up { color:#67c23a; margin-left:8px; }
.trend-down { color:#f56c6c; margin-left:8px; }
</style>
```

- [ ] **Step 2: 构建验证**

```bash
cd wms-web && npx vite build 2>&1 | grep "✓ built"
```

- [ ] **Step 3: Commit**

```bash
git add wms-web/src/views/dashboard/index.vue
git commit -m "feat: rewrite dashboard with ECharts trends, aging, efficiency and expiry alerts"
```

---

### Task 5: 后端 Report 导出 API（9 个端点）

**Files:**
- Create: `wms-server/wms-web/src/main/java/com/wms/web/controller/ReportController.java`

**Interfaces:**
- Produces: 9 个 `POST /api/v1/report/{name}` 端点，每个接收 `{warehouseId?, dateFrom?, dateTo?}` → Excel blob

- [ ] **Step 1: 创建 ReportController**

```java
// wms-server/wms-web/src/main/java/com/wms/web/controller/ReportController.java
package com.wms.web.controller;

import com.wms.common.util.EasyExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {
    private final JdbcTemplate jdbc;

    private Map<String, Object> parseBody(Map<String, Object> body) {
        return body != null ? body : Map.of();
    }

    @PostMapping("/inventory-summary")
    public void inventorySummary(@RequestBody(required=false) Map<String,Object> body,
                                  HttpServletResponse resp) throws IOException {
        var sql = "SELECT s.sku_code, s.sku_name, s.batch_no, SUM(s.qty_on_hand) total_qty, " +
            "SUM(s.qty_allocated) alloc_qty, SUM(s.qty_available) avail_qty " +
            "FROM wms_inventory_stock s WHERE s.is_deleted=0 GROUP BY s.sku_code, s.sku_name, s.batch_no";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "收发存汇总", "Sheet1", null, rows2Maps(rows));
    }

    @PostMapping("/inventory-aging")
    public void inventoryAging(@RequestBody(required=false) Map<String,Object> body,
                                HttpServletResponse resp) throws IOException {
        var sql = "SELECT sku_code, sku_name, batch_no, qty_on_hand, first_in_time, " +
            "DATEDIFF(NOW(), first_in_time) AS age_days FROM wms_inventory_stock WHERE is_deleted=0 AND qty_on_hand>0 " +
            "ORDER BY age_days DESC";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "库龄分析", "Sheet1", null, rows2Maps(rows));
    }

    @PostMapping("/inventory-expiry")
    public void inventoryExpiry(@RequestBody(required=false) Map<String,Object> body,
                                 HttpServletResponse resp) throws IOException {
        var sql = "SELECT sku_code, sku_name, batch_no, expiry_date, qty_on_hand, " +
            "DATEDIFF(expiry_date, NOW()) AS days_left FROM wms_inventory_stock " +
            "WHERE is_deleted=0 AND expiry_date IS NOT NULL ORDER BY days_left ASC";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "效期预警", "Sheet1", null, rows2Maps(rows));
    }

    @PostMapping("/receive-detail")
    public void receiveDetail(@RequestBody(required=false) Map<String,Object> body,
                               HttpServletResponse resp) throws IOException {
        var sql = "SELECT rh.receive_no, rh.created_at, rl.sku_code, rl.sku_name, " +
            "rl.receive_qty, rl.batch_no FROM wms_inbound_receive_line rl " +
            "JOIN wms_inbound_receive_header rh ON rl.receive_header_id=rh.id WHERE rl.is_deleted=0 " +
            "ORDER BY rh.created_at DESC LIMIT 10000";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "收货明细", "Sheet1", null, rows2Maps(rows));
    }

    @PostMapping("/pick-detail")
    public void pickDetail(@RequestBody(required=false) Map<String,Object> body,
                            HttpServletResponse resp) throws IOException {
        var sql = "SELECT ph.pick_no, ph.created_at, pl.sku_code, pl.sku_name, " +
            "pl.pick_qty, pl.picked_qty, pl.to_container FROM wms_outbound_pick_line pl " +
            "JOIN wms_outbound_pick_header ph ON pl.pick_header_id=ph.id WHERE pl.is_deleted=0 " +
            "ORDER BY ph.created_at DESC LIMIT 10000";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "拣货明细", "Sheet1", null, rows2Maps(rows));
    }

    @PostMapping("/ship-detail")
    public void shipDetail(@RequestBody(required=false) Map<String,Object> body,
                            HttpServletResponse resp) throws IOException {
        var sql = "SELECT sh.ship_no, sh.carrier_name, sh.tracking_no, sh.created_at, sl.sku_code, " +
            "sl.sku_name, sl.ship_qty FROM wms_outbound_ship_line sl " +
            "JOIN wms_outbound_ship_header sh ON sl.ship_header_id=sh.id WHERE sl.is_deleted=0 " +
            "ORDER BY sh.created_at DESC LIMIT 10000";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "发货明细", "Sheet1", null, rows2Maps(rows));
    }

    @PostMapping("/asn-detail")
    public void asnDetail(@RequestBody(required=false) Map<String,Object> body,
                           HttpServletResponse resp) throws IOException {
        var sql = "SELECT ah.asn_no, ah.asn_type, ah.created_at, al.sku_code, al.sku_name, " +
            "al.expected_qty, al.received_qty FROM wms_inbound_asn_line al " +
            "JOIN wms_inbound_asn_header ah ON al.asn_header_id=ah.id WHERE al.is_deleted=0 " +
            "ORDER BY ah.created_at DESC LIMIT 10000";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "ASN明细", "Sheet1", null, rows2Maps(rows));
    }

    @PostMapping("/order-detail")
    public void orderDetail(@RequestBody(required=false) Map<String,Object> body,
                             HttpServletResponse resp) throws IOException {
        var sql = "SELECT oh.order_no, oh.order_type, oh.customer_name, oh.created_at, ol.sku_code, " +
            "ol.sku_name, ol.order_qty, ol.allocated_qty, ol.picked_qty, ol.shipped_qty " +
            "FROM wms_outbound_order_line ol JOIN wms_outbound_order_header oh ON ol.order_header_id=oh.id " +
            "WHERE ol.is_deleted=0 ORDER BY oh.created_at DESC LIMIT 10000";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "订单明细", "Sheet1", null, rows2Maps(rows));
    }

    @PostMapping("/stocktake-diff")
    public void stocktakeDiff(@RequestBody(required=false) Map<String,Object> body,
                               HttpServletResponse resp) throws IOException {
        var sql = "SELECT sh.stocktake_no, sl.location_code, sl.sku_code, sl.sku_name, sl.batch_no, " +
            "sl.book_qty, sl.first_count_qty, sl.second_count_qty, sl.diff_qty, sl.status " +
            "FROM wms_inventory_stocktake_line sl JOIN wms_inventory_stocktake_header sh " +
            "ON sl.stocktake_header_id=sh.id WHERE sl.is_deleted=0 AND sl.diff_qty IS NOT NULL " +
            "ORDER BY sh.created_at DESC LIMIT 10000";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        EasyExcelUtil.export(resp, "盘点差异", "Sheet1", null, rows2Maps(rows));
    }

    // 简单导出：将 Map 列表转为纯数据列表（忽略 Map.class 类型问题）
    private List<Map<String, Object>> rows2Maps(List<Map<String, Object>> rows) { return rows; }
}
```

**注：** `EasyExcelUtil.export()` 原签名需要 `Class<T> headClass`。需修改为支持无表头模式，或传入 null 跳过表头生成。如遇到编译问题，将 `null` 改为 Map.class 并使用 `@ExcelProperty` 忽略。

- [ ] **Step 2: 修改 EasyExcelUtil 支持无表头导出**

在 `EasyExcelUtil.java` 中新增重载方法：

```java
public static void export(HttpServletResponse response, String fileName,
                           String sheetName, List<Map<String, Object>> dataList) throws IOException {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("UTF-8");
    String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    response.setHeader("Content-Disposition", "attachment;filename=" + encodedName + ".xlsx");
    // 写入表头（Map 的 key 作为第一行）
    // 简化处理：直接用 EasyExcel.write() 写 List<List<Object>>
    List<List<Object>> sheetData = new ArrayList<>();
    if (!dataList.isEmpty()) {
        sheetData.add(new ArrayList<>(dataList.get(0).keySet()));
        for (Map<String, Object> row : dataList) {
            sheetData.add(new ArrayList<>(row.values()));
        }
    }
    EasyExcel.write(response.getOutputStream())
            .sheet(sheetName)
            .doWrite(sheetData);
}
```

- [ ] **Step 3: 编译验证**

```bash
cd wms-server && mvn compile -pl wms-web
```

- [ ] **Step 4: Commit**

```bash
git add wms-server/wms-web/src/main/java/com/wms/web/controller/ReportController.java
git add wms-server/wms-common/src/main/java/com/wms/common/util/EasyExcelUtil.java
git commit -m "feat: add 9 report export APIs with EasyExcel"
```

---

### Task 6: 前端 useExport + Report 页面

**Files:**
- Create: `wms-web/src/composables/useExport.ts`
- Create: `wms-web/src/views/report/index.vue`
- Modify: `wms-web/src/router/index.ts` (add `/report` route)

- [ ] **Step 1: 创建 useExport.ts**

```typescript
// wms-web/src/composables/useExport.ts
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

export function useExport() {
  async function exportExcel(url: string, params: Record<string, any> = {}, filename: string = 'export') {
    try {
      const token = getToken()
      const resp = await fetch(`/api/v1${url}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(params || {})
      })
      if (!resp.ok) throw new Error('Export failed')
      const blob = await resp.blob()
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `${filename}.xlsx`
      a.click()
      URL.revokeObjectURL(a.href)
      ElMessage.success('导出成功')
    } catch {
      ElMessage.error('导出失败')
    }
  }
  return { exportExcel }
}
```

- [ ] **Step 2: 创建 Report 页面**

```vue
<!-- wms-web/src/views/report/index.vue -->
<template>
  <div class="page-container">
    <el-card><template #header><span>报表中心</span></template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="库存报表" name="inventory">
          <el-row :gutter="16">
            <el-col :span="8" v-for="r in inventoryReports" :key="r.key">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <el-icon :size="40" color="#409eff"><Document /></el-icon>
                  <h3 style="margin:12px 0">{{ r.label }}</h3>
                  <p style="color:#999;font-size:13px;margin-bottom:16px">{{ r.desc }}</p>
                  <el-button type="primary" @click="doExport(r.url, r.key)" :loading="loading===r.key">
                    导出 Excel
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="作业报表" name="ops">
          <el-row :gutter="16">
            <el-col :span="8" v-for="r in opsReports" :key="r.key">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <el-icon :size="40" color="#67c23a"><Document /></el-icon>
                  <h3 style="margin:12px 0">{{ r.label }}</h3>
                  <p style="color:#999;font-size:13px;margin-bottom:16px">{{ r.desc }}</p>
                  <el-button type="primary" @click="doExport(r.url, r.key)" :loading="loading===r.key">
                    导出 Excel
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="单据报表" name="docs">
          <el-row :gutter="16">
            <el-col :span="8" v-for="r in docReports" :key="r.key">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <el-icon :size="40" color="#e6a23c"><Document /></el-icon>
                  <h3 style="margin:12px 0">{{ r.label }}</h3>
                  <p style="color:#999;font-size:13px;margin-bottom:16px">{{ r.desc }}</p>
                  <el-button type="primary" @click="doExport(r.url, r.key)" :loading="loading===r.key">
                    导出 Excel
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useExport } from '@/composables/useExport'

const { exportExcel } = useExport()
const activeTab = ref('inventory')
const loading = ref<string>()

const inventoryReports = [
  { key:'summary', label:'收发存汇总', desc:'SKU维度汇总在手/在途/可用', url:'/report/inventory-summary' },
  { key:'aging', label:'库龄分析', desc:'库存按入库天数分区间统计', url:'/report/inventory-aging' },
  { key:'expiry', label:'效期预警', desc:'临期库存明细(30天内到期)', url:'/report/inventory-expiry' },
]
const opsReports = [
  { key:'receive', label:'收货明细', desc:'收货单行明细+批次', url:'/report/receive-detail' },
  { key:'pick', label:'拣货明细', desc:'拣货单行明细+容器号', url:'/report/pick-detail' },
  { key:'ship', label:'发货明细', desc:'发货单行明细+承运商', url:'/report/ship-detail' },
]
const docReports = [
  { key:'asn', label:'ASN明细', desc:'ASN行明细+收货状态', url:'/report/asn-detail' },
  { key:'order', label:'订单明细', desc:'销售订单行+分配/拣货/发货量', url:'/report/order-detail' },
  { key:'diff', label:'盘点差异', desc:'盘点差异明细(账面vs实盘)', url:'/report/stocktake-diff' },
]

async function doExport(url: string, key: string) {
  loading.value = key
  await exportExcel(url, {}, key)
  loading.value = undefined
}
</script>
```

- [ ] **Step 3: 添加 /report 路由**

在 `router/index.ts` 中 strategy 路由后添加：
```ts
{
  path: 'report',
  name: 'Report',
  component: () => import('@/views/report/index.vue'),
  meta: { title: '报表中心', icon: 'Document' }
}
```

- [ ] **Step 4: 构建验证**

```bash
cd wms-web && npx vite build 2>&1 | grep "✓ built"
```

- [ ] **Step 5: Commit**

```bash
git add wms-web/src/composables/useExport.ts wms-web/src/views/report/index.vue wms-web/src/router/index.ts
git commit -m "feat: add report center page with 9 export options"
```

---

### Task 7: 后端 Import + Template API（12 个端点）

**Files:**
- Create: `wms-server/wms-web/src/main/java/com/wms/web/controller/ImportController.java`

- [ ] **Step 1: 创建 ImportController**

```java
// wms-server/wms-web/src/main/java/com/wms/web/controller/ImportController.java
package com.wms.web.controller;

import com.wms.common.base.ApiResponse;
import com.wms.common.util.EasyExcelUtil;
import com.wms.masterdata.domain.entity.*;
import com.wms.masterdata.infrastructure.mapper.*;
import com.wms.inbound.domain.entity.*;
import com.wms.inbound.infrastructure.mapper.*;
import com.wms.outbound.domain.entity.*;
import com.wms.outbound.infrastructure.mapper.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImportController {
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final OwnerMapper ownerMapper;
    private final SkuMapper skuMapper;

    @PostMapping("/import/warehouses")
    public ApiResponse<Integer> importWarehouses(@RequestParam("file") MultipartFile file) throws IOException {
        List<Warehouse> list = new ArrayList<>();
        EasyExcelUtil.read(file.getInputStream(), Warehouse.class, batch -> list.addAll(batch));
        list.forEach(w -> { w.setStatus(1); warehouseMapper.insert(w); });
        return ApiResponse.ok(list.size());
    }

    @PostMapping("/import/locations")
    public ApiResponse<Integer> importLocations(@RequestParam("file") MultipartFile file) throws IOException {
        List<Location> list = new ArrayList<>();
        EasyExcelUtil.read(file.getInputStream(), Location.class, batch -> list.addAll(batch));
        list.forEach(l -> { l.setStatus(1); locationMapper.insert(l); });
        return ApiResponse.ok(list.size());
    }

    @PostMapping("/import/owners")
    public ApiResponse<Integer> importOwners(@RequestParam("file") MultipartFile file) throws IOException {
        List<Owner> list = new ArrayList<>();
        EasyExcelUtil.read(file.getInputStream(), Owner.class, batch -> list.addAll(batch));
        list.forEach(o -> { o.setStatus(1); ownerMapper.insert(o); });
        return ApiResponse.ok(list.size());
    }

    @PostMapping("/import/skus")
    public ApiResponse<Integer> importSkus(@RequestParam("file") MultipartFile file) throws IOException {
        List<Sku> list = new ArrayList<>();
        EasyExcelUtil.read(file.getInputStream(), Sku.class, batch -> list.addAll(batch));
        list.forEach(s -> { s.setStatus(1); skuMapper.insert(s); });
        return ApiResponse.ok(list.size());
    }

    // Templates — 返回含表头的空 Excel
    @GetMapping("/template/warehouses")
    public void templateWarehouses(HttpServletResponse resp) throws IOException {
        List<Warehouse> empty = Collections.singletonList(new Warehouse());
        EasyExcelUtil.export(resp, "warehouse_template", "仓库", Warehouse.class, empty);
    }

    @GetMapping("/template/locations")
    public void templateLocations(HttpServletResponse resp) throws IOException {
        List<Location> empty = Collections.singletonList(new Location());
        EasyExcelUtil.export(resp, "location_template", "库位", Location.class, empty);
    }

    @GetMapping("/template/owners")
    public void templateOwners(HttpServletResponse resp) throws IOException {
        List<Owner> empty = Collections.singletonList(new Owner());
        EasyExcelUtil.export(resp, "owner_template", "货主", Owner.class, empty);
    }

    @GetMapping("/template/skus")
    public void templateSkus(HttpServletResponse resp) throws IOException {
        List<Sku> empty = Collections.singletonList(new Sku());
        EasyExcelUtil.export(resp, "sku_template", "SKU", Sku.class, empty);
    }
}
```

**注：** ASN 和 Order 的导入需要额外字段映射（lines 嵌套），可后续迭代。模板也先覆盖基础数据 4 个。

- [ ] **Step 2: 编译**

```bash
cd wms-server && mvn compile
```

- [ ] **Step 3: Commit**

---

### Task 8: 前端 ImportButton 组件 + 页面集成

**Files:**
- Create: `wms-web/src/components/common/ImportButton.vue`
- Create: `wms-web/src/api/modules/import.ts`
- Modify: `warehouse/index.vue`, `location/index.vue`, `owner/index.vue`, `sku/index.vue`

- [ ] **Step 1: 创建 import API 模块**

```typescript
// wms-web/src/api/modules/import.ts
import request from '../request'

export function downloadTemplate(entity: string) {
  window.open(`/api/v1/template/${entity}`, '_blank')
}

export function importData(entity: string, file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post(`/import/${entity}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

- [ ] **Step 2: 创建 ImportButton.vue**

```vue
<template>
  <span style="display:inline-flex;gap:8px">
    <el-button @click="downloadTemplate(type)">下载模板</el-button>
    <el-upload :auto-upload="false" :on-change="handleFile" accept=".xlsx" :show-file-list="false">
      <el-button type="warning">批量导入</el-button>
    </el-upload>
    <el-dialog v-model="previewVisible" title="导入确认" width="400px">
      <p>已选择文件: <strong>{{ fileName }}</strong></p>
      <el-button type="primary" @click="doImport" :loading="importing">确认导入</el-button>
    </el-dialog>
  </span>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadTemplate, importData } from '@/api/modules/import'

const props = defineProps<{ type: string }>()
const emit = defineEmits(['done'])
const previewVisible = ref(false)
const fileName = ref('')
const importing = ref(false)
let pendingFile: File | null = null

function handleFile(file: any) {
  pendingFile = file.raw
  fileName.value = file.name
  previewVisible.value = true
}

async function doImport() {
  if (!pendingFile) return
  importing.value = true
  try {
    const res = await importData(props.type, pendingFile)
    ElMessage.success(`成功导入 ${(res as any).data} 条数据`)
    previewVisible.value = false
    emit('done')
  } catch { /* handled by interceptor */ }
  finally { importing.value = false }
}
</script>
```

- [ ] **Step 3: 集成到仓库管理页面**

在 `warehouse/index.vue` header 中：
```html
<div class="page-header">
  <span>仓库管理</span>
  <div style="display:flex;gap:8px">
    <ImportButton type="warehouses" @done="fetchData" />
    <el-button type="primary" @click="openDialog()">新增仓库</el-button>
  </div>
</div>
```

同一模式应用到 location/owner/sku 页面。

- [ ] **Step 4: 构建+提交**

---

### Task 9: TagsView 页签导航

**Files:**
- Create: `wms-web/src/stores/tabs.ts`
- Create: `wms-web/src/layout/components/TagsView.vue`
- Modify: `wms-web/src/layout/index.vue`

- [ ] **Step 1: 创建 tabs store**

```typescript
// wms-web/src/stores/tabs.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'

export interface TabView {
  path: string
  name: string
  title: string
}

export const useTabsStore = defineStore('tabs', () => {
  const visitedViews = ref<TabView[]>([])
  const cachedViews = ref<string[]>([])

  function addView(route: RouteLocationNormalized) {
    const exists = visitedViews.value.find(v => v.path === route.path)
    if (!exists && route.meta?.title) {
      visitedViews.value.push({
        path: route.path,
        name: route.name as string,
        title: route.meta.title as string
      })
      if (!route.meta.noCache) {
        cachedViews.value.push(route.name as string)
      }
    }
  }

  function removeView(path: string) {
    const idx = visitedViews.value.findIndex(v => v.path === path)
    if (idx > -1) {
      const name = visitedViews.value[idx].name
      visitedViews.value.splice(idx, 1)
      cachedViews.value = cachedViews.value.filter(n => n !== name)
    }
  }

  function removeOthers(path: string) {
    visitedViews.value = visitedViews.value.filter(v => v.path === path || v.path === '/dashboard')
  }

  function removeAll() {
    visitedViews.value = visitedViews.value.filter(v => v.path === '/dashboard')
    cachedViews.value = []
  }

  return { visitedViews, cachedViews, addView, removeView, removeOthers, removeAll }
}, { persist: true })
```

- [ ] **Step 2: 创建 TagsView.vue**

```vue
<template>
  <div class="tags-view">
    <el-tag v-for="tab in tabs.visitedViews" :key="tab.path"
            :type="route.path===tab.path?'':''" :closable="tab.path!=='/dashboard'"
            :effect="route.path===tab.path?'dark':'plain'"
            class="tag-item" @click="router.push(tab.path)" @close="closeTab(tab.path)">
      {{ tab.title }}
    </el-tag>
  </div>
</template>
<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/tabs'

const route = useRoute()
const router = useRouter()
const tabs = useTabsStore()

function closeTab(path: string) {
  tabs.removeView(path)
  if (route.path === path) {
    const remaining = tabs.visitedViews
    router.push(remaining[remaining.length - 1]?.path || '/dashboard')
  }
}
</script>
<style scoped>
.tags-view { display:flex; gap:4px; padding:6px 12px; background:#fff; border-bottom:1px solid #e4e7ed; overflow-x:auto; }
.tag-item { cursor:pointer; flex-shrink:0; }
</style>
```

- [ ] **Step 3: 修改 layout/index.vue**

在 `<el-main>` 前插入 TagsView：
```html
<tags-view />
<el-main class="app-main">
  <router-view v-slot="{ Component }">
    <keep-alive :include="tabsStore.cachedViews">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</el-main>
```

在 router.afterEach 中添加 `tabsStore.addView(to)`。

- [ ] **Step 4: 构建+提交**

---

### Task 10: 全局搜索 (后端 + 前端)

**Files:**
- Modify: `wms-server/wms-web/src/main/java/com/wms/web/controller/` — add SearchController
- Create: `wms-web/src/components/common/GlobalSearch.vue`
- Create: `wms-web/src/api/modules/search.ts`
- Modify: `wms-web/src/layout/index.vue`

- [ ] **Step 1: 后端 SearchController**

```java
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {
    private final JdbcTemplate jdbc;

    @GetMapping("/search")
    public ApiResponse<List<Map<String,Object>>> search(@RequestParam String q,
                                                         @RequestParam(defaultValue="asn,order,sku,location,owner,wave") String types) {
        List<Map<String,Object>> results = new ArrayList<>();
        String[] ts = types.split(",");
        for (String t : ts) {
            switch(t.trim()) {
                case "asn":
                    jdbc.query("SELECT 'ASN' AS type, id, asn_no AS title, status AS subtitle FROM wms_inbound_asn_header WHERE asn_no LIKE ? AND is_deleted=0 LIMIT 5",
                        rs -> results.add(Map.of("type","ASN","id",rs.getLong("id"),"title",rs.getString("title"),"subtitle",rs.getString("subtitle"),"url","/inbound/asn")),
                        "%"+q+"%");
                    break;
                case "order":
                    jdbc.query("SELECT 'ORDER' AS type, id, order_no AS title, status AS subtitle FROM wms_outbound_order_header WHERE order_no LIKE ? AND is_deleted=0 LIMIT 5",
                        rs -> results.add(Map.of("type","ORDER","id",rs.getLong("id"),"title",rs.getString("title"),"subtitle",rs.getString("subtitle"),"url","/outbound/order")),
                        "%"+q+"%");
                    break;
                case "sku":
                    jdbc.query("SELECT 'SKU' AS type, id, CONCAT(sku_code,' ',sku_name) AS title, '' AS subtitle FROM wms_masterdata_sku WHERE sku_code LIKE ? OR sku_name LIKE ? AND is_deleted=0 LIMIT 5",
                        rs -> results.add(Map.of("type","SKU","id",rs.getLong("id"),"title",rs.getString("title"),"subtitle","","url","/masterdata/sku")),
                        "%"+q+"%", "%"+q+"%");
                    break;
            }
        }
        return ApiResponse.ok(results);
    }
}
```

- [ ] **Step 2: 前端 GlobalSearch + search API + layout 集成**

创建 `wms-web/src/api/modules/search.ts`:
```typescript
import request from '../request'
export function globalSearch(q: string, types: string = 'asn,order,sku,location') {
  return request.get('/search', { params: { q, types } })
}
```

创建 `wms-web/src/components/common/GlobalSearch.vue`:
```vue
<template>
  <div class="global-search" @keydown.escape="visible=false">
    <el-input v-model="keyword" placeholder="搜索 ASN/订单/SKU..." :prefix-icon="Search"
              @input="onInput" @focus="visible=true" @blur="hideDelay" size="default"
              style="width:280px" clearable />
    <div v-if="visible && results.length" class="search-dropdown" @mousedown.prevent>
      <div v-for="group in groupedResults" :key="group[0].type" class="search-group">
        <div class="search-group-title">{{ typeLabel(group[0].type) }}</div>
        <div v-for="r in group" :key="r.id" class="search-item" @click="goTo(r)">
          <span>{{ r.title }}</span><span style="color:#999;font-size:12px">{{ r.subtitle }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { globalSearch } from '@/api/modules/search'

const router = useRouter()
const keyword = ref('')
const visible = ref(false)
const results = ref<any[]>([])
let timer: any = null

const groupedResults = computed(() => {
  const groups: Record<string, any[]> = {}
  results.value.forEach(r => { (groups[r.type]||=[]).push(r) })
  return Object.values(groups)
})

function typeLabel(t: string) {
  return { ASN:'📥 ASN', ORDER:'📤 订单', SKU:'📦 SKU', LOCATION:'📍 库位', OWNER:'🏢 货主', WAVE:'🌊 波次' }[t] || t
}

function onInput() {
  clearTimeout(timer)
  if (keyword.value.length >= 2) {
    timer = setTimeout(async () => {
      try { const r = await globalSearch(keyword.value); results.value = (r as any).data || []; visible.value = true }
      catch { results.value = [] }
    }, 300)
  } else { results.value = [] }
}

function hideDelay() { setTimeout(() => { visible.value = false }, 200) }

function goTo(item: any) {
  visible.value = false; keyword.value = ''
  router.push(item.url)
}
</script>
<style scoped>
.global-search { position:relative; }
.search-dropdown { position:absolute; top:100%; left:0; right:0; background:#fff; border:1px solid #e4e7ed;
  border-radius:4px; box-shadow:0 4px 12px rgba(0,0,0,.1); z-index:2000; max-height:400px; overflow-y:auto; }
.search-group-title { padding:6px 12px; font-size:12px; color:#999; background:#f5f7fa; font-weight:bold; }
.search-item { padding:8px 16px; cursor:pointer; display:flex; justify-content:space-between; }
.search-item:hover { background:#ecf5ff; }
</style>
```

在 `layout/index.vue` header 中嵌入：
```html
<global-search style="margin:0 16px" />
```

添加全局快捷键（在 layout script 中）：
```ts
import { onMounted, onBeforeUnmount } from 'vue'
function handleKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    document.querySelector<HTMLInputElement>('.global-search input')?.focus()
  }
}
onMounted(() => document.addEventListener('keydown', handleKeydown))
onBeforeUnmount(() => document.removeEventListener('keydown', handleKeydown))
```

- [ ] **Step 3: 构建+提交**

---

### Task 11: ColumnSelector 列自定义

**Files:**
- Create: `wms-web/src/components/common/ColumnSelector.vue`

- [ ] **Step 1: 创建 ColumnSelector.vue**

```vue
<template>
  <el-popover trigger="click" :width="200" placement="bottom-end">
    <template #reference>
      <el-button link><el-icon><Setting /></el-icon></el-button>
    </template>
    <div class="col-selector">
      <el-checkbox v-for="col in localColumns" :key="col.prop" v-model="col.visible"
                   :label="col.label" @change="save" />
      <el-divider style="margin:8px 0" />
      <el-button size="small" link @click="reset">重置</el-button>
    </div>
  </el-popover>
</template>
<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'

export interface ColumnDef { prop: string; label: string; visible: boolean }

const props = defineProps<{ columns: ColumnDef[] }>()
const emit = defineEmits<{ 'update:columns': [cols: ColumnDef[]] }>()
const route = useRoute()
const storageKey = `table_cols_${route.path}`

const localColumns = ref<ColumnDef[]>([])

watchEffect(() => {
  const saved = localStorage.getItem(storageKey)
  if (saved) {
    const savedCols = JSON.parse(saved) as ColumnDef[]
    localColumns.value = props.columns.map(c => {
      const sc = savedCols.find(s => s.prop === c.prop)
      return sc ? { ...c, visible: sc.visible } : c
    })
  } else {
    localColumns.value = [...props.columns]
  }
})

function save() {
  localStorage.setItem(storageKey, JSON.stringify(localColumns.value))
  emit('update:columns', localColumns.value)
}

function reset() {
  localStorage.removeItem(storageKey)
  localColumns.value = [...props.columns]
  emit('update:columns', localColumns.value)
}
</script>
```

- [ ] **Step 2: 构建+提交**

---

## 验证

- 后端: `mvn compile` 全部模块通过
- 前端: `npx vite build` 构建通过
- 功能: 启动后端+前端，浏览器访问 Dashboard 验证图表、Report 验证导出、Import 验证导入
