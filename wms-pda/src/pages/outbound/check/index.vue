<template>
  <view class="page-container">
    <!-- 任务选择 -->
    <view class="card" v-if="!currentTask">
      <text class="section-title">复核任务</text>
      <view class="tab-row">
        <view class="tab-chip" :class="{ active: filterTab === 'all' }" @click="filterTab = 'all'">全部</view>
        <view class="tab-chip" :class="{ active: filterTab === 'pending' }" @click="filterTab = 'pending'">待复核</view>
      </view>

      <view class="task-list">
        <view class="task-item" v-for="t in filteredTasks" :key="t.id" @click="selectTask(t)">
          <view class="task-left">
            <text class="task-no">{{ t.waveNo || t.checkNo }}</text>
            <text class="task-detail">订单数 {{ t.orderCount || 0 }}</text>
          </view>
          <view class="task-right">
            <up-tag :text="t.statusText" :type="t.statusType" size="small" />
            <up-icon name="arrow-right" size="16" color="#c0c4cc" />
          </view>
        </view>
      </view>
      <view class="empty" v-if="filteredTasks.length === 0">
        <text class="empty-text">暂无复核任务</text>
      </view>
    </view>

    <!-- 复核操作 -->
    <view v-if="currentTask" class="check-workspace">
      <view class="top-bar">
        <view class="top-left" @click="backToList">
          <up-icon name="arrow-left" size="22" color="#fff" />
        </view>
        <text class="top-title">{{ currentTask.waveNo || '复核' }}</text>
        <text class="top-progress">{{ checkedCount }}/{{ totalCount }}</text>
      </view>

      <!-- 扫描容器 -->
      <view class="card" v-if="!currentLine">
        <text class="guide-title">开始复核</text>
        <text class="guide-desc">扫描容器/周转箱条码，系统将显示该容器内所有待复核商品</text>
        <view class="scan-row">
          <input
            class="scan-input lg"
            v-model="containerCode"
            placeholder="扫描容器条码"
            @confirm="onContainerScanned"
          />
          <view class="scan-btn" @click="onScanClick('container')">
            <up-icon name="scan" size="28" color="#fff" />
          </view>
        </view>
        <view class="recent-containers" v-if="recentContainers.length > 0">
          <text class="helper-text">最近容器</text>
          <view class="chip-row">
            <view class="container-chip" v-for="c in recentContainers" :key="c" @click="containerCode = c; onContainerScanned()">
              {{ c }}
            </view>
          </view>
        </view>
      </view>

      <!-- 逐件复核 -->
      <view class="card check-card" v-if="currentLine">
        <view class="check-header">
          <view class="header-left">
            <text class="container-tag">容器: {{ containerCode }}</text>
            <text class="line-progress">{{ lineIdx + 1 }} / {{ checkLines.length }}</text>
          </view>
          <up-tag v-if="currentLine.isMatch === 1" text="已匹配" type="success" size="small" />
          <up-tag v-else-if="currentLine.isMatch === 0" text="差异" type="error" size="small" />
          <up-tag v-else text="待复核" type="warning" size="small" />
        </view>

        <!-- 预期信息 -->
        <view class="expect-section">
          <text class="expect-label">预期商品</text>
          <view class="expect-row">
            <text class="expect-sku">{{ currentLine.skuCode }}</text>
            <text class="expect-name">{{ currentLine.skuName }}</text>
          </view>
          <view class="expect-meta">
            <text>订单数量: {{ currentLine.orderQty || 0 }} 件</text>
            <text v-if="currentLine.batchNo">批次: {{ currentLine.batchNo }}</text>
          </view>
        </view>

        <!-- 实际扫描 -->
        <view class="scan-section">
          <text class="field-label">扫描商品条码</text>
          <view class="scan-row">
            <input
              class="scan-input lg highlight"
              v-model="scannedSku"
              placeholder="逐件扫描SKU"
              @confirm="onSkuScanned"
              ref="skuInputRef"
            />
            <view class="scan-btn lg" @click="onScanClick('sku')">
              <up-icon name="scan" size="28" color="#fff" />
            </view>
          </view>
        </view>

        <!-- 比对结果 -->
        <view class="match-result" v-if="matchResult !== null">
          <view class="match-row" v-if="matchResult">
            <up-icon name="checkmark-circle-fill" size="40" color="#52c41a" />
            <text class="match-text success">SKU 一致</text>
          </view>
          <view class="match-row" v-else>
            <up-icon name="close-circle-fill" size="40" color="#ff4d4f" />
            <text class="match-text error">SKU 不匹配！预期: {{ currentLine.skuCode }}</text>
          </view>
          <view class="match-actions" v-if="!matchResult">
            <up-button text="标记差异" type="error" size="small" @click="markDiff" />
            <up-button text="覆盖确认" type="warning" size="small" plain @click="forceMatch" />
          </view>
        </view>

        <!-- 数量确认 -->
        <view class="qty-section" v-if="matchResult">
          <text class="field-label">复核数量</text>
          <view class="picked-qty-row">
            <input class="qty-input-lg" v-model.number="checkQty" type="digit" :placeholder="String(currentLine.orderQty || 0)" />
            <text class="qty-unit">件</text>
            <view class="quick-btn" @click="checkQty = currentLine.orderQty">快捷</view>
          </view>
          <view class="confirm-btn">
            <up-button type="primary" text="确认复核" shape="round" :loading="confirming" @click="confirmCheck" />
          </view>
        </view>
      </view>

      <!-- 底部行导航 -->
      <view class="line-nav" v-if="checkLines.length > 1">
        <view
          class="nav-dot"
          v-for="(l, i) in checkLines"
          :key="i"
          :class="{ current: i === lineIdx, match: l.isMatch === 1, diff: l.isMatch === 0 }"
          @click="selectLine(i)"
        >{{ i + 1 }}</view>
      </view>
    </view>

    <!-- 完成 -->
    <view class="card complete-card" v-if="showComplete">
      <view class="complete-icon">
        <up-icon name="checkmark-circle-fill" size="96" color="#52c41a" />
      </view>
      <text class="complete-title">复核完成</text>
      <view class="complete-stats">
        <text>通过 {{ passCount }} 件</text>
        <text v-if="diffCount > 0" class="diff-text">差异 {{ diffCount }} 件</text>
      </view>
      <up-button type="primary" text="返回列表" shape="round" @click="backToList(); showComplete = false" />
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

