<template>
  <view class="page-container">
    <!-- 任务筛选 -->
    <view class="card filter-section">
      <view class="filter-row">
        <up-tabs
          :list="statusTabs"
          :current="currentTab"
          @change="onTabChange"
          lineColor="#1677ff"
          :activeStyle="{ color: '#1677ff', fontWeight: 'bold' }"
          :inactiveStyle="{ color: '#606266' }"
        />
      </view>
    </view>

    <!-- 任务列表 -->
    <view class="task-list-section">
      <view class="card task-card" v-for="task in filteredTasks" :key="task.id">
        <view class="task-header">
          <view class="task-type-info">
            <up-tag :text="task.typeName" type="primary" size="small" />
            <text class="task-no">{{ task.taskNo }}</text>
          </view>
          <up-tag
            :text="task.statusText"
            :type="task.statusType"
            size="small"
          />
        </view>

        <view class="task-body">
          <view class="label-row">
            <text class="label">来源单号</text>
            <text class="value">{{ task.sourceNo || '-' }}</text>
          </view>
          <view class="label-row">
            <text class="label">目标库位</text>
            <text class="value">{{ task.targetLocation || '-' }}</text>
          </view>
          <view class="label-row">
            <text class="label">SKU 数</text>
            <text class="value">{{ task.skuCount || 0 }} 种 / {{ task.totalQty || 0 }} 件</text>
          </view>
          <view class="label-row">
            <text class="label">创建时间</text>
            <text class="value">{{ task.createdAt || '-' }}</text>
          </view>
        </view>

        <view class="task-actions" v-if="task.status === 'pending'">
          <up-button text="领取任务" type="primary" size="small" @click="handleClaim(task)" />
        </view>
        <view class="task-actions" v-if="task.status === 'assigned' || task.status === 'executing'">
          <up-button text="开始执行" type="primary" size="small" @click="handleStart(task)" />
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-section" v-if="filteredTasks.length === 0">
        <up-icon name="file-text" size="80" color="#d9d9d9" />
        <text class="empty-text">暂无任务</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

interface TaskItem {
  id: number
  taskNo: string
  typeName: string
  sourceNo: string
  targetLocation: string
  skuCount: number
  totalQty: number
  status: string
  statusText: string
  statusType: string
  createdAt: string
}

const statusTabs = [
  { name: '全部' },
  { name: '待领取' },
  { name: '进行中' },
  { name: '已完成' }
]

const currentTab = ref(0)
const tasks = ref<TaskItem[]>([])

const filteredTasks = computed(() => {
  if (currentTab.value === 0) return tasks.value
  const statusMap = ['', 'pending', 'executing', 'done']
  return tasks.value.filter(t => t.status === statusMap[currentTab.value])
})

function onTabChange(e: { index: number }) {
  currentTab.value = e.index
}

function handleClaim(task: TaskItem) {
  uni.showModal({
    title: '确认领取',
    content: `确认领取任务 ${task.taskNo}？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await request.post(`/tasks/${task.id}/claim`)
          uni.showToast({ title: '任务已领取', icon: 'success' })
          loadTasks()
        } catch { /* handled by interceptor */ }
      }
    }
  })
}

function handleStart(task: TaskItem) {
  uni.showModal({
    title: '开始执行',
    content: `确认开始执行任务 ${task.taskNo}？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await request.post(`/tasks/${task.id}/start`)
          uni.showToast({ title: '任务已开始', icon: 'success' })
          loadTasks()
        } catch { /* handled by interceptor */ }
      }
    }
  })
}

async function loadTasks() {
  try {
    const res = await request.get<any>('/tasks', { pageNum: 1, pageSize: 50 })
    tasks.value = (res.data?.records || []).map((t: any) => ({
      ...t,
      statusType: statusTypeMap(t.status)
    }))
  } catch { /* handled */ }
}

function statusTypeMap(status: string): 'warning' | 'primary' | 'success' {
  switch (status) {
    case 'pending': return 'warning'
    case 'executing': case 'assigned': return 'primary'
    case 'done': return 'success'
    default: return 'warning'
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
.filter-section {
  padding: 0;
}

.filter-row {
  width: 100%;
}

.task-card {
  margin-bottom: 16rpx;
}

.task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.task-type-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.task-no {
  font-size: 26rpx;
  color: #303133;
  font-weight: 500;
}

.task-body {
  margin-bottom: 20rpx;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-text {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: #c0c4cc;
}

.task-list-section {
  min-height: 60vh;
}
</style>
