import request from '../request'

export function getOwnerList() { return request.get('/masterdata/owners') }
export function getOwnerPage(params: any) { return request.post('/masterdata/owners/page', params) }
export function createOwner(data: any) { return request.post('/masterdata/owners', data) }
export function updateOwner(id: number, data: any) { return request.put(`/masterdata/owners/${id}`, data) }
export function deleteOwner(id: number) { return request.delete(`/masterdata/owners/${id}`) }
export function enableOwner(id: number) { return request.post(`/masterdata/owners/${id}/enable`) }
export function disableOwner(id: number) { return request.post(`/masterdata/owners/${id}/disable`) }