const filterTab = ref('all')
const taskList = ref<any[]>([])
const currentTask = ref<any>(null)
const containerCode = ref('')
const recentContainers = ref<string[]>([])

const checkLines = ref<any[]>([])
const lineIdx = ref(0)
const scannedSku = ref('')
const checkQty = ref(0)
const matchResult = ref<boolean | null>(null)
const confirming = ref(false)
const showComplete = ref(false)

const currentLine = computed(() => checkLines.value[lineIdx.value] || null)
const totalCount = computed(() => checkLines.value.length)
const checkedCount = computed(() => checkLines.value.filter(l => l.isMatch != null).length)
const passCount = computed(() => checkLines.value.filter(l => l.isMatch === 1).length)
const diffCount = computed(() => checkLines.value.filter(l => l.isMatch === 0).length)

const filteredTasks = computed(() => {
  if (filterTab.value === 'pending') return taskList.value.filter(t => t.status === 'CREATED' || t.status === 'CHECKING')
  return taskList.value
})

async function loadTasks() {
  try {
    // 复核关联波次 — 加载可复核的波次列表
    const res = await request.get('/outbound/waves/page', { pageNum: 1, pageSize: 50, warehouseId: authStore.warehouseId })
    taskList.value = (res.data?.records || []).map((t: any) => ({
      ...t,
      statusText: statusMap(t.waveStatus || t.status),
      statusType: typeMap(t.waveStatus || t.status)
    }))
  } catch { /* handled */ }
}

function statusMap(s: string) {
  const m: Record<string, string> = { CREATED: '待复核', WAVED: '可复核', CHECKING: '复核中', PASS: '通过', DONE: '完成' }
  return m[s] || s
}
function typeMap(s: string): 'warning' | 'primary' | 'success' {
  if (s === 'DONE' || s === 'PASS') return 'success'
  if (s === 'CHECKING') return 'primary'
  return 'warning'
}

async function selectTask(task: any) {
  currentTask.value = task
  // 加载波次下的订单行作为复核明细
  try {
    const res = await request.get(`/outbound/waves/${task.id}/lines`)
    checkLines.value = (res.data?.records || res.data || []).map((l: any) => ({
      ...l, isMatch: null, actualSku: ''
    }))
  } catch {
    // fallback: 使用模拟数据
    checkLines.value = [{ skuCode: '-', skuName: '-', orderQty: 0, isMatch: null }]
  }
  lineIdx.value = 0
  resetLineState()
}

