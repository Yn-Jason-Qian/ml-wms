<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>SKU管理</span><el-button type="primary" @click="openDialog()">新增SKU</el-button></div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterOwnerId" placeholder="按货主筛选" clearable @change="fetchData" style="width:180px">
          <el-option v-for="o in owners" :key="o.id" :label="o.ownerName" :value="o.id" />
        </el-select>
        <el-input v-model="filterSkuCode" placeholder="SKU编码" clearable style="width:160px;margin-left:8px" />
        <el-input v-model="filterSkuName" placeholder="SKU名称" clearable style="width:160px;margin-left:8px" />
        <el-button type="primary" @click="fetchData" style="margin-left:8px">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="skuCode" label="SKU编码" width="140" />
        <el-table-column prop="skuName" label="SKU名称" />
        <el-table-column prop="category" label="品类" width="100" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column label="批次管理" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.batchManaged?'success':'info'">{{ row.batchManaged?'是':'否' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="shelfLife" label="保质期(天)" width="90" />
        <el-table-column label="状态" width="70">
          <template #default="{ row }"><el-tag size="small" :type="row.status===1?'success':'danger'">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="openPkgDialog(row)">包装</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:12px;text-align:right">
        <el-pagination :current-page="pageNum" :page-size="pageSize" :total="total"
                       @current-change="(p:number) => { pageNum = p; fetchData() }" layout="total, prev, pager, next" />
      </div>
    </el-card>

    <!-- SKU新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑SKU':'新增SKU'" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="货主" prop="ownerId">
              <el-select v-model="form.ownerId" style="width:100%" :disabled="isEdit">
                <el-option v-for="o in owners" :key="o.id" :label="o.ownerName" :value="o.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SKU编码" prop="skuCode">
              <el-input v-model="form.skuCode" :disabled="isEdit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="SKU名称" prop="skuName"><el-input v-model="form.skuName" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="品类"><el-input v-model="form.category" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="规格"><el-input v-model="form.spec" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="保质期(天)"><el-input-number v-model="form.shelfLife" :min="0" controls-position="right" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="批次管理"><el-switch v-model="form.batchManaged" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="序列号管理"><el-switch v-model="form.snManaged" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 包装规格弹窗 -->
    <el-dialog v-model="pkgDialogVisible" title="包装规格管理" width="650px" destroy-on-close>
      <div v-if="pkgSkuId" class="page-header" style="margin-bottom:12px">
        <span>SKU: {{ pkgSkuName }}</span>
        <el-button size="small" type="primary" @click="pkgFormVisible = true">添加包装</el-button>
      </div>
      <el-table :data="pkgList" border size="small">
        <el-table-column prop="packageLevel" label="层级" width="80">
          <template #default="{ row }"><el-tag size="small">{{ pkgLevelMap[row.packageLevel]||row.packageLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="packageName" label="包装名称" />
        <el-table-column prop="qtyPerParent" label="含基础件数" width="100" />
        <el-table-column prop="barcode" label="条码" width="130" />
        <el-table-column label="默认收货/拣货/存储" width="140">
          <template #default="{ row }">
            {{ row.isDefaultReceive?'收货 ':'' }}{{ row.isDefaultPick?'拣货 ':'' }}{{ row.isDefaultStorage?'存储':'' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleDeletePkg(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="pkgFormVisible" style="margin-top:12px;padding:12px;background:#f5f7fa;border-radius:4px">
        <el-row :gutter="10">
          <el-col :span="5"><el-select v-model="pkgForm.packageLevel" placeholder="层级" style="width:100%"><el-option v-for="(v,k) in pkgLevelMap" :key="k" :label="v" :value="k" /></el-select></el-col>
          <el-col :span="5"><el-input v-model="pkgForm.packageName" placeholder="名称" /></el-col>
          <el-col :span="5"><el-select v-model="pkgForm.unitId" placeholder="单位" style="width:100%"><el-option v-for="u in units" :key="u.id" :label="u.unitName" :value="u.id" /></el-select></el-col>
          <el-col :span="5"><el-input-number v-model="pkgForm.qtyPerParent" :min="1" placeholder="含基础件数" controls-position="right" style="width:100%" /></el-col>
          <el-col :span="4"><el-button type="primary" @click="handleSavePkg" :loading="pkgSaving">保存</el-button></el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOwnerList, getUnitList, getSkuPage, createSku, updateSku, deleteSku, getSkuPackages, createSkuPackage, deleteSkuPackage } from '@/api/masterdata'

const pkgLevelMap: Record<string, string> = { EA: '件(each)', CASE: '箱(case)', PALLET: '托盘(pallet)' }

const loading = ref(false), saving = ref(false), pkgSaving = ref(false)
const dialogVisible = ref(false), pkgDialogVisible = ref(false), pkgFormVisible = ref(false), isEdit = ref(false)
const tableData = ref<any[]>([]), owners = ref<any[]>([]), pkgList = ref<any[]>([]), units = ref<any[]>([])
const formRef = ref(), editId = ref<number>(), pkgSkuId = ref<number>(), pkgSkuName = ref('')
const pageNum = ref(1), pageSize = ref(10), total = ref(0)
const filterOwnerId = ref<number>(), filterSkuCode = ref(''), filterSkuName = ref('')

const form = reactive({ ownerId: null as any, skuCode: '', skuName: '', category: '', brand: '', spec: '', shelfLife: 0, batchManaged: 0, snManaged: 0 })
const pkgForm = reactive({ packageLevel: 'CASE', packageName: '', qtyPerParent: 1, unitId: null as any })
const rules = {
  ownerId: [{ required: true, message: '请选择货主', trigger: 'change' }],
  skuCode: [{ required: true, message: '请输入SKU编码', trigger: 'blur' }],
  skuName: [{ required: true, message: '请输入SKU名称', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterOwnerId.value) params.ownerId = filterOwnerId.value
    if (filterSkuCode.value) params.skuCode = filterSkuCode.value
    if (filterSkuName.value) params.skuName = filterSkuName.value
    const res = await getSkuPage(params)
    tableData.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}

async function loadOwners() {
  const res = await getOwnerList(); owners.value = res.data || []
}

function openDialog(row?: any) {
  isEdit.value = !!row
  if (row) { editId.value = row.id; Object.assign(form, row) }
  else { editId.value = undefined; Object.keys(form).forEach(k => (form as any)[k] = ''); form.batchManaged = 0; form.snManaged = 0; form.shelfLife = 0 }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value && editId.value) { await updateSku(editId.value, form); ElMessage.success('更新成功') }
    else { await createSku(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteSku(id); ElMessage.success('删除成功'); fetchData()
}

async function openPkgDialog(row: any) {
  pkgSkuId.value = row.id; pkgSkuName.value = row.skuName; pkgFormVisible.value = false
  const [pkgRes, unitRes] = await Promise.all([getSkuPackages(row.id), getUnitList()])
  pkgList.value = pkgRes.data || []
  units.value = unitRes.data || []
  pkgDialogVisible.value = true
}

async function handleSavePkg() {
  if (!pkgForm.packageLevel || !pkgForm.packageName) { ElMessage.warning('请填写完整'); return }
  pkgSaving.value = true
  try {
    await createSkuPackage(pkgSkuId.value!, pkgForm)
    ElMessage.success('添加成功'); pkgFormVisible.value = false
    Object.assign(pkgForm, { packageLevel: 'CASE', packageName: '', qtyPerParent: 1, unitId: null })
    const res = await getSkuPackages(pkgSkuId.value!); pkgList.value = res.data || []
  } finally { pkgSaving.value = false }
}

async function handleDeletePkg(id: number) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteSkuPackage(id); ElMessage.success('删除成功')
  const res = await getSkuPackages(pkgSkuId.value!); pkgList.value = res.data || []
}

onMounted(() => { loadOwners(); fetchData() })
</script>

<style scoped>
.page-container { }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { margin-bottom: 16px; display: flex; align-items: center; }
</style>
