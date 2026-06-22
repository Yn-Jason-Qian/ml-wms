<template>
  <div class="page-container"><el-card><template #header><div class="page-header"><span>收货管理</span><el-button type="primary" @click="openDialog()">新建收货</el-button></div></template>
    <el-table :data="tableData" v-loading="loading" border stripe size="small">
      <el-table-column prop="receiveNo" label="收货单号" width="180"/><el-table-column prop="receiveType" label="类型" width="80"/>
      <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag size="small" :type="row.status==='DONE'?'success':'info'">{{row.status==='DONE'?'已完成':row.status}}</el-tag></template></el-table-column>
      <el-table-column prop="createdAt" label="时间" width="160"/>
    </el-table>
    <div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新建收货" width="550px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" style="width:100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.whName" :value="w.id"/></el-select></el-form-item>
      <el-form-item label="货主" prop="ownerId"><el-select v-model="form.ownerId" style="width:100%"><el-option v-for="o in owners" :key="o.id" :label="o.ownerName" :value="o.id"/></el-select></el-form-item>
      <el-form-item label="收货方式"><el-radio-group v-model="form.receiveType"><el-radio value="ASN">ASN收货</el-radio><el-radio value="BLIND">盲收</el-radio></el-radio-group></el-form-item>
      <el-form-item v-if="form.receiveType==='ASN'" label="ASN单号"><el-input v-model="form.asnHeaderId" placeholder="ASN ID"/></el-form-item>
      <el-form-item label="SKU" prop="skuId"><el-select v-model="form.skuId" filterable style="width:100%"><el-option v-for="s in skuList" :key="s.id" :label="s.skuCode+' '+s.skuName" :value="s.id"/></el-select></el-form-item>
      <el-form-item label="数量" prop="receiveQty"><el-input-number v-model="form.receiveQty" :min="1" style="width:100%"/></el-form-item>
      <el-form-item label="收货库位" prop="receiveLocationId"><el-input v-model="form.receiveLocationId" placeholder="库位ID"/></el-form-item>
      <el-form-item label="批次号"><el-input v-model="form.batchNo"/></el-form-item>
      <el-form-item label="生产日期"><el-date-picker v-model="form.productionDate" type="date" style="width:100%"/></el-form-item>
      <el-form-item label="失效日期"><el-date-picker v-model="form.expiryDate" type="date" style="width:100%"/></el-form-item>
    </el-form>
    <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">收货</el-button></template>
  </el-dialog></div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarehouseList, getOwnerList, getSkuPage } from '@/api/modules/masterdata'
import { pageReceives, createReceive } from '@/api/modules/operations'

const loading=ref(false),saving=ref(false),dialogVisible=ref(false),tableData=ref<any[]>([]),warehouses=ref<any[]>([]),owners=ref<any[]>([]),skuList=ref<any[]>([]),total=ref(0),formRef=ref()
const query=reactive({pageNum:1,pageSize:20})
const form=reactive({warehouseId:null as any,ownerId:null as any,receiveType:'ASN',asnHeaderId:'',skuId:null as any,receiveQty:1,receiveLocationId:'',batchNo:'',productionDate:null as any,expiryDate:null as any})
const rules={warehouseId:[{required:true,message:'必选'}],ownerId:[{required:true}],skuId:[{required:true}],receiveQty:[{required:true}],receiveLocationId:[{required:true}]}

async function fetchData(){loading.value=true;try{const r=await pageReceives(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
async function loadOpts(){try{warehouses.value=(await getWarehouseList()).data||[]}catch{};try{owners.value=(await getOwnerList()).data||[]}catch{};try{skuList.value=(await getSkuPage({pageNum:1,pageSize:200})).data.records||[]}catch{}}
function openDialog(){Object.keys(form).forEach(k=>{if(k!=='receiveType'){(form as any)[k]=k==='receiveQty'?1:''}});form.receiveType='ASN';dialogVisible.value=true}
async function handleSave(){if(!await formRef.value?.validate().catch(()=>false))return;saving.value=true;try{await createReceive(form);ElMessage.success('收货成功');dialogVisible.value=false;fetchData()}finally{saving.value=false}}

fetchData();loadOpts()
</script>
