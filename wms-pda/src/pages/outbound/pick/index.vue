<template>
  <view class="page-container">
    <!-- 任务选择 -->
    <view class="card" v-if="!currentTask">
      <text class="section-title">拣货任务</text>

      <view class="status-tabs">
        <view
          class="tab-item"
          :class="{ active: statusFilter === 'all' }"
          @click="statusFilter = 'all'"
        ><text>全部</text></view>
        <view
          class="tab-item"
          :class="{ active: statusFilter === 'pending' }"
          @click="statusFilter = 'pending'"
        ><text>待拣货</text><text class="tab-count" v-if="pendingCount > 0">{{ pendingCount }}</text></view>
        <view
          class="tab-item"
          :class="{ active: statusFilter === 'progress' }"
          @click="statusFilter = 'progress'"
        ><text>进行中</text></view>
      </view>

      <view class="task-list" v-if="filteredTasks.length > 0">
        <view
          class="task-item"
          v-for="task in filteredTasks"
          :key="task.id"
          @click="selectTask(task)"
        >
          <view class="task-left">
            <text class="task-no">{{ task.pickNo }}</text>
            <text class="task-type">{{ task.pickTypeName || 'RF拣货' }}</text>
            <text class="task-info" v-if="task.assignTo">
              <text class="task-item-tag">线路 {{ task.pickZone || '-' }}</text>
            </text>
          </view>
          <view class="task-right">
            <up-tag :text="task.statusText" :type="task.statusType" size="small" />
            <text class="task-lines">{{ task.totalLines || 0 }} 行</text>
          </view>
        </view>
      </view>

      <view class="empty-section" v-else>
        <text class="empty-text">暂无拣货任务</text>
      </view>

      <up-button
        text="领取新任务"
        type="primary"
        shape="round"
        block
        @click="claimNewTask"
        style="margin-top: 24rpx;"
      />
    </view>

    <!-- 拣货操作 -->
    <view v-if="currentTask" class="pick-workspace">
      <!-- 顶部任务条 -->
      <view class="top-bar">
        <view class="top-left" @click="currentTask = null; loadTasks()">
          <up-icon name="arrow-left" size="22" color="#fff" />
          <text class="top-title">拣货</text>
        </view>
        <view class="top-right">
          <text class="top-progress">{{ completedLines }}/{{ totalLines }}</text>
        </view>
      </view>

      <!-- 当前行 -->
      <view class="card pick-card" v-if="currentLine">
        <!-- 找货信息 -->
        <view class="find-section">
          <view class="find-header">
            <view class="find-badge">找货</view>
            <text class="find-location">{{ currentLine.locationCode || currentLine.locationId }}</text>
          </view>

          <view class="find-sku">
            <text class="sku-label">SKU</text>
            <text class="sku-value">{{ currentLine.skuCode }}</text>
          </view>

          <view class="find-name" v-if="currentLine.skuName">
            <text>{{ currentLine.skuName }}</text>
          </view>

          <view class="find-details">
            <view class="detail-item">
              <text class="detail-label">应拣数量</text>
              <text class="detail-value big">{{ currentLine.pickQty }} 件</text>
            </view>
            <view class="detail-item" v-if="currentLine.batchNo">
              <text class="detail-label">批次</text>
              <text class="detail-value">{{ currentLine.batchNo }}</text>
            </view>
            <view class="detail-item" v-if="currentLine.lotAttrs">
              <text class="detail-label">批属性</text>
              <text class="detail-value">{{ currentLine.lotAttrs }}</text>
            </view>
          </view>
        </view>

        <!-- 扫描确认区 -->
        <view class="confirm-section">
          <!-- 扫描库位验证 -->
          <text class="field-label">① 验证库位</text>
          <view class="scan-row">
            <input
              class="scan-input"
              :class="{ verified: locationVerified }"
              v-model="scannedLocation"
              placeholder="扫描库位条码"
              :disabled="locationVerified"
              @confirm="verifyLocation"
            />
            <view class="scan-btn" @click="onScanClick('location')" v-if="!locationVerified">
              <up-icon name="scan" size="24" color="#fff" />
            </view>
            <view class="verified-icon" v-else>
              <up-icon name="checkmark-circle-fill" size="28" color="#52c41a" />
            </view>
          </view>

          <!-- 扫描SKU验证 -->
          <text class="field-label">② 验证SKU</text>
          <view class="scan-row">
            <input
              class="scan-input"
              :class="{ verified: skuVerified }"
              v-model="scannedSku"
              placeholder="扫描SKU条码"
              :disabled="skuVerified"
              @confirm="verifySku"
            />
            <view class="scan-btn" @click="onScanClick('sku')" v-if="!skuVerified">
              <up-icon name="scan" size="24" color="#fff" />
            </view>
            <view class="verified-icon" v-else>
              <up-icon name="checkmark-circle-fill" size="28" color="#52c41a" />
            </view>
          </view>

          <!-- 输入拣货数量 -->
          <text class="field-label">③ 拣货数量</text>
          <view class="picked-qty-row">
            <input
              class="qty-input-lg"
              v-model.number="pickedQty"
              type="digit"
              :placeholder="String(currentLine.pickQty || 0)"
            />
            <text class="qty-unit">件</text>
            <view class="quick-qty-btns">
              <view class="quick-btn" @click="pickedQty = currentLine.pickQty">全部</view>
              <view class="quick-btn" @click="pickedQty = (pickedQty || 0) + 1">+1</view>
            </view>
          </view>

          <!-- 扫描容器 -->
          <text class="field-label">④ 绑定容器</text>
          <view class="scan-row">
            <input
              class="scan-input"
              :class="{ verified: containerVerified }"
              v-model="toContainer"
              placeholder="扫描容器/周转箱条码"
              @confirm="verifyContainer"
            />
            <view class="scan-btn" @click="onScanClick('container')">
              <up-icon name="scan" size="24" color="#fff" />
            </view>
          </view>
        </view>

        <!-- 确认按钮 -->
        <view class="pick-confirm-wrap">
          <up-button
            type="primary"
            text="确认拣货"
            size="large"
            shape="round"
            :disabled="!canConfirm"
            :loading="confirming"
            @click="confirmPick"
          />
        </view>

        <!-- 异常按钮 -->
        <view class="exception-wrap">
          <up-button text="标记异常" type="error" plain size="small" @click="markException" />
        </view>
      </view>

      <!-- 行导航条（底部） -->
      <view class="line-nav" v-if="putawayLines.length > 1">
        <view
          class="nav-dot"
          v-for="(line, idx) in putawayLines"
          :key="idx"
          :class="{ current: idx === lineIndex, done: line.done, skip: line.skip }"
          @click="selectLine(idx)"
        >
          <text>{{ idx + 1 }}</text>
        </view>
      </view>
    </view>

    <!-- 全部完成 -->
    <view class="card complete-card" v-if="showComplete">
      <view class="complete-icon">
        <up-icon name="checkmark-circle-fill" size="96" color="#52c41a" />
      </view>
      <text class="complete-title">拣货完成</text>
      <text class="complete-detail">{{ completedLines }}/{{ totalLines }} 行，全部完成</text>
      <up-button type="primary" text="返回任务列表" shape="round" @click="currentTask = null; loadTasks(); showComplete = false" />
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
const statusFilter = ref('all')
const taskList = ref<any[]>([])
const showComplete = ref(false)

