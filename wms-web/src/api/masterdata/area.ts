import request from '../request'

export function getAreasByWarehouse(warehouseId: number) { return request.get(`/masterdata/areas/by-warehouse/${warehouseId}`) }
export function getAreaPage(params: any) { return request.post('/masterdata/areas/page', params) }
export function getArea(id: number) { return request.get(`/masterdata/areas/${id}`) }
export function createArea(data: any) { return request.post('/masterdata/areas', data) }
export function updateArea(id: number, data: any) { return request.put(`/masterdata/areas/${id}`, data) }
export function deleteArea(id: number) { return request.delete(`/masterdata/areas/${id}`) }
export function enableArea(id: number) { return request.post(`/masterdata/areas/${id}/enable`) }
export function disableArea(id: number) { return request.post(`/masterdata/areas/${id}/disable`) }
