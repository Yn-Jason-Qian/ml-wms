import request from '../request'

export function pageStocktakes(params: any) { return request.post('/inventory/stocktakes/page', params) }
export function createStocktake(data: any) { return request.post('/inventory/stocktakes', data) }
