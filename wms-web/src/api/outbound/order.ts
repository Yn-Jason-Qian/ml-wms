import request from '../request'

export function pageOrders(params: any) { return request.post('/outbound/orders/page', params) }
export function getOrder(id: number) { return request.get(`/outbound/orders/${id}`) }
export function createOrder(data: any) { return request.post('/outbound/orders', data) }
