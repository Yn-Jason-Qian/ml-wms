<template><div class="page-container"><el-card><template #header><div class="page-header"><span>发货管理</span><el-button type="primary" @click="openDlg()">创建发货</el-button></div></template>
  <el-table :data="tableData" v-loading="loading" border stripe size="small">
    <el-table-column prop="shipNo" label="发货单号" width="180"/><el-table-column prop="deliveryMethod" label="发货方式" width="100"/><el-table-column prop="carrierName" label="承运商" width="120"/><el-table-column prop="trackingNo" label="运单号" width="150"/><el-table-column prop="status" label="状态" width="80"/><el-table-column prop="createdAt" label="时间" width="160"/>
  </el-table><div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div></el-card>

  <el-dialog v-model="dialogVisible" title="创建发货" width="500px"><el-form label-width="100px"><el-form-item label="仓库"><el-input-number v-model="form.warehouseId" style="width:100%"/></el-form-item><el-form-item label="货主"><el-input-number v-model="form.ownerId" style="width:100%"/></el-form-item><el-form-item label="波次ID" required><el-input v-model="form.waveHeaderId"/></el-form-item><el-form-item label="发货方式"><el-select v-model="form.deliveryMethod" style="width:100%"><el-option label="快递" value="EXPRESS"/><el-option label="零担" value="LTL"/><el-option label="整车" value="FTL"/></el-select></el-form-item><el-form-item label="承运商"><el-input v-model="form.carrierName"/></el-form-item><el-form-item label="运单号"><el-input v-model="form.trackingNo"/></el-form-item><el-form-item label="件数"><el-input-number v-model="form.packageCount" :min="1" style="width:100%"/></el-form-item><el-form-item label="毛重(kg)"><el-input-number v-model="form.grossWeight" :min="0" :precision="2" style="width:100%"/></el-form-item></el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">发货</el-button></template></el-dialog></div>
</template>

<script setup lang="ts">
import {ref,reactive} from 'vue';import {ElMessage} from 'element-plus';import {pageShips,createShip} from '@/api/outbound'
const loading=ref(false),saving=ref(false),dialogVisible=ref(false),tableData=ref<any[]>([]),total=ref(0)
const query=reactive({pageNum:1,pageSize:20})
const form=reactive({warehouseId:1,ownerId:1,waveHeaderId:'',deliveryMethod:'EXPRESS',carrierName:'',trackingNo:'',packageCount:1,grossWeight:0,volume:0})

async function fetchData(){loading.value=true;try{const r=await pageShips(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
function openDlg(){Object.assign(form,{warehouseId:1,ownerId:1,waveHeaderId:'',deliveryMethod:'EXPRESS',carrierName:'',trackingNo:'',packageCount:1,grossWeight:0,volume:0});dialogVisible.value=true}
async function handleSave(){saving.value=true;try{await createShip(form);ElMessage.success('发货完成');dialogVisible.value=false;fetchData()}finally{saving.value=false}}
fetchData()
</script>
