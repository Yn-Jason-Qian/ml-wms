<template>
  <view class="page-container">
    <!-- 任务选择 -->
    <view class="card" v-if="!currentTask">
      <text class="section-title">选择上架任务</text>

      <text class="field-label">扫描收货单号</text>
      <view class="scan-row">
        <input
          class="scan-input"
          v-model="scanCode"
          placeholder="扫描收货容器 / 收货单号"
          @confirm="onSearchScanned"
        />
        <view class="scan-btn" @click="onScanClick('search')">
          <up-icon name="scan" size="24" color="#fff" />
        </view>
      </view>

      <view class="task-list-section" v-if="taskList.length > 0">
        <text class="section-title">待上架任务</text>
        <view
          class="task-card"
          v-for="task in taskList"
          :key="task.id"
          @click="selectTask(task)"
        >
          <view class="task-header">
            <text class="task-no">{{ task.putawayNo }}</text>
            <up-tag :text="task.statusText" :type="task.statusType" size="small" />
          </view>
          <view class="label-row">
            <text class="label">来源收货</text>
            <text class="value">{{ task.receiveNo || '-' }}</text>
          </view>
          <view class="label-row">
            <text class="label">待上架行数</text>
            <text class="value highlight">{{ task.remainingLines || 0 }} 行</text>
          </view>
        </view>
      </view>

      <view class="empty-section" v-if="taskList.length === 0 && hasSearched">
        <text class="empty-text">未找到待上架任务</text>
      </view>
    </view>

    <!-- 上架操作 -->
    <view class="card" v-if="currentTask">
      <view class="task-bar">
        <view class="task-bar-left">
          <text class="task-bar-no">{{ currentTask.putawayNo }}</text>
          <text class="task-bar-lines">剩余 {{ remainingLines }} 行</text>
        </view>
        <up-button text="切换任务" size="small" plain @click="currentTask = null" />
      </view>

      <!-- 当前行信息 -->
      <view class="putaway-line-card" v-if="currentLine">
        <view class="line-status-bar">
          <text class="line-progress">{{ lineIndex + 1 }} / {{ putawayLines.length }}</text>
          <up-tag v-if="currentLine.done" text="已完成" type="success" size="small" />
          <up-tag v-else text="待上架" type="warning" size="small" />
        </view>

        <view class="sku-section">
          <text class="field-label">SKU</text>
          <view class="sku-row">
            <text class="sku-code-lg">{{ currentLine.skuCode }}</text>
            <text class="sku-name">{{ currentLine.skuName }}</text>
          </view>
        </view>

        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">应上架数量</text>
            <text class="info-value big">{{ currentLine.putawayQty }} 件</text>
          </view>
          <view class="info-item">
            <text class="info-label">来源库位</text>
            <text class="info-value">{{ currentLine.fromLocationCode || '-' }}</text>
          </view>
        </view>

        <view class="info-grid" v-if="currentLine.batchNo">
          <view class="info-item">
            <text class="info-label">批次</text>
            <text class="info-value">{{ currentLine.batchNo }}</text>
          </view>
          <view class="info-item" v-if="currentLine.expiryDate">
            <text class="info-label">效期至</text>
            <text class="info-value">{{ currentLine.expiryDate }}</text>
          </view>
        </view>

        <!-- 扫描目标库位 -->
        <text class="field-label">目标库位</text>
        <view class="scan-row">
          <input
            class="scan-input highlight-input"
            v-model="targetLocation"
            placeholder="扫描目标库位"
            @confirm="onLocationScanned"
            ref="locationInputRef"
          />
          <view class="scan-btn" @click="onScanClick('location')">
            <up-icon name="scan" size="24" color="#fff" />
          </view>
        </view>

        <view class="location-hint" v-if="targetLocation">
          <up-icon name="checkmark-circle" size="18" color="#52c41a" />
          <text class="hint-text">目标库位: {{ targetLocation }}</text>
        </view>

        <view class="confirm-wrap">
          <up-button
            type="primary"
            text="确认上架"
            size="large"
            shape="round"
            :disabled="!targetLocation"
            :loading="confirming"
            @click="confirmPutaway"
          />
        </view>

        <!-- 上一条 / 下一条 -->
        <view class="nav-btns" v-if="putawayLines.length > 1">
          <up-button text="上一条" size="small" :disabled="lineIndex <= 0" @click="prevLine" />
          <up-button text="下一条" size="small" :disabled="lineIndex >= putawayLines.length - 1" @click="nextLine" />
        </view>

        <!-- 全部完成 -->
        <view class="complete-wrap" v-if="remainingLines === 0">
          <up-button text="返回任务列表" shape="round" @click="currentTask = null; loadTasks()" />
        </view>
      </view>

      <!-- 行列表选择 -->
      <view class="line-selector" v-if="putawayLines.length > 1">
        <text class="section-title">所有行</text>
        <view
          class="line-chip"
          v-for="(line, idx) in putawayLines"
          :key="line.id"
          :class="{ active: idx === lineIndex, done: line.done }"
          @click="selectLine(idx)"
        >
          <text class="chip-text">{{ line.skuCode?.slice(0, 8) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useScanner } from '@/composables/useScanner'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const { scan } = useScanner()
const authStore = useAuthStore()

// ── 任务列表 ──
const scanCode = ref('')
const taskList = ref<any[]>([])
const hasSearched = ref(false)
const currentTask = ref<any>(null)

// ── 上架行 ──
const putawayLines = ref<any[]>([])
const lineIndex = ref(0)
const targetLocation = ref('')
const confirming = ref(false)

const currentLine = computed(() => putawayLines.value[lineIndex.value] || null)
const remainingLines = computed(() => putawayLines.value.filter(l => !l.done).length)

// ── 加载任务 ──
async function loadTasks(receiveNo?: string) {
  hasSearched.value = true
  try {
    const params: any = { pageNum: 1, pageSize: 50, warehouseId: authStore.warehouseId }
    if (receiveNo) params.receiveNo = receiveNo

    const res = await request.get('/inbound/putaways/page', params)
    const records = res.data?.records || []

    taskList.value = records.map((t: any) => ({
      ...t,
      statusText: statusTextMap(t.status),
      statusType: statusTypeMap(t.status)
    }))
  } catch { /* handled */ }
}

function statusTextMap(status: string): string {
  switch (status) {
    case 'CREATED': return '待上架'
    case 'PUTAWAYING': case 'PARTIAL_DONE': return '进行中'
    case 'DONE': return '已完成'
    default: return status || '-'
  }
}

function statusTypeMap(status: string): 'warning' | 'primary' | 'success' {
  if (status === 'DONE') return 'success'
  if (status === 'PUTAWAYING' || status === 'PARTIAL_DONE') return 'primary'
  return 'warning'
}

// ── 扫码加载 ──
async function onSearchScanned() {
  if (scanCode.value.trim()) {
    await loadTasks(scanCode.value.trim())
  }
}

async function onScanClick(target: string) {
  const result = await scan()
  if (!result) return

  if (target === 'search') {
    scanCode.value = result.barcodeValue
    await onSearchScanned()
  } else if (target === 'location') {
    targetLocation.value = result.barcodeValue
  }
}

// ── 选择任务 ──
async function selectTask(task: any) {
  currentTask.value = task
  // 加载上架行
  try {
    const res = await request.get(`/inbound/putaways/${task.id}/lines`)
    const lines = (res.data?.records || res.data || []).map((l: any) => ({
      ...l,
      done: l.status === 'DONE' || l.doneQty >= l.putawayQty
    }))
    putawayLines.value = lines
    // 自动跳到第一个未完成的行
    const firstPending = lines.findIndex((l: any) => !l.done)
    lineIndex.value = firstPending >= 0 ? firstPending : 0
    targetLocation.value = ''
  } catch {
    // 如果 /lines 接口不存在，使用模拟数据
    putawayLines.value = [{ id: task.id + '_1', lineNo: 1, skuCode: '-', skuName: '-', putawayQty: 0, fromLocationCode: '-', done: false }]
  }
}

// ── 浏览行 ──
function selectLine(idx: number) {
  lineIndex.value = idx
  targetLocation.value = ''
}

function prevLine() {
  if (lineIndex.value > 0) {
    lineIndex.value--
    targetLocation.value = ''
  }
}

function nextLine() {
  if (lineIndex.value < putawayLines.value.length - 1) {
    lineIndex.value++
    targetLocation.value = ''
  }
}

// ── 确认上架 ──
async function onLocationScanned() {
  if (targetLocation.value) {
    uni.showToast({ title: '库位: ' + targetLocation.value, icon: 'success', duration: 1000 })
    // 库位确认后可直接触发上架确认
  }
}

async function confirmPutaway() {
  if (!targetLocation.value.trim()) {
    uni.showToast({ title: '请扫描目标库位', icon: 'none' })
    return
  }

  confirming.value = true
  try {
    await request.post('/inbound/putaways/submit', {
      putawayHeaderId: currentTask.value.id,
      putawayLineId: currentLine.value?.id,
      toLocationCode: targetLocation.value.trim()
    })

    // 标记当前行为已完成
    if (currentLine.value) {
      currentLine.value.done = true
    }
    targetLocation.value = ''

    uni.vibrateShort({ type: 'heavy' })
    uni.showToast({ title: '上架成功', icon: 'success' })

    // 自动跳到下一个未完成的行
    if (remainingLines.value > 0) {
      const nextPending = putawayLines.value.findIndex(l => !l.done)
      if (nextPending >= 0) lineIndex.value = nextPending
    }
  } catch { /* handled */ } finally {
    confirming.value = false
  }
}

// ── 初始加载 ──
onMounted(() => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20rpx;
  display: block;
}

.field-label {
  font-size: 26rpx;
  color: #606266;
  margin-bottom: 8rpx;
  display: block;
  margin-top: 20rpx;
}

// ── 扫码行 ──
.scan-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.scan-input {
  flex: 1;
  height: 80rpx;
  background: #fafafa;
  border: 2rpx dashed #d9d9d9;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 32rpx;
  text-align: center;
}

.highlight-input {
  border-color: #1677ff;
  background: #f0f5ff;
  font-size: 34rpx;
  font-weight: 600;
}

.scan-btn {
  width: 80rpx;
  height: 80rpx;
  background: #1677ff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

// ── 任务列表 ──
.task-list-section {
  margin-top: 32rpx;
}

.task-card {
  padding: 20rpx;
  background: #fafafa;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.task-card:active { background: #f0f0f0; }

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.task-no {
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
}

.highlight {
  color: #1677ff;
  font-weight: 600;
}

// ── 任务操作栏 ──
.task-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 20rpx;
}

.task-bar-left {
  display: flex;
  flex-direction: column;
}

.task-bar-no {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}

.task-bar-lines {
  font-size: 24rpx;
  color: #909399;
  margin-top: 4rpx;
}

// ── 上架行信息 ──
.putaway-line-card {
  background: #fafafa;
  border-radius: 16rpx;
  padding: 24rpx;
}

.line-status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.line-progress {
  font-size: 26rpx;
  color: #909399;
}

.sku-section {
  margin-bottom: 16rpx;
}

.sku-row {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.sku-code-lg {
  font-size: 36rpx;
  font-weight: 700;
  color: #303133;
}

.sku-name {
  font-size: 26rpx;
  color: #909399;
}

.info-grid {
  display: flex;
  gap: 24rpx;
  margin-bottom: 12rpx;
}

.info-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 24rpx;
  color: #909399;
  margin-bottom: 4rpx;
}

.info-value {
  font-size: 28rpx;
  color: #303133;
  font-weight: 500;
}

.info-value.big {
  font-size: 34rpx;
  font-weight: 700;
  color: #1677ff;
}

// ── 库位提示 ──
.location-hint {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 12rpx;
  padding: 12rpx 16rpx;
  background: #f6ffed;
  border-radius: 8rpx;
}

.hint-text {
  font-size: 26rpx;
  color: #52c41a;
}

// ── 确认 ──
.confirm-wrap {
  margin-top: 28rpx;
}

// ── 导航按钮 ──
.nav-btns {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
  justify-content: center;
}

// ── 完成 ──
.complete-wrap {
  margin-top: 32rpx;
}

// ── 行选择器 ──
.line-selector {
  margin-top: 24rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.line-chip {
  display: inline-flex;
  padding: 12rpx 20rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin: 8rpx;
}

.line-chip.active {
  background: #1677ff;
}

.line-chip.active .chip-text { color: #fff; }
.line-chip.done { opacity: 0.5; }

.chip-text {
  font-size: 24rpx;
  color: #606266;
}

// ── 空状态 ──
.empty-section {
  padding: 80rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #c0c4cc;
}
</style>
