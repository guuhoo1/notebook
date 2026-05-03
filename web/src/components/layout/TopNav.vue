<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores'

const router = useRouter()
const authStore = useAuthStore()
const searchQuery = ref('')
const showUserMenu = ref(false)

onMounted(async () => {
  if (authStore.isAuthenticated && !authStore.user) {
    await authStore.fetchUserInfo()
  }
})

function handleSearch() {
  if (searchQuery.value.trim()) {
    console.log('Search:', searchQuery.value)
  }
}

function createNote() {
  router.push({ name: 'NoteNew' })
}

function goLogin() {
  router.push({ name: 'Login' })
}

function goSettings() {
  router.push({ name: 'Settings' })
  showUserMenu.value = false
}

async function handleLogout() {
  showUserMenu.value = false
  await authStore.logout()
  router.push({ name: 'Login' })
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function closeUserMenu() {
  showUserMenu.value = false
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
        <button v-if="authStore.isAuthenticated" class="btn-primary text-sm" @click="createNote">
          新建笔记
        </button>
        
        <div v-if="authStore.isAuthenticated" class="relative">
          <button
            class="w-8 h-8 rounded-full bg-surface-strong flex items-center justify-center hover:bg-primary transition-colors"
            @click="toggleUserMenu"
            @blur="closeUserMenu"
          >
            <span v-if="authStore.user?.avatar" class="w-full h-full rounded-full overflow-hidden">
              <img :src="authStore.user.avatar" alt="avatar" class="w-full h-full object-cover" />
            </span>
            <span v-else class="text-on-dark text-sm font-medium">
              {{ authStore.user?.nickname?.charAt(0) || 'U' }}
            </span>
          </button>
          
          <div
            v-if="showUserMenu"
            class="absolute right-0 top-full mt-2 w-48 bg-canvas border border-hairline rounded-md shadow-lg py-1"
          >
            <div class="px-4 py-2 border-b border-hairline">
              <p class="text-label-md text-ink truncate">{{ authStore.user?.nickname }}</p>
              <p class="text-caption text-muted truncate">{{ authStore.user?.email || authStore.user?.phone }}</p>
            </div>
            <button
              class="w-full px-4 py-2 text-left text-body-md text-body hover:bg-surface-soft"
              @click="goSettings"
            >
              设置
            </button>
            <button
              class="w-full px-4 py-2 text-left text-body-md text-red-600 hover:bg-surface-soft"
              @click="handleLogout"
            >
              退出登录
            </button>
          </div>
        </div>
        
        <button v-else class="btn-primary text-sm" @click="goLogin">
          登录
        </button>
      </div>
    </div>
  </header>
</template>
