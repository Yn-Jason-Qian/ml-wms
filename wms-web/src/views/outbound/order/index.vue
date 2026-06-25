<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>订单管理</span><el-button type="primary" @click="openDialog()">创建订单</el-button></div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="query" inline class="query-form">
        <el-form-item label="仓库">
          <el-select v-model="query.warehouseId" clearable placeholder="全部" style="width:160px">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px">
            <el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }"><el-tag size="small">{{ typeMap[row.orderType] || row.orderType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="ownerName" label="货主" width="120" />
        <el-table-column prop="customerName" label="客户" width="140" />
        <el-table-column prop="priority" label="优先级" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusColor(row.status)" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createdAt?.substring(0, 19) || '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="createWaveForOrder(row)">生成波次</el-button>
          </template>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="800px" destroy-on-close>
      <div v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeMap[detail.orderType] || detail.orderType }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusColor(detail.status)" size="small">{{ statusMap[detail.status] || detail.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="仓库">{{ detail.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="货主">{{ detail.ownerName }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ detail.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ detail.priority }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createdAt?.substring(0, 19) || '' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">订单行</el-divider>
        <el-table :data="detail.lines" border size="small">
          <el-table-column prop="lineNo" label="#" width="50" />
          <el-table-column prop="skuCode" label="SKU编码" width="130" />
          <el-table-column prop="skuName" label="SKU名称" />
          <el-table-column prop="orderQty" label="订单数量" width="90" />
          <el-table-column prop="allocatedQty" label="已分配" width="80" />
          <el-table-column prop="pickedQty" label="已拣货" width="80" />
          <el-table-column prop="shippedQty" label="已发货" width="80" />
          <el-table-column prop="batchNo" label="批次号" width="120" />
          <el-table-column prop="status" label="行状态" width="90" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 创建订单弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建订单" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" style="width:100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="货主" prop="ownerId"><el-select v-model="form.ownerId" style="width:100%"><el-option v-for="o in owners" :key="o.id" :label="o.ownerName" :value="o.id" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="优先级"><el-input-number v-model="form.priority" :min="1" :max="9" controls-position="right" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="客户名称"><el-input v-model="form.customerName" /></el-form-item>
      </el-form>
      <el-divider content-position="left">订单行明细</el-divider>
      <div v-for="(item, idx) in form.lines" :key="idx" class="order-line">
        <el-row :gutter="8">
          <el-col :span="8"><el-select v-model="item.skuId" filterable placeholder="选择SKU" style="width:100%"><el-option v-for="s in skuList" :key="s.id" :label="s.skuCode+' '+s.skuName" :value="s.id" /></el-select></el-col>
          <el-col :span="5"><el-input-number v-model="item.orderQty" :min="1" controls-position="right" style="width:100%" /></el-col>
          <el-col :span="7"><el-input v-model="item.batchNo" placeholder="批次(可选)" /></el-col>
          <el-col :span="3"><el-button link type="danger" @click="form.lines.splice(idx,1)" :disabled="form.lines.length<=1">删</el-button></el-col>
        </el-row>
      </div>
      <el-button style="margin-top:8px" size="small" @click="form.lines.push({skuId:null,orderQty:1,batchNo:''})">+ 行</el-button>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">创建并分配库存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWarehouseList, getOwnerList, getSkuPage } from '@/api/modules/masterdata'
import request from '@/api/request'

const typeMap: Record<string, string> = { SALE: '销售', TRANSFER: '调拨', RETURN_SUPPLIER: '退供' }
const statusMap: Record<string, string> = { CREATED: '已创建', ALLOCATED: '已分配', PICKING: '拣货中', PICKED: '已拣货', CHECKED: '已复核', SHIPPED: '已发货', CANCELLED: '已取消' }
const statusColor = (s: string) => s === 'SHIPPED' ? 'success' : s === 'PICKING' || s === 'PICKED' ? 'warning' : s === 'CANCELLED' ? 'danger' : 'info'

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), detailVisible = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([]), owners = ref<any[]>([]), skuList = ref<any[]>([])
const total = ref(0), pageNum = ref(1), pageSize = ref(10)
const detail = ref<any>(null)
const formRef = ref()

const query = reactive({ warehouseId: null as any, status: null as any })
const form = reactive({
  warehouseId: null as any, ownerId: null as any, orderType: 'SALE', customerName: '', priority: 5,
  lines: [{ skuId: null as any, orderQty: 1, batchNo: '' } as any]
})
const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  ownerId: [{ required: true, message: '请选择货主', trigger: 'change' }]
}

const warehouseMap = computed(() => Object.fromEntries(warehouses.value.map(w => [w.id, w.whName])))
const ownerMap = computed(() => Object.fromEntries(owners.value.map(o => [o.id, o.ownerName])))

async function loadOpts() {
  try { warehouses.value = (await getWarehouseList()).data || [] } catch {}
  try { owners.value = (await getOwnerList()).data || [] } catch {}
  try { skuList.value = (await getSkuPage({ pageNum: 1, pageSize: 200 })).data.records || [] } catch {}
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.warehouseId) params.warehouseId = query.warehouseId
    if (query.status) params.status = query.status
    const res = await request.post('/outbound/orders/page', params)
    tableData.value = (res.data.records || []).map((r: any) => ({
      ...r,
      warehouseName: r.warehouseId ? (warehouseMap.value[r.warehouseId] || '') : '',
      ownerName: r.ownerId ? (ownerMap.value[r.ownerId] || '') : ''
    }))
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchData() }
function resetQuery() { query.warehouseId = null; query.status = null; pageNum.value = 1; fetchData() }

function openDialog() {
  form.warehouseId = null; form.ownerId = null; form.customerName = ''
  form.orderType = 'SALE'; form.priority = 5
  form.lines = [{ skuId: null, orderQty: 1, batchNo: '' }]
  dialogVisible.value = true
}

async function handleSave() {
  if (!await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  try {
    await request.post('/outbound/orders', form)
    ElMessage.success('订单创建成功，库存已自动分配')
    dialogVisible.value = false
    fetchData()
  } catch { /* global error handler shows message */ }
  finally { saving.value = false }
}

async function viewDetail(row: any) {
  const res = await request.get(`/outbound/orders/${row.id}`)
  detail.value = {
    ...res.data,
    warehouseName: row.warehouseName || '',
    ownerName: row.ownerName || ''
  }
  detailVisible.value = true
}

async function createWaveForOrder(row: any) {
  try {
    await ElMessageBox.confirm('确定为此订单生成波次？', '提示')
    await request.post('/outbound/waves', { warehouseId: row.warehouseId, waveType: 'ORDER_POOL', orderIds: [row.id] })
    ElMessage.success('波次已生成')
    fetchData()
  } catch { /* 用户取消或出错，全局错误处理 */ }
}

loadOpts().then(() => fetchData())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.query-form { margin-bottom: 12px; }
.order-line { padding: 8px; margin-bottom: 4px; background: #fafafa; border-radius: 4px; }
.pagination { margin-top: 12px; justify-content: flex-end; }
</style>
