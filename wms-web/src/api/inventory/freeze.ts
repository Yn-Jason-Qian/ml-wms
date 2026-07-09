import request from '../request'

export function pageFreezes(params: any) { return request.post('/inventory/freezes/page', params) }
export function createFreeze(data: any) { return request.post('/inventory/freezes', data) }
export function releaseFreeze(id: number) { return request.post(`/inventory/freezes/${id}/release`) }
