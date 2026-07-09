<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>波次管理</span><el-button type="primary" @click="openDlg()">创建波次</el-button></div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="query" inline class="query-form">
        <el-form-item label="仓库">
          <el-select v-model="query.warehouseId" clearable placeholder="全部" style="width:160px">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.waveStatus" clearable placeholder="全部" style="width:100px">
            <el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="waveNo" label="波次号" width="200" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }"><el-tag size="small">{{ typeMap[row.waveType] || row.waveType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.waveStatus === 'RELEASED' ? 'success' : 'info'">{{ statusMap[row.waveStatus] || row.waveStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="80" />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createdAt?.substring(0, 19) || '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="genPick(row.id)">生成拣货单</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="波次详情" width="700px" destroy-on-close>
      <div v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="波次号">{{ detail.waveNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeMap[detail.waveType] || detail.waveType }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="detail.waveStatus === 'RELEASED' ? 'success' : 'info'">{{ statusMap[detail.waveStatus] || detail.waveStatus }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单数">{{ detail.orderCount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createdAt?.substring(0, 19) || '' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">关联订单</el-divider>
        <el-table :data="detail.orders" border size="small">
          <el-table-column prop="orderNo" label="订单号" width="200" />
          <el-table-column label="类型" width="80">
            <template #default="{ row }"><el-tag size="small">{{ orderTypeMap[row.orderType] || row.orderType }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="customerName" label="客户" />
          <el-table-column prop="priority" label="优先级" width="70" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'SHIPPED' ? 'success' : 'info'">{{ orderStatusMap[row.status] || row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 创建波次弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建波次" width="480px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="仓库">
          <el-select v-model="form.warehouseId" style="width:100%">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="波次类型">
          <el-select v-model="form.waveType" style="width:100%">
            <el-option label="订单池" value="ORDER_POOL" />
            <el-option label="优先级" value="PRIORITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单ID">
          <el-input v-model="form.orderIdsStr" placeholder="逗号分隔，如 1,2,3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarehouseList } from '@/api/masterdata'
import { pageWaves, createWave, createPickFromWave, getWave } from '@/api/outbound'

const typeMap: Record<string, string> = { ORDER_POOL: '订单池', PRIORITY: '优先级' }
const statusMap: Record<string, string> = { CREATED: '已创建', RELEASED: '已释放', CLOSED: '已关闭' }
const orderTypeMap: Record<string, string> = { SALE: '销售', TRANSFER: '调拨', RETURN_SUPPLIER: '退供' }
const orderStatusMap: Record<string, string> = { CREATED: '已创建', ALLOCATED: '已分配', PICKING: '拣货中', PICKED: '已拣货', CHECKED: '已复核', SHIPPED: '已发货' }

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), detailVisible = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([])
const total = ref(0), detail = ref<any>(null)

const query = reactive({ pageNum: 1, pageSize: 10, warehouseId: null as any, waveStatus: null as any })
const form = reactive({ warehouseId: null as any, waveType: 'ORDER_POOL', orderIdsStr: '' })

const warehouseMap = computed(() => Object.fromEntries(warehouses.value.map(w => [w.id, w.whName])))

async function loadOpts() {
  try { warehouses.value = (await getWarehouseList()).data || [] } catch {}
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.warehouseId) params.warehouseId = query.warehouseId
    if (query.waveStatus) params.waveStatus = query.waveStatus
    const r = await pageWaves(params)
    tableData.value = (r.data.records || []).map((v: any) => ({
      ...v,
      warehouseName: v.warehouseId ? (warehouseMap.value[v.warehouseId] || '') : ''
    }))
    total.value = r.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { query.pageNum = 1; fetchData() }
function resetQuery() { query.warehouseId = null; query.waveStatus = null; query.pageNum = 1; fetchData() }

function openDlg() {
  form.warehouseId = null; form.waveType = 'ORDER_POOL'; form.orderIdsStr = ''
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const ids = form.orderIdsStr.split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n))
    await createWave({ warehouseId: form.warehouseId, waveType: form.waveType, orderIds: ids })
    ElMessage.success('波次已创建'); dialogVisible.value = false; fetchData()
  } catch { /* global handler */ }
  finally { saving.value = false }
}

async function viewDetail(row: any) {
  const res = await getWave(row.id)
  detail.value = {
    ...res.data,
    warehouseName: row.warehouseName || ''
  }
  detailVisible.value = true
}

async function genPick(waveId: number) {
  saving.value = true
  try {
    await createPickFromWave(waveId)
    ElMessage.success('拣货单已生成')
  } catch { /* global handler */ }
  finally { saving.value = false }
}

loadOpts().then(() => fetchData())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.query-form { margin-bottom: 12px; }
.pagination { margin-top: 12px; justify-content: flex-end; }
</style>
