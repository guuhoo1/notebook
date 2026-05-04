import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import type { User, LoginParams, RegisterParams } from '@/types'

const TOKEN_KEY = 'notebook_token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem(TOKEN_KEY, newToken)
  }

  function clearAuth() {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  async function login(params: LoginParams) {
    try {
      const res = await authApi.login(params)
      if (res.code === 200 && res.data) {
        setToken(res.data.token)
        user.value = res.data.user
      }
      return res
    } catch (error: any) {
      clearAuth()
      throw error
    }
  }

  async function register(params: RegisterParams) {
    try {
      const res = await authApi.register(params)
      if (res.code === 200 && res.data) {
        setToken(res.data.token)
        user.value = res.data.user
      }
      return res
    } catch (error: any) {
      clearAuth()
      throw error
    }
  }

  async function logout() {
    if (token.value) {
      try {
        await authApi.logout()
      } catch (error) {
        console.error('登出接口调用失败:', error)
      }
    }
    clearAuth()
  }

  async function fetchUserInfo(): Promise<{ success: boolean; tokenInvalid?: boolean }> {
    if (!token.value) {
      return { success: false, tokenInvalid: true }
    }

    try {
      const res = await authApi.getUserInfo()
      if (res.code === 200 && res.data) {
        user.value = res.data
        return { success: true }
      }
      return { success: false }
    } catch (error: any) {
      if (error.message === '请先登录') {
        clearAuth()
        return { success: false, tokenInvalid: true }
      }
      console.error('获取用户信息失败:', error.message)
      return { success: false }
    }
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
})
