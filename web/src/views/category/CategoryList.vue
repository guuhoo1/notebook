<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCategoryStore } from '@/stores'
import type { Category, CategoryParams } from '@/types'

const categoryStore = useCategoryStore()

const categories = computed(() => categoryStore.categories)
const loading = computed(() => categoryStore.loading)

const showEditor = ref(false)
const editingCategory = ref<Category | null>(null)
const formData = ref<CategoryParams>({
  name: '',
  description: '',
  color: '#181d26',
  icon: '',
})
const saving = ref(false)
const errorMessage = ref('')

const predefinedColors = [
  '#aa2d00',
  '#0a2e0e',
  '#1b61c9',
  '#006400',
  '#f4d35e',
  '#fcab79',
  '#a8d8c4',
  '#d9a441',
  '#181d26',
  '#333840',
]

function openCreateModal() {
  editingCategory.value = null
  formData.value = {
    name: '',
    description: '',
    color: '#181d26',
    icon: '',
  }
  errorMessage.value = ''
  showEditor.value = true
}

function openEditModal(category: Category) {
  editingCategory.value = category
  formData.value = {
    name: category.name,
    description: category.description || '',
    color: category.color,
    icon: category.icon || '',
  }
  errorMessage.value = ''
  showEditor.value = true
}

function closeEditor() {
  showEditor.value = false
  editingCategory.value = null
  errorMessage.value = ''
}

async function handleSave() {
  if (!formData.value.name.trim()) {
    errorMessage.value = '请输入分类名称'
    return
  }

  saving.value = true
  errorMessage.value = ''

  try {
    let res
    if (editingCategory.value) {
      res = await categoryStore.updateCategory(editingCategory.value.id, formData.value)
    } else {
      res = await categoryStore.createCategory(formData.value)
    }

    if (res.code === 200) {
      closeEditor()
    } else {
      errorMessage.value = res.msg || '保存失败'
    }
  } catch (e: any) {
    errorMessage.value = e.message || '保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

async function handleDelete(category: Category) {
  if (!confirm(`确定要删除分类「${category.name}」吗？\n该分类下的笔记将变为无分类状态。`)) return
  await categoryStore.deleteCategory(category.id)
}

onMounted(() => {
  categoryStore.fetchCategories()
})
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-title-lg text-ink">分类管理</h1>
        <p class="text-body text-muted mt-1">共 {{ categories.length }} 个分类</p>
      </div>
      <button class="btn-primary flex items-center gap-2" @click="openCreateModal">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          />
        </svg>
        新建分类
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <div v-else-if="categories.length === 0" class="text-center py-12">
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
          d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
        />
      </svg>
      <p class="text-body text-muted">暂无分类</p>
      <button class="btn-secondary mt-4" @click="openCreateModal">创建第一个分类</button>
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div v-for="category in categories" :key="category.id" class="card group relative">
        <div class="flex items-start justify-between mb-2">
          <div class="flex items-center gap-3">
            <div class="w-4 h-4 rounded-full" :style="{ backgroundColor: category.color }"></div>
            <h3 class="text-label-md text-ink">{{ category.name }}</h3>
          </div>
          <span class="text-xs text-muted">{{ category.noteCount }} 篇笔记</span>
        </div>
        <p v-if="category.description" class="text-body text-muted text-sm mb-3">
          {{ category.description }}
        </p>
        <div class="flex items-center justify-between text-xs text-muted">
          <span>创建于 {{ new Date(category.createTime).toLocaleDateString('zh-CN') }}</span>
        </div>
        <div
          class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity flex gap-1"
        >
          <button
            class="p-1.5 bg-surface-soft rounded hover:bg-surface-strong"
            title="编辑"
            @click="openEditModal(category)"
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
            @click="handleDelete(category)"
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

    <div
      v-if="showEditor"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="closeEditor"
    >
      <div class="bg-canvas rounded-lg shadow-xl w-full max-w-md p-6">
        <h2 class="text-title-md text-ink mb-4">
          {{ editingCategory ? '编辑分类' : '新建分类' }}
        </h2>

        <div v-if="errorMessage" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-md">
          <p class="text-sm text-red-600">{{ errorMessage }}</p>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-label-md text-ink mb-2">名称 *</label>
            <input
              v-model="formData.name"
              type="text"
              class="input-base"
              placeholder="请输入分类名称"
            />
          </div>

          <div>
            <label class="block text-label-md text-ink mb-2">描述</label>
            <textarea
              v-model="formData.description"
              class="input-base min-h-[80px] resize-none"
              placeholder="请输入分类描述（可选）"
            ></textarea>
          </div>

          <div>
            <label class="block text-label-md text-ink mb-2">颜色</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="color in predefinedColors"
                :key="color"
                class="w-8 h-8 rounded-full border-2 transition-transform hover:scale-110"
                :class="formData.color === color ? 'border-ink' : 'border-transparent'"
                :style="{ backgroundColor: color }"
                @click="formData.color = color"
              ></button>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-2 mt-6">
          <button class="btn-secondary" @click="closeEditor">取消</button>
          <button class="btn-primary" :disabled="saving" @click="handleSave">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
