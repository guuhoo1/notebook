/**
 * API统一响应类型
 */
export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

/**
 * 分页结果类型
 */
export interface PageResult<T> {
  total: number
  list: T[]
}

/**
 * 分页查询参数
 */
export interface PageQuery {
  pageNum?: number
  pageSize?: number
  orderBy?: string
  sort?: 'asc' | 'desc'
  keyword?: string
  status?: number
  categoryId?: number
}
