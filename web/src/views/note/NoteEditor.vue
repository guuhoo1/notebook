<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNoteStore, useCategoryStore } from '@/stores'
import type { NoteParams, Category } from '@/types'

const router = useRouter()
const route = useRoute()
const noteStore = useNoteStore()
const categoryStore = useCategoryStore()

const isNew = computed(() => route.name === 'NoteNew')
const noteId = computed(() => Number(route.params.id))

const title = ref('')
const content = ref('')
const categoryId = ref<number | null>(null)
const status = ref(1)
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')

const categories = computed(() => categoryStore.categories)

const editorRef = ref<HTMLElement | null>(null)

function execCommand(command: string, value: string = '') {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
}

function handleHeading() {
  const selection = window.getSelection()
  if (selection && selection.rangeCount > 0) {
    execCommand('formatBlock', 'h2')
  }
}

function handleQuote() {
  execCommand('formatBlock', 'blockquote')
}

function handleCode() {
  const selection = window.getSelection()
  if (selection && selection.toString()) {
    execCommand('insertHTML', `<code>${selection.toString()}</code>`)
  }
}

function handleLink() {
  const url = prompt('请输入链接地址：')
  if (url) {
    execCommand('createLink', url)
  }
}

function handleImage() {
  const url = prompt('请输入图片地址：')
  if (url) {
    execCommand('insertImage', url)
  }
}

function handleList(ordered: boolean) {
  execCommand(ordered ? 'insertOrderedList' : 'insertUnorderedList')
}

function handleEditorInput() {
  if (editorRef.value) {
    content.value = editorRef.value.innerHTML
  }
}

async function loadNote() {
  if (isNew.value) return

  loading.value = true
  try {
    const res = await noteStore.fetchNoteDetail(noteId.value)
    if (res.code === 200 && noteStore.currentNote) {
      title.value = noteStore.currentNote.title
      content.value = noteStore.currentNote.content || ''
      categoryId.value = noteStore.currentNote.categoryId
      status.value = noteStore.currentNote.status
      
      await nextTick()
      if (editorRef.value) {
        editorRef.value.innerHTML = content.value
      }
    } else {
      errorMessage.value = '笔记不存在'
    }
  } finally {
    loading.value = false
  }
}

async function handleSave(asDraft: boolean = false) {
  if (!title.value.trim()) {
    errorMessage.value = '请输入标题'
    return
  }

  saving.value = true
  errorMessage.value = ''

  const params: NoteParams = {
    title: title.value.trim(),
    content: content.value,
    categoryId: categoryId.value || undefined,
  }

  try {
    let res
    if (isNew.value) {
      res = await noteStore.createNote(params)
    } else {
      res = await noteStore.updateNote(noteId.value, params)
    }

    if (res.code === 200) {
      router.push(isNew.value ? '/' : `/note/${noteId.value}`)
    } else {
      errorMessage.value = res.msg || '保存失败'
    }
  } catch (e: any) {
    errorMessage.value = e.message || '保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

function goBack() {
  if (title.value || content.value) {
    if (!confirm('有未保存的内容，确定要离开吗？')) return
  }
  router.back()
}

onMounted(() => {
  categoryStore.fetchCategories()
  loadNote()
})
</script>

<template>
  <div class="p-6 max-w-4xl mx-auto">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <template v-else>
      <div class="flex items-center justify-between mb-6">
        <button
          class="flex items-center gap-2 text-muted hover:text-ink transition-colors"
          @click="goBack"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            />
          </svg>
          返回
        </button>
        <div class="flex items-center gap-2">
          <button class="btn-secondary" :disabled="saving" @click="handleSave(true)">
            存为草稿
          </button>
          <button class="btn-primary" :disabled="saving" @click="handleSave(false)">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>

      <div v-if="errorMessage" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-md">
        <p class="text-sm text-red-600">{{ errorMessage }}</p>
      </div>

      <div class="bg-canvas border border-hairline rounded-lg overflow-hidden">
        <div class="p-4 border-b border-hairline">
          <input
            v-model="title"
            type="text"
            class="w-full text-title-lg text-ink bg-transparent border-none outline-none placeholder:text-muted"
            placeholder="请输入标题..."
          />
        </div>

        <div class="p-4 border-b border-hairline flex flex-wrap gap-1">
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="标题"
            @click="handleHeading"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
              />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="粗体"
            @click="execCommand('bold')"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 4h8a4 4 0 014 4 4 4 0 01-4 4H6z M6 12h9a4 4 0 014 4 4 4 0 01-4 4H6z"
              />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="斜体"
            @click="execCommand('italic')"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M10 4h4m-2 0v16m4-16h-4m0 16h4"
                transform="skewX(-10)"
              />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="下划线"
            @click="execCommand('underline')"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M7 8v4a5 5 0 0010 0V8M5 20h14"
              />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="删除线"
            @click="execCommand('strikeThrough')"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M17 10H7m10 4H7m5-8v12"
              />
            </svg>
          </button>
          <div class="w-px h-6 bg-hairline mx-1 self-center"></div>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="引用"
            @click="handleQuote"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
              />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="代码"
            @click="handleCode"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
              />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="链接"
            @click="handleLink"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
              />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="图片"
            @click="handleImage"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          </button>
          <div class="w-px h-6 bg-hairline mx-1 self-center"></div>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="无序列表"
            @click="handleList(false)"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 6h16M4 12h16M4 18h16"
              />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors"
            title="有序列表"
            @click="handleList(true)"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M7 20h14M7 12h14M7 4h14M3 20h.01M3 12h.01M3 4h.01"
              />
            </svg>
          </button>
        </div>

        <div class="p-4">
          <div class="mb-4">
            <label class="block text-label-md text-ink mb-2">分类</label>
            <select v-model="categoryId" class="input-base">
              <option :value="null">无分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>

          <div
            ref="editorRef"
            class="min-h-[400px] prose prose-slate max-w-none outline-none"
            contenteditable="true"
            @input="handleEditorInput"
            v-html="content"
          ></div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.prose :deep(h1) {
  @apply text-2xl text-ink font-medium mb-4;
}
.prose :deep(h2) {
  @apply text-xl text-ink font-medium mb-3 mt-6;
}
.prose :deep(h3) {
  @apply text-lg text-ink font-medium mb-2 mt-4;
}
.prose :deep(p) {
  @apply text-body mb-4 leading-relaxed;
}
.prose :deep(ul),
.prose :deep(ol) {
  @apply pl-6 mb-4;
}
.prose :deep(li) {
  @apply text-body mb-1;
}
.prose :deep(blockquote) {
  @apply border-l-4 border-signature-coral pl-4 italic text-muted my-4;
}
.prose :deep(code) {
  @apply bg-surface-soft px-1.5 py-0.5 rounded text-sm font-mono;
}
.prose :deep(pre) {
  @apply bg-surface-dark text-on-dark p-4 rounded-lg overflow-x-auto my-4;
}
.prose :deep(a) {
  @apply text-link hover:underline;
}
.prose :deep(img) {
  @apply max-w-full h-auto rounded-lg my-4;
}
</style>
