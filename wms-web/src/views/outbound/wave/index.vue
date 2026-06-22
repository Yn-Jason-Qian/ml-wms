<template><div class="page-container"><el-card><template #header><div class="page-header"><span>波次管理</span><el-button type="primary" @click="openDlg()">创建波次</el-button></div></template>
  <el-table :data="tableData" v-loading="loading" border stripe size="small">
    <el-table-column prop="waveNo" label="波次号" width="180"/><el-table-column prop="waveType" label="类型" width="100"/><el-table-column prop="waveStatus" label="状态" width="90"><template #default="{row}"><el-tag size="small" :type="row.waveStatus==='RELEASED'?'success':'info'">{{row.waveStatus}}</el-tag></template></el-table-column>
    <el-table-column prop="orderCount" label="订单数" width="80"/><el-table-column prop="createdAt" label="时间" width="160"/>
    <el-table-column label="操作" width="120"><template #default="{row}"><el-button link type="primary" size="small" @click="genPick(row.id)">生成拣货单</el-button></template></el-table-column>
  </el-table><div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div></el-card>

  <el-dialog v-model="dialogVisible" title="创建波次" width="450px"><el-form label-width="100px"><el-form-item label="仓库"><el-input-number v-model="form.warehouseId" style="width:100%"/></el-form-item><el-form-item label="波次类型"><el-select v-model="form.waveType" style="width:100%"><el-option label="订单池" value="ORDER_POOL"/><el-option label="优先级" value="PRIORITY"/></el-select></el-form-item><el-form-item label="订单ID"><el-input v-model="form.orderIdsStr" placeholder="逗号分隔,如 1,2,3"/></el-form-item></el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">创建</el-button></template></el-dialog></div>
</template>

<script setup lang="ts">
import {ref,reactive} from 'vue';import {ElMessage} from 'element-plus';import {pageWaves,createWave,createPickFromWave} from '@/api/modules/operations'
const loading=ref(false),saving=ref(false),dialogVisible=ref(false),tableData=ref<any[]>([]),total=ref(0)
const query=reactive({pageNum:1,pageSize:20})
const form=reactive({warehouseId:1,waveType:'ORDER_POOL',orderIdsStr:''})

async function fetchData(){loading.value=true;try{const r=await pageWaves(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
function openDlg(){form.orderIdsStr='';form.waveType='ORDER_POOL';dialogVisible.value=true}
async function handleSave(){saving.value=true;try{const ids=form.orderIdsStr.split(',').map(s=>parseInt(s.trim())).filter(n=>!isNaN(n));await createWave({warehouseId:form.warehouseId,waveType:form.waveType,orderIds:ids});ElMessage.success('波次已创建');dialogVisible.value=false;fetchData()}finally{saving.value=false}}
async function genPick(waveId:number){saving.value=true;try{await createPickFromWave(waveId);ElMessage.success('拣货单已生成')}finally{saving.value=false}}
fetchData()
</script>
