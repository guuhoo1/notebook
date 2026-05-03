<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNoteStore, useCategoryStore } from '@/stores'
import type { NoteParams } from '@/types'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

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
const markdownMode = ref(false)

const categories = computed(() => categoryStore.categories)

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
    } else {
      errorMessage.value = '笔记不存在'
    }
  } finally {
    loading.value = false
  }
}

async function handleSave(asDraft = false) {
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
    status: asDraft ? 0 : 1,
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
  <div class="p-6 max-w-4xl mx-auto h-[calc(100vh-80px)]">
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

      <div class="bg-canvas border border-hairline rounded-lg overflow-hidden flex flex-col h-[calc(100%-120px)]">
        <div class="p-4 border-b border-hairline">
          <input
            v-model="title"
            type="text"
            class="w-full text-title-lg text-ink bg-transparent border-none outline-none placeholder:text-muted"
            placeholder="请输入标题..."
          />
        </div>

        <div class="p-4 border-b border-hairline">
          <label class="block text-label-md text-ink mb-2">分类</label>
          <select v-model="categoryId" class="input-base">
            <option :value="null">无分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
        </div>

        <div class="flex-1 overflow-hidden">
          <MarkdownEditor v-model="content" v-model:markdown-mode="markdownMode" />
        </div>
      </div>
    </template>
  </div>
</template>
