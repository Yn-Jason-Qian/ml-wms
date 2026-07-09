import request from '../request'

export function pageAsns(params: any) { return request.post('/inbound/asns/page', params) }
export function getAsn(id: number) { return request.get(`/inbound/asns/${id}`) }
export function createAsn(data: any) { return request.post('/inbound/asns', data) }
