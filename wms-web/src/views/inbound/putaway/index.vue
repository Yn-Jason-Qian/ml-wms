<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header"><span>上架管理</span><el-button type="primary" @click="openGenDlg()">生成上架单</el-button></div>
      </template>
      <el-form :model="query" inline class="query-form">
        <el-form-item label="仓库"><el-select v-model="query.warehouseId" placeholder="仓库" clearable style="width:160px"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id" /></el-select></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="putawayNo" label="上架单号" width="180" />
        <el-table-column prop="warehouseName" label="仓库" width="140" />
        <el-table-column prop="ownerName" label="货主" width="140" />
        <el-table-column prop="receiveNo" label="收货单号" width="180" />
        <el-table-column label="SKU" width="180">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].skuCode }} {{ row.lines[0].skuName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上架数量" width="90">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].putawayQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="目标库位" width="100">
          <template #default="{ row }">
            <span v-if="row.lines && row.lines.length">{{ row.lines[0].locationName || row.lines[0].toLocationId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.status==='DONE'?'success':row.status==='PARTIAL_DONE'?'warning':'info'">{{ statusMap[row.status] || row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row.id)">详情</el-button>
            <el-button v-if="row.status!=='DONE'" link type="primary" size="small" @click="openConfirmDlg(row)">确认上架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="上架详情" width="850px" destroy-on-close>
      <div v-if="detailPw">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="上架单号">{{ detailPw.putawayNo }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag size="small" :type="detailPw.status==='DONE'?'success':detailPw.status==='PARTIAL_DONE'?'warning':'info'">{{ statusMap[detailPw.status] }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="仓库">{{ detailPw.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="货主">{{ detailPw.ownerName }}</el-descriptions-item>
          <el-descriptions-item label="收货单号">{{ detailPw.receiveNo }}</el-descriptions-item>
          <el-descriptions-item label="策略ID">{{ detailPw.strategyId }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detailPw.remark }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailPw.createdAt }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detailPw.lines" border size="small" style="margin-top:12px">
          <el-table-column prop="lineNo" label="#" width="50" />
          <el-table-column prop="skuCode" label="SKU编码" width="130" />
          <el-table-column prop="skuName" label="SKU名称" />
          <el-table-column prop="putawayQty" label="上架数量" width="90" />
          <el-table-column prop="doneQty" label="已上架" width="80" />
          <el-table-column label="目标库位" width="100">
            <template #default="{ row }"><span>{{ row.locationName || row.toLocationId }}</span></template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次号" width="120" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }"><el-tag size="small" :type="row.status==='DONE'?'success':'info'">{{ row.status }}</el-tag></template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 生成上架单弹窗 -->
    <el-dialog v-model="genVisible" title="生成上架单" width="400px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="仓库"><el-input-number v-model="genForm.warehouseId" style="width:100%" /></el-form-item>
        <el-form-item label="收货单ID" required><el-input v-model="genForm.receiveHeaderId" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="genVisible=false">取消</el-button><el-button type="primary" @click="saveGen" :loading="saving">生成</el-button></template>
    </el-dialog>

    <!-- 上架确认弹窗 -->
    <el-dialog v-model="confirmVisible" title="上架确认" width="400px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="上架单ID"><el-input v-model="confirmForm.putawayHeaderId" disabled /></el-form-item>
        <el-form-item label="行ID"><el-input v-model="confirmForm.putawayLineId" /></el-form-item>
        <el-form-item label="目标库位"><el-input v-model="confirmForm.toLocationId" placeholder="库位ID" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="confirmVisible=false">取消</el-button><el-button type="primary" @click="saveConfirm" :loading="saving">确认上架</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarehouseList, getOwnerList, getLocationPage } from '@/api/modules/masterdata'
import { pagePutaways, createPutaway, submitPutaway } from '@/api/modules/operations'
import request from '@/api/request'

const statusMap: Record<string, string> = { CREATED: '已创建', PUTAWAYING: '上架中', PARTIAL_DONE: '部分完成', DONE: '已完成' }

const loading = ref(false), saving = ref(false)
const genVisible = ref(false), confirmVisible = ref(false), detailVisible = ref(false)
const tableData = ref<any[]>([]), warehouses = ref<any[]>([]), owners = ref<any[]>([]), locations = ref<any[]>([])
const total = ref(0), pageNum = ref(1), pageSize = ref(10)
const detailPw = ref<any>(null)

const query = reactive({ warehouseId: null as any })
const genForm = reactive({ warehouseId: 1, receiveHeaderId: '' })
const confirmForm = reactive({ putawayHeaderId: null as any, putawayLineId: '', toLocationId: '' })

const warehouseMap = computed(() => Object.fromEntries(warehouses.value.map(w => [w.id, w.whName])))
const ownerMap = computed(() => Object.fromEntries(owners.value.map(o => [o.id, o.ownerName])))
const locationMap = computed(() => Object.fromEntries(locations.value.map((l: any) => [l.id, l.locationCode || l.locCode || l.id])))

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.warehouseId) params.warehouseId = query.warehouseId
    const res = await pagePutaways(params)
    const records = (res.data.records || []).map((r: any) => ({
      ...r,
      warehouseName: warehouseMap.value[r.warehouseId] || '',
      ownerName: ownerMap.value[r.ownerId] || '',
      lines: (r.lines || []).map((l: any) => ({
        ...l,
        locationName: locationMap.value[l.toLocationId] || l.toLocationId
      }))
    }))
    tableData.value = records
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchData() }
function resetQuery() { query.warehouseId = null; pageNum.value = 1; fetchData() }

async function loadOpts() {
  try { warehouses.value = (await getWarehouseList()).data || [] } catch {}
  try { owners.value = (await getOwnerList()).data || [] } catch {}
  try { locations.value = (await getLocationPage({ pageNum: 1, pageSize: 200 })).data.records || [] } catch {}
}

function openGenDlg() { genForm.receiveHeaderId = ''; genVisible.value = true }
function openConfirmDlg(row: any) { confirmForm.putawayHeaderId = row.id; confirmForm.putawayLineId = ''; confirmForm.toLocationId = ''; confirmVisible.value = true }

async function saveGen() { saving.value = true; try { await createPutaway(genForm); ElMessage.success('上架单已生成'); genVisible.value = false; fetchData() } finally { saving.value = false } }
async function saveConfirm() { saving.value = true; try { await submitPutaway(confirmForm); ElMessage.success('上架确认完成'); confirmVisible.value = false; fetchData() } finally { saving.value = false } }

async function viewDetail(id: number) {
  const res = await request.get(`/inbound/putaways/${id}`)
  detailPw.value = {
    ...res.data,
    warehouseName: warehouseMap.value[res.data.warehouseId] || '',
    ownerName: ownerMap.value[res.data.ownerId] || '',
    lines: (res.data.lines || []).map((l: any) => ({
      ...l,
      locationName: locationMap.value[l.toLocationId] || l.toLocationId
    }))
  }
  detailVisible.value = true
}

loadOpts().then(() => fetchData())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.query-form { margin-bottom: 12px; }
.pagination { margin-top: 12px; justify-content: flex-end; }
</style>
