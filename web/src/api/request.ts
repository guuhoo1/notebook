import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types'

const TOKEN_KEY = 'notebook_token'

class HttpClient {
  private instance: AxiosInstance

  constructor() {
    this.instance = axios.create({
      baseURL: '/api',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    this.instance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem(TOKEN_KEY)
        if (token) {
          config.headers.Authorization = token
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        const { data } = response
        
        if (data.code === 200) {
          return data as any
        }
        
        if (data.code === 401) {
          localStorage.removeItem(TOKEN_KEY)
          if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
            window.location.href = '/login'
          }
          return Promise.reject(new Error(data.msg || '请先登录'))
        }
        
        return Promise.reject(new Error(data.msg || '请求失败'))
      },
      (error) => {
        if (error.response) {
          const { status, data } = error.response
          if (status === 401) {
            localStorage.removeItem(TOKEN_KEY)
            if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
              window.location.href = '/login'
            }
            return Promise.reject(new Error('请先登录'))
          }
          if (status === 403) {
            return Promise.reject(new Error('没有权限访问'))
          }
          if (status === 404) {
            return Promise.reject(new Error('请求的资源不存在'))
          }
          if (status === 500) {
            return Promise.reject(new Error('服务器错误，请稍后重试'))
          }
          return Promise.reject(new Error(data?.msg || `请求错误: ${status}`))
        }
        if (error.code === 'ECONNABORTED') {
          return Promise.reject(new Error('请求超时，请检查网络'))
        }
        return Promise.reject(new Error('网络错误，请检查网络连接'))
      }
    )
  }

  get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.get(url, config)
  }

  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.post(url, data, config)
  }

  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.put(url, data, config)
  }

  delete<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.delete(url, config)
  }
}

export const http = new HttpClient()
