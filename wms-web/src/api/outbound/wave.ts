import request from '../request'

export function pageWaves(params: any) { return request.post('/outbound/waves/page', params) }
export function getWave(id: number) { return request.get(`/outbound/waves/${id}`) }
export function createWave(data: any) { return request.post('/outbound/waves', data) }
