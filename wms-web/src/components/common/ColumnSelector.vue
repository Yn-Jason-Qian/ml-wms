<template>
  <el-popover trigger="click" :width="200" placement="bottom-end">
    <template #reference><el-button link><el-icon><Setting/></el-icon></el-button></template>
    <div><el-checkbox v-for="col in localCols" :key="col.prop" v-model="col.visible" :label="col.label" @change="save"/><el-divider style="margin:8px 0"/><el-button size="small" link @click="reset">重置</el-button></div>
  </el-popover>
</template>
<script setup lang="ts">
import {ref,watchEffect} from 'vue';import {useRoute} from 'vue-router'
export interface ColDef {prop:string;label:string;visible:boolean}
const props=defineProps<{columns:ColDef[]}>();const emit=defineEmits<{(e:'update:columns',cols:ColDef[]):void}>()
const route=useRoute();const key=`tbl_cols_${route.path}`;const localCols=ref<ColDef[]>([])
watchEffect(()=>{const s=localStorage.getItem(key);localCols.value=s?props.columns.map(c=>{const sc=(JSON.parse(s)as ColDef[]).find(x=>x.prop===c.prop);return sc?{...c,visible:sc.visible}:c}):[...props.columns]})
function save(){localStorage.setItem(key,JSON.stringify(localCols.value));emit('update:columns',localCols.value)}
function reset(){localStorage.removeItem(key);localCols.value=[...props.columns];emit('update:columns',localCols.value)}
</script>
