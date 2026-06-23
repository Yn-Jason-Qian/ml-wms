<template>
  <view class="page-container">
    <!-- 发货任务选择 -->
    <view class="card" v-if="!currentTask">
      <text class="section-title">发货任务</text>
      <view class="tab-row">
        <view class="tab-chip" :class="{ active: tab === 'all' }" @click="tab = 'all'">全部</view>
        <view class="tab-chip" :class="{ active: tab === 'pending' }" @click="tab = 'pending'">待发货</view>
      </view>

      <view class="task-list">
        <view class="task-item" v-for="t in filteredTasks" :key="t.id" @click="selectTask(t)">
          <view class="task-left">
            <text class="task-no">{{ t.shipNo || t.waveNo }}</text>
            <text class="task-detail">{{ t.carrierName || '未指定承运商' }}</text>
          </view>
          <view class="task-right">
            <up-tag :text="t.statusText" :type="t.statusType" size="small" />
            <up-icon name="arrow-right" size="16" color="#c0c4cc" />
          </view>
        </view>
      </view>
      <view class="empty" v-if="filteredTasks.length === 0"><text class="empty-text">暂无发货任务</text></view>
    </view>

    <!-- 发货操作 -->
    <view v-if="currentTask" class="ship-workspace">
      <view class="top-bar">
        <view class="top-left" @click="currentTask = null; loadTasks()">
          <up-icon name="arrow-left" size="22" color="#fff" />
        </view>
        <text class="top-title">发货确认</text>
      </view>

      <!-- 基本信息 -->
      <view class="card">
        <text class="section-title">单据信息</text>
        <view class="label-row"><text class="label">发货单号</text><text class="value">{{ currentTask.shipNo || currentTask.waveNo }}</text></view>
        <view class="label-row"><text class="label">承运商</text><text class="value">{{ form.carrierName || '-' }}</text></view>
        <view class="label-row"><text class="label">配送方式</text><text class="value">{{ form.deliveryMethod || '标准运输' }}</text></view>
        <view class="label-row"><text class="label">包裹数</text><text class="value highlight">{{ form.packageCount || 0 }} 件</text></view>
      </view>

      <!-- 发货信息填写 -->
      <view class="card">
        <text class="section-title">发货信息</text>

        <text class="field-label">承运商</text>
        <input class="text-input" v-model="form.carrierName" placeholder="输入承运商名称" />

        <text class="field-label">配送方式</text>
        <view class="chip-select">
          <view class="sel-chip" :class="{ active: form.deliveryMethod === 'EXPRESS' }" @click="form.deliveryMethod = 'EXPRESS'">快递</view>
          <view class="sel-chip" :class="{ active: form.deliveryMethod === 'LTL' }" @click="form.deliveryMethod = 'LTL'">零担</view>
          <view class="sel-chip" :class="{ active: form.deliveryMethod === 'FTL' }" @click="form.deliveryMethod = 'FTL'">整车</view>
          <view class="sel-chip" :class="{ active: form.deliveryMethod === 'SELF' }" @click="form.deliveryMethod = 'SELF'">自提</view>
        </view>

        <text class="field-label">运单号</text>
        <view class="scan-row">
          <input class="scan-input" v-model="form.trackingNo" placeholder="扫描运单条码" @confirm="onTrackingScanned" />
          <view class="scan-btn" @click="onScanClick('tracking')">
            <up-icon name="scan" size="24" color="#fff" />
          </view>
        </view>

        <text class="field-label">包裹数量</text>
        <view class="pkg-row">
          <view class="pkg-btn" @click="form.packageCount = Math.max(0, (form.packageCount || 0) - 1)"><text>-</text></view>
          <input class="pkg-input" v-model.number="form.packageCount" type="digit" placeholder="件数" />
          <view class="pkg-btn" @click="form.packageCount = (form.packageCount || 0) + 1"><text>+</text></view>
        </view>

        <text class="field-label">毛重 (kg)</text>
        <input class="text-input" v-model.number="form.grossWeight" type="digit" placeholder="可选" />

        <text class="field-label">体积 (m³)</text>
        <input class="text-input" v-model.number="form.volume" type="digit" placeholder="可选" />
      </view>

      <!-- 确认按钮 -->
      <view class="confirm-wrap">
        <up-button type="primary" text="确认发货" size="large" shape="round"
          :loading="confirming" @click="confirmShip" />
      </view>
    </view>

    <!-- 结果 -->
    <view class="card result-card" v-if="showResult">
      <view class="result-icon"><up-icon name="checkmark-circle-fill" size="96" color="#52c41a" /></view>
      <text class="result-title">发货完成</text>
      <view class="result-info">
        <text>运单: {{ form.trackingNo || '-' }}</text>
        <text>{{ form.packageCount || 0 }} 件 · {{ form.carrierName || '-' }}</text>
      </view>
      <up-button type="primary" text="继续发货" shape="round" @click="currentTask = null; showResult = false; loadTasks()" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useScanner } from '@/composables/useScanner'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const { scan } = useScanner()
const authStore = useAuthStore()

const tab = ref('all')
const taskList = ref<any[]>([])
const currentTask = ref<any>(null)
const confirming = ref(false)
const showResult = ref(false)

const form = reactive({
  carrierName: '',
  deliveryMethod: 'EXPRESS',
  trackingNo: '',
  packageCount: 0,
  grossWeight: null as number | null,
  volume: null as number | null
})

const filteredTasks = computed(() => {
  if (tab.value === 'pending') return taskList.value.filter(t => t.status === 'CREATED')
  return taskList.value
})

