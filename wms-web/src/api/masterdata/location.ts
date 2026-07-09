import request from '../request'

export function getLocationsByArea(areaId: number) { return request.get(`/masterdata/locations/by-area/${areaId}`) }
export function getLocationPage(params: any) { return request.post('/masterdata/locations/page', params) }
export function createLocation(data: any) { return request.post('/masterdata/locations', data) }
export function batchCreateLocations(data: any) { return request.post('/masterdata/locations/batch', data) }
export function updateLocation(id: number, data: any) { return request.put(`/masterdata/locations/${id}`, data) }
export function deleteLocation(id: number) { return request.delete(`/masterdata/locations/${id}`) }
export function enableLocation(id: number) { return request.post(`/masterdata/locations/${id}/enable`) }
export function disableLocation(id: number) { return request.post(`/masterdata/locations/${id}/disable`) }
