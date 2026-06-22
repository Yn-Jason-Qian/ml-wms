<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>库区管理</span>
          <el-button type="primary" @click="openDialog()">新增库区</el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterWarehouseId" placeholder="按仓库筛选" clearable
                   @change="fetchData" style="width:200px">
          <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
        </el-select>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="areaCode" label="库区编码" width="150" />
        <el-table-column prop="areaName" label="库区名称" />
        <el-table-column prop="areaType" label="库区类型" width="120">
          <template #default="{ row }">
            <el-tag :type="areaTypeColor(row.areaType)">{{ areaTypeMap[row.areaType] || row.areaType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="temperatureMin" label="最低温(℃)" width="100" />
        <el-table-column prop="temperatureMax" label="最高温(℃)" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.status===1?'warning':'success'"
                       @click="row.status===1?handleDisable(row.id):handleEnable(row.id)">
              {{ row.status===1?'禁用':'启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑库区' : '新增库区'" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" style="width:100%" placeholder="选择仓库" :disabled="isEdit">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="库区编码" prop="areaCode">
          <el-input v-model="form.areaCode" placeholder="AR-开头" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="库区名称" prop="areaName">
          <el-input v-model="form.areaName" />
        </el-form-item>
        <el-form-item label="库区类型" prop="areaType">
          <el-select v-model="form.areaType" style="width:100%">
            <el-option v-for="(v,k) in areaTypeMap" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="温度范围">
          <el-input v-model="form.temperatureMin" placeholder="最低温" style="width:120px" />&nbsp;℃ ~ &nbsp;
          <el-input v-model="form.temperatureMax" placeholder="最高温" style="width:120px" />&nbsp;℃
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWarehouseList, getAreasByWarehouse, createArea, updateArea, deleteArea } from '@/api/modules/masterdata'

const areaTypeMap: Record<string, string> = {
  RECEIVE: '收货区', SHIPPING: '发货区', STORAGE: '存储区', PICKING: '拣货区', RETURN: '退货区', QC: '质检区'
}
const areaTypeColor = (t: string) => t==='STORAGE'?'success':t==='RECEIVE'?'primary':t==='SHIPPING'?'warning':t==='QC'?'danger':'info'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref<any[]>([])
const warehouses = ref<any[]>([])
const filterWarehouseId = ref<number>()
const formRef = ref()
const editId = ref<number>()

const form = reactive({ warehouseId: null as any, areaCode: '', areaName: '', areaType: 'STORAGE', temperatureMin: '', temperatureMax: '' })
const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  areaCode: [{ required: true, message: '请输入库区编码', trigger: 'blur' }],
  areaName: [{ required: true, message: '请输入库区名称', trigger: 'blur' }],
  areaType: [{ required: true, message: '请选择库区类型', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const wid = filterWarehouseId.value
    if (wid) {
      const res = await getAreasByWarehouse(wid)
      tableData.value = res.data
    } else {
      tableData.value = []
    }
  } finally { loading.value = false }
}

async function loadWarehouses() {
  const res = await getWarehouseList()
  warehouses.value = res.data || []
}

function openDialog(row?: any) {
  isEdit.value = !!row
  if (row) {
    editId.value = row.id
    Object.assign(form, row)
  } else {
    editId.value = undefined
    Object.keys(form).forEach(k => (form as any)[k] = '')
    form.areaType = 'STORAGE'
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateArea(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createArea(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally { saving.value = false }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteArea(id)
  ElMessage.success('删除成功')
  fetchData()
}

async function handleDisable(id: number) {
  await ElMessageBox.confirm('确定禁用？', '提示', { type: 'warning' })
  ElMessage.info('已禁用')
  fetchData()
}
async function handleEnable(id: number) { ElMessage.info('已启用'); fetchData() }

onMounted(() => { loadWarehouses(); fetchData() })
</script>

<style scoped>
.page-container { }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { margin-bottom: 16px; }
</style>
