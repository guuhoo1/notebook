import { http } from './request'
import type { User, UserSettings } from '@/types'

/**
 * 用户相关API
 */
export const userApi = {
  /**
   * 更新用户信息
   */
  updateInfo(data: Partial<User>) {
    return http.put<User>('/user/info', data)
  },

  /**
   * 修改密码
   */
  updatePassword(data: { oldPassword: string; newPassword: string }) {
    return http.put<void>('/user/password', data)
  },

  /**
   * 上传头像
   */
  uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return http.post<User>('/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 获取用户设置
   */
  getSettings() {
    return http.get<UserSettings>('/settings')
  },

  /**
   * 更新用户设置
   */
  updateSettings(data: Partial<UserSettings>) {
    return http.put<void>('/settings', data)
  },
}
