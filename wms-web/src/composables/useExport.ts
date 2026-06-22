import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

export function useExport() {
  async function exportExcel(url: string, params: Record<string, any> = {}, filename: string = 'export') {
    try {
      const token = getToken()
      const resp = await fetch(`/api/v1${url}`, {method:'POST',headers:{'Content-Type':'application/json',Authorization:`Bearer ${token}`},body:JSON.stringify(params||{})})
      if(!resp.ok) throw new Error('Export failed')
      const blob=await resp.blob()
      const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download=`${filename}.xlsx`;a.click();URL.revokeObjectURL(a.href)
      ElMessage.success('导出成功')
    }catch{ElMessage.error('导出失败')}
  }
  return { exportExcel }
}
