<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNoteStore, useCategoryStore } from '@/stores'
import type { NoteParams } from '@/types'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import { useLocalStorage } from '@vueuse/core'

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

// 宽度预设配置
const WIDTH_PRESETS = {
  narrow: { label: '窄版', value: 600 },
  standard: { label: '标准版', value: 800 },
  wide: { label: '宽版', value: 1000 },
  full: { label: '全屏', value: 'full' }
} as const

type WidthPreset = keyof typeof WIDTH_PRESETS

// 使用 localStorage 保存用户偏好
const currentWidthPreset = useLocalStorage<WidthPreset>('editor-width-preset', 'standard')

// 计算当前宽度样式
const editorWidthStyle = computed(() => {
  const preset = WIDTH_PRESETS[currentWidthPreset.value]
  if (preset.value === 'full') {
    return { maxWidth: '100%', width: '100%' }
  }
  return { maxWidth: `${preset.value}px`, width: '100%' }
})

// 切换宽度预设
function setWidthPreset(preset: WidthPreset) {
  currentWidthPreset.value = preset
}

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
  <div class="p-3 md:p-4 mx-auto h-[calc(100vh-64px)] flex flex-col transition-all duration-300" :style="editorWidthStyle">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <template v-else>
      <!-- 顶部栏：返回、分类、宽度选择、保存按钮 -->
      <div class="flex items-center justify-between mb-3 gap-2 md:gap-3">
        <div class="flex items-center gap-2 md:gap-3">
          <button
            class="btn-icon"
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
          
          <!-- 分类选择（紧凑样式） -->
          <div class="hidden sm:flex items-center gap-2">
            <span class="text-sm text-muted">分类:</span>
            <select v-model="categoryId" class="text-sm px-3 py-2 bg-canvas border border-hairline rounded-md text-ink focus:outline-none focus:ring-1 focus:ring-info-border focus:border-info-border min-h-[44px]">
              <option :value="null">无分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="flex items-center gap-2">
          <!-- 宽度预设选择器（紧凑样式）- 移动端隐藏 -->
          <div class="hidden md:flex items-center gap-1 bg-surface-soft rounded-md p-0.5">
            <button
              v-for="(preset, key) in WIDTH_PRESETS"
              :key="key"
              @click="setWidthPreset(key as WidthPreset)"
              :class="[
                'px-2.5 py-1 text-xs rounded transition-all duration-200',
                currentWidthPreset === key
                  ? 'bg-primary text-white'
                  : 'text-muted hover:text-ink hover:bg-canvas'
              ]"
            >
              {{ preset.label }}
            </button>
          </div>
          
          <button class="btn-secondary text-sm px-3 md:px-4 py-2" :disabled="saving" @click="handleSave(true)">
            <span class="hidden sm:inline">草稿</span>
            <span class="sm:hidden">草</span>
          </button>
          <button class="btn-primary text-sm px-3 md:px-4 py-2" :disabled="saving" @click="handleSave(false)">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>

      <!-- 移动端分类选择 -->
      <div class="sm:hidden mb-3">
        <select v-model="categoryId" class="w-full text-sm px-4 py-2 bg-canvas border border-hairline rounded-md text-ink focus:outline-none focus:ring-1 focus:ring-info-border focus:border-info-border min-h-[44px]">
          <option :value="null">无分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.name }}
          </option>
        </select>
      </div>

      <div v-if="errorMessage" class="mb-3 p-2.5 bg-red-50 border border-red-200 rounded-md transition-all duration-300">
        <p class="text-xs text-red-600">{{ errorMessage }}</p>
      </div>

      <div class="bg-canvas border border-hairline rounded-lg overflow-hidden flex flex-col flex-1 min-h-0 transition-all duration-300">
        <!-- 标题输入（紧凑样式） -->
        <div class="px-4 py-3 border-b border-hairline">
          <input
            v-model="title"
            type="text"
            class="w-full text-xl text-ink bg-transparent border-none outline-none placeholder:text-muted transition-all duration-200"
            placeholder="请输入标题..."
          />
        </div>

        <!-- 编辑器区域 -->
        <div class="flex-1 overflow-hidden min-h-0">
          <MarkdownEditor v-model="content" v-model:markdown-mode="markdownMode" />
        </div>
      </div>
    </template>
  </div>
</template>