const pendingCount = computed(() =>
  taskList.value.filter(t => t.status === 'CREATED' || t.status === 'ASSIGNED').length
)

const filteredTasks = computed(() => {
  if (statusFilter.value === 'all') return taskList.value
  if (statusFilter.value === 'pending') return taskList.value.filter(t => t.status === 'CREATED' || t.status === 'ASSIGNED')
  if (statusFilter.value === 'progress') return taskList.value.filter(t => t.status === 'PICKING' || t.status === 'EXECUTING')
  return taskList.value
})

// ── 拣货操作 ──
const currentTask = ref<any>(null)
const putawayLines = ref<any[]>([])
const lineIndex = ref(0)
const confirming = ref(false)

const scannedLocation = ref('')
const scannedSku = ref('')
const pickedQty = ref(0)
const toContainer = ref('')
const locationVerified = ref(false)
const skuVerified = ref(false)
const containerVerified = ref(false)

const currentLine = computed(() => putawayLines.value[lineIndex.value] || null)
const totalLines = computed(() => putawayLines.value.length)
const completedLines = computed(() => putawayLines.value.filter(l => l.done).length)

const canConfirm = computed(() => {
  if (!currentLine.value) return false
  if (currentLine.value.done) return false
  // 至少需要：库位验证通过 + SKU验证通过 + 数量 > 0 + 容器已填
  return locationVerified.value && skuVerified.value && pickedQty.value > 0 && toContainer.value.trim().length > 0
})

