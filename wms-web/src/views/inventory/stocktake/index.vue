<template><div class="page-container"><el-card><template #header><div class="page-header"><span>盘点管理</span><el-button type="primary" @click="openDlg()">新建盘点</el-button></div></template>
  <el-table :data="tableData" v-loading="loading" border stripe size="small">
    <el-table-column prop="stocktakeNo" label="盘点单号" width="180"/><el-table-column prop="stocktakeType" label="类型" width="80"/>
    <el-table-column prop="stocktakeMode" label="模式" width="80"><template #default="{row}"><el-tag size="small" :type="row.stocktakeMode==='BLIND'?'danger':'success'">{{row.stocktakeMode==='BLIND'?'盲盘':'明盘'}}</el-tag></template></el-table-column>
    <el-table-column prop="status" label="状态" width="90"/><el-table-column prop="totalLines" label="行数" width="70"/><el-table-column prop="diffLines" label="差异" width="70"/><el-table-column prop="createdAt" label="时间" width="160"/>
  </el-table><div style="margin-top:12px;text-align:right"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" @current-change="fetchData" layout="total,prev,pager,next"/></div></el-card>

  <el-dialog v-model="dialogVisible" title="新建盘点" width="500px"><el-form label-width="100px">
    <el-form-item label="仓库"><el-input-number v-model="form.warehouseId" style="width:100%"/></el-form-item>
    <el-form-item label="盘点类型"><el-select v-model="form.stocktakeType" style="width:100%"><el-option label="全盘" value="FULL"/><el-option label="区盘" value="AREA"/><el-option label="SKU盘点" value="SKU"/></el-select></el-form-item>
    <el-form-item label="盘点模式"><el-radio-group v-model="form.stocktakeMode"><el-radio value="PLAN">明盘</el-radio><el-radio value="BLIND">盲盘</el-radio></el-radio-group></el-form-item>
    <el-form-item label="库位范围"><el-input v-model="form.locationRange" placeholder="JSON,如 [&quot;A-01&quot;,&quot;A-02&quot;]"/></el-form-item>
    <el-form-item label="计划开始"><el-date-picker v-model="form.planStartTime" type="datetime" style="width:100%"/></el-form-item>
    <el-form-item label="计划结束"><el-date-picker v-model="form.planEndTime" type="datetime" style="width:100%"/></el-form-item>
  </el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">创建</el-button></template></el-dialog></div>
</template>

<script setup lang="ts">
import {ref,reactive} from 'vue';import {ElMessage} from 'element-plus';import {pageStocktakes,createStocktake} from '@/api/inventory'
const loading=ref(false),saving=ref(false),dialogVisible=ref(false),tableData=ref<any[]>([]),total=ref(0)
const query=reactive({pageNum:1,pageSize:20})
const form=reactive({warehouseId:1,stocktakeType:'FULL',stocktakeMode:'PLAN',locationRange:'',planStartTime:null as any,planEndTime:null as any})

async function fetchData(){loading.value=true;try{const r=await pageStocktakes(query);tableData.value=r.data.records||[];total.value=r.data.total||0}finally{loading.value=false}}
function openDlg(){Object.assign(form,{warehouseId:1,stocktakeType:'FULL',stocktakeMode:'PLAN',locationRange:'',planStartTime:null,planEndTime:null});dialogVisible.value=true}
async function handleSave(){saving.value=true;try{await createStocktake(form);ElMessage.success('盘点单已创建');dialogVisible.value=false;fetchData()}finally{saving.value=false}}
fetchData()
</script>
