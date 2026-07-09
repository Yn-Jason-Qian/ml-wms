<template>
  <div class="page-container">
    <el-card>
      <template #header><span>任务管理</span></template>
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="类型"><el-select v-model="query.taskType" clearable placeholder="全部"><el-option v-for="(v,k) in typeMap" :key="k" :label="v" :value="k" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部"><el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="k" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="taskNo" label="任务号" width="180" />
        <el-table-column prop="taskType" label="类型" width="100"><template #default="{row}"><el-tag size="small">{{typeMap[row.taskType]||row.taskType}}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{row}"><el-tag size="small" :type="statusColor(row.status)">{{statusMap[row.status]||row.status}}</el-tag></template></el-table-column>
        <el-table-column prop="taskSourceNo" label="来源单号" width="150" />
        <el-table-column prop="priority" label="优先级" width="70" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status==='CREATED'||row.status==='RELEASED'" link type="primary" size="small" @click="doAction(row.id,'claim')">领取</el-button>
            <el-button v-if="row.status==='ASSIGNED'" link type="warning" size="small" @click="doAction(row.id,'start')">开始</el-button>
            <el-button v-if="row.status==='EXECUTING'" link type="success" size="small" @click="doAction(row.id,'complete')">完成</el-button>
            <el-button v-if="row.status!=='DONE'&&row.status!=='CANCELLED'" link type="danger" size="small" @click="doAction(row.id,'cancel')">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px;text-align:right">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total, prev, pager, next" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageTasks, claimTask, startTask, completeTask, cancelTask } from '@/api/task'

const typeMap: Record<string, string> = { PUTAWAY: '上架', PICK: '拣货', MOVE: '移库', REPLENISH: '补货', STOCKTAKE: '盘点' }
const statusMap: Record<string, string> = { CREATED: '已创建', RELEASED: '已释放', ASSIGNED: '已领取', EXECUTING: '执行中', DONE: '已完成', CANCELLED: '已取消' }
const statusColor = (s: string) => s === 'DONE' ? 'success' : s === 'EXECUTING' ? 'warning' : s === 'CANCELLED' ? 'danger' : 'info'

const loading = ref(false), tableData = ref<any[]>([]), total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, taskType: '', status: '' })

async function fetchData() {
  loading.value = true
  try { const res = await pageTasks(query); tableData.value = res.data.records || []; total.value = res.data.total || 0 } finally { loading.value = false }
}

async function doAction(id: number, action: string) {
  try {
    if (action === 'cancel') await ElMessageBox.confirm('确定取消？', '提示', { type: 'warning' })
    if (action === 'claim') { await claimTask(id); ElMessage.success('已领取') }
    else if (action === 'start') { await startTask(id); ElMessage.success('已开始') }
    else if (action === 'complete') { await completeTask(id); ElMessage.success('已完成') }
    else if (action === 'cancel') { await cancelTask(id); ElMessage.success('已取消') }
    fetchData()
  } catch {}
}

fetchData()
</script>
