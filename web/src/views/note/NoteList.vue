<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNoteStore, useCategoryStore } from '@/stores'
import type { NoteListItem } from '@/types'

const router = useRouter()
const route = useRoute()
const noteStore = useNoteStore()
const categoryStore = useCategoryStore()

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const activeCategory = computed(() => categoryStore.activeCategory)

const noteStatus = computed(() => {
  const status = route.query.status
  if (status === 'archived') return 2
  if (status === 'draft') return 0
  return undefined
})

const pageTitle = computed(() => {
  if (noteStatus.value === 2) return '归档笔记'
  if (activeCategory.value) return activeCategory.value.name
  return '全部笔记'
})

const filteredNotes = computed(() => {
  let notes = noteStore.notes
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    notes = notes.filter(
      (n) =>
        n.title.toLowerCase().includes(keyword) ||
        (n.summary && n.summary.toLowerCase().includes(keyword))
    )
  }
  return notes
})

const pinnedNotes = computed(() => filteredNotes.value.filter((n) => n.isPinned === 1))

const normalNotes = computed(() => filteredNotes.value.filter((n) => n.isPinned === 0))

async function loadNotes() {
  const params: any = {
    pageNum: currentPage.value,
    pageSize: pageSize.value,
  }
  if (activeCategory.value) {
    params.categoryId = activeCategory.value.id
  }
  if (noteStatus.value !== undefined) {
    params.status = noteStatus.value
  }
  await noteStore.fetchNotes(params)
}

function handleSearch() {
  currentPage.value = 1
  loadNotes()
}

function goToDetail(note: NoteListItem) {
  router.push(`/note/${note.id}`)
}

function goToEdit(note: NoteListItem) {
  router.push(`/note/${note.id}/edit`)
}

function goToNew() {
  router.push('/note/new')
}

async function handlePin(note: NoteListItem, e: Event) {
  e.stopPropagation()
  const newPinned = note.isPinned === 1 ? 0 : 1
  await noteStore.pinNote(note.id, newPinned)
}

async function handleDelete(note: NoteListItem, e: Event) {
  e.stopPropagation()
  if (confirm('确定要删除这篇笔记吗？')) {
    await noteStore.deleteNote(note.id)
  }
}

