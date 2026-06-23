/**
 * 应用全局状态
 * 复用 wms-web stores/app.ts 模式
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNetworkType, type NetworkType, isConnected } from '@/utils/network'

export const useAppStore = defineStore('app', () => {
  const networkType = ref<NetworkType>('unknown')
  const online = ref(true)
  const isScanning = ref(false)

  /** 检查网络状态 */
  async function checkNetworkStatus(): Promise<void> {
    networkType.value = await getNetworkType()
    online.value = isConnected.value
  }

  /** 锁定扫码（防重复） */
  function lockScanner(): void {
    isScanning.value = true
  }

  /** 解锁扫码 */
  function unlockScanner(): void {
    isScanning.value = false
  }

  return { networkType, online, isScanning, checkNetworkStatus, lockScanner, unlockScanner }
})