// ── 加载任务 ──
async function loadTasks() {
  try {
    const res = await request.get('/outbound/picks/page', {
      pageNum: 1, pageSize: 50, warehouseId: authStore.warehouseId,
      assignTo: authStore.username || undefined
    })
    const records = res.data?.records || []
    taskList.value = records.map((t: any) => ({
      ...t,
      statusText: statusTextMap(t.status),
      statusType: statusTypeMap(t.status),
      pickTypeName: t.pickType === 'RF' ? 'RF拣货' : (t.pickType || '普通拣货')
    }))
  } catch { /* handled */ }
}

function statusTextMap(status: string): string {
  switch (status) {
    case 'CREATED': return '待分配'
    case 'ASSIGNED': return '已分配'
    case 'PICKING': case 'EXECUTING': return '拣货中'
    case 'PICKED': case 'DONE': return '已完成'
    default: return status || '-'
  }
}

function statusTypeMap(status: string): 'warning' | 'primary' | 'success' {
  if (status === 'DONE' || status === 'PICKED') return 'success'
  if (status === 'PICKING' || status === 'EXECUTING') return 'primary'
  return 'warning'
}

// ── 领取任务 ──
async function claimNewTask() {
  // 查找第一个待领取的任务
  const pending = taskList.value.find(t => t.status === 'CREATED')
  if (!pending) {
    uni.showToast({ title: '暂无可领取的任务', icon: 'none' })
    return
  }
  try {
    await request.post(`/tasks/${pending.id}/claim`)
    uni.showToast({ title: '任务已领取', icon: 'success' })
    await loadTasks()
  } catch { /* handled */ }
}

// ── 选择任务 ──
async function selectTask(task: any) {
  currentTask.value = task
  // 加载拣货行（按库位路径排序）
  try {
    const res = await request.get(`/outbound/picks/${task.id}/lines`)
    const lines = (res.data?.records || res.data || []).map((l: any) => ({
      ...l,
      done: l.status === 'PICKED' || l.status === 'DONE' || l.pickedQty >= l.pickQty,
      skip: false
    }))
    // 按库位排序（优化路径）
    lines.sort((a: any, b: any) => (a.locationCode || '').localeCompare(b.locationCode || ''))
    putawayLines.value = lines

    // 跳到第一个未完成行
    const firstPending = lines.findIndex((l: any) => !l.done)
    lineIndex.value = firstPending >= 0 ? firstPending : 0
    resetLineInputs()
  } catch {
    // fallback
    putawayLines.value = [{
      id: task.id + '_1', lineNo: 1,
      skuCode: '-', skuName: '-', pickQty: 0,
      locationCode: '-', locationId: 0, done: false
    }]
  }
}

// ── 行导航 ──
function selectLine(idx: number) {
  lineIndex.value = idx
  resetLineInputs()
}

