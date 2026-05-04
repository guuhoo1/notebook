<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNoteStore } from '@/stores'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css';

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
  <div class="note-detail-page">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <div v-else-if="!note" class="text-center py-12">
      <svg class="w-16 h-16 mx-auto text-hairline mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <p class="text-body text-muted">笔记不存在</p>
      <button class="btn-secondary mt-4" @click="goBack">返回</button>
    </div>

    <template v-else>
      <div class="note-toolbar">
        <button class="toolbar-btn" @click="goBack">
          <svg class="w-5 h-5 text-ink" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <div class="toolbar-actions">
          <button class="toolbar-btn" :class="note.isPinned === 1 ? 'text-signature-coral' : 'text-body'"
            @click="handlePin" :title="note.isPinned === 1 ? '取消置顶' : '置顶'">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path v-if="note.isPinned === 1" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M5 15l7-7 7 7" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M19 14l-7 7m0 0l-7-7m7 7V3" />
            </svg>
          </button>
          <button class="toolbar-btn text-body" @click="handleArchive" :title="note.status === 2 ? '恢复' : '归档'">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
            </svg>
          </button>
          <button class="toolbar-btn text-body" @click="goEdit" title="编辑">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </button>
          <button class="toolbar-btn text-red-600" @click="handleDelete" title="删除">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>

      <article class="note-article">
        <header class="note-header">
          <h1 class="note-title">{{ note.title }}</h1>
          <div class="note-meta">
            <div class="meta-item">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{{ formatDate(note.createTime) }}</span>
            </div>
            <div class="meta-item">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              <span>{{ formatDate(note.updateTime) }}</span>
            </div>
            <div class="meta-item">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              <span>{{ note.viewCount }} 次浏览</span>
            </div>
          </div>
          <div class="note-badges">
            <span v-if="note.categoryName" class="badge" :style="{ backgroundColor: note.categoryColor || '#6b7280' }">
              {{ note.categoryName }}
            </span>
            <span v-if="note.isPinned === 1" class="badge badge-pinned">置顶</span>
            <span v-if="note.status === 0" class="badge badge-draft">草稿</span>
            <span v-if="note.status === 2" class="badge badge-archived">已归档</span>
          </div>
          <div v-if="note.tags && note.tags.length > 0" class="note-tags">
            <span v-for="tag in note.tags" :key="tag.id" class="tag" :style="{
              backgroundColor: tag.color + '20',
              color: tag.color,
              border: `1px solid ${tag.color}`,
            }">
              #{{ tag.name }}
            </span>
          </div>
        </header>

        <!-- <div class="markdown-content" v-html="getRenderedContent()"></div> -->
        <MdPreview :modelValue="note.mdContent" class="p-0" />
      </article>
    </template>
  </div>
</template>

<style scoped>
.note-detail-page {
  max-width: 56rem;
  margin: 0 auto;
  padding: 0.75rem;
  width: 100%;
  box-sizing: border-box;
}

@media (min-width: 768px) {
  .note-detail-page {
    padding: 1.5rem;
  }
}

.note-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.toolbar-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.toolbar-btn:hover {
  background-color: #f3f4f6;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.note-article {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 1rem;
  box-sizing: border-box;
  max-width: 100%;
  overflow: hidden;
}

@media (min-width: 768px) {
  .note-article {
    padding: 2rem;
  }
}

.note-header {
  /* margin-bottom: 1.5rem; */
  /* padding-bottom: 1.5rem; */
  border-bottom: 1px solid #e5e7eb;
}

.note-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 1rem;
  line-height: 1.3;
  word-break: break-word;
  overflow-wrap: break-word;
}

@media (min-width: 768px) {
  .note-title {
    font-size: 1.875rem;
  }
}

