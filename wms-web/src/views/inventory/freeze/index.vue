<template><div class="page-container"><el-card><template #header><div class="page-header"><span>冻结管理</span><el-button type="primary" @click="openDlg()">新建冻结</el-button></div></template>
  <el-table :data="tableData" v-loading="loading" border stripe size="small">
    <el-table-column prop="freezeType" label="类型" width="80"/><el-table-column prop="freezeQty" label="冻结数量" width="90"/><el-table-column prop="reason" label="原因" min-width="120"/>
    <el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag size="small" :type="row.status==='ACTIVE'?'danger':'success'">{{row.status==='ACTIVE'?'冻结中':'已解冻'}}</el-tag></template></el-table-column>
    <el-table-column prop="freezeAt" label="冻结时间" width="160"/><el-table-column label="操作" width="80"><template #default="{row}"><el-button v-if="row.status==='ACTIVE'" link type="danger" size="small" @click="handleRelease(row.id)">解冻</el-button></template></el-table-column>
  </el-table><div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div></el-card>

  <el-dialog v-model="dialogVisible" title="新建冻结" width="450px"><el-form label-width="100px">
    <el-form-item label="仓库"><el-input-number v-model="form.warehouseId" style="width:100%"/></el-form-item>
    <el-form-item label="冻结类型" required><el-select v-model="form.freezeType" style="width:100%"><el-option label="手动冻结" value="MANUAL"/><el-option label="效期冻结" value="LOT_EXPIRY"/><el-option label="质检冻结" value="QC_HOLD"/></el-select></el-form-item>
    <el-form-item label="库存ID"><el-input v-model="form.stockId"/></el-form-item>
    <el-form-item label="SKU ID"><el-input v-model="form.skuId"/></el-form-item>
    <el-form-item label="冻结数量" required><el-input-number v-model="form.freezeQty" :min="1" style="width:100%"/></el-form-item>
    <el-form-item label="原因" required><el-input v-model="form.reason"/></el-form-item>
  </el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">冻结</el-button></template></el-dialog></div>
</template>

<script setup lang="ts">
import {ref,reactive} from 'vue';import {ElMessage,ElMessageBox} from 'element-plus';import {pageFreezes,createFreeze,releaseFreeze} from '@/api/inventory'
const loading=ref(false),saving=ref(false),dialogVisible=ref(false),tableData=ref<any[]>([]),total=ref(0)
const query=reactive({pageNum:1,pageSize:20})
const form=reactive({warehouseId:1,freezeType:'MANUAL',stockId:'',skuId:'',freezeQty:1,reason:''})

async function fetchData(){loading.value=true;try{const r=await pageFreezes(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
function openDlg(){Object.assign(form,{warehouseId:1,freezeType:'MANUAL',stockId:'',skuId:'',freezeQty:1,reason:''});dialogVisible.value=true}
async function handleSave(){saving.value=true;try{await createFreeze(form);ElMessage.success('冻结完成');dialogVisible.value=false;fetchData()}finally{saving.value=false}}
async function handleRelease(id:number){await ElMessageBox.confirm('确定解冻？','提示',{type:'warning'});try{await releaseFreeze(id);ElMessage.success('已解冻');fetchData()}catch{}}
fetchData()
</script>
