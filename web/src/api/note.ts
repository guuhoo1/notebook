import { http } from './request'
import type { Note, NoteDetail, NoteListItem, NoteParams, PageQuery, PageResult, NoteVersion } from '@/types'

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

  /**
   * 设置笔记分享状态
   */
  setShare(id: number, data: { isPublic: number }) {
    return http.post<Note>(`/note/${id}/share`, data)
  },

  /**
   * 获取笔记分享信息
   */
  getShareInfo(id: number) {
    return http.get<{ isPublic: number; shareUrl: string; shareViewCount: number }>(`/note/${id}/share`)
  },

  /**
   * 取消笔记分享
   */
  cancelShare(id: number) {
    return http.delete<void>(`/note/${id}/share`)
  },

  /**
   * 获取回收站笔记列表
   */
  getRecycleBin() {
    return http.get<NoteListItem[]>('/note/recycle-bin')
  },

  /**
   * 恢复笔记
   */
  restore(id: number) {
    return http.put<void>(`/note/${id}/restore`)
  },

  /**
   * 永久删除笔记
   */
  permanentDelete(id: number) {
    return http.delete<void>(`/note/${id}/permanent`)
  },

  /**
   * 清空回收站
   */
  emptyRecycleBin() {
    return http.delete<void>('/note/recycle-bin')
  },

  /**
   * 获取笔记版本列表
   */
  getVersions(noteId: number) {
    return http.get<NoteVersion[]>(`/note/${noteId}/versions`)
  },

  /**
   * 获取特定版本详情
   */
  getVersion(noteId: number, versionNumber: number) {
    return http.get<NoteVersion>(`/note/${noteId}/versions/${versionNumber}`)
  },

  /**
   * 恢复到指定版本
   */
  restoreVersion(noteId: number, versionNumber: number) {
    return http.put<void>(`/note/${noteId}/versions/${versionNumber}/restore`)
  },
}
