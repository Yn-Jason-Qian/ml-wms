<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card header="字典类型">
          <div v-for="t in dictTypes" :key="t" class="dict-type-item"
               :class="{ active: currentType === t }" @click="selectType(t)">
            {{ t }}
          </div>
          <el-empty v-if="!dictTypes.length" description="暂无" :image-size="60" />
          <div style="margin-top:12px">
            <el-input v-model="newType" placeholder="新类型名" size="small" style="width:calc(100% - 60px)" />
            <el-button size="small" @click="addType" style="margin-left:4px">+</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <template #header>
            <div class="page-header">
              <span>{{ currentType || '请选择字典类型' }}</span>
              <el-button v-if="currentType" type="primary" size="small" @click="openDialog()">新增字典项</el-button>
            </div>
          </template>
          <el-table :data="items" border stripe size="small" v-if="currentType">
            <el-table-column prop="dictCode" label="编码" width="150" />
            <el-table-column prop="dictName" label="名称" />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column prop="parentCode" label="父编码" width="120" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }"><el-tag size="small" :type="row.status===1?'success':'danger'">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="请从左侧选择字典类型" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑字典项':'新增字典项'" width="450px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="字典编码" prop="dictCode"><el-input v-model="form.dictCode" :disabled="isEdit" /></el-form-item>
        <el-form-item label="字典名称" prop="dictName"><el-input v-model="form.dictName" /></el-form-item>
        <el-form-item label="父编码"><el-input v-model="form.parentCode" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" controls-position="right" /></el-form-item>
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
import request from '@/api/request'

const dictTypes = ref<string[]>([])
const currentType = ref('')
const items = ref<any[]>([])
const newType = ref('')
const loading = ref(false), saving = ref(false), dialogVisible = ref(false), isEdit = ref(false)
const formRef = ref(), editId = ref<number>()
const form = reactive({ dictType: '', dictCode: '', dictName: '', parentCode: '0', sortOrder: 0 })
const rules = {
  dictCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

async function loadTypes() {
  const res = await request.get('/masterdata/dict/types')
  dictTypes.value = res.data || []
}

async function selectType(type: string) {
  currentType.value = type
  const res = await request.get(`/masterdata/dict/items/${type}`)
  items.value = res.data || []
}

async function addType() {
  if (!newType.value) return
  await request.post('/masterdata/dict', { dictType: newType.value, dictCode: 'START', dictName: '初始项', sortOrder: 0 })
  ElMessage.success('添加成功'); loadTypes(); currentType.value = newType.value; newType.value = ''
}

function openDialog(row?: any) {
  isEdit.value = !!row
  if (row) { editId.value = row.id; Object.assign(form, row) }
  else { editId.value = undefined; Object.keys(form).forEach(k => (form as any)[k] = ''); form.dictType = currentType.value; form.parentCode = '0'; form.sortOrder = 0 }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value && editId.value) {
      await request.put(`/masterdata/dict/${editId.value}`, form); ElMessage.success('更新成功')
    } else {
      await request.post('/masterdata/dict', form); ElMessage.success('创建成功')
    }
    dialogVisible.value = false; selectType(currentType.value)
  } finally { saving.value = false }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await request.delete(`/masterdata/dict/${id}`)
  ElMessage.success('删除成功'); selectType(currentType.value)
}

onMounted(loadTypes)
</script>

<style scoped>
.dict-type-item { padding: 8px 12px; cursor: pointer; border-radius: 4px; margin-bottom: 2px; }
.dict-type-item:hover { background: #f0f2f5; }
.dict-type-item.active { background: #ecf5ff; color: #409eff; font-weight: bold; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
