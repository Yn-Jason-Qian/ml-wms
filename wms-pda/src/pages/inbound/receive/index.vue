<template>
  <view class="page-container">
    <!-- 步骤指示器 -->
    <view class="steps-bar">
      <view class="step" :class="{ active: currentStep >= 1, done: currentStep > 1 }">
        <view class="step-num">1</view>
        <text class="step-label">选择类型</text>
      </view>
      <view class="step-line" :class="{ active: currentStep > 1 }" />
      <view class="step" :class="{ active: currentStep >= 2, done: currentStep > 2 }">
        <view class="step-num">2</view>
        <text class="step-label">扫描SKU</text>
      </view>
      <view class="step-line" :class="{ active: currentStep > 2 }" />
      <view class="step" :class="{ active: currentStep >= 3 }">
        <view class="step-num">3</view>
        <text class="step-label">确认收货</text>
      </view>
    </view>

    <!-- Step 1: 选择收货类型 -->
    <view class="card" v-if="currentStep === 1">
      <text class="section-title">收货模式</text>
      <view class="receive-type-grid">
        <view
          class="type-card"
          :class="{ selected: form.receiveType === 'ASN' }"
          @click="selectType('ASN')"
        >
          <up-icon name="list" size="36" :color="form.receiveType === 'ASN' ? '#1677ff' : '#909399'" />
          <text class="type-name">ASN 收货</text>
          <text class="type-desc">扫描预发货通知单</text>
        </view>
        <view
          class="type-card"
          :class="{ selected: form.receiveType === 'BLIND' }"
          @click="selectType('BLIND')"
        >
          <up-icon name="scan" size="36" :color="form.receiveType === 'BLIND' ? '#1677ff' : '#909399'" />
          <text class="type-name">盲收</text>
          <text class="type-desc">无单据直接收货</text>
        </view>
      </view>

      <!-- ASN 扫码输入 -->
      <view class="scan-section" v-if="form.receiveType === 'ASN'">
        <text class="field-label">ASN 单号</text>
        <view class="scan-row">
          <input
            class="scan-input"
            v-model="form.asnNo"
            placeholder="扫描或输入 ASN 条码"
            @confirm="onAsnScanned"
          />
          <view class="scan-btn" @click="onScanClick('asn')">
            <up-icon name="scan" size="24" color="#fff" />
          </view>
        </view>
        <view class="asn-info" v-if="asnInfo">
          <view class="label-row"><text class="label">单号</text><text class="value">{{ asnInfo.asnNo }}</text></view>
          <view class="label-row"><text class="label">货主</text><text class="value">{{ asnInfo.ownerName || '-' }}</text></view>
          <view class="label-row"><text class="label">SKU 数</text><text class="value">{{ asnInfo.totalLines || 0 }} 行</text></view>
        </view>
      </view>

      <view class="step-action">
        <up-button
          type="primary"
          text="下一步 → 扫描SKU"
          shape="round"
          @click="goStep2"
          :disabled="form.receiveType === 'ASN' && !asnInfo"
        />
      </view>
    </view>

    <!-- Step 2: 扫描SKU + 输入数量 -->
    <view class="card" v-if="currentStep === 2">
      <text class="section-title">收货明细</text>

      <!-- SKU 扫码 -->
      <text class="field-label">SKU 条码</text>
      <view class="scan-row">
        <input
          class="scan-input"
          v-model="skuInput"
          placeholder="扫描或输入 SKU 条码"
          @confirm="onSkuScanned"
          ref="skuInputRef"
        />
        <view class="scan-btn" @click="onScanClick('sku')">
          <up-icon name="scan" size="24" color="#fff" />
        </view>
      </view>

      <!-- SKU 信息 -->
      <view class="sku-info-card" v-if="currentSku">
        <view class="sku-header">
          <text class="sku-code">{{ currentSku.skuCode }}</text>
          <up-tag :text="currentSku.skuName" type="primary" size="small" />
        </view>

        <view class="qty-row">
          <text class="field-label">收货数量</text>
          <view class="qty-input-wrap">
            <view class="qty-btn" @click="adjustQty(-1)"><text>-</text></view>
            <input
              class="qty-input"
              v-model.number="form.receiveQty"
              type="digit"
              placeholder="0"
            />
            <view class="qty-btn" @click="adjustQty(1)"><text>+</text></view>
          </view>
        </view>

        <view class="qty-row">
          <text class="field-label">收货库位</text>
          <view class="scan-row sm">
            <input class="scan-input sm" v-model="form.locationCode" placeholder="扫描库位" @confirm="onLocationScanned" />
            <view class="scan-btn sm" @click="onScanClick('location')">
              <up-icon name="scan" size="20" color="#fff" />
            </view>
          </view>
        </view>

        <!-- 批次信息（可选） -->
        <view class="batch-row">
          <text class="field-label">批次号</text>
          <input class="text-input" v-model="form.batchNo" placeholder="可选" />
        </view>
        <view class="batch-row">
          <text class="field-label">生产日期</text>
          <input class="text-input" v-model="form.productionDate" type="text" placeholder="YYYY-MM-DD 可选" />
        </view>
        <view class="batch-row">
          <text class="field-label">效期至</text>
          <input class="text-input" v-model="form.expiryDate" type="text" placeholder="YYYY-MM-DD 可选" />
        </view>

        <view class="add-btn-wrap">
          <up-button text="添加到列表" type="success" icon="plus" @click="addSkuToList" />
        </view>
      </view>

      <!-- 已添加明细列表 -->
      <view class="item-list" v-if="receivedItems.length > 0">
        <text class="section-title">已添加 ({{ receivedItems.length }})</text>
        <view class="item-row" v-for="(item, idx) in receivedItems" :key="idx">
          <view class="item-info">
            <text class="item-sku">{{ item.skuCode }}</text>
            <text class="item-detail">库位: {{ item.locationCode }} | 数量: {{ item.receiveQty }}</text>
          </view>
          <view class="item-del" @click="removeItem(idx)">
            <up-icon name="close-circle-fill" size="20" color="#ff4d4f" />
          </view>
        </view>
      </view>

      <view class="step-action dual">
        <up-button text="上一步" shape="round" @click="currentStep = 1" />
        <up-button
          type="primary"
          text="下一步 → 确认收货"
          shape="round"
          :disabled="receivedItems.length === 0"
          @click="currentStep = 3"
        />
      </view>
    </view>

    <!-- Step 3: 确认提交 -->
    <view class="card" v-if="currentStep === 3">
      <text class="section-title">确认收货</text>

      <view class="summary-section">
        <view class="label-row"><text class="label">收货类型</text><text class="value">{{ form.receiveType }}</text></view>
        <view class="label-row" v-if="asnInfo"><text class="label">ASN</text><text class="value">{{ asnInfo.asnNo }}</text></view>
        <view class="label-row"><text class="label">明细行数</text><text class="value">{{ receivedItems.length }} 行</text></view>
        <view class="label-row">
          <text class="label">总数量</text>
          <text class="value highlight">{{ totalQty }} 件</text>
        </view>
      </view>

      <view class="step-action dual">
        <up-button text="上一步" shape="round" @click="currentStep = 2" />
        <up-button
          type="primary"
          text="确认收货"
          shape="round"
          :loading="submitting"
          @click="submitReceive"
        />
      </view>
    </view>

    <!-- 结果页 -->
    <view class="card result-card" v-if="showResult">
      <view class="result-icon">
        <up-icon name="checkmark-circle-fill" size="80" color="#52c41a" />
      </view>
      <text class="result-title">收货完成</text>
      <text class="result-no">{{ resultReceiveNo }}</text>
      <up-button type="primary" text="继续收货" shape="round" @click="resetForm" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useScanner } from '@/composables/useScanner'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const { scan } = useScanner()
