<template><div class="page-container"><el-card><template #header><div class="page-header"><span>上架管理</span><el-button type="primary" @click="openGenDlg()">生成上架单</el-button></div></template>
  <el-table :data="tableData" v-loading="loading" border stripe size="small">
    <el-table-column prop="putawayNo" label="上架单号" width="180"/><el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag size="small" :type="row.status==='DONE'?'success':'warning'">{{row.status}}</el-tag></template></el-table-column>
    <el-table-column prop="createdAt" label="时间" width="160"/><el-table-column label="操作" width="120"><template #default="{row}"><el-button v-if="row.status!=='DONE'" link type="primary" size="small" @click="openConfirmDlg(row)">确认上架</el-button></template></el-table-column>
  </el-table><div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div></el-card>

  <el-dialog v-model="genVisible" title="生成上架单" width="400px"><el-form label-width="100px"><el-form-item label="仓库"><el-input-number v-model="genForm.warehouseId" style="width:100%"/></el-form-item><el-form-item label="收货单ID" required><el-input v-model="genForm.receiveHeaderId"/></el-form-item></el-form><template #footer><el-button @click="genVisible=false">取消</el-button><el-button type="primary" @click="saveGen" :loading="saving">生成</el-button></template></el-dialog>

  <el-dialog v-model="confirmVisible" title="上架确认" width="400px"><el-form label-width="100px"><el-form-item label="上架单ID"><el-input v-model="confirmForm.putawayHeaderId" disabled/></el-form-item><el-form-item label="行ID"><el-input v-model="confirmForm.putawayLineId"/></el-form-item><el-form-item label="目标库位"><el-input v-model="confirmForm.toLocationId" placeholder="库位ID"/></el-form-item></el-form><template #footer><el-button @click="confirmVisible=false">取消</el-button><el-button type="primary" @click="saveConfirm" :loading="saving">确认上架</el-button></template></el-dialog></div>
</template>

<script setup lang="ts">
import {ref,reactive} from 'vue';import {ElMessage} from 'element-plus';import {pagePutaways,createPutaway,submitPutaway} from '@/api/modules/operations'
const loading=ref(false),saving=ref(false),genVisible=ref(false),confirmVisible=ref(false),tableData=ref<any[]>([]),total=ref(0)
const query=reactive({pageNum:1,pageSize:20})
const genForm=reactive({warehouseId:1,receiveHeaderId:''})
const confirmForm=reactive({putawayHeaderId:null as any,putawayLineId:'',toLocationId:''})

async function fetchData(){loading.value=true;try{const r=await pagePutaways(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
function openGenDlg(){genForm.receiveHeaderId='';genVisible.value=true}
function openConfirmDlg(row:any){confirmForm.putawayHeaderId=row.id;confirmForm.putawayLineId='';confirmForm.toLocationId='';confirmVisible.value=true}
async function saveGen(){saving.value=true;try{await createPutaway(genForm);ElMessage.success('上架单已生成');genVisible.value=false;fetchData()}finally{saving.value=false}}
async function saveConfirm(){saving.value=true;try{await submitPutaway(confirmForm);ElMessage.success('上架确认完成');confirmVisible.value=false;fetchData()}finally{saving.value=false}}
fetchData()
</script>
