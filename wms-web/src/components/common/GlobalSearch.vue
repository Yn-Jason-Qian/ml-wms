<template>
  <div class="global-search" @keydown.escape="visible = false">
    <el-input
      v-model="keyword"
      placeholder="搜索 ASN/订单/SKU..."
      :prefix-icon="Search"
      @input="onInput"
      @focus="visible = !!results.length"
      @blur="hideDelay"
      size="default"
      style="width: 260px"
      clearable
    />
    <div v-if="visible && results.length" class="search-dropdown" @mousedown.prevent>
      <div v-for="group in grouped" :key="group[0].type" class="search-group">
        <div class="sg-title">{{ labelMap[group[0].type] || group[0].type }}</div>
        <div v-for="r in group" :key="r.id" class="sg-item" @click="goTo(r)">
          <span>{{ r.title }}</span>
          <span class="sg-sub">{{ r.subtitle }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { globalSearch } from '@/api/search'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const keyword = ref('')
const visible = ref(false)
const results = ref<any[]>([])
let timer: any = null

const labelMap: Record<string, string> = {
  ASN: '📥 ASN',
  ORDER: '📤 订单',
  SKU: '📦 SKU',
  LOCATION: '📍 库位',
  OWNER: '🏢 货主',
  WAVE: '🌊 波次',
}

const grouped = computed(() => {
  const g: Record<string, any[]> = {}
  results.value.forEach((r) => {
    (g[r.type] ||= []).push(r)
  })
  return Object.values(g)
})

function onInput() {
  clearTimeout(timer)
  if (keyword.value.length >= 2) {
    timer = setTimeout(async () => {
      try {
        const r = await globalSearch(keyword.value)
        results.value = (r as any).data || []
        visible.value = !!results.value.length
      } catch {
        results.value = []
      }
    }, 300)
  } else {
    results.value = []
  }
}

function hideDelay() {
  setTimeout(() => {
    visible.value = false
  }, 200)
}

function goTo(item: any) {
  visible.value = false
  keyword.value = ''
  router.push(item.url)
}
</script>

<style scoped>
.global-search {
  position: relative;
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 2000;
  max-height: 400px;
  overflow-y: auto;
}

.sg-title {
  padding: 6px 12px;
  font-size: 12px;
  color: #999;
  background: #f5f7fa;
  font-weight: bold;
}

.sg-item {
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
}

.sg-item:hover {
  background: #ecf5ff;
}

.sg-sub {
  color: #999;
  font-size: 12px;
}
</style>