function backToList() {
  currentTask.value = null
  containerCode.value = ''
  checkLines.value = []
  loadTasks()
}

async function onContainerScanned() {
  if (!containerCode.value.trim()) return
  if (!recentContainers.value.includes(containerCode.value)) {
    recentContainers.value.unshift(containerCode.value)
    if (recentContainers.value.length > 5) recentContainers.value.pop()
  }
  // 容器已绑定，跳到第一行
  if (checkLines.value.length > 0) {
    lineIdx.value = 0
    resetLineState()
  }
}

async function onSkuScanned() {
  const actual = scannedSku.value.trim()
  if (!actual || !currentLine.value) return

  const expected = currentLine.value.skuCode || ''
  if (expected && actual === expected) {
    matchResult.value = true
    currentLine.value.isMatch = 1
    checkQty.value = 1
  } else {
    matchResult.value = false
    currentLine.value.isMatch = 0
    currentLine.value.actualSku = actual
  }
}

function forceMatch() {
  if (!currentLine.value) return
  currentLine.value.isMatch = 1
  matchResult.value = true
  checkQty.value = 1
}

function markDiff() {
  if (!currentLine.value) return
  currentLine.value.isMatch = 0
  advanceLine()
}

async function confirmCheck() {
  const line = currentLine.value
  if (!line) return

  // 数量: 显式检查 NaN + null，不依赖 falsy 陷阱
  const safeQty = (checkQty.value != null && !Number.isNaN(checkQty.value))
    ? checkQty.value
    : line.orderQty

  confirming.value = true
  try {
    // 提交复核结果到后端
    await request.post('/outbound/checks/submit', {
      checkHeaderId: currentTask.value?.id,
      checkLineId: line.id,
      checkQty: safeQty,
      isMatch: matchResult.value ? 1 : 0,
      diffReason: matchResult.value ? null : 'SKU不匹配',
      fromContainer: containerCode.value
    })

    line.checkQty = safeQty
    line.isMatch = matchResult.value ? 1 : 0
    advanceLine()
    // 检查是否全部完成
    if (checkedCount.value >= totalCount.value) {
      showComplete.value = true
    }
  } catch { /* handled */ } finally { confirming.value = false }
}

function advanceLine() {
  resetLineState()
  const next = checkLines.value.findIndex((l, i) => i > lineIdx.value && l.isMatch == null)
  if (next >= 0) lineIdx.value = next
}

function selectLine(idx: number) {
  lineIdx.value = idx
  resetLineState()
}

function resetLineState() {
  scannedSku.value = ''
  checkQty.value = 0
  matchResult.value = null
}

async function onScanClick(target: string) {
  const r = await scan()
  if (!r) return
  if (target === 'container') { containerCode.value = r.barcodeValue; onContainerScanned() }
  if (target === 'sku') { scannedSku.value = r.barcodeValue; onSkuScanned() }
}

onMounted(loadTasks)
</script>

