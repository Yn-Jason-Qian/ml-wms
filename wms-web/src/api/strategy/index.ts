import request from '../request'

export function getStrategyConfigsByType(strategyType: string) { return request.get(`/strategy/configs/type/${strategyType}`) }
export function getStrategyConfig(id: number) { return request.get(`/strategy/configs/${id}`) }
export function createStrategyConfig(data: any) { return request.post('/strategy/configs', data) }
export function deleteStrategyConfig(id: number) { return request.delete(`/strategy/configs/${id}`) }
export function createStrategyRule(data: any) { return request.post('/strategy/rules', data) }
export function deleteStrategyRule(id: number) { return request.delete(`/strategy/rules/${id}`) }
