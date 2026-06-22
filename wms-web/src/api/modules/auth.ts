import request from '../request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginUser {
  userId: number
  username: string
  realName: string
  tenantId: number
  tenantName: string
  token: string
}

export function login(params: LoginParams) {
  return request.post<never, { data: LoginUser }>('/auth/login', params)
}
