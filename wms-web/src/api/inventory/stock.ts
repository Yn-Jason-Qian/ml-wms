import request from '../request'

export function pageStocks(params: any) { return request.post('/inventory/stocks/page', params) }
