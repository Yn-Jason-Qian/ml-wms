<template>
  <div class="page-container">
    <el-card>
      <template #header><span>库存查询</span></template>
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="仓库"><el-select v-model="query.warehouseId" clearable placeholder="全部" style="width:150px"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" /></el-select></el-form-item>
        <el-form-item label="货主"><el-select v-model="query.ownerId" clearable placeholder="全部" style="width:150px"><el-option v-for="o in owners" :key="o.id" :label="o.ownerName" :value="o.id" /></el-select></el-form-item>
        <el-form-item label="SKU编码"><el-input v-model="query.skuCode" placeholder="SKU编码" clearable style="width:140px" /></el-form-item>
        <el-form-item label="SKU名称"><el-input v-model="query.skuName" placeholder="SKU名称" clearable style="width:140px" /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="query.batchNo" placeholder="批次号" clearable style="width:130px" /></el-form-item>
        <el-form-item label="仅查有货"><el-checkbox v-model="query.onlyAvailable" /></el-form-item>
        <el-form-item label="效期预警"><el-input-number v-model="query.expiryWithinDays" :min="0" placeholder="天数" controls-position="right" style="width:110px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="ownerName" label="货主" width="120" />
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
        <el-table-column label="最后入库" width="150">
          <template #default="{ row }">{{ row.lastInTime?.substring(0, 19) || '' }}</template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { getWarehouseList, getOwnerList, getLocationPage } from '@/api/masterdata'
import { pageStocks } from '@/api/inventory'

const loading = ref(false), tableData = ref<any[]>([]), warehouses = ref<any[]>([]), owners = ref<any[]>([]), locations = ref<any[]>([])
const total = ref(0), pageNum = ref(1), pageSize = ref(20)

const query = reactive({ warehouseId: null as any, ownerId: null as any, skuCode: '', skuName: '', batchNo: '', onlyAvailable: false, expiryWithinDays: null as any })

const warehouseMap = computed(() => Object.fromEntries(warehouses.value.map(w => [w.id, w.whName])))
const ownerMap = computed(() => Object.fromEntries(owners.value.map(o => [o.id, o.ownerName])))
const locationMap = computed(() => Object.fromEntries(locations.value.map((l: any) => [l.id, l.locationCode || l.locCode || l.id])))

function isExpiringSoon(date: string) {
  if (!date) return false
  const d = new Date(date); const now = new Date()
  return (d.getTime() - now.getTime()) < 30 * 24 * 3600 * 1000
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.warehouseId) params.warehouseId = query.warehouseId
    if (query.ownerId) params.ownerId = query.ownerId
    if (query.skuCode) params.skuCode = query.skuCode
    if (query.skuName) params.skuName = query.skuName
    if (query.batchNo) params.batchNo = query.batchNo
    if (query.onlyAvailable) params.onlyAvailable = true
    if (query.expiryWithinDays) params.expiryWithinDays = query.expiryWithinDays
    const res = await pageStocks(params)
    tableData.value = (res.data.records || []).map((r: any) => ({
      ...r,
      warehouseName: r.warehouseId && r.warehouseId !== '0' ? (warehouseMap.value[r.warehouseId] || '') : '',
      ownerName: r.ownerId && r.ownerId !== '0' ? (ownerMap.value[r.ownerId] || '') : '',
      locationCode: r.locationCode || (r.locationId ? locationMap.value[r.locationId] || '' : '')
    }))
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchData() }
function resetQuery() { query.warehouseId = null; query.ownerId = null; query.skuCode = ''; query.skuName = ''; query.batchNo = ''; query.onlyAvailable = false; query.expiryWithinDays = null; pageNum.value = 1; fetchData() }

async function loadOpts() {
  try { warehouses.value = (await getWarehouseList()).data || [] } catch {}
  try { owners.value = (await getOwnerList()).data || [] } catch {}
  try { locations.value = (await getLocationPage({ pageNum: 1, pageSize: 200 })).data.records || [] } catch {}
}

loadOpts().then(() => fetchData())
</script>

<style scoped>
.pagination { margin-top: 12px; justify-content: flex-end; }
</style>