function resetLineInputs() {
  scannedLocation.value = ''
  scannedSku.value = ''
  pickedQty.value = 0
  toContainer.value = ''
  locationVerified.value = false
  skuVerified.value = false
  containerVerified.value = false
}

// ── 验证扫描 ──
function verifyLocation() {
  if (!currentLine.value) return
  const expected = String(currentLine.value.locationCode || currentLine.value.locationId || '')
  const actual = scannedLocation.value.trim()
  if (!actual) return

  if (expected && actual !== expected) {
    uni.showToast({ title: '库位不匹配! 预期: ' + expected, icon: 'error', duration: 3000 })
    uni.vibrateLong()
    return
  }
  locationVerified.value = true
  uni.vibrateShort({ type: 'light' })
  uni.showToast({ title: '库位验证通过', icon: 'success', duration: 1000 })
}

function verifySku() {
  if (!currentLine.value) return
  const expected = String(currentLine.value.skuCode || '')
  const actual = scannedSku.value.trim()
  if (!actual) return

  if (expected && actual !== expected) {
    uni.showToast({ title: 'SKU不匹配! 预期: ' + expected, icon: 'error', duration: 3000 })
    uni.vibrateLong()
    return
  }
  skuVerified.value = true
  uni.vibrateShort({ type: 'light' })
  uni.showToast({ title: 'SKU验证通过', icon: 'success', duration: 1000 })
}

function verifyContainer() {
  if (toContainer.value.trim()) {
    containerVerified.value = true
    uni.showToast({ title: '容器: ' + toContainer.value, icon: 'success', duration: 1000 })
  }
}

async function onScanClick(target: string) {
  const result = await scan()
  if (!result) return

  switch (target) {
    case 'location':
      scannedLocation.value = result.barcodeValue
      verifyLocation()
      break
    case 'sku':
      scannedSku.value = result.barcodeValue
      verifySku()
      break
    case 'container':
      toContainer.value = result.barcodeValue
      verifyContainer()
      break
  }
}

// ── 确认拣货 ──
async function confirmPick() {
  const line = currentLine.value
  if (!line) return

  // 如果没验证库位但已输入，尝试验证
  if (!locationVerified.value && scannedLocation.value.trim()) {
    verifyLocation()
    if (!locationVerified.value) return
  }
  if (!skuVerified.value && scannedSku.value.trim()) {
    verifySku()
    if (!skuVerified.value) return
  }

  confirming.value = true
  try {
    await request.post('/outbound/picks/submit', {
      pickHeaderId: currentTask.value.id,
      pickLineId: line.id,
      pickedQty: pickedQty.value || line.pickQty,
      toContainer: toContainer.value.trim()
    })

    line.done = true
    uni.vibrateShort({ type: 'heavy' })
    uni.showToast({ title: '拣货成功', icon: 'success' })

    // 检查是否全部完成
    if (completedLines.value >= totalLines.value) {
      showComplete.value = true
      return
    }

    // 自动跳到下一个未完成行
    const nextPending = putawayLines.value.findIndex(l => !l.done)
    if (nextPending >= 0) {
      lineIndex.value = nextPending
      resetLineInputs()
    }
  } catch { /* handled */ } finally {
    confirming.value = false
  }
}

// ── 异常标记 ──
function markException() {
  uni.showActionSheet({
    itemList: ['SKU不匹配', '数量不足', '库位为空', '货损/破损', '其他异常'],
    success: (res) => {
      const reasons = ['SKU不匹配', '数量不足', '库位为空', '货损/破损', '其他异常']
      uni.showToast({ title: '已标记: ' + reasons[res.tapIndex], icon: 'none' })
      if (currentLine.value) {
        currentLine.value.skip = true
      }
      // 跳到下一行
      const nextPending = putawayLines.value.findIndex(l => !l.done && !l.skip)
      if (nextPending >= 0) {
        lineIndex.value = nextPending
        resetLineInputs()
      }
    }
  })
}

