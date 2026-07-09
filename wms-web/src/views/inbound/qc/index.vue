<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>质检管理</span><el-button type="primary" @click="openDlg('qc')">新建质检</el-button></div>
      </template>
      <el-form :model="query" inline class="query-form">
        <el-form-item label="仓库"><el-select v-model="query.warehouseId" placeholder="仓库" clearable style="width:160px"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" /></el-select></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="qcNo" label="质检单号" width="180" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }"><el-tag size="small">{{ qcTypeMap[row.qcType] || row.qcType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="140" />
        <el-table-column prop="ownerName" label="货主" width="140" />
        <el-table-column prop="receiveNo" label="收货单号" width="180" />
        <el-table-column label="SKU" width="180">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].skuCode }} {{ row.lines[0].skuName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="检验数" width="80">
          <template #default="{ row }"><span v-if="row.lines && row.lines.length">{{ row.lines[0].inspectQty }}</span></template>
        </el-table-column>
        <el-table-column label="合格数" width="80">
          <template #default="{ row }"><span v-if="row.lines && row.lines.length">{{ row.lines[0].passQty }}</span></template>
        </el-table-column>
        <el-table-column label="不合格数" width="90">
          <template #default="{ row }"><span v-if="row.lines && row.lines.length">{{ row.lines[0].rejectQty }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.status==='PASS'?'success':row.status==='REJECT'?'danger':'info'">{{ statusMap[row.status] || row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row.id)">详情</el-button>
            <el-button link type="primary" size="small" @click="openDlg('submit', row)">提交结果</el-button>
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
    <el-dialog v-model="detailVisible" title="质检详情" width="800px" destroy-on-close>
      <div v-if="detailQc">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="质检单号">{{ detailQc.qcNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ qcTypeMap[detailQc.qcType] }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag size="small" :type="detailQc.status==='PASS'?'success':detailQc.status==='REJECT'?'danger':'info'">{{ statusMap[detailQc.status] }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="仓库">{{ detailQc.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="货主">{{ detailQc.ownerName }}</el-descriptions-item>
          <el-descriptions-item label="收货单号">{{ detailQc.receiveNo }}</el-descriptions-item>
          <el-descriptions-item label="抽检比例">{{ detailQc.sampleRatio }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailQc.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="质检时间">{{ detailQc.qcAt }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detailQc.lines" border size="small" style="margin-top:12px">
          <el-table-column prop="lineNo" label="#" width="50" />
          <el-table-column prop="skuCode" label="SKU编码" width="130" />
          <el-table-column prop="skuName" label="SKU名称" />
          <el-table-column prop="inspectQty" label="检验数" width="80" />
          <el-table-column prop="passQty" label="合格数" width="80" />
          <el-table-column prop="rejectQty" label="不合格数" width="90" />
          <el-table-column prop="rejectReason" label="不合格原因" width="120" />
          <el-table-column prop="batchNo" label="批次号" width="120" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 新建质检弹窗 -->
    <el-dialog v-model="qcVisible" title="新建质检" width="450px" destroy-on-close>
      <el-form ref="qcFormRef" :model="qcForm" label-width="100px">
        <el-form-item label="仓库"><el-input-number v-model="qcForm.warehouseId" style="width:100%" /></el-form-item>
        <el-form-item label="收货单ID" required><el-input v-model="qcForm.receiveHeaderId" /></el-form-item>
        <el-form-item label="质检类型"><el-select v-model="qcForm.qcType" style="width:100%"><el-option v-for="(v,k) in qcTypeMap" :key="k" :label="v" :value="k" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="qcVisible=false">取消</el-button><el-button type="primary" @click="saveQc" :loading="saving">创建</el-button></template>
    </el-dialog>

    <!-- 提交结果弹窗 -->
    <el-dialog v-model="submitVisible" title="提交质检结果" width="450px" destroy-on-close>
      <el-form ref="submitFormRef" :model="submitForm" label-width="100px">
        <el-form-item label="质检单ID"><el-input v-model="submitForm.headerId" disabled /></el-form-item>
        <el-form-item label="SKU ID"><el-input v-model="submitForm.skuId" /></el-form-item>
        <el-form-item label="检验数"><el-input-number v-model="submitForm.inspectQty" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="合格数"><el-input-number v-model="submitForm.passQty" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="不合格数"><el-input-number v-model="submitForm.rejectQty" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="不合格原因"><el-input v-model="submitForm.rejectReason" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="submitVisible=false">取消</el-button><el-button type="primary" @click="saveSubmit" :loading="saving">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarehouseList, getOwnerList } from '@/api/masterdata'
import { pageQcs, createQc, submitQc, getQc } from '@/api/inbound'

const qcTypeMap: Record<string, string> = { FULL: '全检', SAMPLE: '抽检', NONE: '免检' }
const statusMap: Record<string, string> = { CREATED: '已创建', QCING: '质检中', PASS: '合格', REJECT: '不合格' }

const loading = ref(false), saving = ref(false)
const qcVisible = ref(false), submitVisible = ref(false), detailVisible = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([]), owners = ref<any[]>([])
const total = ref(0), pageNum = ref(1), pageSize = ref(10)
const detailQc = ref<any>(null)

const query = reactive({ warehouseId: null as any })
const qcForm = reactive({ warehouseId: 1, receiveHeaderId: '', qcType: 'FULL' })
const submitForm = reactive({ headerId: null as any, skuId: '', inspectQty: 0, passQty: 0, rejectQty: 0, rejectReason: '', batchNo: '' })

const warehouseMap = computed(() => Object.fromEntries(warehouses.value.map(w => [w.id, w.whName])))
const ownerMap = computed(() => Object.fromEntries(owners.value.map(o => [o.id, o.ownerName])))

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.warehouseId) params.warehouseId = query.warehouseId
    const res = await pageQcs(params)
    const records = (res.data.records || []).map((r: any) => ({
      ...r,
      warehouseName: warehouseMap.value[r.warehouseId] || '',
      ownerName: ownerMap.value[r.ownerId] || ''
    }))
    tableData.value = records
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchData() }
function resetQuery() { query.warehouseId = null; pageNum.value = 1; fetchData() }

async function loadOpts() {
  try { warehouses.value = (await getWarehouseList()).data || [] } catch {}
  try { owners.value = (await getOwnerList()).data || [] } catch {}
}

function openDlg(type: string, row?: any) {
  if (type === 'qc') {
    qcForm.receiveHeaderId = ''; qcForm.warehouseId = 1; qcVisible.value = true
  } else {
    submitForm.headerId = row.id; submitForm.skuId = ''; submitForm.inspectQty = 0
    submitForm.passQty = 0; submitForm.rejectQty = 0; submitForm.rejectReason = ''; submitForm.batchNo = ''
    submitVisible.value = true
  }
}

async function saveQc() {
  saving.value = true
  try { await createQc(qcForm); ElMessage.success('创建成功'); qcVisible.value = false; fetchData() } finally { saving.value = false }
}

async function saveSubmit() {
  saving.value = true
  try { await submitQc(submitForm); ElMessage.success('提交成功'); submitVisible.value = false; fetchData() } finally { saving.value = false }
}

async function viewDetail(id: number) {
  const res = await getQc(id)
  detailQc.value = {
    ...res.data,
    warehouseName: warehouseMap.value[res.data.warehouseId] || '',
    ownerName: ownerMap.value[res.data.ownerId] || ''
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