function handlePageChange(delta: number) {
  currentPage.value += delta
  loadNotes()
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
      return minutes <= 1 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

watch(
  () => route.query,
  () => {
    currentPage.value = 1
    loadNotes()
  },
  { deep: true }
)

watch(activeCategory, () => {
  currentPage.value = 1
  loadNotes()
})

onMounted(() => {
  categoryStore.fetchCategories()
  loadNotes()
})
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-title-lg text-ink">{{ pageTitle }}</h1>
        <p class="text-body text-muted mt-1">共 {{ noteStore.pagination.total }} 篇笔记</p>
      </div>
      <button class="btn-primary flex items-center gap-2" @click="goToNew">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          />
        </svg>
        新建笔记
      </button>
    </div>

    <div class="mb-6">
      <div class="relative">
        <svg
          class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
        <input
          v-model="searchKeyword"
          type="text"
          class="input-base pl-12"
          placeholder="搜索笔记..."
          @keyup.enter="handleSearch"
        />
      </div>
    </div>

    <div v-if="noteStore.loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <div v-else-if="filteredNotes.length === 0" class="text-center py-12">
      <svg
        class="w-16 h-16 mx-auto text-hairline mb-4"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1.5"
          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
        />
      </svg>
      <p class="text-body text-muted">暂无笔记</p>
      <button class="btn-secondary mt-4" @click="goToNew">创建第一篇笔记</button>
    </div>

    <div v-else class="space-y-8">
      <div v-if="pinnedNotes.length > 0 && noteStatus !== 2">
        <h2 class="text-title-sm text-ink mb-4 flex items-center gap-2">
          <svg class="w-5 h-5 text-signature-coral" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M16 4a1 1 0 01.117 1.993L16 6H8a1 1 0 01-.117-1.993L8 4h8zm0 3a1 1 0 01.117 1.993L16 9H8a1 1 0 01-.117-1.993L8 7h8zm0 3a1 1 0 01.117 1.993L16 12H8a1 1 0 01-.117-1.993L8 10h8z"
            />
          </svg>
          置顶
        </h2>
        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <div
            v-for="note in pinnedNotes"
            :key="note.id"
            class="card cursor-pointer group relative"
            @click="goToDetail(note)"
          >
            <div class="flex items-start justify-between mb-2">
              <h3 class="text-label-md text-ink line-clamp-1">{{ note.title }}</h3>
              <span
                v-if="note.categoryName"
                class="px-2 py-0.5 text-xs rounded-full text-white"
                :style="{ backgroundColor: note.categoryColor }"
              >
                {{ note.categoryName }}
              </span>
            </div>
            <p class="text-body text-muted text-sm line-clamp-2 mb-3">
              {{ note.summary || '无内容' }}
            </p>
            <div class="flex items-center justify-between text-xs text-muted">
              <span>{{ formatDate(note.updateTime) }}</span>
              <span>{{ note.viewCount }} 次浏览</span>
            </div>
            <div
              class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity flex gap-1"
            >
              <button
                class="p-1.5 bg-surface-soft rounded hover:bg-surface-strong"
                title="取消置顶"
                @click="handlePin(note, $event)"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M5 15l7-7 7 7"
                  />
                </svg>
              </button>
              <button
                class="p-1.5 bg-surface-soft rounded hover:bg-surface-strong"
                title="编辑"
                @click="goToEdit(note)"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                  />
                </svg>
              </button>
              <button
                class="p-1.5 bg-surface-soft rounded hover:bg-red-100 hover:text-red-600"
                title="删除"
                @click="handleDelete(note, $event)"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="normalNotes.length > 0">
        <h2 v-if="pinnedNotes.length > 0 && noteStatus !== 2" class="text-title-sm text-ink mb-4">
          其他笔记
        </h2>
        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <div
            v-for="note in normalNotes"
            :key="note.id"
            class="card cursor-pointer group relative"
            @click="goToDetail(note)"
          >
            <div class="flex items-start justify-between mb-2">
              <h3 class="text-label-md text-ink line-clamp-1">{{ note.title }}</h3>
              <span
                v-if="note.categoryName"
                class="px-2 py-0.5 text-xs rounded-full text-white"
                :style="{ backgroundColor: note.categoryColor }"
              >
                {{ note.categoryName }}
              </span>
            </div>
            <p class="text-body text-muted text-sm line-clamp-2 mb-3">
              {{ note.summary || '无内容' }}
            </p>
            <div class="flex items-center justify-between text-xs text-muted">
              <span>{{ formatDate(note.updateTime) }}</span>
              <span>{{ note.viewCount }} 次浏览</span>
            </div>
            <div
              class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity flex gap-1"
            >
              <button
                v-if="noteStatus !== 2"
                class="p-1.5 bg-surface-soft rounded hover:bg-surface-strong"
                title="置顶"
                @click="handlePin(note, $event)"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 14l-7 7m0 0l-7-7m7 7V3"
                  />
                </svg>
              </button>
              <button
                class="p-1.5 bg-surface-soft rounded hover:bg-surface-strong"
                title="编辑"
                @click="goToEdit(note)"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                  />
                </svg>
              </button>
              <button
                class="p-1.5 bg-surface-soft rounded hover:bg-red-100 hover:text-red-600"
                title="删除"
                @click="handleDelete(note, $event)"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="noteStore.pagination.total > pageSize" class="mt-8 flex justify-center">
      <div class="flex items-center gap-2">
        <button
          class="btn-secondary px-4 py-2"
          :disabled="currentPage === 1"
          @click="handlePageChange(-1)"
        >
          上一页
        </button>
        <span class="text-body text-muted">
          第 {{ currentPage }} / {{ Math.ceil(noteStore.pagination.total / pageSize) }} 页
        </span>
        <button
          class="btn-secondary px-4 py-2"
          :disabled="currentPage * pageSize >= noteStore.pagination.total"
          @click="handlePageChange(1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>