onMounted(() => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
// ── 公用 ──
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

// ── 顶部条 ──
.top-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: #1677ff;
  margin: -24rpx -24rpx 24rpx;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.top-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.top-progress {
  font-size: 28rpx;
  color: rgba(255,255,255,0.85);
}

// ── Tab ──
.status-tabs {
  display: flex;
  gap: 8rpx;
  margin-bottom: 24rpx;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 12rpx 24rpx;
  background: #f0f0f0;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #606266;
}

.tab-item.active {
  background: #1677ff;
  color: #fff;
}

.tab-count {
  background: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  border-radius: 16rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

// ── 任务项 ──
.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #fafafa;
  border-radius: 12rpx;
  margin-bottom: 12rpx;
}

.task-item:active { background: #f0f0f0; }

.task-left {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.task-no {
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
}

.task-type {
  font-size: 24rpx;
  color: #909399;
}

.task-info {
  font-size: 22rpx;
  color: #c0c4cc;
}

.task-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.task-lines {
  font-size: 24rpx;
  color: #909399;
}

// ── 找货区 ──
.pick-card {
  padding: 0;
  overflow: hidden;
}

.find-section {
  padding: 24rpx;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
}

.find-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.find-badge {
  background: #1677ff;
  color: #fff;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.find-location {
  font-size: 36rpx;
  font-weight: 700;
  color: #1677ff;
}

.find-sku {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.sku-label {
  font-size: 22rpx;
  color: #999;
}

.sku-value {
  font-size: 30rpx;
  font-weight: 700;
  color: #303133;
}

.find-name {
  font-size: 24rpx;
  color: #909399;
  margin-bottom: 16rpx;
}

.find-details {
  display: flex;
  gap: 24rpx;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-size: 22rpx;
  color: #999;
}

.detail-value {
  font-size: 28rpx;
  color: #303133;
  font-weight: 500;
}

.detail-value.big {
  font-size: 34rpx;
  font-weight: 700;
  color: #1677ff;
}

// ── 确认区 ──
.confirm-section {
  padding: 24rpx;
}

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

.scan-input.verified {
  border-color: #52c41a;
  background: #f6ffed;
  color: #52c41a;
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

.verified-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

// ── 数量输入 ──
.picked-qty-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.qty-input-lg {
  width: 160rpx;
  height: 80rpx;
  background: #fafafa;
  border: 2rpx solid #1677ff;
  border-radius: 12rpx;
  text-align: center;
  font-size: 40rpx;
  font-weight: 700;
  color: #1677ff;
}

.qty-unit {
  font-size: 28rpx;
  color: #606266;
}

.quick-qty-btns {
  display: flex;
  gap: 8rpx;
  margin-left: auto;
}

.quick-btn {
  padding: 10rpx 20rpx;
  background: #f0f5ff;
  color: #1677ff;
  border-radius: 8rpx;
  font-size: 24rpx;
}

// ── 确认按钮 ──
.pick-confirm-wrap {
  padding: 0 24rpx 24rpx;
}

.exception-wrap {
  padding: 0 24rpx 24rpx;
  display: flex;
  justify-content: center;
}

// ── 底部行导航 ──
.line-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 12rpx;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06);
  overflow-x: auto;
}

.nav-dot {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #606266;
  flex-shrink: 0;
}

.nav-dot.current {
  background: #1677ff;
  color: #fff;
  transform: scale(1.2);
}

.nav-dot.done {
  background: #52c41a;
  color: #fff;
}

.nav-dot.skip {
  background: #faad14;
  color: #fff;
}

// ── 完成页 ──
.complete-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 32rpx;
}

.complete-icon { margin-bottom: 24rpx; }

.complete-title {
  font-size: 38rpx;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8rpx;
}

.complete-detail {
  font-size: 26rpx;
  color: #909399;
  margin-bottom: 40rpx;
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
