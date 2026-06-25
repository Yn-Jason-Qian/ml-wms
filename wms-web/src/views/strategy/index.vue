<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>策略管理</span><el-button type="primary" @click="openConfigDialog()">新增策略</el-button></div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="fetchConfigs">
        <el-tab-pane label="上架策略" name="PUTAWAY" />
        <el-tab-pane label="分配策略" name="ALLOCATION" />
        <el-tab-pane label="波次策略" name="WAVE" />
        <el-tab-pane label="拣货策略" name="PICKING" />
      </el-tabs>

      <el-table :data="configs" border stripe size="small">
        <el-table-column prop="strategyCode" label="策略编码" width="180" />
        <el-table-column prop="strategyName" label="策略名称" />
        <el-table-column prop="sortOrder" label="优先级" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.isEnabled?'success':'danger'">{{ row.isEnabled?'启用':'禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="规则数" width="70">
          <template #default="{ row }">{{ row.rules?.length || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openRuleDialog(row)">管理规则</el-button>
            <el-button link type="danger" size="small" @click="deleteConfig(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 策略弹窗 -->
    <el-dialog v-model="configDialogVisible" title="新增策略" width="450px" destroy-on-close>
      <el-form ref="configFormRef" :model="configForm" label-width="100px">
        <el-form-item label="策略编码" required><el-input v-model="configForm.strategyCode" /></el-form-item>
        <el-form-item label="策略名称" required><el-input v-model="configForm.strategyName" /></el-form-item>
        <el-form-item label="优先级"><el-input-number v-model="configForm.sortOrder" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="configForm.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig" :loading="cfgSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 规则管理弹窗 -->
    <el-dialog v-model="ruleDialogVisible" :title="'规则管理: ' + (currentConfig?.strategyName||'')" width="800px" destroy-on-close>
      <div style="margin-bottom:8px"><el-button size="small" type="primary" @click="openRuleForm()">添加规则</el-button></div>
      <el-table :data="currentRules" border size="small">
        <el-table-column prop="ruleNo" label="序号" width="60" />
        <el-table-column prop="ruleName" label="规则名" width="120" />
        <el-table-column prop="conditionsJson" label="条件(JSON)" min-width="240">
          <template #default="{ row }"><code style="font-size:12px">{{ row.conditionsJson }}</code></template>
        </el-table-column>
        <el-table-column prop="actionsJson" label="动作(JSON)" min-width="200">
          <template #default="{ row }"><code style="font-size:12px">{{ row.actionsJson }}</code></template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }"><el-button link type="danger" size="small" @click="deleteRule(row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>

      <div v-if="ruleFormVisible" style="margin-top:12px;padding:0;background:#f8f9fb;border-radius:8px">
        <div style="padding:12px 16px 0">
          <el-row :gutter="10">
            <el-col :span="4"><el-form-item label="序号" label-position="top"><el-input-number v-model="ruleForm.ruleNo" :min="1" controls-position="right" style="width:100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="规则名称" label-position="top"><el-input v-model="ruleForm.ruleName" placeholder="如：重货上架规则" /></el-form-item></el-col>
            <el-col :span="8" style="display:flex;align-items:flex-end;gap:8px;padding-bottom:12px">
              <el-button type="primary" @click="saveRule" :loading="ruleSaving" style="margin-left:auto">保存规则</el-button>
              <el-button @click="ruleFormVisible=false">取消</el-button>
            </el-col>
          </el-row>
        </div>
        <RuleEditor v-model="ruleEditorModel" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import RuleEditor from './RuleEditor.vue'

const activeTab = ref('PUTAWAY')
const configs = ref<any[]>([])
const configDialogVisible = ref(false), ruleDialogVisible = ref(false), ruleFormVisible = ref(false)
const cfgSaving = ref(false), ruleSaving = ref(false)
const currentConfig = ref<any>(null), currentRules = ref<any[]>([])
const configFormRef = ref()

const configForm = reactive({ strategyCode: '', strategyName: '', strategyType: '', sortOrder: 0, description: '' })
const ruleForm = reactive({ strategyId: null as any, ruleNo: 1, ruleName: '', conditionsJson: '[]', actionsJson: '[]' })
const ruleEditorModel = reactive({ conditionsJson: '[]', actionsJson: '[]' })

watch(ruleEditorModel, (v) => {
  ruleForm.conditionsJson = v.conditionsJson
  ruleForm.actionsJson = v.actionsJson
})

async function fetchConfigs() {
  const res = await request.get(`/strategy/configs/type/${activeTab.value}`)
  configs.value = res.data || []
}

function openConfigDialog() {
  Object.assign(configForm, { strategyCode: '', strategyName: '', sortOrder: 0, description: '' })
  configForm.strategyType = activeTab.value; configDialogVisible.value = true
}

async function saveConfig() {
  cfgSaving.value = true
  try { await request.post('/strategy/configs', configForm); ElMessage.success('创建成功'); configDialogVisible.value = false; fetchConfigs() }
  finally { cfgSaving.value = false }
}

async function deleteConfig(id: number) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await request.delete(`/strategy/configs/${id}`); ElMessage.success('删除成功'); fetchConfigs()
}

async function openRuleDialog(config: any) {
  currentConfig.value = config; ruleFormVisible.value = false
  const res = await request.get(`/strategy/configs/${config.id}`)
  currentRules.value = res.data.rules || []; ruleDialogVisible.value = true
}

function openRuleForm() {
  const init = { strategyId: currentConfig.value.id, ruleNo: (currentRules.value.length||0)+1, ruleName: '', conditionsJson: '[]', actionsJson: '[]' }
  Object.assign(ruleForm, init)
  Object.assign(ruleEditorModel, { conditionsJson: '[]', actionsJson: '[]' })
  ruleFormVisible.value = true
}

async function saveRule() {
  if (!ruleForm.ruleName) { ElMessage.warning('请输入规则名'); return }
  ruleSaving.value = true
  try { await request.post('/strategy/rules', ruleForm); ElMessage.success('已添加'); ruleFormVisible.value = false; openRuleDialog(currentConfig.value) }
  finally { ruleSaving.value = false }
}

async function deleteRule(id: number) {
  await request.delete(`/strategy/rules/${id}`); ElMessage.success('已删除'); openRuleDialog(currentConfig.value)
}

onMounted(fetchConfigs)
</script>
