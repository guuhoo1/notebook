/**
 * 笔记状态枚举
 */
export enum NoteStatus {
  DRAFT = 0,
  NORMAL = 1,
  ARCHIVED = 2,
}

/**
 * 笔记类型
 */
export interface Note {
  id: number
  userId: number
  categoryId: number | null
  title: string
  content: string | null
  mdContent: string | null
  htmlContent: string | null
  summary: string | null
  status: NoteStatus
  isPinned: number
  viewCount: number
  createTime: string
  updateTime: string
}

/**
 * 笔记列表项类型（包含分类信息）
 */
export interface NoteListItem {
  id: number
  title: string
  summary: string | null
  categoryId: number | null
  categoryName: string | null
  categoryColor: string | null
  status: NoteStatus
  isPinned: number
  viewCount: number
  createTime: string
  updateTime: string
}

/**
 * 笔记详情类型（包含标签信息）
 */
export interface NoteDetail extends Note {
  categoryName: string | null
  categoryColor: string | null
  tags: Tag[]
}

/**
 * 创建/更新笔记参数
 */
export interface NoteParams {
  title: string
  content?: string
  mdContent?: string
  htmlContent?: string
  categoryId?: number
  tagIds?: number[]
  status?: NoteStatus
}

/**
 * 分类类型
 */
export interface Category {
  id: number
  userId: number
  name: string
  description: string | null
  color: string
  icon: string | null
  sortOrder: number
  noteCount: number
  createTime: string
}

/**
 * 分类参数
 */
export interface CategoryParams {
  name: string
  description?: string
  color?: string
  icon?: string
}

/**
 * 标签类型
 */
export interface Tag {
  id: number
  userId: number
  name: string
  color: string
}

/**
 * 标签参数
 */
export interface TagParams {
  name: string
  color?: string
}
