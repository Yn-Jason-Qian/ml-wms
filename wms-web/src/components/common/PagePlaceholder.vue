<template>
  <div class="placeholder">
    <el-empty :description="desc || '功能开发中...'" :image-size="120" />
  </div>
</template>

<script setup lang="ts">
defineProps<{ desc?: string }>()
</script>

<style scoped>
.placeholder {
  display: flex; align-items: center; justify-content: center;
  min-height: 400px;
}
</style>
