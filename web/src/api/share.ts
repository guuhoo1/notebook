import { http } from './request'
import type { ShareHistoryItem } from '@/types'

/**
 * 分享相关API
 */
export const shareApi = {
  /**
   * 获取当前用户的分享浏览历史
   */
  getHistory(limit?: number) {
    return http.get<ShareHistoryItem[]>('/share/history', { params: { limit } })
  },

  /**
   * 清空当前用户的分享浏览历史
   */
  clearHistory() {
    return http.delete<void>('/share/history')
  },
}
