import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTabsStore = defineStore('tabs', () => {
  const visitedViews = ref<{path:string;name:string;title:string}[]>([])
  const cachedViews = ref<string[]>([])

  function addView(route: {path:string;name?:string|symbol|null;meta?:any}) {
    if (!route.meta?.title || route.meta?.noAuth) return
    const name = (route.name as string) || route.path
    if (!visitedViews.value.find(v => v.path === route.path)) {
      visitedViews.value.push({ path: route.path, name, title: route.meta.title as string })
      if (!route.meta.noCache) cachedViews.value.push(name)
    }
  }
  function removeView(path: string) {
    const idx = visitedViews.value.findIndex(v => v.path === path)
    if (idx > -1) { cachedViews.value = cachedViews.value.filter(n => n !== visitedViews.value[idx].name); visitedViews.value.splice(idx, 1) }
  }
  function removeOthers(path: string) { visitedViews.value = visitedViews.value.filter(v => v.path === path || v.path === '/dashboard') }
  function removeAll() { visitedViews.value = visitedViews.value.filter(v => v.path === '/dashboard'); cachedViews.value = [] }
  return { visitedViews, cachedViews, addView, removeView, removeOthers, removeAll }
}, { persist: true })