<style lang="scss" scoped>
.section-title { font-size: 30rpx; font-weight: 600; color: #303133; margin-bottom: 20rpx; display: block; }
.field-label { font-size: 26rpx; color: #606266; margin-bottom: 8rpx; display: block; margin-top: 16rpx; }

.tab-row { display: flex; gap: 12rpx; margin-bottom: 20rpx; }
.tab-chip { padding: 12rpx 24rpx; background: #f0f0f0; border-radius: 32rpx; font-size: 26rpx; color: #606266; }
.tab-chip.active { background: #1677ff; color: #fff; }

.task-item { display: flex; justify-content: space-between; align-items: center; padding: 20rpx; background: #fafafa; border-radius: 12rpx; margin-bottom: 12rpx; }
.task-item:active { background: #f0f0f0; }
.task-left { display: flex; flex-direction: column; gap: 4rpx; }
.task-no { font-size: 28rpx; font-weight: 600; color: #303133; }
.task-detail { font-size: 24rpx; color: #909399; }
.task-right { display: flex; align-items: center; gap: 12rpx; }

.empty { padding: 80rpx 0; text-align: center; }
.empty-text { font-size: 28rpx; color: #c0c4cc; }

// 顶部条
.top-bar { display: flex; align-items: center; padding: 20rpx 24rpx; background: #1677ff; margin: -24rpx -24rpx 24rpx; }
.top-left { margin-right: 16rpx; }
.top-title { flex: 1; font-size: 32rpx; font-weight: 600; color: #fff; }
.top-progress { font-size: 26rpx; color: rgba(255,255,255,0.85); }

// 容器扫描
.guide-title { font-size: 32rpx; font-weight: 600; color: #303133; margin-bottom: 8rpx; display: block; }
.guide-desc { font-size: 26rpx; color: #909399; margin-bottom: 24rpx; display: block; }
.scan-row { display: flex; align-items: center; gap: 16rpx; }
.scan-input { flex: 1; height: 80rpx; background: #fafafa; border: 2rpx dashed #d9d9d9; border-radius: 12rpx; padding: 0 24rpx; font-size: 32rpx; text-align: center; }
.scan-input.lg { height: 96rpx; font-size: 36rpx; }
.scan-input.highlight { border-color: #1677ff; background: #f0f5ff; }
.scan-btn { width: 80rpx; height: 80rpx; background: #1677ff; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.scan-btn.lg { width: 96rpx; height: 96rpx; }

.recent-containers { margin-top: 20rpx; }
.helper-text { font-size: 24rpx; color: #909399; margin-bottom: 8rpx; display: block; }
.chip-row { display: flex; gap: 12rpx; flex-wrap: wrap; }
.container-chip { padding: 8rpx 20rpx; background: #f0f5ff; color: #1677ff; border-radius: 8rpx; font-size: 24rpx; }

// 行头
.check-card { padding: 0; overflow: hidden; }
.check-header { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 24rpx; background: #fafafa; }
.header-left { display: flex; flex-direction: column; gap: 4rpx; }
.container-tag { font-size: 26rpx; color: #1677ff; font-weight: 500; }
.line-progress { font-size: 24rpx; color: #909399; }

// 预期区
.expect-section { padding: 20rpx 24rpx; }
.expect-label { font-size: 22rpx; color: #999; margin-bottom: 4rpx; display: block; }
.expect-row { display: flex; align-items: baseline; gap: 12rpx; margin-bottom: 8rpx; }
.expect-sku { font-size: 34rpx; font-weight: 700; color: #303133; }
.expect-name { font-size: 26rpx; color: #909399; }
.expect-meta { display: flex; gap: 24rpx; font-size: 24rpx; color: #909399; }

// 扫描区
.scan-section { padding: 0 24rpx 20rpx; }

// 比对结果
.match-result { margin: 0 24rpx 16rpx; padding: 20rpx; background: #fafafa; border-radius: 12rpx; }
.match-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 12rpx; }
.match-text { font-size: 30rpx; font-weight: 600; }
.match-text.success { color: #52c41a; }
.match-text.error { color: #ff4d4f; }
.match-actions { display: flex; gap: 12rpx; }

// 数量
.qty-section { padding: 0 24rpx 24rpx; }
.picked-qty-row { display: flex; align-items: center; gap: 12rpx; }
.qty-input-lg { width: 160rpx; height: 80rpx; background: #fafafa; border: 2rpx solid #1677ff; border-radius: 12rpx; text-align: center; font-size: 40rpx; font-weight: 700; color: #1677ff; }
.qty-unit { font-size: 28rpx; color: #606266; }
.quick-btn { padding: 10rpx 20rpx; background: #f0f5ff; color: #1677ff; border-radius: 8rpx; font-size: 24rpx; }
.confirm-btn { margin-top: 20rpx; }

// 底部导航
.line-nav { position: fixed; bottom: 0; left: 0; right: 0; display: flex; justify-content: center; gap: 12rpx; padding: 20rpx 24rpx; padding-bottom: calc(20rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06); overflow-x: auto; }
.nav-dot { width: 56rpx; height: 56rpx; border-radius: 50%; background: #f0f0f0; display: flex; align-items: center; justify-content: center; font-size: 24rpx; color: #606266; flex-shrink: 0; }
.nav-dot.current { background: #1677ff; color: #fff; transform: scale(1.2); }
.nav-dot.match { background: #52c41a; color: #fff; }
.nav-dot.diff { background: #ff4d4f; color: #fff; }

// 完成
.complete-card { display: flex; flex-direction: column; align-items: center; padding: 80rpx 32rpx; }
.complete-icon { margin-bottom: 24rpx; }
.complete-title { font-size: 38rpx; font-weight: 700; color: #303133; margin-bottom: 8rpx; }
.complete-stats { display: flex; gap: 32rpx; margin-bottom: 40rpx; font-size: 28rpx; color: #606266; }
.diff-text { color: #ff4d4f; }
</style>
