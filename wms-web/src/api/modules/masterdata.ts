import request from '../request'

// ─── Warehouse ───
export function getWarehouseList() { return request.get('/masterdata/warehouses') }
export function getWarehousePage(params: any) { return request.post('/masterdata/warehouses/page', params) }
export function getWarehouse(id: number) { return request.get(`/masterdata/warehouses/${id}`) }
export function createWarehouse(data: any) { return request.post('/masterdata/warehouses', data) }
export function updateWarehouse(id: number, data: any) { return request.put(`/masterdata/warehouses/${id}`, data) }
export function deleteWarehouse(id: number) { return request.delete(`/masterdata/warehouses/${id}`) }
export function enableWarehouse(id: number) { return request.post(`/masterdata/warehouses/${id}/enable`) }
export function disableWarehouse(id: number) { return request.post(`/masterdata/warehouses/${id}/disable`) }

// ─── Area ───
export function getAreasByWarehouse(warehouseId: number) { return request.get(`/masterdata/areas/by-warehouse/${warehouseId}`) }
export function getAreaPage(params: any) { return request.post('/masterdata/areas/page', params) }
export function getArea(id: number) { return request.get(`/masterdata/areas/${id}`) }
export function createArea(data: any) { return request.post('/masterdata/areas', data) }
export function updateArea(id: number, data: any) { return request.put(`/masterdata/areas/${id}`, data) }
export function deleteArea(id: number) { return request.delete(`/masterdata/areas/${id}`) }
export function enableArea(id: number) { return request.post(`/masterdata/areas/${id}/enable`) }
export function disableArea(id: number) { return request.post(`/masterdata/areas/${id}/disable`) }

// ─── Location ───
export function getLocationsByArea(areaId: number) { return request.get(`/masterdata/locations/by-area/${areaId}`) }
export function getLocationPage(params: any) { return request.post('/masterdata/locations/page', params) }
export function createLocation(data: any) { return request.post('/masterdata/locations', data) }
export function batchCreateLocations(data: any) { return request.post('/masterdata/locations/batch', data) }
export function updateLocation(id: number, data: any) { return request.put(`/masterdata/locations/${id}`, data) }
export function deleteLocation(id: number) { return request.delete(`/masterdata/locations/${id}`) }
export function enableLocation(id: number) { return request.post(`/masterdata/locations/${id}/enable`) }
export function disableLocation(id: number) { return request.post(`/masterdata/locations/${id}/disable`) }

// ─── Owner ───
export function getOwnerList() { return request.get('/masterdata/owners') }
export function getOwnerPage(params: any) { return request.post('/masterdata/owners/page', params) }
export function createOwner(data: any) { return request.post('/masterdata/owners', data) }
export function updateOwner(id: number, data: any) { return request.put(`/masterdata/owners/${id}`, data) }
export function deleteOwner(id: number) { return request.delete(`/masterdata/owners/${id}`) }
export function enableOwner(id: number) { return request.post(`/masterdata/owners/${id}/enable`) }
export function disableOwner(id: number) { return request.post(`/masterdata/owners/${id}/disable`) }

// ─── Unit ───
export function getUnitList() { return request.get('/masterdata/units') }

// ─── SKU ───
export function getSkuPage(params: any) { return request.post('/masterdata/skus/page', params) }
export function getSku(id: number) { return request.get(`/masterdata/skus/${id}`) }
export function createSku(data: any) { return request.post('/masterdata/skus', data) }
export function updateSku(id: number, data: any) { return request.put(`/masterdata/skus/${id}`, data) }
export function deleteSku(id: number) { return request.delete(`/masterdata/skus/${id}`) }
export function getSkuPackages(skuId: number) { return request.get(`/masterdata/skus/${skuId}/packages`) }
export function createSkuPackage(skuId: number, data: any) { return request.post(`/masterdata/skus/${skuId}/packages`, data) }
export function deleteSkuPackage(id: number) { return request.delete(`/masterdata/skus/packages/${id}`) }
