import request from '../request'

export function getUnitList() { return request.get('/masterdata/units') }

export function getSkuPage(params: any) { return request.post('/masterdata/skus/page', params) }
export function getSkuListByOwner(ownerId: number) { return request.get(`/masterdata/skus/by-owner/${ownerId}`) }
export function getSku(id: number) { return request.get(`/masterdata/skus/${id}`) }
export function createSku(data: any) { return request.post('/masterdata/skus', data) }
export function updateSku(id: number, data: any) { return request.put(`/masterdata/skus/${id}`, data) }
export function deleteSku(id: number) { return request.delete(`/masterdata/skus/${id}`) }
export function getSkuPackages(skuId: number) { return request.get(`/masterdata/skus/${skuId}/packages`) }
export function createSkuPackage(skuId: number, data: any) { return request.post(`/masterdata/skus/${skuId}/packages`, data) }
export function deleteSkuPackage(id: number) { return request.delete(`/masterdata/skus/packages/${id}`) }
