<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>拣货管理</span>
      </template>

      <!-- 查询表单 -->
      <el-form :model="query" inline class="query-form">
        <el-form-item label="仓库">
          <el-select v-model="query.warehouseId" clearable placeholder="全部" style="width:160px">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px">
            <el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="pickNo" label="拣货单号" width="200" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }"><el-tag size="small">{{ typeMap[row.pickType] || row.pickType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusColor(row.status)" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createdAt?.substring(0, 19) || '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDlg(row)">拣货确认</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 拣货确认弹窗 -->
    <el-dialog v-model="dialogVisible" title="拣货确认" width="800px" destroy-on-close>
      <div v-if="detail" style="margin-bottom:12px">
        <span style="font-weight:bold">{{ detail.pickNo }}</span>
        <el-tag size="small" style="margin-left:8px">{{ typeMap[detail.pickType] || detail.pickType }}</el-tag>
      </div>
      <el-table :data="detail?.lines || []" v-loading="detailLoading" border size="small" max-height="360">
        <el-table-column prop="lineNo" label="#" width="50" />
        <el-table-column prop="skuCode" label="SKU" width="130" />
        <el-table-column prop="skuName" label="名称" min-width="100" />
        <el-table-column prop="pickQty" label="应拣" width="70" />
        <el-table-column label="状态" width="80">
          <template #default="{ row: ln }">
            <el-tag :type="ln.status === 'PICKED' ? 'success' : 'info'" size="small">{{ lineStatusMap[ln.status] || ln.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="本次拣货数" width="110">
          <template #default="{ row: ln }">
            <el-input-number v-model="ln._qty" :min="0" size="small" controls-position="right" style="width:100px" :disabled="ln.status === 'PICKED'" />
          </template>
        </el-table-column>
        <el-table-column label="容器号" width="130">
          <template #default="{ row: ln }">
            <el-input v-model="ln._container" size="small" style="width:120px" placeholder="周转箱" :disabled="ln.status === 'PICKED'" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row: ln, $index }">
            <el-button link type="primary" size="small" :loading="ln._saving" :disabled="ln.status === 'PICKED'" @click="handleLineSave(ln, $index)">确认</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarehouseList } from '@/api/masterdata'
import { pagePicks, submitPick, getPick } from '@/api/outbound'

const typeMap: Record<string, string> = { PIECE: '按件', PALLET: '按托', PAPER: '按单', RF: 'RF' }
const statusMap: Record<string, string> = { CREATED: '已创建', ASSIGNED: '已分配', PICKING: '拣货中', PICKED: '已拣货', DONE: '已完成', CANCELLED: '已取消' }
const lineStatusMap: Record<string, string> = { CREATED: '待拣', PICKING: '拣货中', PICKED: '已拣' }
const statusColor = (s: string) => s === 'PICKED' || s === 'DONE' ? 'success' : s === 'PICKING' ? 'warning' : s === 'CANCELLED' ? 'danger' : 'info'

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), detailLoading = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([])
const total = ref(0), detail = ref<any>(null)

const query = reactive({ pageNum: 1, pageSize: 10, warehouseId: null as any, status: null as any })

const warehouseMap = computed(() => Object.fromEntries(warehouses.value.map((w: any) => [w.id, w.whName])))

async function loadOpts() {
  try { warehouses.value = (await getWarehouseList()).data || [] } catch {}
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.warehouseId) params.warehouseId = query.warehouseId
    if (query.status) params.status = query.status
    const r = await pagePicks(params)
    tableData.value = (r.data.records || []).map((v: any) => ({
      ...v,
      warehouseName: v.warehouseId ? (warehouseMap.value[v.warehouseId] || '') : ''
    }))
    total.value = r.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { query.pageNum = 1; fetchData() }
function resetQuery() { query.warehouseId = null; query.status = null; query.pageNum = 1; fetchData() }

async function openDlg(row: any) {
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const res = await getPick(row.id)
    detail.value = res.data
    // add temp fields for each line
    if (detail.value?.lines) {
      detail.value.lines.forEach((ln: any) => {
        ln._qty = ln.pickQty - (ln.pickedQty || 0)
        ln._container = ''
        ln._saving = false
      })
    }
  } finally { detailLoading.value = false }
}

async function handleLineSave(ln: any, idx: number) {
  if (!ln._qty || ln._qty <= 0) { ElMessage.warning('请输入拣货数量'); return }
  ln._saving = true
  try {
    await submitPick({
      pickHeaderId: detail.value.id,
      pickLineId: ln.id,
      pickedQty: ln._qty,
      toContainer: ln._container || ''
    })
    ElMessage.success(`第${ln.lineNo}行拣货确认完成`)
    // update local state
    ln.status = 'PICKED'
    ln.pickedQty = (ln.pickedQty || 0) + ln._qty
    ln._qty = 0
    // refresh list in background
    fetchData()
  } catch { /* global handler */ }
  finally { ln._saving = false }
}

loadOpts().then(() => fetchData())
</script>

<style scoped>
.query-form { margin-bottom: 12px; }
.pagination { margin-top: 12px; justify-content: flex-end; }
</style>
