<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore, useAuthStore } from '@/stores'

const router = useRouter()
const categoryStore = useCategoryStore()
const authStore = useAuthStore()

const activeItem = ref('all')

const categories = computed(() => categoryStore.categories)

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await categoryStore.fetchCategories()
  }
})

function selectAllNotes() {
  activeItem.value = 'all'
  categoryStore.setActiveCategory(null)
  router.push({ path: '/', query: { status: undefined } })
}

function selectArchived() {
  activeItem.value = 'archived'
  categoryStore.setActiveCategory(null)
  router.push({ path: '/', query: { status: 'archived' } })
}

function selectCategory(categoryId: number) {
  activeItem.value = `category-${categoryId}`
  const category = categories.value.find((c) => c.id === categoryId)
  categoryStore.setActiveCategory(category || null)
  router.push({ path: '/', query: { category: categoryId } })
}
</script>

<template>
  <aside
    v-if="authStore.isAuthenticated"
    class="hidden md:block fixed left-0 top-16 bottom-0 w-60 bg-surface-soft border-r border-hairline overflow-y-auto"
  >
    <nav class="p-4">
      <div class="space-y-1">
        <button
          :class="[
            'w-full h-11 px-4 flex items-center rounded-md text-left transition-colors',
            activeItem === 'all' ? 'bg-canvas text-ink' : 'text-body hover:bg-canvas/50',
          ]"
          @click="selectAllNotes"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
            />
          </svg>
          <span class="flex-1">全部笔记</span>
        </button>

        <button
          :class="[
            'w-full h-11 px-4 flex items-center rounded-md text-left transition-colors',
            activeItem === 'archived' ? 'bg-canvas text-ink' : 'text-body hover:bg-canvas/50',
          ]"
          @click="selectArchived"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4"
            />
          </svg>
          <span class="flex-1">归档</span>
        </button>
      </div>

      <div class="mt-6">
        <div class="flex items-center justify-between px-4 mb-2">
          <span class="text-caption text-muted uppercase tracking-wide">分类</span>
          <router-link to="/category" class="text-muted hover:text-ink transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 4v16m8-8H4"
              />
            </svg>
          </router-link>
        </div>
        <div v-if="categoryStore.loading" class="px-4 py-2 text-caption text-muted">加载中...</div>
        <div
          v-else-if="categoryStore.categories.length === 0"
          class="px-4 py-2 text-caption text-muted"
        >
          暂无分类
        </div>
        <div v-else class="space-y-1">
          <button
            v-for="category in categories"
            :key="category.id"
            :class="[
              'w-full h-11 px-4 flex items-center rounded-md text-left transition-colors',
              activeItem === `category-${category.id}`
                ? 'bg-canvas text-ink'
                : 'text-body hover:bg-canvas/50',
            ]"
            @click="selectCategory(category.id)"
          >
            <span class="w-3 h-3 rounded-full mr-3" :style="{ backgroundColor: category.color }" />
            <span class="flex-1 truncate">{{ category.name }}</span>
            <span class="text-caption text-muted">{{ category.noteCount }}</span>
          </button>
        </div>
      </div>
    </nav>
  </aside>
</template>
