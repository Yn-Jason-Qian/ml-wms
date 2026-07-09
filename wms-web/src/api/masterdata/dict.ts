import request from '../request'

export function getDictTypes() { return request.get('/masterdata/dict/types') }
export function createDictType(data: any) { return request.post('/masterdata/dict/types', data) }
export function getDictItems(typeCode: string) { return request.get(`/masterdata/dict/items/${typeCode}`) }
export function createDict(data: any) { return request.post('/masterdata/dict', data) }
export function updateDict(id: number, data: any) { return request.put(`/masterdata/dict/${id}`, data) }
export function deleteDict(id: number) { return request.delete(`/masterdata/dict/${id}`) }
export function enableDict(id: number) { return request.post(`/masterdata/dict/${id}/enable`) }
export function disableDict(id: number) { return request.post(`/masterdata/dict/${id}/disable`) }
