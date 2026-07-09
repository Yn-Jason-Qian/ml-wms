<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>收货管理</span><el-button type="primary" @click="openDialog()">新建收货</el-button></div>
      </template>
      <el-form :model="query" inline class="query-form">
        <el-form-item label="ASN单号"><el-input v-model="query.asnNo" placeholder="ASN单号" clearable style="width:180px" /></el-form-item>
        <el-form-item label="仓库"><el-select v-model="query.warehouseId" placeholder="仓库" clearable style="width:160px"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" /></el-select></el-form-item>
        <el-form-item label="货主"><el-select v-model="query.ownerId" placeholder="货主" clearable style="width:160px"><el-option v-for="o in owners" :key="o.id" :label="o.ownerName" :value="o.id" /></el-select></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="receiveNo" label="收货单号" width="180" />
        <el-table-column prop="asnNo" label="ASN单号" width="180" />
        <el-table-column prop="receiveType" label="类型" width="80" />
        <el-table-column prop="warehouseName" label="仓库" width="140" />
        <el-table-column prop="ownerName" label="货主" width="140" />
        <el-table-column label="SKU" width="180">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].skuCode }} {{ row.lines[0].skuName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="80">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].receiveQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批次号" width="120">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].batchNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收货库位" width="100">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].locationName || row.lines[0].receiveLocationId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生产日期" width="110">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].productionDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="失效日期" width="110">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].expiryDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.status==='DONE'?'success':'info'">{{ row.status==='DONE'?'已完成':row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row.id)">详情</el-button>
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
    <el-dialog v-model="detailVisible" title="收货详情" width="850px" destroy-on-close>
      <div v-if="detailRecv">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="收货单号">{{ detailRecv.receiveNo }}</el-descriptions-item>
          <el-descriptions-item label="ASN单号">{{ detailRecv.asnNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detailRecv.receiveType }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="detailRecv.status==='DONE'?'success':'info'" size="small">{{ detailRecv.status==='DONE'?'已完成':detailRecv.status }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="仓库">{{ detailRecv.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="货主">{{ detailRecv.ownerName }}</el-descriptions-item>
          <el-descriptions-item label="收货时间">{{ detailRecv.receivedAt }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailRecv.createdAt }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detailRecv.lines" border size="small" style="margin-top:12px">
          <el-table-column prop="lineNo" label="#" width="50" />
          <el-table-column prop="skuCode" label="SKU编码" width="130" />
          <el-table-column prop="skuName" label="SKU名称" />
          <el-table-column prop="receiveQty" label="收货数量" width="90" />
          <el-table-column prop="receivePackage" label="包装" width="80" />
          <el-table-column label="收货库位" width="100">
            <template #default="{ row }"><span>{{ row.locationName || row.receiveLocationId }}</span></template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次号" width="120" />
          <el-table-column prop="productionDate" label="生产日期" width="100" />
          <el-table-column prop="expiryDate" label="失效日期" width="100" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 新建收货弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建收货" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" style="width:100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" /></el-select></el-form-item>
        <el-form-item label="货主" prop="ownerId"><el-select v-model="form.ownerId" style="width:100%"><el-option v-for="o in owners" :key="o.id" :label="o.ownerName" :value="o.id" /></el-select></el-form-item>
        <el-form-item label="收货方式"><el-radio-group v-model="form.receiveType"><el-radio value="ASN">ASN收货</el-radio><el-radio value="BLIND">盲收</el-radio></el-radio-group></el-form-item>
        <el-form-item v-if="form.receiveType==='ASN'" label="ASN单号"><el-input v-model="form.asnHeaderId" placeholder="ASN ID" /></el-form-item>
        <el-form-item label="SKU" prop="skuId"><el-select v-model="form.skuId" filterable style="width:100%"><el-option v-for="s in skuList" :key="s.id" :label="s.skuCode+' '+s.skuName" :value="s.id" /></el-select></el-form-item>
        <el-form-item label="数量" prop="receiveQty"><el-input-number v-model="form.receiveQty" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="收货库位" prop="receiveLocationId"><el-input v-model="form.receiveLocationId" placeholder="库位ID" /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="form.batchNo" /></el-form-item>
        <el-form-item label="生产日期"><el-date-picker v-model="form.productionDate" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="失效日期"><el-date-picker v-model="form.expiryDate" type="date" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">收货</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarehouseList, getOwnerList, getSkuPage, getLocationPage } from '@/api/masterdata'
import { pageReceives, createReceive, getReceive } from '@/api/inbound'

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), detailVisible = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([]), owners = ref<any[]>([]), skuList = ref<any[]>([]), locations = ref<any[]>([])
const total = ref(0), pageNum = ref(1), pageSize = ref(10), formRef = ref()
const detailRecv = ref<any>(null)

const query = reactive({ asnNo: '', warehouseId: null as any, ownerId: null as any })
const form = reactive({ warehouseId: null as any, ownerId: null as any, receiveType: 'ASN', asnHeaderId: '', skuId: null as any, receiveQty: 1, receiveLocationId: '', batchNo: '', productionDate: null as any, expiryDate: null as any })
const rules = { warehouseId: [{ required: true, message: '必选' }], ownerId: [{ required: true }], skuId: [{ required: true }], receiveQty: [{ required: true }], receiveLocationId: [{ required: true }] }

const warehouseMap = computed(() => Object.fromEntries(warehouses.value.map(w => [w.id, w.whName])))
const ownerMap = computed(() => Object.fromEntries(owners.value.map(o => [o.id, o.ownerName])))
const locationMap = computed(() => Object.fromEntries(locations.value.map((l: any) => [l.id, l.locationCode || l.locCode || l.id])))

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.asnNo) params.asnNo = query.asnNo
    if (query.warehouseId) params.warehouseId = query.warehouseId
    if (query.ownerId) params.ownerId = query.ownerId
    const res = await pageReceives(params)
    const records = (res.data.records || []).map((r: any) => ({
      ...r,
      warehouseName: warehouseMap.value[r.warehouseId] || '',
      ownerName: ownerMap.value[r.ownerId] || '',
      lines: (r.lines || []).map((l: any) => ({
        ...l,
        locationName: locationMap.value[l.receiveLocationId] || l.receiveLocationId
      }))
    }))
    tableData.value = records
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchData() }
function resetQuery() { query.asnNo = ''; query.warehouseId = null; query.ownerId = null; pageNum.value = 1; fetchData() }

async function loadOpts() {
  try { warehouses.value = (await getWarehouseList()).data || [] } catch {}
  try { owners.value = (await getOwnerList()).data || [] } catch {}
  try { skuList.value = (await getSkuPage({ pageNum: 1, pageSize: 200 })).data.records || [] } catch {}
  try { locations.value = (await getLocationPage({ pageNum: 1, pageSize: 200 })).data.records || [] } catch {}
}

function openDialog() {
  Object.keys(form).forEach(k => { if (k !== 'receiveType') { (form as any)[k] = k === 'receiveQty' ? 1 : '' } })
  form.receiveType = 'ASN'
  dialogVisible.value = true
}

async function handleSave() {
  if (!await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  try { await createReceive(form); ElMessage.success('收货成功'); dialogVisible.value = false; fetchData() } finally { saving.value = false }
}

async function viewDetail(id: number) {
  const res = await getReceive(id)
  detailRecv.value = {
    ...res.data,
    warehouseName: warehouseMap.value[res.data.warehouseId] || '',
    ownerName: ownerMap.value[res.data.ownerId] || '',
    lines: (res.data.lines || []).map((l: any) => ({
      ...l,
      locationName: locationMap.value[l.receiveLocationId] || l.receiveLocationId
    }))
  }
  detailVisible.value = true
}

loadOpts().then(() => fetchData())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.query-form { margin-bottom: 12px; }
.pagination { margin-top: 12px; justify-content: flex-end; }
</style>