.note-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.note-badges {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.badge {
  padding: 2px 8px;
  font-size: 0.75rem;
  border-radius: 9999px;
  color: #ffffff;
  white-space: nowrap;
}

.badge-pinned {
  background-color: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.badge-draft {
  background-color: #f3f4f6;
  color: #6b7280;
}

.badge-archived {
  background-color: #f3f4f6;
  color: #6b7280;
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.tag {
  padding: 2px 8px;
  font-size: 0.75rem;
  border-radius: 9999px;
  white-space: nowrap;
}

.markdown-content {
  font-size: 15px;
  line-height: 1.8;
  color: #1f2937;
  max-width: 100%;
  box-sizing: border-box;
  overflow-wrap: break-word;
  word-break: break-word;
  overflow: hidden;
}

.markdown-content :deep(h1) {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.3em;
  line-height: 1.4;
  word-break: break-word;
  overflow-wrap: break-word;
}

.markdown-content :deep(h2) {
  font-size: 1.25rem;
  font-weight: 600;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.2em;
  line-height: 1.4;
  word-break: break-word;
  overflow-wrap: break-word;
}

.markdown-content :deep(h3) {
  font-size: 1.125rem;
  font-weight: 600;
  margin-top: 1.25rem;
  margin-bottom: 0.5rem;
  line-height: 1.4;
  word-break: break-word;
  overflow-wrap: break-word;
}

.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  font-size: 1rem;
  font-weight: 600;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
  line-height: 1.4;
  word-break: break-word;
  overflow-wrap: break-word;
}

.markdown-content :deep(p) {
  margin-bottom: 1rem;
  line-height: 1.8;
  overflow-wrap: break-word;
  word-break: break-word;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 1.5rem;
  margin-bottom: 1rem;
}

.markdown-content :deep(li) {
  margin-bottom: 0.5rem;
  line-height: 1.6;
  overflow-wrap: break-word;
  word-break: break-word;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #3b82f6;
  padding: 0.75rem 1rem;
  margin: 1rem 0;
  color: #6b7280;
  background: #f9fafb;
  border-radius: 0 0.375rem 0.375rem 0;
  overflow-wrap: break-word;
  word-break: break-word;
}

.markdown-content :deep(code) {
  background-color: #f3f4f6;
  padding: 0.2em 0.4em;
  border-radius: 0.25rem;
  font-size: 0.875em;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  color: #ef4444;
  overflow-wrap: break-word;
  word-break: break-all;
}

.markdown-content :deep(pre) {
  background-color: #1f2937;
  color: #e5e7eb;
  padding: 0.75rem;
  border-radius: 0.375rem;
  overflow-x: auto;
  margin: 1rem 0;
  font-size: 13px;
  line-height: 1.6;
  max-width: 100%;
  -webkit-overflow-scrolling: touch;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: inherit;
  white-space: pre;
  word-break: normal;
  overflow-wrap: normal;
  display: block;
}

.markdown-content :deep(a) {
  color: #3b82f6;
  text-decoration: underline;
  overflow-wrap: break-word;
  word-break: break-all;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 0.5rem;
  margin: 1rem 0;
  display: block;
}

.markdown-content :deep(table) {
  display: block;
  overflow-x: auto;
  border-collapse: collapse;
  margin: 1rem 0;
  max-width: 100%;
  -webkit-overflow-scrolling: touch;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 0.5rem;
  text-align: left;
  white-space: nowrap;
}

.markdown-content :deep(th) {
  background-color: #f9fafb;
  font-weight: 600;
}

.markdown-content :deep(tr:nth-child(even)) {
  background-color: #f9fafb;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 2px solid #e5e7eb;
  margin: 1.5rem 0;
}

@media (max-width: 640px) {
  .markdown-content {
    font-size: 14px;
  }

  .markdown-content :deep(h1) {
    font-size: 1.375rem;
  }

  .markdown-content :deep(h2) {
    font-size: 1.125rem;
  }

  .markdown-content :deep(h3) {
    font-size: 1rem;
  }

  .markdown-content :deep(pre) {
    font-size: 12px;
    padding: 0.625rem;
  }

  .markdown-content :deep(th),
  .markdown-content :deep(td) {
    padding: 0.375rem 0.5rem;
    font-size: 0.75rem;
  }
}

:deep(.md-editor-preview-wrapper) {
  padding: 0;
  margin: 0;
}
</style>
