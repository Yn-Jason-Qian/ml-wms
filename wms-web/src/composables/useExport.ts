import request from '@/api/request'
import { ElMessage } from 'element-plus'

export function useExport() {
  async function exportExcel(url: string, params: Record<string, any> = {}, filename: string = 'export') {
    try {
      const resp = await request.post(url, params || {}, { responseType: 'blob' })
      const blob = new Blob([resp as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = `${filename}.xlsx`; a.click(); URL.revokeObjectURL(a.href)
      ElMessage.success('导出成功')
    } catch { ElMessage.error('导出失败') }
  }
  return { exportExcel }
}
