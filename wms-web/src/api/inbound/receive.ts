import request from '../request'

export function pageReceives(params: any) { return request.post('/inbound/receives/page', params) }
export function getReceive(id: number) { return request.get(`/inbound/receives/${id}`) }
export function createReceive(data: any) { return request.post('/inbound/receives', data) }
