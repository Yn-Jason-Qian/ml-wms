<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>库位管理</span>
          <div style="display:flex;gap:8px">
            <el-button type="success" @click="batchDialogVisible = true">批量生成</el-button>
            <el-button type="primary" @click="openDialog()">新增库位</el-button>
          </div>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterWarehouseId" placeholder="按仓库筛选" clearable @change="onFilterWarehouseChange" style="width:180px">
          <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
        </el-select>
        <el-select v-model="filterAreaId" placeholder="按库区筛选" clearable @change="onAreaChange" style="width:180px;margin-left:8px">
          <el-option v-for="a in areas" :key="a.id" :label="a.areaName" :value="a.id" />
        </el-select>
        <el-button type="primary" @click="onSearch" style="margin-left:8px">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column label="所属仓库" width="160">
          <template #default="{ row }">
            {{ getWarehouseName(row.warehouseId) }}
          </template>
        </el-table-column>
        <el-table-column label="所属库区" width="120">
          <template #default="{ row }">
            {{ getAreaName(row.areaId) }}
          </template>
        </el-table-column>
        <el-table-column prop="locationCode" label="库位编码" width="160" />
        <el-table-column prop="locationName" label="库位名称" width="140" />
        <el-table-column prop="locationType" label="类型" width="90">
          <template #default="{ row }"><el-tag size="small">{{ locTypeMap[row.locationType]||row.locationType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="aisle" label="巷道" width="70" />
        <el-table-column prop="shelf" label="货架" width="70" />
        <el-table-column prop="tier" label="层" width="60" />
        <el-table-column prop="depthPos" label="位" width="60" />
        <el-table-column prop="maxWeight" label="承重(kg)" width="90" />
        <el-table-column prop="maxQty" label="最大数量" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status===1?'success':row.status===2?'warning':'danger'">
              {{ row.status===1?'空闲':row.status===2?'占用':'禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status===1" link type="warning" size="small" @click="handleDisable(row.id)">禁用</el-button>
            <el-button v-if="row.status===0" link type="success" size="small" @click="handleEnable(row.id)">启用</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData" @size-change="fetchData"
        style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <!-- 单个新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑库位':'新增库位'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" style="width:100%" placeholder="选择" :disabled="isEdit" @change="loadAreas">
                <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库区" prop="areaId">
              <el-select v-model="form.areaId" style="width:100%" placeholder="选择" :disabled="isEdit">
                <el-option v-for="a in areaOptions" :key="a.id" :label="a.areaName" :value="a.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="库位编码" prop="locationCode">
          <el-input v-model="form.locationCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="库位名称">
          <el-input v-model="form.locationName" />
        </el-form-item>
        <el-form-item label="库位类型" prop="locationType">
          <el-radio-group v-model="form.locationType">
            <el-radio v-for="(v,k) in locTypeMap" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="6"><el-form-item label="巷道"><el-input v-model="form.aisle" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="货架"><el-input v-model="form.shelf" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="层"><el-input v-model="form.tier" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="位"><el-input v-model="form.depthPos" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量生成弹窗 -->
    <el-dialog v-model="batchDialogVisible" title="批量生成库位" width="550px" destroy-on-close>
      <el-form ref="batchFormRef" :model="batchForm" label-width="100px">
        <el-form-item label="仓库" required>
          <el-select v-model="batchForm.warehouseId" style="width:100%" placeholder="选择仓库" @change="loadBatchAreas">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="库区" required>
          <el-select v-model="batchForm.areaId" style="width:100%" placeholder="选择库区">
            <el-option v-for="a in batchAreaOptions" :key="a.id" :label="a.areaName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码前缀" required>
          <el-input v-model="batchForm.warehousePrefix" placeholder="如 WH01" />
        </el-form-item>
        <el-form-item label="库位类型">
          <el-select v-model="batchForm.locationType" style="width:100%">
            <el-option v-for="(v,k) in locTypeMap" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="6"><el-form-item label="巷道从"><el-input-number v-model="batchForm.aisleFrom" :min="1" controls-position="right" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="到"><el-input-number v-model="batchForm.aisleTo" :min="1" controls-position="right" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="货架从"><el-input-number v-model="batchForm.shelfFrom" :min="1" controls-position="right" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="到"><el-input-number v-model="batchForm.shelfTo" :min="1" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="6"><el-form-item label="层从"><el-input-number v-model="batchForm.tierFrom" :min="1" controls-position="right" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="到"><el-input-number v-model="batchForm.tierTo" :min="1" controls-position="right" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="位从"><el-input-number v-model="batchForm.depthFrom" :min="1" controls-position="right" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="到"><el-input-number v-model="batchForm.depthTo" :min="1" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="最大承重(kg)">
          <el-input-number v-model="batchForm.maxWeight" :min="0" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchCreate" :loading="batchSaving">
          生成 ({{ batchPreviewCount }} 个库位)
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWarehouseList, getAreasByWarehouse, getAreaPage, getLocationPage, createLocation, batchCreateLocations, updateLocation, deleteLocation, enableLocation, disableLocation } from '@/api/modules/masterdata'

const locTypeMap: Record<string, string> = { FLOOR: '地堆位', RACK: '货架位', SHELF: '隔板位', BIN: '周转箱位' }

const loading = ref(false), saving = ref(false), batchSaving = ref(false)
const dialogVisible = ref(false), batchDialogVisible = ref(false), isEdit = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([]), areas = ref<any[]>([]), allAreas = ref<any[]>([])
const filterWarehouseId = ref<number>(), filterAreaId = ref<number>()
const formRef = ref(), batchFormRef = ref()
const editId = ref<number>(), areaOptions = ref<any[]>([]), batchAreaOptions = ref<any[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const form = reactive({ warehouseId: null as any, areaId: null as any, locationCode: '', locationName: '', locationType: 'RACK', aisle: '', shelf: '', tier: '', depthPos: '', maxWeight: '', maxQty: '' })
const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  areaId: [{ required: true, message: '请选择库区', trigger: 'change' }],
  locationCode: [{ required: true, message: '请输入库位编码', trigger: 'blur' }],
  locationType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const batchForm = reactive({ warehouseId: null as any, areaId: null as any, warehousePrefix: '', locationType: 'RACK', aisleFrom: 1, aisleTo: 2, shelfFrom: 1, shelfTo: 3, tierFrom: 1, tierTo: 4, depthFrom: 1, depthTo: 2, maxWeight: 0 })

const batchPreviewCount = computed(() => {
  const a = batchForm.aisleTo - batchForm.aisleFrom + 1
  const s = batchForm.shelfTo - batchForm.shelfFrom + 1
  const t = batchForm.tierTo - batchForm.tierFrom + 1
  const d = batchForm.depthTo - batchForm.depthFrom + 1
  return Math.max(0, a * s * t * d)
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getLocationPage({
      pageNum: pageNum.value, pageSize: pageSize.value,
      warehouseId: filterWarehouseId.value,
      areaId: filterAreaId.value
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function loadAllAreas() {
  const res = await getAreaPage({ pageNum: 1, pageSize: 200 })
  allAreas.value = res.data.records || []
}

async function loadWarehouses() {
  const res = await getWarehouseList()
  warehouses.value = res.data || []
}

function getWarehouseName(id: number) {
  const w = warehouses.value.find((w: any) => w.id === id || String(w.id) === String(id))
  return w ? `${w.whCode} ${w.whName}` : ''
}
function getAreaName(id: number) {
  const a = allAreas.value.find((a: any) => a.id === id || String(a.id) === String(id))
  return a ? `${a.areaCode} ${a.areaName}` : ''
}

async function loadAreas() {
  if (form.warehouseId) {
    const res = await getAreasByWarehouse(form.warehouseId)
    areaOptions.value = res.data || []
  }
}
async function loadBatchAreas() {
  if (batchForm.warehouseId) {
    const res = await getAreasByWarehouse(batchForm.warehouseId)
    batchAreaOptions.value = res.data || []
  }
}

async function onFilterWarehouseChange() {
  filterAreaId.value = undefined
  if (filterWarehouseId.value) {
    const res = await getAreasByWarehouse(filterWarehouseId.value)
    areas.value = res.data || []
  } else {
    areas.value = allAreas.value
  }
}

function onAreaChange() {
  pageNum.value = 1
}

function onSearch() {
  pageNum.value = 1
  fetchData()
}

function openDialog(row?: any) {
  isEdit.value = !!row
  if (row) { editId.value = row.id; Object.assign(form, row); loadAreas() }
  else { editId.value = undefined; Object.keys(form).forEach(k => (form as any)[k] = ''); form.locationType = 'RACK'; areaOptions.value = [] }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value && editId.value) { await updateLocation(editId.value, form); ElMessage.success('更新成功') }
    else { await createLocation(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}

async function handleDisable(id: number) {
  try {
    await ElMessageBox.confirm('确定禁用该库位？', '提示', { type: 'warning' })
    await disableLocation(id)
    ElMessage.success('禁用成功'); fetchData()
  } catch { /* 用户取消 */ }
}
async function handleEnable(id: number) {
  try {
    await ElMessageBox.confirm('确定启用该库位？', '提示', { type: 'warning' })
    await enableLocation(id)
    ElMessage.success('启用成功'); fetchData()
  } catch { /* 用户取消 */ }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteLocation(id)
  ElMessage.success('删除成功'); fetchData()
}

async function handleBatchCreate() {
  if (!batchForm.warehouseId || !batchForm.areaId || !batchForm.warehousePrefix) {
    ElMessage.warning('请填写必填项'); return
  }
  batchSaving.value = true
  try {
    await batchCreateLocations(batchForm)
    ElMessage.success(`成功生成 ${batchPreviewCount.value} 个库位`)
    batchDialogVisible.value = false
    filterAreaId.value = batchForm.areaId
    filterWarehouseId.value = batchForm.warehouseId
    // Load areas for the filter
    if (filterWarehouseId.value) {
      const res = await getAreasByWarehouse(filterWarehouseId.value)
      areas.value = res.data || []
    }
    onSearch()
  } finally { batchSaving.value = false }
}

onMounted(async () => {
  await Promise.all([loadWarehouses(), loadAllAreas()])
  fetchData()
})
</script>

<style scoped>
.page-container { }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { margin-bottom: 16px; }
</style>