const authStore = useAuthStore()

// ── 步骤状态 ──
const currentStep = ref(1)
const showResult = ref(false)
const resultReceiveNo = ref('')
const submitting = ref(false)

// ── 表单 ──
const form = reactive({
  receiveType: '' as 'ASN' | 'BLIND',
  asnNo: '',
  skuCode: '',
  receiveQty: 0,
  locationCode: '',
  batchNo: '',
  productionDate: '',
  expiryDate: ''
})

const skuInput = ref('')
const asnInfo = ref<any>(null)
const currentSku = ref<any>(null)
const receivedItems = ref<any[]>([])

const totalQty = computed(() =>
  receivedItems.value.reduce((sum, item) => sum + (item.receiveQty || 0), 0)
)

// ── Step 1 操作 ──
function selectType(type: 'ASN' | 'BLIND') {
  form.receiveType = type
  asnInfo.value = null
}

async function onAsnScanned() {
  const asnNo = form.asnNo.trim()
  if (!asnNo) return
  try {
    // 根据 ASN 号查询 ASN 信息
    const res = await request.get('/inbound/asns/page', { asnNo, pageNum: 1, pageSize: 1 })
    if (res.data?.records?.length > 0) {
      asnInfo.value = res.data.records[0]
    } else {
      uni.showToast({ title: '未找到该ASN', icon: 'none' })
    }
  } catch { /* handled by interceptor */ }
}

