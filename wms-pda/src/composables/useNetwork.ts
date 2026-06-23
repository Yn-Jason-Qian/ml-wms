/**
 * 网络状态监听 Composable
 * 提供响应式网络状态 + 离线队列事件
 */

import { ref, onMounted, onUnmounted } from 'vue'
import { getNetworkType, type NetworkType, getOfflineQueue } from '@/utils/network'

export function useNetwork() {
  const networkType = ref<NetworkType>('unknown')
  const online = ref(true)
  const offlineTaskCount = ref(0)
  const reconnectedCallbacks: Array<() => void> = []

  // 网络恢复回调（带清理）
  function onReconnected(callback: () => void): () => void {
    reconnectedCallbacks.push(callback)
    uni.$on('network:reconnected', callback)
    // 返回取消订阅函数
    return () => {
      uni.$off('network:reconnected', callback)
      const idx = reconnectedCallbacks.indexOf(callback)
      if (idx >= 0) reconnectedCallbacks.splice(idx, 1)
    }
  }

  // 网络状态变化
  function onStatusChange(callback: (res: UniApp.OnNetworkStatusChangeSuccess) => void): void {
    uni.onNetworkStatusChange(callback)
  }

  onMounted(async () => {
    networkType.value = await getNetworkType()
    online.value = networkType.value !== 'none'
    offlineTaskCount.value = getOfflineQueue().length
  })

  onUnmounted(() => {
    // 清理所有注册的回调
    for (const cb of reconnectedCallbacks) {
      uni.$off('network:reconnected', cb)
    }
    reconnectedCallbacks.length = 0
  })

  return { networkType, online, offlineTaskCount, onReconnected, onStatusChange }
}
