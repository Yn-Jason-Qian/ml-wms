import request from '../request'

// ─── Inbound ───
export function pageAsns(params: any) { return request.post('/inbound/asns/page', params) }
export function getAsn(id: number) { return request.get(`/inbound/asns/${id}`) }
export function createAsn(data: any) { return request.post('/inbound/asns', data) }
export function pageReceives(params: any) { return request.post('/inbound/receives/page', params) }
export function createReceive(data: any) { return request.post('/inbound/receives', data) }
export function pageQcs(params: any) { return request.post('/inbound/qcs/page', params) }
export function createQc(data: any) { return request.post('/inbound/qcs', data) }
export function submitQc(data: any) { return request.post('/inbound/qcs/submit', data) }
export function pagePutaways(params: any) { return request.post('/inbound/putaways/page', params) }
export function createPutaway(data: any) { return request.post('/inbound/putaways', data) }
export function submitPutaway(data: any) { return request.post('/inbound/putaways/submit', data) }

// ─── Outbound ───
export function pageOrders(params: any) { return request.post('/outbound/orders/page', params) }
export function createOrder(data: any) { return request.post('/outbound/orders', data) }
export function pageWaves(params: any) { return request.post('/outbound/waves/page', params) }
export function createWave(data: any) { return request.post('/outbound/waves', data) }
export function pagePicks(params: any) { return request.post('/outbound/picks/page', params) }
export function createPickFromWave(waveId: number) { return request.post(`/outbound/picks/from-wave/${waveId}`) }
export function submitPick(data: any) { return request.post('/outbound/picks/submit', data) }
export function pageShips(params: any) { return request.post('/outbound/ships/page', params) }
export function createShip(data: any) { return request.post('/outbound/ships', data) }

// ─── Inventory ───
export function pageMoves(params: any) { return request.post('/inventory/moves/page', params) }
export function createMove(data: any) { return request.post('/inventory/moves', data) }
export function pageStocktakes(params: any) { return request.post('/inventory/stocktakes/page', params) }
export function createStocktake(data: any) { return request.post('/inventory/stocktakes', data) }
export function pageFreezes(params: any) { return request.post('/inventory/freezes/page', params) }
export function createFreeze(data: any) { return request.post('/inventory/freezes', data) }
export function releaseFreeze(id: number) { return request.post(`/inventory/freezes/${id}/release`) }

// ─── Task ───
export function pageTasks(params: any) { return request.post('/tasks/page', params) }
export function claimTask(id: number) { return request.post(`/tasks/${id}/claim`) }
export function startTask(id: number) { return request.post(`/tasks/${id}/start`) }
export function completeTask(id: number) { return request.post(`/tasks/${id}/complete`) }
export function cancelTask(id: number) { return request.post(`/tasks/${id}/cancel`) }
