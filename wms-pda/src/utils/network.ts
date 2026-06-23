/**
 * 网络状态工具
 * 监听网络变化，提供网络状态查询和离线暂存能力
 */

import { ref } from 'vue'

// ── 网络状态 ──
export type NetworkType = 'wifi' | '4g' | '3g' | '2g' | 'none' | 'unknown'

const networkType = ref<NetworkType>('unknown')
const isConnected = ref(true)

// ── 获取当前网络 ──
export function getNetworkType(): Promise<NetworkType> {
  return new Promise((resolve) => {
    uni.getNetworkType({
      success: (res) => {
        networkType.value = res.networkType as NetworkType
        isConnected.value = res.networkType !== 'none'
        resolve(networkType.value)
      },
      fail: () => {
        networkType.value = 'unknown'
        isConnected.value = false
        resolve('unknown')
      }
    })
  })
}

// ── 初始化网络监听 ──
export function initNetworkListener(): void {
  // 首次获取
  getNetworkType()

  // 监听变化
  uni.onNetworkStatusChange((res) => {
    networkType.value = res.networkType as NetworkType
    isConnected.value = res.isConnected

    if (!res.isConnected) {
      uni.showToast({
        title: '网络已断开',
        icon: 'none',
        duration: 3000
      })
    } else {
      uni.showToast({
        title: '网络已恢复',
        icon: 'success',
        duration: 1500
      })
      // 网络恢复事件，页面可监听以触发离线数据同步
      uni.$emit('network:reconnected')
    }
  })
}

// ── 离线暂存 ──
const OFFLINE_QUEUE_KEY = 'wms_offline_queue'

interface OfflineTask {
  id: string
  url: string
  method: 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown>
  createdAt: number
}

/** 暂存到离线队列 */
export function saveToOfflineQueue(task: Omit<OfflineTask, 'id' | 'createdAt'>): void {
  const queue = getOfflineQueue()
  queue.push({
    ...task,
    id: `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    createdAt: Date.now()
  })
  uni.setStorageSync(OFFLINE_QUEUE_KEY, queue)
  uni.showToast({ title: '已暂存，网络恢复后自动提交', icon: 'none' })
}

/** 获取离线队列 */
export function getOfflineQueue(): OfflineTask[] {
  return uni.getStorageSync(OFFLINE_QUEUE_KEY) || []
}

/** 清空离线队列 */
export function clearOfflineQueue(): void {
  uni.removeStorageSync(OFFLINE_QUEUE_KEY)
}

export { networkType, isConnected }
