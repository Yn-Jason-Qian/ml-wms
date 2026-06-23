<template>
  <view class="page-container">
    <!-- 新移库 -->
    <view class="card" v-if="!currentLine">
      <text class="section-title">移库操作</text>
      <text class="guide-desc">扫描来源库位 → 扫描SKU → 输入数量 → 扫描目标库位</text>

      <!-- Step 1: 来源库位 -->
      <text class="step-label"><text class="step-num">1</text> 来源库位</text>
      <view class="scan-row">
        <input class="scan-input" :class="{ done: fromLocation }" v-model="fromLocation"
          placeholder="扫描来源库位" @confirm="onFromLocationDone" />
        <view class="scan-btn" @click="onScanClick('fromLocation')" v-if="!fromLocation">
          <up-icon name="scan" size="24" color="#fff" />
        </view>
        <view class="done-icon" v-else><up-icon name="checkmark-circle-fill" size="28" color="#52c41a" /></view>
      </view>

      <!-- Step 2: SKU -->
      <text class="step-label"><text class="step-num">2</text> SKU</text>
      <view class="scan-row">
        <input class="scan-input" :class="{ done: skuInfo }" v-model="skuInput"
          placeholder="扫描SKU条码" :disabled="!fromLocation" @confirm="onSkuScanned" />
        <view class="scan-btn" @click="onScanClick('sku')" v-if="!skuInfo">
          <up-icon name="scan" size="24" color="#fff" />
        </view>
        <view class="done-icon" v-else><up-icon name="checkmark-circle-fill" size="28" color="#52c41a" /></view>
      </view>

      <!-- SKU 信息卡 -->
      <view class="sku-card" v-if="skuInfo">
        <view class="sku-header"><text class="sku-code">{{ skuInfo.skuCode }}</text><text class="sku-name">{{ skuInfo.skuName }}</text></view>
        <view class="label-row"><text class="label">当前库存</text><text class="value">{{ skuInfo.qty || '-' }} 件</text></view>
        <view class="label-row" v-if="skuInfo.batchNo"><text class="label">批次</text><text class="value">{{ skuInfo.batchNo }}</text></view>
      </view>

      <!-- Step 3: 移库数量 -->
      <text class="step-label"><text class="step-num">3</text> 移库数量</text>
      <view class="qty-row">
        <view class="qty-btn" @click="moveQty = Math.max(0, (moveQty || 0) - 1)"><text>-</text></view>
        <input class="qty-input-big" v-model.number="moveQty" type="digit" placeholder="数量" :disabled="!skuInfo" />
        <view class="qty-btn" @click="moveQty = (moveQty || 0) + 1"><text>+</text></view>
      </view>
      <view class="quick-row">
        <view class="quick-chip" @click="moveQty = skuInfo?.qty || 0">全部</view>
      </view>

      <!-- Step 4: 目标库位 -->
      <text class="step-label"><text class="step-num">4</text> 目标库位</text>
      <view class="scan-row">
        <input class="scan-input highlight" :class="{ done: toLocation }" v-model="toLocation"
          placeholder="扫描目标库位" :disabled="!moveQty" @confirm="onToLocationDone" />
        <view class="scan-btn" @click="onScanClick('toLocation')" v-if="!toLocation">
          <up-icon name="scan" size="24" color="#fff" />
        </view>
        <view class="done-icon" v-else><up-icon name="checkmark-circle-fill" size="28" color="#52c41a" /></view>
      </view>

      <!-- 确认移库 -->
      <view class="confirm-wrap">
        <up-button type="primary" text="确认移库" size="large" shape="round"
          :disabled="!canConfirm" :loading="confirming" @click="confirmMove" />
      </view>
    </view>

    <!-- 结果 -->
    <view class="card result-card" v-if="showResult">
      <view class="result-icon"><up-icon name="checkmark-circle-fill" size="96" color="#52c41a" /></view>
      <text class="result-title">移库完成</text>
      <view class="result-info">
        <text>{{ skuInfo?.skuCode }} × {{ moveQty }} 件</text>
        <text>{{ fromLocation }} → {{ toLocation }}</text>
      </view>
      <up-button type="primary" text="继续移库" shape="round" @click="resetForm" />
    </view>

    <!-- 最近移库记录 -->
    <view class="card" v-if="recentMoves.length > 0 && !currentLine">
      <text class="section-title">最近移库</text>
      <view class="recent-item" v-for="(m, i) in recentMoves" :key="i">
        <text class="recent-sku">{{ m.skuCode }} × {{ m.qty }}</text>
        <text class="recent-path">{{ m.from }} → {{ m.to }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useScanner } from '@/composables/useScanner'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const { scan } = useScanner()
const authStore = useAuthStore()

const fromLocation = ref('')
const skuInput = ref('')
const skuInfo = ref<any>(null)
const moveQty = ref(0)
const toLocation = ref('')
const confirming = ref(false)
const showResult = ref(false)
const currentLine = ref<any>(null)
const recentMoves = ref<any[]>([])

const canConfirm = computed(() =>
  fromLocation.value && skuInfo.value && moveQty.value > 0 && toLocation.value
)

