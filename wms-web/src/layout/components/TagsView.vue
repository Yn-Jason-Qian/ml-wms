<template>
  <div class="tags-view">
    <el-tag v-for="tab in tabs.visitedViews" :key="tab.path" :type="route.path===tab.path?undefined:'info'"
            :closable="tab.path!=='/dashboard'" :effect="route.path===tab.path?'dark':'plain'"
            class="tag-item" size="default" @click="router.push(tab.path)" @close="closeTab(tab.path)">
      {{ tab.title }}
    </el-tag>
  </div>
</template>
<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/tabs'
const route = useRoute(); const router = useRouter(); const tabs = useTabsStore()
function closeTab(path: string) {
  tabs.removeView(path)
  if (route.path === path) { const r = tabs.visitedViews; router.push(r[r.length-1]?.path || '/dashboard') }
}
</script>
<style scoped>
.tags-view {
  display: flex; gap: 4px; padding: 6px 12px; background: #fff;
  border-bottom: 1px solid #e4e7ed; overflow-x: auto; flex-shrink: 0;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
}
.tags-view::-webkit-scrollbar { height: 3px; }
.tags-view::-webkit-scrollbar-track { background: transparent; }
.tags-view::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.08); border-radius: 2px; }
.tags-view:hover::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.18); }
.tag-item { cursor: pointer; flex-shrink: 0; }
</style>
