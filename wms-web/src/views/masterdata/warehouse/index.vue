<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>仓库管理</span>
          <el-button type="primary" @click="openDialog()">新增仓库</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="whCode" label="仓库编码" width="150" />
        <el-table-column prop="whName" label="仓库名称" />
        <el-table-column prop="whType" label="仓库类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ whTypeMap[row.whType] || row.whType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑仓库' : '新增仓库'"
               width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库编码" prop="whCode">
          <el-input v-model="form.whCode" placeholder="WH-开头" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="whName">
          <el-input v-model="form.whName" />
        </el-form-item>
        <el-form-item label="仓库类型" prop="whType">
          <el-select v-model="form.whType" style="width:100%">
            <el-option v-for="(v, k) in whTypeMap" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" />
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
import { getWarehousePage, createWarehouse, updateWarehouse, deleteWarehouse, enableWarehouse, disableWarehouse } from '@/api/masterdata'

const whTypeMap: Record<string, string> = {
  STANDARD: '标准仓', COLD: '冷库', HAZARDOUS: '危化品仓', BONDED: '保税仓'
}

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const formRef = ref()
const editId = ref<number>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const form = reactive({
  whCode: '', whName: '', whType: 'STANDARD', address: '', contactPerson: '', contactPhone: ''
})

const rules = {
  whCode: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
  whName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  whType: [{ required: true, message: '请选择仓库类型', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getWarehousePage({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  isEdit.value = !!row
  if (row) {
    editId.value = row.id
    Object.assign(form, row)
  } else {
    editId.value = undefined
    Object.keys(form).forEach(k => (form as any)[k] = '')
    form.whType = 'STANDARD'
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateWarehouse(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createWarehouse(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(row: any) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}仓库「${row.whName}」？`, '提示', { type: 'warning' })
    if (row.status === 1) {
      await disableWarehouse(row.id)
    } else {
      await enableWarehouse(row.id)
    }
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch {
    // 用户取消
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该仓库？', '提示', { type: 'warning' })
  await deleteWarehouse(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
