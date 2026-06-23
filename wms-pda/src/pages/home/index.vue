<template>
  <view class="page-container">
    <!-- 用户信息栏 -->
    <view class="card user-card">
      <view class="user-info">
        <up-icon name="account-fill" size="44" color="#1677ff" />
        <view class="user-text">
          <text class="user-name">{{ authStore.realName || authStore.username }}</text>
          <text class="tenant-name">{{ authStore.tenantName }}</text>
        </view>
      </view>
      <view class="warehouse-info" v-if="authStore.warehouseName">
        <up-tag :text="authStore.warehouseName" type="primary" plain />
      </view>
    </view>

    <!-- 今日任务统计 -->
    <view class="card stats-section">
      <text class="section-title">今日任务</text>
      <view class="stats-grid">
        <view class="stat-item" @click="navigateTo('/pages/inbound/receive/index')">
          <text class="stat-num">{{ stats.pendingReceive }}</text>
          <text class="stat-label">待收货</text>
        </view>
        <view class="stat-item" @click="navigateTo('/pages/inbound/putaway/index')">
          <text class="stat-num">{{ stats.pendingPutaway }}</text>
          <text class="stat-label">待上架</text>
        </view>
        <view class="stat-item" @click="navigateTo('/pages/outbound/pick/index')">
          <text class="stat-num">{{ stats.pendingPick }}</text>
          <text class="stat-label">待拣货</text>
        </view>
        <view class="stat-item" @click="navigateTo('/pages/inventory/stocktake/index')">
          <text class="stat-num">{{ stats.pendingStocktake }}</text>
          <text class="stat-label">待盘点</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="card menu-section">
      <text class="section-title">快捷操作</text>
      <view class="menu-grid">
        <view class="menu-item" v-for="item in menuList" :key="item.path" @click="navigateTo(item.path)">
          <view class="menu-icon-wrap">
            <up-icon :name="item.icon" size="32" color="#fff" />
          </view>
          <text class="menu-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <!-- 待办任务列表 -->
    <view class="card task-section">
      <view class="section-header">
        <text class="section-title">我的任务</text>
        <text class="view-all" @click="navigateTo('/pages/task/index')">查看全部</text>
      </view>
      <view class="task-list" v-if="taskList.length > 0">
        <view class="task-item" v-for="task in taskList" :key="task.id">
          <view class="task-info">
            <text class="task-type">{{ task.typeName }}</text>
            <text class="task-desc">{{ task.description }}</text>
          </view>
          <up-tag :text="task.statusText" :type="task.statusType" size="small" />
        </view>
      </view>
      <view class="empty-tasks" v-else>
        <text class="empty-text">暂无待处理任务</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-wrapper">
      <up-button text="退出登录" type="error" plain @click="handleLogout" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const stats = reactive({
  pendingReceive: 0,
  pendingPutaway: 0,
  pendingPick: 0,
  pendingStocktake: 0
})

const menuList = [
  { label: '收货', icon: 'download', path: '/pages/inbound/receive/index' },
  { label: '上架', icon: 'arrow-up-fill', path: '/pages/inbound/putaway/index' },
  { label: '拣货', icon: 'search', path: '/pages/outbound/pick/index' },
  { label: '复核', icon: 'checkmark-circle', path: '/pages/outbound/check/index' },
  { label: '盘点', icon: 'list', path: '/pages/inventory/stocktake/index' },
  { label: '移库', icon: 'swap', path: '/pages/inventory/move/index' }
]

const taskList = ref<any[]>([])

function navigateTo(path: string) {
  uni.navigateTo({ url: path })
}

function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        authStore.logout()
      }
    }
  })
}

onMounted(() => {
  // TODO: 阶段 3.4 接入 Dashboard API 获取统计数据
})
</script>

<style lang="scss" scoped>
.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.user-text {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
}

.tenant-name {
  font-size: 24rpx;
  color: #909399;
  margin-top: 4rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24rpx;
  display: block;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-header .section-title {
  margin-bottom: 0;
}

.view-all {
  font-size: 24rpx;
  color: #1677ff;
}

.stats-grid {
  display: flex;
  justify-content: space-between;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  padding: 16rpx 0;
}

.stat-num {
  font-size: 40rpx;
  font-weight: 700;
  color: #1677ff;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #909399;
}

.menu-grid {
  display: flex;
  flex-wrap: wrap;
}

.menu-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0;
}

.menu-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  background: #1677ff;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.menu-label {
  font-size: 24rpx;
  color: #303133;
}

.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.task-type {
  font-size: 28rpx;
  color: #303133;
  margin-bottom: 4rpx;
}

.task-desc {
  font-size: 24rpx;
  color: #909399;
}

.empty-tasks {
  padding: 48rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 26rpx;
  color: #c0c4cc;
}

.logout-wrapper {
  margin-top: 48rpx;
  padding-bottom: 48rpx;
}
</style>
