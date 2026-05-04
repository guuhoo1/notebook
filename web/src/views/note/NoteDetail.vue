<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNoteStore } from '@/stores'
import { parseMarkdown } from '@/utils/markdown'

const router = useRouter()
const route = useRoute()
const noteStore = useNoteStore()

const noteId = computed(() => Number(route.params.id))
const note = computed(() => noteStore.currentNote)
const loading = computed(() => noteStore.loading)

function goBack() {
  router.back()
}

function goEdit() {
  router.push(`/note/${noteId.value}/edit`)
}

async function handlePin() {
  if (!note.value) return
  const newPinned = note.value.isPinned === 1 ? 0 : 1
  await noteStore.pinNote(noteId.value, newPinned)
  if (note.value) {
    note.value.isPinned = newPinned
  }
}

async function handleArchive() {
  if (!note.value) return
  const newStatus = note.value.status === 2 ? 1 : 2
  const res = await noteStore.updateNote(noteId.value, { status: newStatus } as any)
  if (res.code === 200 && note.value) {
    note.value.status = newStatus
  }
}

async function handleDelete() {
  if (!confirm('确定要删除这篇笔记吗？')) return
  const res = await noteStore.deleteNote(noteId.value)
  if (res.code === 200) {
    router.push('/')
  }
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  noteStore.fetchNoteDetail(noteId.value)
})
</script>

<template>
  <div class="p-3 md:p-6 mx-auto max-w-4xl">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <div v-else-if="!note" class="text-center py-12">
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
          d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <p class="text-body text-muted">笔记不存在</p>
      <button class="btn-secondary mt-4" @click="goBack">返回</button>
    </div>

    <template v-else>
      <div class="flex items-center justify-between mb-3">
        <button
          class="w-10 h-10 rounded-lg bg-surface-soft flex items-center justify-center hover:bg-surface-strong transition-colors"
          @click="goBack"
        >
          <svg class="w-5 h-5 text-ink" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            />
          </svg>
        </button>
        <div class="flex items-center gap-1">
          <button
            class="w-9 h-9 rounded-lg flex items-center justify-center transition-colors"
            :class="note.isPinned === 1 ? 'text-signature-coral hover:bg-surface-soft' : 'text-body hover:bg-surface-soft'"
            @click="handlePin"
            :title="note.isPinned === 1 ? '取消置顶' : '置顶'"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                v-if="note.isPinned === 1"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 15l7-7 7 7"
              />
              <path
                v-else
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 14l-7 7m0 0l-7-7m7 7V3"
              />
            </svg>
          </button>
          <button 
            class="w-9 h-9 rounded-lg flex items-center justify-center text-body hover:bg-surface-soft transition-colors"
            @click="handleArchive"
            :title="note.status === 2 ? '恢复' : '归档'"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4"
              />
            </svg>
          </button>
          <button 
            class="w-9 h-9 rounded-lg flex items-center justify-center text-body hover:bg-surface-soft transition-colors md:mr-2"
            @click="goEdit"
            title="编辑"
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
            class="w-9 h-9 rounded-lg flex items-center justify-center text-red-600 hover:bg-red-50 transition-colors"
            @click="handleDelete"
            title="删除"
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

      <article class="bg-canvas rounded-lg p-4 md:p-8">
        <header class="mb-6 pb-6 border-b border-hairline">
          <h1 class="text-display-md text-ink mb-4">{{ note.title }}</h1>
          <div class="flex flex-wrap items-center gap-4 text-body text-muted">
            <div class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <span>创建于 {{ formatDate(note.createTime) }}</span>
            </div>
            <div class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
              <span>更新于 {{ formatDate(note.updateTime) }}</span>
            </div>
            <div class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                />
              </svg>
              <span>{{ note.viewCount }} 次浏览</span>
            </div>
            <span
              v-if="note.categoryName"
              class="px-2 py-0.5 text-xs rounded-full text-white"
              :style="{ backgroundColor: note.categoryColor || '#6b7280' }"
            >
              {{ note.categoryName }}
            </span>
            <span
              v-if="note.status === 0"
              class="px-2 py-0.5 text-xs rounded-full bg-surface-strong text-muted"
            >
              草稿
            </span>
            <span
              v-if="note.status === 2"
              class="px-2 py-0.5 text-xs rounded-full bg-surface-strong text-muted"
            >
              已归档
            </span>
          </div>
          <div v-if="note.tags && note.tags.length > 0" class="flex flex-wrap gap-2 mt-4">
            <span
              v-for="tag in note.tags"
              :key="tag.id"
              class="px-2 py-0.5 text-xs rounded-full"
              :style="{
                backgroundColor: tag.color + '20',
                color: tag.color,
                border: `1px solid ${tag.color}`,
              }"
            >
              #{{ tag.name }}
            </span>
          </div>
        </header>

        <div
          class="markdown-content"
          v-html="note.content ? parseMarkdown(note.content) : '<p class=\'text-muted\'>暂无内容</p>'"
        ></div>
      </article>
    </template>
  </div>
</template>

<style scoped>
.markdown-content :deep(h1) {
  font-size: 1.875rem;
  font-weight: 700;
  margin-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h2) {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h3) {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.markdown-content :deep(p) {
  margin-bottom: 1rem;
  line-height: 1.6;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 2rem;
  margin-bottom: 1rem;
}

.markdown-content :deep(li) {
  margin-bottom: 0.25rem;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #ef4444;
  padding-left: 1rem;
  font-style: italic;
  color: #6b7280;
  margin: 1rem 0;
}

.markdown-content :deep(code) {
  background-color: #f3f4f6;
  padding: 0.2em 0.4em;
  border-radius: 0.25rem;
  font-size: 0.875em;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
}

.markdown-content :deep(pre) {
  background-color: #1f2937;
  color: #e5e7eb;
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  margin: 1rem 0;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.markdown-content :deep(a) {
  color: #3b82f6;
  text-decoration: underline;
}

.markdown-content :deep(img) {
  max-width: 100%;
  border-radius: 0.5rem;
  margin: 1rem 0;
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 0.5rem 0.75rem;
  text-align: left;
}

.markdown-content :deep(th) {
  background-color: #f9fafb;
  font-weight: 600;
}

.markdown-content :deep(tr:nth-child(even)) {
  background-color: #f9fafb;
}
</style>
