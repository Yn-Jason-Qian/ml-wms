<template>
  <span style="display:inline-flex;gap:8px">
    <el-button @click="downloadTemplate(type)" size="default">下载模板</el-button>
    <el-upload :auto-upload="false" :on-change="handleFile" accept=".xlsx" :show-file-list="false">
      <el-button type="warning" size="default">批量导入</el-button>
    </el-upload>
    <el-dialog v-model="previewVisible" title="导入确认" width="400px">
      <p>已选择文件: <strong>{{ fileName }}</strong></p>
      <el-button type="primary" @click="doImport" :loading="importing">确认导入</el-button>
    </el-dialog>
  </span>
</template>
<script setup lang="ts">
import { ref } from 'vue'; import { ElMessage } from 'element-plus'; import { downloadTemplate, importData } from '@/api/import'
const props=defineProps<{type:string}>();const emit=defineEmits(['done'])
const previewVisible=ref(false);const fileName=ref('');const importing=ref(false);let pendingFile:File|null=null
function handleFile(file:any){pendingFile=file.raw;fileName.value=file.name;previewVisible.value=true}
async function doImport(){if(!pendingFile)return;importing.value=true;try{const res=await importData(props.type,pendingFile);ElMessage.success(`成功导入 ${(res as any).data} 条数据`);previewVisible.value=false;emit('done')}catch{}finally{importing.value=false}}
</script>
