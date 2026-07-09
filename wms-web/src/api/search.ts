import request from './request'

export function globalSearch(q: string, types: string = 'asn,order,sku,location,owner') {
  return request.get('/search', { params: { q, types } })
}
