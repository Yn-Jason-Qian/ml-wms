import request from './request'

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

export function getDashboardTrend(days: number = 30) {
  return request.post('/dashboard/trend', { days })
}

export function getExpiryAlert() {
  return request.get('/dashboard/expiry-alert')
}
