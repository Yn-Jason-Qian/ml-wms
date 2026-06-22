<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="28"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="16">
        <el-card>
          <template #header>📈 今日作业概览</template>
          <div style="height:300px;display:flex;align-items:center;justify-content:center;color:#999">
            图表区域 — 待接入 ECharts
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>⚠️ 效期预警</template>
          <el-empty description="暂无临期库存" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const stats = ref([
  { label: '待收货', value: 0, color: '#409eff', icon: 'Download' },
  { label: '待上架', value: 0, color: '#e6a23c', icon: 'Place' },
  { label: '待拣货', value: 0, color: '#67c23a', icon: 'Upload' },
  { label: '待发货', value: 0, color: '#f56c6c', icon: 'Van' }
])

onMounted(async () => {
  const api = request.post
  try { const r = await api('/inbound/receives/page', { pageNum: 1, pageSize: 1, status: 'RECEIVING' }); stats.value[0].value = (r as any).data?.total || 0 } catch {}
  try { const r = await api('/inbound/putaways/page', { pageNum: 1, pageSize: 1, status: 'PUTAWAYING' }); stats.value[1].value = (r as any).data?.total || 0 } catch {}
  try { const r = await api('/outbound/picks/page', { pageNum: 1, pageSize: 1, status: 'CREATED' }); stats.value[2].value = (r as any).data?.total || 0 } catch {}
  try { const r = await api('/outbound/ships/page', { pageNum: 1, pageSize: 1, status: 'CREATED' }); stats.value[3].value = (r as any).data?.total || 0 } catch {}
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { font-size: 14px; color: #999; margin-top: 4px; }
</style>
