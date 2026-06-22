<template>
  <div class="app-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="app-sidebar">
        <div class="logo">
          <span v-if="!isCollapse">📦 WMS</span>
          <span v-else>📦</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          router
          background-color="#1a365d"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <!-- 有子菜单 -->
            <el-sub-menu v-if="route.children?.length" :index="resolvePath(route)">
              <template #title>
                <el-icon><component :is="route.meta?.icon" /></el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>
              <el-menu-item v-for="child in route.children" :key="child.path"
                            :index="resolvePath(route) + '/' + child.path">
                {{ child.meta?.title }}
              </el-menu-item>
            </el-sub-menu>
            <!-- 无子菜单 -->
            <el-menu-item v-else :index="resolvePath(route)">
              <el-icon><component :is="route.meta?.icon" /></el-icon>
              <template #title>{{ route.meta?.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部栏 -->
        <el-header class="app-header">
          <div class="header-left">
            <el-button link @click="toggleSidebar">
              <el-icon :size="20"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
            </el-button>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <span class="user-info">{{ authStore.realName || authStore.username }}</span>
            <span class="tenant-tag">{{ authStore.tenantName }}</span>
            <el-button type="danger" link @click="handleLogout">退出</el-button>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const isCollapse = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)

const currentTitle = computed(() => route.meta?.title as string || '')

const menuRoutes = computed(() => {
  return router.options.routes
    .find(r => r.path === '/')?.children
    ?.filter(r => r.meta?.title) || []
})

function resolvePath(route: any) {
  if (route.path.startsWith('/')) return route.path
  return '/' + route.path
}

function toggleSidebar() {
  appStore.toggleSidebar()
}

function handleLogout() {
  authStore.logout()
}
</script>

<style scoped>
.app-layout { height: 100vh; }
.app-sidebar {
  background: #1a365d; overflow-y: auto; overflow-x: hidden;
  transition: width 0.3s;
}
.logo {
  height: 60px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px; font-weight: bold; border-bottom: 1px solid rgba(255,255,255,.1);
}
.app-header {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-bottom: 1px solid #e4e7ed; padding: 0 20px; height: 60px;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-info { font-size: 14px; color: #333; }
.tenant-tag { font-size: 12px; color: #999; background: #f0f2f5; padding: 2px 8px; border-radius: 4px; }
.app-main { background: #f0f2f5; min-height: calc(100vh - 60px); padding: 20px; }
</style>