async function onScanClick(target: string) {
  const result = await scan()
  if (!result) return // 取消或防重复

  switch (target) {
    case 'asn':
      form.asnNo = result.barcodeValue
      onAsnScanned()
      break
    case 'sku':
      skuInput.value = result.barcodeValue
      onSkuScanned()
      break
    case 'location':
      form.locationCode = result.barcodeValue
      break
  }
}

function goStep2() {
  if (form.receiveType === 'ASN' && !asnInfo.value) {
    uni.showToast({ title: '请先扫描ASN', icon: 'none' })
    return
  }
  currentStep.value = 2
  // 自动聚焦到 SKU 输入
  setTimeout(() => {
    const el = uni.createSelectorQuery().select('.scan-input')
    if (el) {
      // @ts-ignore
      uni.hideKeyboard()
    }
  }, 300)
}

// ── Step 2 操作 ──
async function onSkuScanned() {
  const code = skuInput.value.trim()
  if (!code) return
  try {
    const res = await request.get('/masterdata/skus/page', { skuCode: code, pageNum: 1, pageSize: 1 })
    if (res.data?.records?.length > 0) {
      currentSku.value = res.data.records[0]
      form.skuCode = code
      form.receiveQty = 1 // 默认数量 1
    } else {
      uni.showToast({ title: '未找到该SKU', icon: 'none' })
    }
  } catch { /* handled */ }
}

function adjustQty(delta: number) {
  const newQty = (form.receiveQty || 0) + delta
  if (newQty >= 0) form.receiveQty = newQty
}

function onLocationScanned() {
  // 库位已通过 v-model 绑定，此处仅做确认
  if (form.locationCode) {
    uni.showToast({ title: '库位: ' + form.locationCode, icon: 'success', duration: 1500 })
  }
}

function addSkuToList() {
  if (!currentSku.value) {
    uni.showToast({ title: '请先扫描SKU', icon: 'none' })
    return
  }
  if (!form.receiveQty || form.receiveQty <= 0) {
    uni.showToast({ title: '请输入数量', icon: 'none' })
    return
  }
  if (!form.locationCode) {
    uni.showToast({ title: '请扫描收货库位', icon: 'none' })
    return
  }

  receivedItems.value.push({
    skuCode: currentSku.value.skuCode,
    skuName: currentSku.value.skuName,
    skuId: currentSku.value.id,
    receiveQty: form.receiveQty,
    locationCode: form.locationCode,
    batchNo: form.batchNo,
    productionDate: form.productionDate,
    expiryDate: form.expiryDate
  })

  // 重置当前 SKU 输入，准备扫描下一个
  currentSku.value = null
  skuInput.value = ''
  form.receiveQty = 0
  form.batchNo = ''
  form.productionDate = ''
  form.expiryDate = ''
  form.skuCode = ''
  // 保留库位（通常同一批货在同一库位验收）
}

function removeItem(idx: number) {
  receivedItems.value.splice(idx, 1)
}

