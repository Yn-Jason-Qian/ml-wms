<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>货主管理</span><el-button type="primary" @click="openDialog()">新增货主</el-button></div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="ownerCode" label="货主编码" width="150" />
        <el-table-column prop="ownerName" label="货主名称" />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.status===1?'warning':'success'" @click="handleToggleStatus(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑货主':'新增货主'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="货主编码" prop="ownerCode">
          <el-input v-model="form.ownerCode" placeholder="OW-开头" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="货主名称" prop="ownerName"><el-input v-model="form.ownerName" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
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
import { getOwnerPage, createOwner, updateOwner, deleteOwner, enableOwner, disableOwner } from '@/api/modules/masterdata'

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), isEdit = ref(false)
const tableData = ref<any[]>([]), formRef = ref(), editId = ref<number>()
const pageNum = ref(1), pageSize = ref(10), total = ref(0)
const form = reactive({ ownerCode: '', ownerName: '', contactPerson: '', contactPhone: '', address: '' })
const rules = {
  ownerCode: [{ required: true, message: '请输入货主编码', trigger: 'blur' }],
  ownerName: [{ required: true, message: '请输入货主名称', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getOwnerPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openDialog(row?: any) {
  isEdit.value = !!row
  if (row) { editId.value = row.id; Object.assign(form, row) }
  else { editId.value = undefined; Object.keys(form).forEach(k => (form as any)[k] = '') }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value && editId.value) { await updateOwner(editId.value, form); ElMessage.success('更新成功') }
    else { await createOwner(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}

async function handleToggleStatus(row: any) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}货主「${row.ownerName}」？`, '提示', { type: 'warning' })
    if (row.status === 1) { await disableOwner(row.id) } else { await enableOwner(row.id) }
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch { /* 用户取消 */ }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteOwner(id); ElMessage.success('删除成功'); fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
