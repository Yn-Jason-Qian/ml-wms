import request from '../request'

export function pagePicks(params: any) { return request.post('/outbound/picks/page', params) }
export function getPick(id: number) { return request.get(`/outbound/picks/${id}`) }
export function createPickFromWave(waveId: number) { return request.post(`/outbound/picks/from-wave/${waveId}`) }
export function submitPick(data: any) { return request.post('/outbound/picks/submit', data) }
