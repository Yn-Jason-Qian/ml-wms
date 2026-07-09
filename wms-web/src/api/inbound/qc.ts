import request from '../request'

export function pageQcs(params: any) { return request.post('/inbound/qcs/page', params) }
export function getQc(id: number) { return request.get(`/inbound/qcs/${id}`) }
export function createQc(data: any) { return request.post('/inbound/qcs', data) }
export function submitQc(data: any) { return request.post('/inbound/qcs/submit', data) }
