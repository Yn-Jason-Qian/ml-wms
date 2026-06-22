<template><div class="page-container"><el-card><template #header><span>拣货管理</span></template>
  <el-table :data="tableData" v-loading="loading" border stripe size="small">
    <el-table-column prop="pickNo" label="拣货单号" width="180"/><el-table-column prop="pickType" label="类型" width="80"/><el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag size="small" :type="row.status==='PICKED'?'success':'warning'">{{row.status}}</el-tag></template></el-table-column>
    <el-table-column prop="createdAt" label="时间" width="160"/><el-table-column label="操作" width="120"><template #default="{row}"><el-button link type="primary" size="small" @click="openDlg(row)">拣货确认</el-button></template></el-table-column>
  </el-table><div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div></el-card>

  <el-dialog v-model="dialogVisible" title="拣货确认" width="400px"><el-form label-width="100px"><el-form-item label="拣货行ID"><el-input v-model="form.pickLineId"/></el-form-item><el-form-item label="拣货数量"><el-input-number v-model="form.pickedQty" :min="1" style="width:100%"/></el-form-item><el-form-item label="容器号"><el-input v-model="form.toContainer" placeholder="周转箱号"/></el-form-item></el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">确认</el-button></template></el-dialog></div>
</template>

<script setup lang="ts">
import {ref,reactive} from 'vue';import {ElMessage} from 'element-plus';import {pagePicks,submitPick} from '@/api/modules/operations'
const loading=ref(false),saving=ref(false),dialogVisible=ref(false),tableData=ref<any[]>([]),total=ref(0)
const query=reactive({pageNum:1,pageSize:20})
const form=reactive({pickHeaderId:null as any,pickLineId:'',pickedQty:1,toContainer:''})

async function fetchData(){loading.value=true;try{const r=await pagePicks(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
function openDlg(row:any){form.pickHeaderId=row.id;form.pickLineId='';form.pickedQty=1;form.toContainer='';dialogVisible.value=true}
async function handleSave(){saving.value=true;try{await submitPick(form);ElMessage.success('拣货确认完成');dialogVisible.value=false;fetchData()}finally{saving.value=false}}
fetchData()
</script>
