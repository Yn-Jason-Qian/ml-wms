<template><div class="page-container"><el-card><template #header><div class="page-header"><span>移库管理</span><el-button type="primary" @click="openDlg()">新建移库</el-button></div></template>
  <el-table :data="tableData" v-loading="loading" border stripe size="small">
    <el-table-column prop="moveNo" label="移库单号" width="180"/><el-table-column prop="moveType" label="类型" width="80"/><el-table-column prop="status" label="状态" width="80"/><el-table-column prop="createdAt" label="时间" width="160"/>
  </el-table><div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div></el-card>

  <el-dialog v-model="dialogVisible" title="新建移库" width="500px" destroy-on-close><el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
    <el-form-item label="仓库" prop="warehouseId"><el-input-number v-model="form.warehouseId" style="width:100%"/></el-form-item>
    <el-form-item label="移库类型"><el-select v-model="form.moveType" style="width:100%"><el-option label="手动移库" value="MANUAL"/><el-option label="补货" value="REPLENISH"/></el-select></el-form-item>
    <el-form-item label="SKU" prop="skuId"><el-input-number v-model="form.skuId" style="width:100%"/></el-form-item>
    <el-form-item label="数量" prop="moveQty"><el-input-number v-model="form.moveQty" :min="1" style="width:100%"/></el-form-item>
    <el-form-item label="来源库位" prop="fromLocationId"><el-input v-model="form.fromLocationId" placeholder="库位ID"/></el-form-item>
    <el-form-item label="目标库位" prop="toLocationId"><el-input v-model="form.toLocationId" placeholder="库位ID"/></el-form-item>
    <el-form-item label="批次号"><el-input v-model="form.batchNo"/></el-form-item>
    <el-form-item label="备注"><el-input v-model="form.remark"/></el-form-item>
  </el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">移库</el-button></template></el-dialog></div>
</template>

<script setup lang="ts">
import {ref,reactive} from 'vue';import {ElMessage} from 'element-plus';import {pageMoves,createMove} from '@/api/inventory'
const loading=ref(false),saving=ref(false),dialogVisible=ref(false),tableData=ref<any[]>([]),total=ref(0),formRef=ref()
const query=reactive({pageNum:1,pageSize:20})
const form=reactive({warehouseId:1,moveType:'MANUAL',skuId:null as any,moveQty:1,fromLocationId:'',toLocationId:'',batchNo:'',remark:''})
const rules={warehouseId:[{required:true}],moveType:[{required:true}],skuId:[{required:true}],moveQty:[{required:true}],fromLocationId:[{required:true}],toLocationId:[{required:true}]}

async function fetchData(){loading.value=true;try{const r=await pageMoves(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
function openDlg(){Object.keys(form).forEach(k=>{(form as any)[k]=k==='warehouseId'?1:k==='moveType'?'MANUAL':k==='moveQty'?1:''});dialogVisible.value=true}
async function handleSave(){if(!await formRef.value?.validate().catch(()=>false))return;saving.value=true;try{await createMove(form);ElMessage.success('移库完成');dialogVisible.value=false;fetchData()}finally{saving.value=false}}
fetchData()
</script>
