import request from '../request'

export function pageTasks(params: any) { return request.post('/tasks/page', params) }
export function claimTask(id: number) { return request.post(`/tasks/${id}/claim`) }
export function startTask(id: number) { return request.post(`/tasks/${id}/start`) }
export function completeTask(id: number) { return request.post(`/tasks/${id}/complete`) }
export function cancelTask(id: number) { return request.post(`/tasks/${id}/cancel`) }
