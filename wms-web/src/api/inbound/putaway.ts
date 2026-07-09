import request from '../request'

export function pagePutaways(params: any) { return request.post('/inbound/putaways/page', params) }
export function getPutaway(id: number) { return request.get(`/inbound/putaways/${id}`) }
export function createPutaway(data: any) { return request.post('/inbound/putaways', data) }
export function submitPutaway(data: any) { return request.post('/inbound/putaways/submit', data) }
