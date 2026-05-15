<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { noteApi } from '@/api'
import type { NoteListItem } from '@/types'

const router = useRouter()
const notes = ref<NoteListItem[]>([])
const loading = ref(false)

async function loadNotes() {
  loading.value = true
  try {
    const result = await noteApi.getRecycleBin()
    if (result.code === 200 && result.data) {
      notes.value = result.data
    }
  } finally {
    loading.value = false
  }
}

async function restoreNote(note: NoteListItem) {
  if (!confirm(`确定要恢复笔记「${note.title}」吗？`)) {
    return
  }
  try {
    const result = await noteApi.restore(note.id)
    if (result.code === 200) {
      await loadNotes()
    }
  } catch (e) {
    console.error('恢复失败', e)
  }
}

async function permanentDelete(note: NoteListItem) {
  if (!confirm(`确定要永久删除笔记「${note.title}」吗？此操作不可恢复！`)) {
    return
  }
  try {
    const result = await noteApi.permanentDelete(note.id)
    if (result.code === 200) {
      await loadNotes()
    }
  } catch (e) {
    console.error('删除失败', e)
  }
}

async function emptyRecycleBin() {
  if (notes.value.length === 0) {
    return
  }
  if (!confirm('确定要清空回收站吗？所有笔记将被永久删除！')) {
    return
  }
  try {
    const result = await noteApi.emptyRecycleBin()
    if (result.code === 200) {
      await loadNotes()
    }
  } catch (e) {
    console.error('清空失败', e)
  }
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 1 ? '刚刚删除' : `${minutes}分钟前删除`
    }
    return `${hours}小时前删除`
  } else if (days === 1) {
    return '昨天删除'
  } else if (days < 7) {
    return `${days}天前删除`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) + '删除'
  }
}

onMounted(() => {
  loadNotes()
})
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-title-lg text-ink">回收站</h1>
        <p class="text-body text-muted mt-1">共 {{ notes.length }} 篇笔记</p>
      </div>
      <button v-if="notes.length > 0" class="btn-secondary text-red-600 hover:bg-red-50" @click="emptyRecycleBin">
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
        清空回收站
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <div v-else-if="notes.length === 0" class="text-center py-12">
      <svg class="w-16 h-16 mx-auto text-hairline mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
      </svg>
      <p class="text-body text-muted">回收站是空的</p>
      <button class="btn-secondary mt-4" @click="router.push('/')">
        返回笔记列表
      </button>
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="note in notes"
        :key="note.id"
        class="card flex items-center justify-between opacity-75"
      >
        <div class="flex-1 min-w-0">
          <h3 class="text-label-md text-ink line-clamp-1 mb-1">{{ note.title }}</h3>
          <p class="text-body text-muted text-sm line-clamp-1 mb-2">{{ note.summary || '无内容' }}</p>
          <p class="text-xs text-muted">
            <span v-if="note.deletedAt">{{ formatDate(note.deletedAt) }}</span>
          </p>
        </div>
        <div class="flex items-center gap-2 ml-4">
          <button class="btn-secondary px-3 py-2 text-sm" @click="restoreNote(note)">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            恢复
          </button>
          <button class="px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg" @click="permanentDelete(note)">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
