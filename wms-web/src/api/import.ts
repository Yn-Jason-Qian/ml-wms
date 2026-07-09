import request from './request'

export function downloadTemplate(entity: string) {
  window.open(`/api/v1/template/${entity}`, '_blank')
}

export function importData(entity: string, file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post(`/import/${entity}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
