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

  // 网络恢复回调
  function onReconnected(callback: () => void): void {
    uni.$on('network:reconnected', callback)
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

  return { networkType, online, offlineTaskCount, onReconnected, onStatusChange }
}
