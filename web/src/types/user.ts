/**
 * 用户信息类型
 */
export interface User {
  id: number
  email: string | null
  phone: string | null
  nickname: string
  avatar: string | null
  gender: number | null
  status: number
  createTime: string
}

/**
 * 用户设置类型
 */
export interface UserSettings {
  theme: 'light' | 'dark'
  fontSize: number
  fontFamily: string
  editorMode: number
}

/**
 * 登录请求参数
 */
export interface LoginParams {
  account: string
  password: string
}

/**
 * 注册请求参数
 */
export interface RegisterParams {
  email?: string
  phone?: string
  password: string
  nickname: string
}

/**
 * 登录响应数据
 */
export interface LoginResult {
  user: User
  token: string
}
