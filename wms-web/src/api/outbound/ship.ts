import request from '../request'

export function pageShips(params: any) { return request.post('/outbound/ships/page', params) }
export function createShip(data: any) { return request.post('/outbound/ships', data) }
