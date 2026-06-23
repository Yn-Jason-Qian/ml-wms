<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { initNetworkListener } from '@/utils/network'

const authStore = useAuthStore()
const appStore = useAppStore()

onLaunch(() => {
  console.log('[PDA] App Launch')
  // 初始化网络监听
  initNetworkListener()
  // 恢复登录态
  authStore.restoreSession()
})

onShow(() => {
  console.log('[PDA] App Show')
  appStore.checkNetworkStatus()
})

onHide(() => {
  console.log('[PDA] App Hide')
})
</script>

<style lang="scss">
@import 'uview-plus/index.scss';
@import '@/styles/common.scss';
</style>
