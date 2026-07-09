import request from '../request'

export function pageMoves(params: any) { return request.post('/inventory/moves/page', params) }
export function createMove(data: any) { return request.post('/inventory/moves', data) }
