/**
 * 分享浏览历史项
 */
export interface ShareHistoryItem {
  id: number
  noteId: number
  shareCode: string
  title: string
  authorId: number
  authorName: string
  authorAvatar?: string
  shareViewCount: number
  visitTime: string
  createTime: string
}

