<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>ASN管理</span><el-button type="primary" @click="openDialog()">创建ASN</el-button></div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="asnNo" label="ASN单号" width="180" />
        <el-table-column prop="asnType" label="类型" width="100">
          <template #default="{ row }"><el-tag size="small">{{ asnTypeMap[row.asnType]||row.asnType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="sourceNo" label="来源单号" width="140" />
        <el-table-column prop="carrierName" label="承运商" width="120" />
        <el-table-column prop="expectedArriveTime" label="预计到货" width="150" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag :type="statusColor(row.status)">{{ statusMap[row.status]||row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建ASN弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建ASN" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" style="width:100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="货主" prop="ownerId"><el-select v-model="form.ownerId" style="width:100%"><el-option v-for="o in owners" :key="o.id" :label="o.ownerName" :value="o.id" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="类型"><el-select v-model="form.asnType" style="width:100%"><el-option v-for="(v,k) in asnTypeMap" :key="k" :label="v" :value="k" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="来源单号"><el-input v-model="form.sourceNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="预计到货"><el-date-picker v-model="form.expectedArriveTime" type="datetime" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="承运商"><el-input v-model="form.carrierName" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <el-divider content-position="left">ASN行明细</el-divider>
      <div v-for="(item, idx) in form.lines" :key="idx" class="asn-line">
        <el-row :gutter="8" style="align-items:center">
          <el-col :span="8"><el-select v-model="item.skuId" filterable placeholder="选择SKU" style="width:100%"><el-option v-for="s in skuList" :key="s.id" :label="s.skuCode+' '+s.skuName" :value="s.id" /></el-select></el-col>
          <el-col :span="4"><el-input-number v-model="item.expectedQty" :min="1" placeholder="数量" controls-position="right" style="width:100%" /></el-col>
          <el-col :span="4"><el-input v-model="item.batchNo" placeholder="批次号" /></el-col>
          <el-col :span="4"><el-date-picker v-model="item.productionDate" type="date" placeholder="生产日期" style="width:100%" /></el-col>
          <el-col :span="3"><el-button link type="danger" @click="form.lines.splice(idx,1)" :disabled="form.lines.length<=1">删除</el-button></el-col>
        </el-row>
      </div>
      <el-button style="margin-top:8px" size="small" @click="form.lines.push({skuId:null,expectedQty:1,batchNo:'',productionDate:null,expiryDate:null})">+ 添加行</el-button>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">创建</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="ASN详情" width="750px" destroy-on-close>
      <div v-if="detailAsn">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="ASN单号">{{ detailAsn.asnNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ asnTypeMap[detailAsn.asnType] }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusColor(detailAsn.status)">{{ statusMap[detailAsn.status] }}</el-tag></el-descriptions-item>
        </el-descriptions>
        <el-table :data="detailAsn.lines" border size="small" style="margin-top:12px">
          <el-table-column prop="lineNo" label="#" width="50" />
          <el-table-column prop="skuCode" label="SKU编码" width="130" />
          <el-table-column prop="skuName" label="SKU名称" />
          <el-table-column prop="expectedQty" label="预计数量" width="90" />
          <el-table-column prop="receivedQty" label="已收数量" width="90" />
          <el-table-column prop="remainingQty" label="剩余" width="80" />
          <el-table-column prop="batchNo" label="批次号" width="120" />
          <el-table-column prop="productionDate" label="生产日期" width="100" />
          <el-table-column prop="expiryDate" label="失效日期" width="100" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarehouseList, getOwnerList, getSkuPage } from '@/api/modules/masterdata'
import request from '@/api/request'

const asnTypeMap: Record<string, string> = { PURCHASE: '采购入库', RETURN: '退货入库', TRANSFER: '调拨入库', ADJUST: '调整入库' }
const statusMap: Record<string, string> = { CREATED: '已创建', RECEIVING: '收货中', PARTIAL_RECEIVED: '部分收货', RECEIVED: '已收货', CANCELLED: '已取消', CLOSED: '已关闭' }
const statusColor = (s: string) => s==='RECEIVED'||s==='CLOSED'?'success':s==='RECEIVING'||s==='PARTIAL_RECEIVED'?'warning':s==='CANCELLED'?'danger':'info'

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), detailVisible = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([]), owners = ref<any[]>([]), skuList = ref<any[]>([])
const formRef = ref(), detailAsn = ref<any>(null), detailLines = ref<any[]>([])

const form = reactive({
  warehouseId: null as any, ownerId: null as any, asnType: 'PURCHASE', sourceNo: '', expectedArriveTime: '', carrierName: '',
  lines: [{ skuId: null as any, expectedQty: 1, batchNo: '', productionDate: null as any, expiryDate: null as any } as any]
})
const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  ownerId: [{ required: true, message: '请选择货主', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try { const res = await request.post('/inbound/asns/page', { pageNum: 1, pageSize: 50 }); tableData.value = res.data.records || [] } finally { loading.value = false }
}

async function loadOptions() {
  try { const r = await getWarehouseList(); warehouses.value = r.data || [] } catch {}
  try { const r = await getOwnerList(); owners.value = r.data || [] } catch {}
  try { const r = await getSkuPage({ pageNum: 1, pageSize: 100 }); skuList.value = r.data.records || [] } catch {}
}

function openDialog() {
  Object.keys(form).forEach(k => { if (k !== 'lines') (form as any)[k] = '' })
  form.asnType = 'PURCHASE'; form.lines = [{ skuId: null, expectedQty: 1, batchNo: '', productionDate: null, expiryDate: null }]
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await request.post('/inbound/asns', form)
    ElMessage.success('ASN创建成功')
    dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}

async function viewDetail(id: number) {
  const res = await request.get(`/inbound/asns/${id}`)
  detailAsn.value = res.data; detailVisible.value = true
}

onMounted(() => { loadOptions(); fetchData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.asn-line { padding: 8px; margin-bottom: 4px; background: #fafafa; border-radius: 4px; }
</style>
