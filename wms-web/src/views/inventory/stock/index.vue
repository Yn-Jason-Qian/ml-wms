<template>
  <div class="page-container">
    <el-card>
      <template #header><span>库存查询</span></template>
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="仓库"><el-select v-model="query.warehouseId" clearable placeholder="全部" style="width:150px"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" /></el-select></el-form-item>
        <el-form-item label="SKU编码"><el-input v-model="query.skuCode" placeholder="SKU编码" clearable style="width:140px" /></el-form-item>
        <el-form-item label="SKU名称"><el-input v-model="query.skuName" placeholder="SKU名称" clearable style="width:140px" /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="query.batchNo" placeholder="批次号" clearable style="width:130px" /></el-form-item>
        <el-form-item label="仅查有货"><el-checkbox v-model="query.onlyAvailable" /></el-form-item>
        <el-form-item label="效期预警"><el-input-number v-model="query.expiryWithinDays" :min="0" placeholder="天数" controls-position="right" style="width:110px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="skuCode" label="SKU编码" width="130" />
        <el-table-column prop="skuName" label="SKU名称" min-width="140" />
        <el-table-column prop="locationCode" label="库位" width="140" />
        <el-table-column prop="batchNo" label="批次号" width="120" />
        <el-table-column prop="qtyOnHand" label="在手数量" width="90" />
        <el-table-column prop="qtyAllocated" label="已分配" width="80" />
        <el-table-column prop="qtyAvailable" label="可用数量" width="90">
          <template #default="{ row }"><span :style="{ color: row.qtyAvailable > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">{{ row.qtyAvailable }}</span></template>
        </el-table-column>
        <el-table-column prop="qtyFrozen" label="冻结" width="70" />
        <el-table-column prop="productionDate" label="生产日期" width="100" />
        <el-table-column prop="expiryDate" label="失效日期" width="110">
          <template #default="{ row }">
            <span :style="{ color: isExpiringSoon(row.expiryDate) ? '#f56c6c' : '' }">{{ row.expiryDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastInTime" label="最后入库" width="150" />
      </el-table>

      <div style="margin-top:12px;text-align:right">
        <el-pagination :current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       @current-change="(p:number)=>{query.pageNum = p; fetchData()}" layout="total, prev, pager, next" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getWarehouseList } from '@/api/modules/masterdata'
import request from '@/api/request'

const loading = ref(false), tableData = ref<any[]>([]), warehouses = ref<any[]>([]), total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, warehouseId: null as any, skuCode: '', skuName: '', batchNo: '', onlyAvailable: false, expiryWithinDays: null as any })

function isExpiringSoon(date: string) {
  if (!date) return false
  const d = new Date(date); const now = new Date()
  return (d.getTime() - now.getTime()) < 30 * 24 * 3600 * 1000
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.post('/inventory/stocks/page', query)
    tableData.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(async () => {
  try { const res = await getWarehouseList(); warehouses.value = res.data || [] } catch {}
  fetchData()
})
</script>
