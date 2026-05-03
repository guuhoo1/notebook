import { defineStore } from 'pinia'
import { ref } from 'vue'
import { noteApi } from '@/api'
import type { Note, NoteDetail, NoteListItem, NoteParams, PageQuery, PageResult } from '@/types'

/**
 * 笔记状态管理Store
 */
export const useNoteStore = defineStore('note', () => {
  const notes = ref<NoteListItem[]>([])
  const currentNote = ref<NoteDetail | null>(null)
  const pagination = ref({
    page: 1,
    pageSize: 10,
    total: 0,
  })
  const loading = ref(false)

  /**
   * 获取笔记列表
   */
  async function fetchNotes(params: PageQuery) {
    loading.value = true
    try {
      const res = await noteApi.getList(params)
      if (res.code === 200 && res.data) {
        notes.value = res.data.list
        pagination.value.total = res.data.total
        pagination.value.page = params.pageNum || 1
        pagination.value.pageSize = params.pageSize || 10
      }
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取笔记详情
   */
  async function fetchNoteDetail(id: number) {
    loading.value = true
    try {
      const res = await noteApi.getDetail(id)
      if (res.code === 200 && res.data) {
        currentNote.value = res.data
      }
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建笔记
   */
  async function createNote(data: NoteParams) {
    const res = await noteApi.create(data)
    return res
  }

  /**
   * 更新笔记
   */
  async function updateNote(id: number, data: Partial<NoteParams>) {
    const res = await noteApi.update(id, data)
    if (res.code === 200 && currentNote.value?.id === id) {
      currentNote.value = { ...currentNote.value, ...res.data } as NoteDetail
    }
    return res
  }

  /**
   * 删除笔记
   */
  async function deleteNote(id: number) {
    const res = await noteApi.delete(id)
    if (res.code === 200) {
      notes.value = notes.value.filter(n => n.id !== id)
    }
    return res
  }

  /**
   * 置顶笔记
   */
  async function pinNote(id: number, isPinned: number) {
    const res = await noteApi.pin(id, isPinned)
    if (res.code === 200) {
      const note = notes.value.find(n => n.id === id)
      if (note) {
        note.isPinned = isPinned
      }
    }
    return res
  }

  /**
   * 清空当前笔记
   */
  function clearCurrentNote() {
    currentNote.value = null
  }

  return {
    notes,
    currentNote,
    pagination,
    loading,
    fetchNotes,
    fetchNoteDetail,
    createNote,
    updateNote,
    deleteNote,
    pinNote,
    clearCurrentNote,
  }
})
