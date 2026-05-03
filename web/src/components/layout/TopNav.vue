<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref } from 'vue'

const router = useRouter()
const searchQuery = ref('')

function handleSearch() {
  if (searchQuery.value.trim()) {
    console.log('Search:', searchQuery.value)
  }
}

function createNote() {
  router.push({ name: 'NoteNew' })
}
</script>

<template>
  <header class="fixed top-0 left-0 right-0 h-16 bg-canvas border-b border-hairline z-50">
    <div class="h-full px-4 md:px-6 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <router-link to="/" class="text-title-md text-ink font-semibold">
          记事本
        </router-link>
      </div>
      
      <div class="hidden md:flex flex-1 max-w-md mx-8">
        <div class="relative w-full">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索笔记..."
            class="input-base pl-10"
            @keyup.enter="handleSearch"
          />
          <svg
            class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted"
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
        </div>
      </div>
      
      <div class="flex items-center gap-3">
        <button class="btn-primary text-sm" @click="createNote">
          新建笔记
        </button>
        <div class="w-8 h-8 rounded-full bg-surface-strong flex items-center justify-center">
          <span class="text-on-dark text-sm">U</span>
        </div>
      </div>
    </div>
  </header>
</template>
