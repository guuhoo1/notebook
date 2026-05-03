import { http } from './request'
import type { LoginParams, LoginResult, RegisterParams, User } from '@/types'

/**
 * 认证相关API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login(params: LoginParams) {
    return http.post<LoginResult>('/auth/login', params)
  },

  /**
   * 用户注册
   */
  register(params: RegisterParams) {
    return http.post<LoginResult>('/auth/register', params)
  },

  /**
   * 用户登出
   */
  logout() {
    return http.post<void>('/auth/logout')
  },

  /**
   * 获取当前用户信息
   */
  getUserInfo() {
    return http.get<User>('/auth/userInfo')
  },
}
