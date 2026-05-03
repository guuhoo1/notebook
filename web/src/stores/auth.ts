import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import type { User, LoginParams, RegisterParams } from '@/types'

/**
 * 认证状态管理Store
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  /**
   * 用户登录
   */
  async function login(params: LoginParams) {
    try {
      const res = await authApi.login(params)
      if (res.code === 200 && res.data) {
        token.value = res.data.token
        user.value = res.data.user
        localStorage.setItem('token', res.data.token)
      }
      return res
    } catch (error: any) {
      clearAuth()
      throw error
    }
  }

  /**
   * 用户注册
   */
  async function register(params: RegisterParams) {
    try {
      const res = await authApi.register(params)
      if (res.code === 200 && res.data) {
        token.value = res.data.token
        user.value = res.data.user
        localStorage.setItem('token', res.data.token)
      }
      return res
    } catch (error: any) {
      clearAuth()
      throw error
    }
  }

  /**
   * 用户登出
   */
  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 忽略登出错误
    } finally {
      clearAuth()
    }
  }

  /**
   * 获取用户信息
   */
  async function fetchUserInfo() {
    if (!token.value) return null
    
    try {
      const res = await authApi.getUserInfo()
      if (res.code === 200 && res.data) {
        user.value = res.data
        return res
      } else {
        clearAuth()
        return null
      }
    } catch {
      clearAuth()
      return null
    }
  }

  /**
   * 设置Token
   */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 清除认证信息
   */
  function clearAuth() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    fetchUserInfo,
    setToken,
    clearAuth,
  }
}, {
  persist: {
    key: 'auth-store',
    paths: ['token'],
  },
})
