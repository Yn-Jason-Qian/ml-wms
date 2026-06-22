<template><div class="page-container"><el-card><template #header><div class="page-header"><span>质检管理</span><el-button type="primary" @click="openDlg('qc')">新建质检</el-button></div></template>
  <el-table :data="tableData" v-loading="loading" border stripe size="small">
    <el-table-column prop="qcNo" label="质检单号" width="180"/><el-table-column prop="qcType" label="类型" width="80"><template #default="{row}"><el-tag size="small">{{qcTypeMap[row.qcType]||row.qcType}}</el-tag></template></el-table-column>
    <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag size="small" :type="row.status==='PASS'?'success':row.status==='REJECT'?'danger':'info'">{{row.status}}</el-tag></template></el-table-column>
    <el-table-column prop="createdAt" label="时间" width="160"/><el-table-column label="操作" width="120"><template #default="{row}"><el-button link type="primary" size="small" @click="openDlg('submit',row)">提交结果</el-button></template></el-table-column>
  </el-table><div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div></el-card>

  <el-dialog v-model="qcVisible" title="新建质检" width="450px" destroy-on-close><el-form ref="qcFormRef" :model="qcForm" label-width="100px">
    <el-form-item label="仓库"><el-input-number v-model="qcForm.warehouseId" style="width:100%"/></el-form-item>
    <el-form-item label="收货单ID" required><el-input v-model="qcForm.receiveHeaderId"/></el-form-item>
    <el-form-item label="质检类型"><el-select v-model="qcForm.qcType" style="width:100%"><el-option v-for="(v,k) in qcTypeMap" :key="k" :label="v" :value="k"/></el-select></el-form-item>
  </el-form><template #footer><el-button @click="qcVisible=false">取消</el-button><el-button type="primary" @click="saveQc" :loading="saving">创建</el-button></template></el-dialog>

  <el-dialog v-model="submitVisible" title="提交质检结果" width="450px" destroy-on-close><el-form ref="submitFormRef" :model="submitForm" label-width="100px">
    <el-form-item label="质检单ID"><el-input v-model="submitForm.headerId" disabled/></el-form-item>
    <el-form-item label="SKU ID"><el-input v-model="submitForm.skuId"/></el-form-item>
    <el-form-item label="检验数"><el-input-number v-model="submitForm.inspectQty" :min="0" style="width:100%"/></el-form-item>
    <el-form-item label="合格数"><el-input-number v-model="submitForm.passQty" :min="0" style="width:100%"/></el-form-item>
    <el-form-item label="不合格数"><el-input-number v-model="submitForm.rejectQty" :min="0" style="width:100%"/></el-form-item>
    <el-form-item label="不合格原因"><el-input v-model="submitForm.rejectReason"/></el-form-item>
  </el-form><template #footer><el-button @click="submitVisible=false">取消</el-button><el-button type="primary" @click="saveSubmit" :loading="saving">提交</el-button></template></el-dialog></div>
</template>

<script setup lang="ts">
import {ref,reactive} from 'vue';import {ElMessage} from 'element-plus';import {pageQcs,createQc,submitQc} from '@/api/modules/operations'
const qcTypeMap:Record<string,string>={FULL:'全检',SAMPLE:'抽检',NONE:'免检'}
const loading=ref(false),saving=ref(false),qcVisible=ref(false),submitVisible=ref(false),tableData=ref<any[]>([]),total=ref(0)
const query=reactive({pageNum:1,pageSize:20})
const qcForm=reactive({warehouseId:1,receiveHeaderId:'',qcType:'FULL'})
const submitForm=reactive({headerId:null as any,skuId:'',inspectQty:0,passQty:0,rejectQty:0,rejectReason:''})

async function fetchData(){loading.value=true;try{const r=await pageQcs(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
function openDlg(type:string,row?:any){if(type==='qc'){qcForm.receiveHeaderId='';qcForm.warehouseId=1;qcVisible.value=true}else{submitForm.headerId=row.id;submitForm.skuId='';submitForm.inspectQty=0;submitForm.passQty=0;submitForm.rejectQty=0;submitForm.rejectReason='';submitVisible.value=true}}
async function saveQc(){saving.value=true;try{await createQc(qcForm);ElMessage.success('创建成功');qcVisible.value=false;fetchData()}finally{saving.value=false}}
async function saveSubmit(){saving.value=true;try{await submitQc(submitForm);ElMessage.success('提交成功');submitVisible.value=false;fetchData()}finally{saving.value=false}}
fetchData()
</script>