// ── Step 3 提交 ──
async function submitReceive() {
  submitting.value = true
  try {
    // 并行提交所有行，使用 Promise.allSettled 追踪每行状态
    const results = await Promise.allSettled(
      receivedItems.value.map(item =>
        request.post('/inbound/receives', {
          warehouseId: authStore.warehouseId,
          ownerId: authStore.tenantId,
          asnHeaderId: asnInfo.value?.id || null,
          receiveType: form.receiveType,
          skuId: item.skuId,
          receiveQty: item.receiveQty,
          receiveLocationId: null,
          receiveLocationCode: item.locationCode,
          batchNo: item.batchNo || undefined,
          productionDate: item.productionDate || undefined,
          expiryDate: item.expiryDate || undefined
        })
      )
    )

    // 统计失败行
    const failed = results.filter(r => r.status === 'rejected').length
    const succeeded = results.length - failed

    if (failed === 0) {
      showResult.value = true
      resultReceiveNo.value = `共 ${succeeded} 行, ${totalQty.value} 件`
    } else {
      uni.showModal({
        title: '部分提交失败',
        content: `成功 ${succeeded} 行，失败 ${failed} 行。请检查网络后重试。`,
        showCancel: false
      })
    }
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  showResult.value = false
  currentStep.value = 1
  form.receiveType = ''
  form.asnNo = ''
  skuInput.value = ''
  form.receiveQty = 0
  form.locationCode = ''
  form.batchNo = ''
  form.productionDate = ''
  form.expiryDate = ''
  asnInfo.value = null
  currentSku.value = null
  receivedItems.value = []
}
</script>

<style lang="scss" scoped>
// ── 步骤指示器 ──
.steps-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.step-num {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #e8e8e8;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
}

.step.active .step-num {
  background: #1677ff;
  color: #fff;
}

.step.done .step-num {
  background: #52c41a;
  color: #fff;
}

.step-label {
  font-size: 22rpx;
  color: #999;
}

.step.active .step-label { color: #1677ff; font-weight: 500; }
.step.done .step-label { color: #52c41a; }

.step-line {
  width: 60rpx;
  height: 2rpx;
  background: #e8e8e8;
  margin: 0 12rpx;
}

.step-line.active {
  background: #1677ff;
}

// ── 类型选择 ──
.receive-type-grid {
  display: flex;
  gap: 24rpx;
}

.type-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 16rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  gap: 12rpx;
}

.type-card.selected {
  border-color: #1677ff;
  background: #f0f5ff;
}

.type-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
}

.type-desc {
  font-size: 22rpx;
  color: #909399;
}

// ── 扫码行 ──
.scan-section {
  margin-top: 24rpx;
}

.field-label {
  font-size: 26rpx;
  color: #606266;
  margin-bottom: 8rpx;
  display: block;
  margin-top: 16rpx;
}

.scan-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.scan-row.sm { gap: 8rpx; }

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

.scan-input.sm {
  height: 68rpx;
  font-size: 28rpx;
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

.scan-btn.sm {
  width: 68rpx;
  height: 68rpx;
}

// ── ASN / SKU 信息卡 ──
.asn-info {
  margin-top: 16rpx;
  padding: 16rpx;
  background: #f0f5ff;
  border-radius: 12rpx;
}

.sku-info-card {
  margin-top: 16rpx;
  padding: 20rpx;
  background: #fafafa;
  border-radius: 12rpx;
}

.sku-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.sku-code {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}

// ── 数量调整 ──
.qty-row {
  margin-bottom: 16rpx;
}

.qty-input-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.qty-btn {
  width: 64rpx;
  height: 64rpx;
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
}

.qty-input {
  flex: 1;
  height: 64rpx;
  background: #fff;
  border: 1rpx solid #d9d9d9;
  border-radius: 8rpx;
  text-align: center;
  font-size: 32rpx;
  width: 120rpx;
}

// ── 批次行 ──
.batch-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.batch-row .field-label {
  min-width: 120rpx;
  margin-top: 0;
}

.text-input {
  flex: 1;
  height: 60rpx;
  background: #fff;
  border: 1rpx solid #d9d9d9;
  border-radius: 8rpx;
  padding: 0 16rpx;
  font-size: 26rpx;
}

// ── 添加按钮 ──
.add-btn-wrap {
  margin-top: 20rpx;
}

// ── 已添加列表 ──
.item-list {
  margin-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 16rpx;
}

.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-sku {
  font-size: 28rpx;
  font-weight: 500;
  color: #303133;
}

.item-detail {
  font-size: 24rpx;
  color: #909399;
  margin-top: 4rpx;
}

.item-del {
  padding: 8rpx;
}

// ── 操作栏 ──
.step-action {
  margin-top: 32rpx;
}

.step-action.dual {
  display: flex;
  gap: 16rpx;
}

.step-action.dual > * {
  flex: 1;
}

// ── 汇总 ──
.summary-section {
  margin-bottom: 24rpx;
}

.highlight {
  color: #1677ff;
  font-weight: 600;
  font-size: 32rpx !important;
}

// ── 结果页 ──
.result-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64rpx 32rpx;
}

.result-icon {
  margin-bottom: 24rpx;
}

.result-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8rpx;
}

.result-no {
  font-size: 26rpx;
  color: #909399;
  margin-bottom: 32rpx;
}
</style>
