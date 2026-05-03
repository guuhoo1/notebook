import { http } from './request'
import type { NoteListItem, PageQuery, PageResult } from '@/types'

/**
 * 搜索相关API
 */
export const searchApi = {
  /**
   * 关键词搜索
   */
  search(params: PageQuery & { keyword: string }) {
    return http.get<PageResult<NoteListItem>>('/search', { params })
  },

  /**
   * 高级搜索
   */
  advancedSearch(
    params: PageQuery & {
      keyword?: string
      categoryId?: number
      tagId?: number
      startDate?: string
      endDate?: string
    }
  ) {
    return http.post<PageResult<NoteListItem>>('/search/advanced', params)
  },
}
