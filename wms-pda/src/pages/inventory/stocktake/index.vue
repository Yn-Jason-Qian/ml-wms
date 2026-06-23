<template>
  <view class="page-container">
    <!-- 任务选择 -->
    <view class="card" v-if="!currentTask">
      <text class="section-title">盘点任务</text>
      <view class="tab-row">
        <view class="tab-chip" :class="{ active: tab === 'all' }" @click="tab = 'all'">全部</view>
        <view class="tab-chip" :class="{ active: tab === 'pending' }" @click="tab = 'pending'">待盘点</view>
        <view class="tab-chip" :class="{ active: tab === 'progress' }" @click="tab = 'progress'">进行中</view>
      </view>
      <view class="task-list">
        <view class="task-item" v-for="t in filteredTasks" :key="t.id" @click="selectTask(t)">
          <view class="task-left">
            <text class="task-no">{{ t.stocktakeNo }}</text>
            <text class="task-detail">{{ t.stocktakeType || '全盘' }} · {{ t.stocktakeMode || '明盘' }}</text>
          </view>
          <view class="task-right">
            <up-tag :text="t.statusText" :type="t.statusType" size="small" />
            <text class="task-lines">{{ t.totalLines || 0 }} 行</text>
          </view>
        </view>
      </view>
      <view class="empty" v-if="filteredTasks.length === 0"><text class="empty-text">暂无盘点任务</text></view>
    </view>

    <!-- 盘点操作 -->
    <view v-if="currentTask" class="stocktake-workspace">
      <view class="top-bar">
        <view class="top-left" @click="backToList">
          <up-icon name="arrow-left" size="22" color="#fff" />
        </view>
        <view class="top-center">
          <text class="top-title">{{ currentTask.stocktakeNo }}</text>
          <text class="top-sub">{{ modeText }} · {{ currentTask.locationRange || '全部库位' }}</text>
        </view>
        <text class="top-progress">{{ doneCount }}/{{ totalCount }}</text>
      </view>

      <!-- 当前行 -->
      <view class="card stock-card" v-if="currentLine">
        <!-- 库位信息 -->
        <view class="location-bar">
          <view class="loc-badge">库位</view>
          <text class="loc-code">{{ currentLine.locationCode }}</text>
          <up-tag v-if="currentLine.status === 'COUNTED'" text="已盘" type="success" size="small" />
          <up-tag v-else-if="currentLine.status === 'DIFF'" text="差异" type="error" size="small" />
          <up-tag v-else text="待盘" type="warning" size="small" />
        </view>

        <!-- SKU信息 -->
        <view class="sku-row" v-if="currentLine.skuCode">
          <text class="sku-code">{{ currentLine.skuCode }}</text>
          <text class="sku-name">{{ currentLine.skuName }}</text>
        </view>

        <!-- 明盘 / 盲盘 -->
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">账存数量</text>
            <text class="info-value big">{{ isBlind ? '***' : (currentLine.bookQty ?? '-') }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">实盘数量</text>
            <text class="info-value big highlight">{{ currentLine.countQty != null ? currentLine.countQty : '-' }}</text>
          </view>
          <view class="info-item" v-if="currentLine.batchNo">
            <text class="info-label">批次</text>
            <text class="info-value">{{ currentLine.batchNo }}</text>
          </view>
        </view>

        <!-- 差异显示 -->
        <view class="diff-alert" v-if="currentLine.diffQty != null && currentLine.diffQty !== 0">
          <up-icon name="error-fill" size="20" color="#ff4d4f" />
          <text class="diff-text">差异: {{ currentLine.diffQty > 0 ? '+' : '' }}{{ currentLine.diffQty }}</text>
        </view>

        <!-- 输入区 -->
        <view class="input-section" v-if="!currentLine.status || currentLine.status === 'DIFF'">
          <text class="field-label">实盘数量</text>
          <view class="qty-row">
            <view class="qty-btn" @click="countQty = Math.max(0, (countQty || 0) - 1)"><text>-</text></view>
            <input
              class="qty-input-big"
              v-model.number="countQty"
              type="digit"
              placeholder="输入数量"
              ref="qtyInputRef"
            />
            <view class="qty-btn" @click="countQty = (countQty || 0) + 1"><text>+</text></view>
          </view>

          <!-- 快速操作 -->
          <view class="quick-row">
            <view class="quick-chip" @click="countQty = currentLine.bookQty || 0">账存</view>
            <view class="quick-chip" @click="countQty = 0">零库存</view>
            <view class="quick-chip warn" @click="markDiff">标记差异</view>
          </view>

          <view class="confirm-btn">
            <up-button type="primary" text="确认盘点" shape="round" :loading="confirming" @click="confirmStocktake" />
          </view>
        </view>

        <!-- 备注 -->
        <view class="remark-section" v-if="currentLine.status === 'COUNTED' || currentLine.status === 'DIFF'">
          <text class="field-label">盘点备注</text>
          <input class="text-input" v-model="remark" placeholder="差异原因等" />
          <up-button text="重新盘点" size="small" plain @click="currentLine.status = null; currentLine.countQty = null; countQty = 0" />
        </view>
      </view>

      <!-- 底部导航 -->
      <view class="line-nav" v-if="lines.length > 1">
        <view
          class="nav-dot"
          v-for="(l, i) in lines"
          :key="i"
          :class="{ current: i === lineIdx, counted: l.status === 'COUNTED', diff: l.status === 'DIFF' }"
          @click="selectLine(i)"
        >{{ i + 1 }}</view>
      </view>
    </view>

    <!-- 完成 -->
    <view class="card complete-card" v-if="showComplete">
      <view class="complete-icon"><up-icon name="checkmark-circle-fill" size="96" color="#52c41a" /></view>
      <text class="complete-title">盘点完成</text>
      <view class="complete-stats">
        <text>已盘 {{ doneCount }} 行</text>
        <text v-if="diffLines > 0" class="diff-text">差异 {{ diffLines }} 行</text>
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

const tab = ref('all')
const taskList = ref<any[]>([])
const currentTask = ref<any>(null)
const lines = ref<any[]>([])
const lineIdx = ref(0)
const countQty = ref(0)
const remark = ref('')
const confirming = ref(false)
const showComplete = ref(false)

const currentLine = computed(() => lines.value[lineIdx.value] || null)
const totalCount = computed(() => lines.value.length)
const doneCount = computed(() => lines.value.filter(l => l.status === 'COUNTED' || l.status === 'DIFF').length)
const diffLines = computed(() => lines.value.filter(l => l.status === 'DIFF').length)
const isBlind = computed(() => currentTask.value?.stocktakeMode === 'BLIND')
const modeText = computed(() => isBlind.value ? '盲盘' : '明盘')

const filteredTasks = computed(() => {
  if (tab.value === 'pending') return taskList.value.filter(t => t.status === 'CREATED')
  if (tab.value === 'progress') return taskList.value.filter(t => t.status === 'COUNTING')
  return taskList.value
})

async function loadTasks() {
  try {
    const res = await request.get('/inventory/stocktakes/page', { pageNum: 1, pageSize: 50, warehouseId: authStore.warehouseId })
    taskList.value = (res.data?.records || []).map((t: any) => ({
      ...t,
      statusText: sMap(t.status), statusType: tMap(t.status)
    }))
  } catch { /* handled */ }
}
function sMap(s: string) {
  const m: Record<string, string> = { CREATED: '待盘点', COUNTING: '盘点中', COUNTED: '已盘点', DIFF_REVIEW: '差异复核', DONE: '完成' }
  return m[s] || s
}
function tMap(s: string): 'warning' | 'primary' | 'success' {
  if (s === 'DONE' || s === 'COUNTED') return 'success'
  if (s === 'COUNTING') return 'primary'
  return 'warning'
}

async function selectTask(task: any) {
  currentTask.value = task
  try {
    const res = await request.get(`/inventory/stocktakes/${task.id}/lines`)
    lines.value = (res.data?.records || res.data || []).map((l: any) => ({
      ...l, status: l.status || null, countQty: l.firstCountQty || null, diffQty: l.diffQty || null
    }))
  } catch {
    lines.value = [{ locationCode: '-', skuCode: '-', skuName: '', bookQty: 0, batchNo: '', status: null }]
  }
  lineIdx.value = lines.value.findIndex(l => !l.status || l.status === 'DIFF')
  if (lineIdx.value < 0) lineIdx.value = 0
  countQty.value = 0
}

function backToList() { currentTask.value = null; lines.value = []; loadTasks() }

async function confirmStocktake() {
  // 守卫: null/undefined 以及 NaN (v-model.number 清空后产生)
  if (countQty.value == null || Number.isNaN(countQty.value)) {
    uni.showToast({ title: '请输入有效数量', icon: 'none' })
    return
  }
  confirming.value = true
  try {
    const line = currentLine.value
    if (!line) return
    // 调用盘点提交API
    await request.post(`/inventory/stocktakes/submit`, {
      stocktakeHeaderId: currentTask.value.id,
      stocktakeLineId: line.id,
      countQty: countQty.value
    })

    line.status = countQty.value === (line.bookQty || 0) ? 'COUNTED' : 'DIFF'
    line.diffQty = countQty.value - (line.bookQty || 0)
    line.countQty = countQty.value

    uni.vibrateShort({ type: 'heavy' })
    uni.showToast({ title: line.status === 'DIFF' ? `差异: ${line.diffQty}` : '盘点OK', icon: line.status === 'DIFF' ? 'none' : 'success' })

    // 自动推进
    const next = lines.value.findIndex((l, i) => i > lineIdx.value && (!l.status || l.status === 'DIFF'))
    if (next >= 0) lineIdx.value = next; countQty.value = 0
    if (doneCount.value >= totalCount.value) showComplete.value = true
  } catch { /* handled */ } finally { confirming.value = false }
}

function markDiff() {
  if (!currentLine.value) return
  const safeQty = (countQty.value != null && !Number.isNaN(countQty.value)) ? countQty.value : 0
  currentLine.value.status = 'DIFF'
  currentLine.value.diffQty = safeQty - (currentLine.value.bookQty || 0)
  const next = lines.value.findIndex((l, i) => i > lineIdx.value && (!l.status || l.status === 'DIFF'))
  if (next >= 0) { lineIdx.value = next; countQty.value = 0 }
}

function selectLine(idx: number) { lineIdx.value = idx; countQty.value = 0 }

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
.task-lines { font-size: 24rpx; color: #909399; }

.empty { padding: 80rpx 0; text-align: center; }
.empty-text { font-size: 28rpx; color: #c0c4cc; }

.top-bar { display: flex; align-items: center; padding: 20rpx 24rpx; background: #1677ff; margin: -24rpx -24rpx 24rpx; }
.top-left { margin-right: 16rpx; }
.top-center { flex: 1; display: flex; flex-direction: column; }
.top-title { font-size: 30rpx; font-weight: 600; color: #fff; }
.top-sub { font-size: 22rpx; color: rgba(255,255,255,0.7); margin-top: 2rpx; }
.top-progress { font-size: 26rpx; color: rgba(255,255,255,0.85); }

.stock-card { padding: 0; overflow: hidden; }
.location-bar { display: flex; align-items: center; gap: 12rpx; padding: 20rpx 24rpx; background: #f0f5ff; }
.loc-badge { background: #1677ff; color: #fff; padding: 4rpx 16rpx; border-radius: 6rpx; font-size: 24rpx; font-weight: 600; }
.loc-code { font-size: 34rpx; font-weight: 700; color: #1677ff; flex: 1; }

.sku-row { display: flex; align-items: baseline; gap: 12rpx; padding: 20rpx 24rpx 0; }
.sku-code { font-size: 30rpx; font-weight: 700; color: #303133; }
.sku-name { font-size: 26rpx; color: #909399; }

.info-grid { display: flex; gap: 24rpx; padding: 16rpx 24rpx; }
.info-item { flex: 1; display: flex; flex-direction: column; }
.info-label { font-size: 22rpx; color: #999; }
.info-value { font-size: 28rpx; color: #303133; font-weight: 500; }
.info-value.big { font-size: 34rpx; font-weight: 700; }
.info-value.highlight { color: #1677ff; }

.diff-alert { display: flex; align-items: center; gap: 8rpx; margin: 0 24rpx 12rpx; padding: 12rpx 16rpx; background: #fff2f0; border-radius: 8rpx; }
.diff-text { font-size: 26rpx; color: #ff4d4f; font-weight: 600; }

.input-section { padding: 16rpx 24rpx 24rpx; }
.qty-row { display: flex; align-items: center; gap: 20rpx; margin-bottom: 16rpx; }
.qty-btn { width: 72rpx; height: 72rpx; background: #1677ff; color: #fff; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; font-size: 36rpx; font-weight: 600; }
.qty-input-big { flex: 1; height: 88rpx; background: #fafafa; border: 2rpx solid #1677ff; border-radius: 12rpx; text-align: center; font-size: 44rpx; font-weight: 700; color: #1677ff; }

.quick-row { display: flex; gap: 12rpx; margin-bottom: 20rpx; }
.quick-chip { padding: 8rpx 20rpx; background: #f0f5ff; color: #1677ff; border-radius: 8rpx; font-size: 24rpx; }
.quick-chip.warn { background: #fff7e6; color: #faad14; }

.confirm-btn { margin-top: 8rpx; }
.remark-section { padding: 0 24rpx 24rpx; }
.text-input { width: 100%; height: 64rpx; background: #fafafa; border: 1rpx solid #d9d9d9; border-radius: 8rpx; padding: 0 16rpx; font-size: 26rpx; margin-bottom: 12rpx; box-sizing: border-box; }

.line-nav { position: fixed; bottom: 0; left: 0; right: 0; display: flex; justify-content: center; gap: 12rpx; padding: 20rpx 24rpx; padding-bottom: calc(20rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06); overflow-x: auto; }
.nav-dot { width: 56rpx; height: 56rpx; border-radius: 50%; background: #f0f0f0; display: flex; align-items: center; justify-content: center; font-size: 24rpx; color: #606266; flex-shrink: 0; }
.nav-dot.current { background: #1677ff; color: #fff; transform: scale(1.2); }
.nav-dot.counted { background: #52c41a; color: #fff; }
.nav-dot.diff { background: #ff4d4f; color: #fff; }

.complete-card { display: flex; flex-direction: column; align-items: center; padding: 80rpx 32rpx; }
.complete-icon { margin-bottom: 24rpx; }
.complete-title { font-size: 38rpx; font-weight: 700; color: #303133; margin-bottom: 8rpx; }
.complete-stats { display: flex; gap: 32rpx; margin-bottom: 40rpx; font-size: 28rpx; color: #606266; }
.diff-text { color: #ff4d4f; }
</style>
