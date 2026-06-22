<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>订单管理</span><el-button type="primary" @click="openDialog()">创建订单</el-button></div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="orderType" label="类型" width="90">
          <template #default="{ row }"><el-tag size="small">{{ typeMap[row.orderType]||row.orderType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="priority" label="优先级" width="70" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusColor(row.status)" size="small">{{ statusMap[row.status]||row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="createWaveForOrder(row)">生成波次</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWarehouseList, getOwnerList, getSkuPage } from '@/api/modules/masterdata'
import request from '@/api/request'

const typeMap: Record<string, string> = { SALE: '销售', TRANSFER: '调拨', RETURN_SUPPLIER: '退供' }
const statusMap: Record<string, string> = { CREATED: '已创建', ALLOCATED: '已分配', PICKING: '拣货中', PICKED: '已拣货', CHECKED: '已复核', SHIPPED: '已发货' }
const statusColor = (s: string) => s==='SHIPPED'?'success':s==='PICKING'?'warning':'info'

const loading = ref(false), saving = ref(false), dialogVisible = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([]), owners = ref<any[]>([]), skuList = ref<any[]>([])
const formRef = ref()

const form = reactive({
  warehouseId: null as any, ownerId: null as any, orderType: 'SALE', customerName: '', priority: 5,
  lines: [{ skuId: null as any, orderQty: 1, batchNo: '' } as any]
})
const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  ownerId: [{ required: true, message: '请选择货主', trigger: 'change' }]
}

async function loadOptions() {
  try { warehouses.value = (await getWarehouseList()).data || [] } catch {}
  try { owners.value = (await getOwnerList()).data || [] } catch {}
  try { skuList.value = (await getSkuPage({ pageNum: 1, pageSize: 100 })).data.records || [] } catch {}
}

function openDialog() {
  Object.keys(form).forEach(k => { if (!['lines','orderType','priority'].includes(k)) (form as any)[k] = '' })
  form.orderType = 'SALE'; form.priority = 5; form.lines = [{ skuId: null, orderQty: 1, batchNo: '' }]
  dialogVisible.value = true
}

async function handleSave() {
  if (!await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  try {
    await request.post('/outbound/orders', form)
    ElMessage.success('订单创建成功，库存已自动分配'); dialogVisible.value = false
  } finally { saving.value = false }
}

async function fetchData() {
  loading.value = true
  try { const res = await request.post('/outbound/orders/page', { pageNum: 1, pageSize: 50 }); tableData.value = res.data.records || [] } finally { loading.value = false }
}

async function createWaveForOrder(row: any) {
  try {
    await ElMessageBox.confirm('确定为此订单生成波次？', '提示')
    await request.post('/outbound/waves', { warehouseId: row.warehouseId || 1, waveType: 'ORDER_POOL', orderIds: [row.id] })
    ElMessage.success('波次已生成'); fetchData()
  } catch {}
}

onMounted(() => { loadOptions(); fetchData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.order-line { padding: 8px; margin-bottom: 4px; background: #fafafa; border-radius: 4px; }
</style>