async function loadTasks() {
  // 并行加载 ships 和 waves，合并结果
  const [shipsRes, wavesRes] = await Promise.allSettled([
    request.get('/outbound/ships/page', { pageNum: 1, pageSize: 50, warehouseId: authStore.warehouseId }),
    request.get('/outbound/waves/page', { pageNum: 1, pageSize: 50, warehouseId: authStore.warehouseId })
  ])

  const ships = shipsRes.status === 'fulfilled'
    ? (shipsRes.value.data?.records || [])
    : []
  const waves = wavesRes.status === 'fulfilled'
    ? (wavesRes.value.data?.records || []).filter((t: any) => t.waveStatus !== 'DONE')
    : []

  const shipTasks = ships.map((t: any) => ({
    ...t, statusText: sMap(t.status), statusType: tMap(t.status)
  }))
  const waveTasks = waves.map((t: any) => ({
    ...t, id: t.id, shipNo: t.waveNo, status: t.waveStatus, statusText: '可发货', statusType: 'warning' as const
  }))

  // 合并去重（以 shipNo 为 key）
  const seen = new Set(shipTasks.map((t: any) => t.shipNo))
  taskList.value = [...shipTasks, ...waveTasks.filter((t: any) => !seen.has(t.shipNo))]
}

function sMap(s: string) {
  const m: Record<string, string> = { CREATED: '待发货', SHIPPING: '发货中', DONE: '已发货' }
  return m[s] || s
}
function tMap(s: string): 'warning' | 'primary' | 'success' {
  if (s === 'DONE') return 'success'
  return 'warning'
}

function selectTask(task: any) {
  currentTask.value = task
  form.carrierName = task.carrierName || ''
  form.deliveryMethod = task.deliveryMethod || 'EXPRESS'
  form.trackingNo = task.trackingNo || ''
  form.packageCount = task.packageCount || 0
}

function onTrackingScanned() { /* v-model handles it */ }

async function onScanClick(target: string) {
  const r = await scan()
  if (!r) return
  if (target === 'tracking') { form.trackingNo = r.barcodeValue }
}

async function confirmShip() {
  confirming.value = true
  try {
    await request.post('/outbound/ships', {
      warehouseId: authStore.warehouseId,
      ownerId: authStore.tenantId,
      waveHeaderId: currentTask.value?.id,
      deliveryMethod: form.deliveryMethod,
      carrierName: form.carrierName || undefined,
      trackingNo: form.trackingNo || undefined,
      packageCount: form.packageCount != null && !Number.isNaN(form.packageCount) ? form.packageCount : undefined,
      grossWeight: form.grossWeight || undefined,
      volume: form.volume || undefined
    })
    uni.vibrateShort({ type: 'heavy' })
    showResult.value = true
  } catch { /* handled */ } finally { confirming.value = false }
}

onMounted(loadTasks)
</script>

<style lang="scss" scoped>
.section-title { font-size: 30rpx; font-weight: 600; color: #303133; margin-bottom: 20rpx; display: block; }
.field-label { font-size: 26rpx; color: #606266; margin-bottom: 8rpx; display: block; margin-top: 20rpx; }

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

.top-bar { display: flex; align-items: center; padding: 20rpx 24rpx; background: #1677ff; margin: -24rpx -24rpx 24rpx; }
.top-left { margin-right: 16rpx; }
.top-title { font-size: 32rpx; font-weight: 600; color: #fff; }

.label-row { display: flex; justify-content: space-between; padding: 8rpx 0; }
.label { font-size: 26rpx; color: #909399; }
.value { font-size: 26rpx; color: #303133; }
.value.highlight { color: #1677ff; font-weight: 600; }

.text-input { width: 100%; height: 72rpx; background: #fafafa; border: 1rpx solid #d9d9d9; border-radius: 10rpx; padding: 0 20rpx; font-size: 28rpx; box-sizing: border-box; }

.chip-select { display: flex; gap: 12rpx; flex-wrap: wrap; }
.sel-chip { padding: 12rpx 24rpx; background: #f0f0f0; border-radius: 10rpx; font-size: 26rpx; color: #606266; }
.sel-chip.active { background: #1677ff; color: #fff; }

.scan-row { display: flex; align-items: center; gap: 16rpx; }
.scan-input { flex: 1; height: 80rpx; background: #fafafa; border: 2rpx dashed #d9d9d9; border-radius: 12rpx; padding: 0 24rpx; font-size: 32rpx; text-align: center; }
.scan-btn { width: 80rpx; height: 80rpx; background: #1677ff; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }

.pkg-row { display: flex; align-items: center; gap: 16rpx; }
.pkg-btn { width: 64rpx; height: 64rpx; background: #1677ff; color: #fff; border-radius: 10rpx; display: flex; align-items: center; justify-content: center; font-size: 32rpx; font-weight: 600; }
.pkg-input { flex: 1; height: 64rpx; background: #fafafa; border: 1rpx solid #d9d9d9; border-radius: 10rpx; text-align: center; font-size: 32rpx; }

.confirm-wrap { margin-top: 32rpx; }

.result-card { display: flex; flex-direction: column; align-items: center; padding: 80rpx 32rpx; }
.result-icon { margin-bottom: 24rpx; }
.result-title { font-size: 38rpx; font-weight: 700; color: #303133; margin-bottom: 12rpx; }
.result-info { display: flex; flex-direction: column; align-items: center; gap: 4rpx; margin-bottom: 32rpx; font-size: 26rpx; color: #909399; }
</style>
