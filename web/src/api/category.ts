import { http } from './request'
import type { Category, CategoryParams } from '@/types'

/**
 * 分类相关API
 */
export const categoryApi = {
  /**
   * 创建分类
   */
  create(data: CategoryParams) {
    return http.post<Category>('/category', data)
  },

  /**
   * 获取分类列表
   */
  getList() {
    return http.get<Category[]>('/category/list')
  },

  /**
   * 更新分类
   */
  update(id: number, data: Partial<CategoryParams>) {
    return http.put<Category>(`/category/${id}`, data)
  },

  /**
   * 删除分类
   */
  delete(id: number) {
    return http.delete<void>(`/category/${id}`)
  },

  /**
   * 分类排序
   */
  sort(orders: Array<{ id: number; sortOrder: number }>) {
    return http.put<void>('/category/sort', { orders })
  },
}
