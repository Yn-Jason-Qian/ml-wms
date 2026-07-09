import request from '../request'

export function getWarehouseList() { return request.get('/masterdata/warehouses') }
export function getWarehousePage(params: any) { return request.post('/masterdata/warehouses/page', params) }
export function getWarehouse(id: number) { return request.get(`/masterdata/warehouses/${id}`) }
export function createWarehouse(data: any) { return request.post('/masterdata/warehouses', data) }
export function updateWarehouse(id: number, data: any) { return request.put(`/masterdata/warehouses/${id}`, data) }
export function deleteWarehouse(id: number) { return request.delete(`/masterdata/warehouses/${id}`) }
export function enableWarehouse(id: number) { return request.post(`/masterdata/warehouses/${id}/enable`) }
export function disableWarehouse(id: number) { return request.post(`/masterdata/warehouses/${id}/disable`) }
