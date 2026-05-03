import { http } from './request'
import type { Note, NoteDetail, NoteListItem, NoteParams, PageQuery, PageResult } from '@/types'

/**
 * 笔记相关API
 */
export const noteApi = {
  /**
   * 创建笔记
   */
  create(data: NoteParams) {
    return http.post<Note>('/note', data)
  },

  /**
   * 获取笔记列表
   */
  getList(params: PageQuery) {
    return http.get<PageResult<NoteListItem>>('/note/list', { params })
  },

  /**
   * 获取笔记详情
   */
  getDetail(id: number) {
    return http.get<NoteDetail>(`/note/${id}`)
  },

  /**
   * 更新笔记
   */
  update(id: number, data: Partial<NoteParams>) {
    return http.put<Note>(`/note/${id}`, data)
  },

  /**
   * 删除笔记
   */
  delete(id: number) {
    return http.delete<void>(`/note/${id}`)
  },

  /**
   * 置顶/取消置顶笔记
   */
  pin(id: number, isPinned: number) {
    return http.put<void>(`/note/${id}/pin`, { isPinned })
  },

  /**
   * 归档/恢复笔记
   */
  archive(id: number, status: number) {
    return http.put<void>(`/note/${id}/archive`, { status })
  },

  /**
   * 移动笔记分类
   */
  move(id: number, categoryId: number | null) {
    return http.put<void>(`/note/${id}/move`, { categoryId })
  },
}