async function onSkuScanned() {
  const code = skuInput.value.trim()
  if (!code) return
  try {
    // 查询该 SKU 在来源库位的库存（可能返回多个批次）
    const res = await request.get('/inventory/search', { skuCode: code, locationCode: fromLocation.value })
    const records = res.data?.records || []
    if (records.length > 1) {
      // 多批次：弹出选择器
      const batchNames = records.map((r: any) =>
        `${r.batchNo || '无批次'} | 库存: ${r.qty || 0} 件 | ${r.lotAttrs || ''}`
      )
      uni.showActionSheet({
        itemList: batchNames.slice(0, 6),
        success: (actionRes) => {
          skuInfo.value = records[actionRes.tapIndex]
        }
      })
    } else if (records.length === 1) {
      skuInfo.value = records[0]
    } else {
      // 如果库位库存查询失败，尝试 SKU 查询
      const skuRes = await request.get('/masterdata/skus/page', { skuCode: code, pageNum: 1, pageSize: 1 })
      if (skuRes.data?.records?.length > 0) {
        skuInfo.value = { ...skuRes.data.records[0], qty: 0 }
      } else {
        uni.showToast({ title: '未找到该SKU', icon: 'none' })
      }
    }
  } catch {
    uni.showToast({ title: '查询失败，请检查网络', icon: 'none' })
  }
}

async function onScanClick(target: string) {
  const r = await scan()
  if (!r) return
  switch (target) {
    case 'fromLocation': fromLocation.value = r.barcodeValue; break
    case 'sku': skuInput.value = r.barcodeValue; onSkuScanned(); break
    case 'toLocation': toLocation.value = r.barcodeValue; break
  }
}

async function confirmMove() {
  confirming.value = true
  try {
    await request.post('/inventory/moves', {
      warehouseId: authStore.warehouseId,
      skuId: skuInfo.value?.id,
      moveQty: moveQty.value,
      fromLocationCode: fromLocation.value,
      toLocationCode: toLocation.value,
      batchNo: skuInfo.value?.batchNo || undefined
    })

    uni.vibrateShort({ type: 'heavy' })
    // 记录最近移库
    recentMoves.value.unshift({
      skuCode: skuInfo.value?.skuCode || '-',
      qty: moveQty.value,
      from: fromLocation.value,
      to: toLocation.value
    })
    if (recentMoves.value.length > 10) recentMoves.value.pop()

    showResult.value = true
  } catch { /* handled */ } finally { confirming.value = false }
}

function resetForm() {
  showResult.value = false
  fromLocation.value = ''
  skuInput.value = ''
  skuInfo.value = null
  moveQty.value = 0
  toLocation.value = ''
}
</script>

<style lang="scss" scoped>
.section-title { font-size: 30rpx; font-weight: 600; color: #303133; margin-bottom: 16rpx; display: block; }
.guide-desc { font-size: 26rpx; color: #909399; margin-bottom: 28rpx; display: block; }

.step-label { font-size: 28rpx; font-weight: 500; color: #303133; margin-bottom: 8rpx; display: flex; align-items: center; gap: 8rpx; margin-top: 24rpx; }
.step-num { width: 40rpx; height: 40rpx; background: #1677ff; color: #fff; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 600; }

.scan-row { display: flex; align-items: center; gap: 16rpx; }
.scan-input { flex: 1; height: 80rpx; background: #fafafa; border: 2rpx dashed #d9d9d9; border-radius: 12rpx; padding: 0 24rpx; font-size: 32rpx; text-align: center; }
.scan-input.done { border-color: #52c41a; background: #f6ffed; color: #52c41a; }
.scan-input.highlight { border-color: #1677ff; background: #f0f5ff; }
.scan-btn { width: 80rpx; height: 80rpx; background: #1677ff; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.done-icon { width: 80rpx; height: 80rpx; display: flex; align-items: center; justify-content: center; }

.sku-card { margin-top: 12rpx; padding: 16rpx; background: #fafafa; border-radius: 12rpx; }
.sku-header { display: flex; align-items: baseline; gap: 12rpx; margin-bottom: 12rpx; }
.sku-code { font-size: 30rpx; font-weight: 700; color: #303133; }
.sku-name { font-size: 26rpx; color: #909399; }

.label-row { display: flex; justify-content: space-between; padding: 6rpx 0; }
.label { font-size: 24rpx; color: #909399; }
.value { font-size: 26rpx; color: #303133; }

.qty-row { display: flex; align-items: center; gap: 20rpx; margin-top: 8rpx; }
.qty-btn { width: 72rpx; height: 72rpx; background: #1677ff; color: #fff; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; font-size: 36rpx; font-weight: 600; }
.qty-input-big { flex: 1; height: 80rpx; background: #fafafa; border: 2rpx solid #1677ff; border-radius: 12rpx; text-align: center; font-size: 40rpx; font-weight: 700; color: #1677ff; }

.quick-row { margin-top: 12rpx; display: flex; gap: 12rpx; }
.quick-chip { padding: 8rpx 20rpx; background: #f0f5ff; color: #1677ff; border-radius: 8rpx; font-size: 24rpx; }

.confirm-wrap { margin-top: 36rpx; }

.result-card { display: flex; flex-direction: column; align-items: center; padding: 64rpx 32rpx; }
.result-icon { margin-bottom: 24rpx; }
.result-title { font-size: 38rpx; font-weight: 700; color: #303133; margin-bottom: 12rpx; }
.result-info { display: flex; flex-direction: column; align-items: center; gap: 4rpx; margin-bottom: 32rpx; font-size: 26rpx; color: #909399; }

.recent-item { display: flex; justify-content: space-between; padding: 14rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.recent-sku { font-size: 26rpx; color: #303133; }
.recent-path { font-size: 24rpx; color: #909399; }
</style>
