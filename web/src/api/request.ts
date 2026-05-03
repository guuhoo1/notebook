import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types'

/**
 * Axios请求封装类
 * 提供统一的HTTP请求处理
 */
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

  /**
   * 配置请求和响应拦截器
   */
  private setupInterceptors() {
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token')
        if (token) {
          config.headers.Authorization = token
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        const { data } = response
        if (data.code === 200) {
          return data as any
        }
        return Promise.reject(new Error(data.msg || '请求失败'))
      },
      (error) => {
        if (error.response) {
          const { status, data } = error.response
          if (status === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
            return Promise.reject(new Error('请先登录'))
          }
          return Promise.reject(new Error(data?.msg || `请求错误: ${status}`))
        }
        return Promise.reject(new Error('网络错误，请检查网络连接'))
      }
    )
  }

  /**
   * GET请求
   */
  get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.get(url, config)
  }

  /**
   * POST请求
   */
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.post(url, data, config)
  }

  /**
   * PUT请求
   */
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.put(url, data, config)
  }

  /**
   * DELETE请求
   */
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.delete(url, config)
  }
}

export const http = new HttpClient()
