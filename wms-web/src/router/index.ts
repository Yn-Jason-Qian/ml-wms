import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'masterdata',
        name: 'MasterData',
        meta: { title: '基础数据', icon: 'Setting' },
        children: [
          {
            path: 'warehouse',
            name: 'Warehouse',
            component: () => import('@/views/masterdata/warehouse/index.vue'),
            meta: { title: '仓库管理' }
          },
          {
            path: 'area',
            name: 'Area',
            component: () => import('@/views/masterdata/area/index.vue'),
            meta: { title: '库区管理' }
          },
          {
            path: 'location',
            name: 'Location',
            component: () => import('@/views/masterdata/location/index.vue'),
            meta: { title: '库位管理' }
          },
          {
            path: 'owner',
            name: 'Owner',
            component: () => import('@/views/masterdata/owner/index.vue'),
            meta: { title: '货主管理' }
          },
          {
            path: 'sku',
            name: 'Sku',
            component: () => import('@/views/masterdata/sku/index.vue'),
            meta: { title: 'SKU管理' }
          },
          {
            path: 'dict',
            name: 'Dict',
            component: () => import('@/views/masterdata/dict/index.vue'),
            meta: { title: '字典管理' }
          }
        ]
      },
      {
        path: 'inventory',
        name: 'Inventory',
        meta: { title: '库存管理', icon: 'Box' },
        children: [
          { path: 'stock', name: 'Stock', component: () => import('@/views/inventory/stock/index.vue'), meta: { title: '库存查询' } },
          { path: 'move', name: 'Move', component: () => import('@/views/inventory/move/index.vue'), meta: { title: '移库管理' } },
          { path: 'stocktake', name: 'Stocktake', component: () => import('@/views/inventory/stocktake/index.vue'), meta: { title: '盘点管理' } },
          { path: 'freeze', name: 'Freeze', component: () => import('@/views/inventory/freeze/index.vue'), meta: { title: '冻结管理' } }
        ]
      },
      {
        path: 'inbound',
        name: 'Inbound',
        meta: { title: '入库管理', icon: 'Download' },
        children: [
          { path: 'asn', name: 'Asn', component: () => import('@/views/inbound/asn/index.vue'), meta: { title: 'ASN管理' } },
          { path: 'receive', name: 'Receive', component: () => import('@/views/inbound/receive/index.vue'), meta: { title: '收货管理' } },
          { path: 'qc', name: 'Qc', component: () => import('@/views/inbound/qc/index.vue'), meta: { title: '质检管理' } },
          { path: 'putaway', name: 'Putaway', component: () => import('@/views/inbound/putaway/index.vue'), meta: { title: '上架管理' } }
        ]
      },
      {
        path: 'outbound',
        name: 'Outbound',
        meta: { title: '出库管理', icon: 'Upload' },
        children: [
          { path: 'order', name: 'Order', component: () => import('@/views/outbound/order/index.vue'), meta: { title: '订单管理' } },
          { path: 'wave', name: 'Wave', component: () => import('@/views/outbound/wave/index.vue'), meta: { title: '波次管理' } },
          { path: 'pick', name: 'Pick', component: () => import('@/views/outbound/pick/index.vue'), meta: { title: '拣货管理' } },
          { path: 'ship', name: 'Ship', component: () => import('@/views/outbound/ship/index.vue'), meta: { title: '发货管理' } }
        ]
      },
      {
        path: 'strategy',
        name: 'Strategy',
        component: () => import('@/views/strategy/index.vue'),
        meta: { title: '策略管理', icon: 'Cpu' }
      },
      {
        path: 'task',
        name: 'Task',
        component: () => import('@/views/task/index.vue'),
        meta: { title: '任务管理', icon: 'List' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '报表中心', icon: 'Document' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 —— 未登录跳转到登录页
router.beforeEach((to, _from, next) => {
  if (to.meta.noAuth) { next() }
  else if (getToken()) { next() }
  else { next('/login') }
})

// Tab tracking via afterEach
router.afterEach((to) => {
  if (!to.meta.noAuth && to.name) {
    import('@/stores/tabs').then(m => m.useTabsStore().addView(to as any)).catch(()=>{})
  }
})

export default router
