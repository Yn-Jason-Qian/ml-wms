<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>报表中心</span>
      </template>
      <el-tabs v-model="tab">
        <!-- 库存报表 -->
        <el-tab-pane label="库存报表" name="inv">
          <el-row :gutter="16">
            <el-col :span="8" v-for="r in invReports" :key="r.key">
              <el-card shadow="hover" style="text-align: center">
                <el-icon :size="40" color="#409eff">
                  <Document />
                </el-icon>
                <h3 style="margin: 12px 0">{{ r.label }}</h3>
                <p style="color: #999; font-size: 13px; margin-bottom: 16px">{{ r.desc }}</p>
                <el-button
                  type="primary"
                  @click="doExport(r.url, r.key)"
                  :loading="loading === r.key"
                >
                  导出 Excel
                </el-button>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 作业报表 -->
        <el-tab-pane label="作业报表" name="ops">
          <el-row :gutter="16">
            <el-col :span="8" v-for="r in opsReports" :key="r.key">
              <el-card shadow="hover" style="text-align: center">
                <el-icon :size="40" color="#67c23a">
                  <Document />
                </el-icon>
                <h3 style="margin: 12px 0">{{ r.label }}</h3>
                <p style="color: #999; font-size: 13px; margin-bottom: 16px">{{ r.desc }}</p>
                <el-button
                  type="primary"
                  @click="doExport(r.url, r.key)"
                  :loading="loading === r.key"
                >
                  导出 Excel
                </el-button>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 单据报表 -->
        <el-tab-pane label="单据报表" name="docs">
          <el-row :gutter="16">
            <el-col :span="8" v-for="r in docReports" :key="r.key">
              <el-card shadow="hover" style="text-align: center">
                <el-icon :size="40" color="#e6a23c">
                  <Document />
                </el-icon>
                <h3 style="margin: 12px 0">{{ r.label }}</h3>
                <p style="color: #999; font-size: 13px; margin-bottom: 16px">{{ r.desc }}</p>
                <el-button
                  type="primary"
                  @click="doExport(r.url, r.key)"
                  :loading="loading === r.key"
                >
                  导出 Excel
                </el-button>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useExport } from '@/composables/useExport'

const { exportExcel } = useExport()

const tab = ref('inv')
const loading = ref<string>()

const invReports = [
  { key: 'summary', label: '收发存汇总', desc: 'SKU维度汇总在手/在途/可用', url: '/report/inventory-summary' },
  { key: 'aging', label: '库龄分析', desc: '库存按入库天数分区间统计', url: '/report/inventory-aging' },
  { key: 'expiry', label: '效期预警', desc: '临期库存明细(30天内到期)', url: '/report/inventory-expiry' },
]

const opsReports = [
  { key: 'receive', label: '收货明细', desc: '收货单行+批次信息', url: '/report/receive-detail' },
  { key: 'pick', label: '拣货明细', desc: '拣货单行+容器号', url: '/report/pick-detail' },
  { key: 'ship', label: '发货明细', desc: '发货单行+承运商/运单号', url: '/report/ship-detail' },
]

const docReports = [
  { key: 'asn', label: 'ASN明细', desc: 'ASN行+收货状态', url: '/report/asn-detail' },
  { key: 'order', label: '订单明细', desc: '订单行+分配/拣货/发货量', url: '/report/order-detail' },
  { key: 'diff', label: '盘点差异', desc: '账面vs实盘差异明细', url: '/report/stocktake-diff' },
]

async function doExport(url: string, key: string) {
  loading.value = key
  await exportExcel(url, {}, key)
  loading.value = undefined
}
</script>
