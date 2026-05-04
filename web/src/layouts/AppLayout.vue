<script setup lang="ts">
import { ref } from 'vue'
import { RouterView } from 'vue-router'
import TopNav from '@/components/layout/TopNav.vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import { useAuthStore } from '@/stores'

const authStore = useAuthStore()
const sidebarOpen = ref(false)

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function closeSidebar() {
  sidebarOpen.value = false
}
</script>

<template>
  <div class="min-h-screen bg-canvas">
    <TopNav @toggle-sidebar="toggleSidebar" />
    
    <div class="fixed inset-0 z-30 bg-black/50 md:hidden" 
         :class="{ 'opacity-100 pointer-events-auto': sidebarOpen, 'opacity-0 pointer-events-none': !sidebarOpen }"
         @click="closeSidebar">
    </div>
    
    <div class="flex pt-16">
      <Sidebar :open="sidebarOpen" @close="closeSidebar" />
      <main
        class="flex-1 min-h-[calc(100vh-64px)] transition-all duration-300"
        :class="{ 
          'md:ml-60': authStore.isAuthenticated,
          'translate-x-0': !sidebarOpen,
          'fixed inset-0 z-40': sidebarOpen && !authStore.isAuthenticated,
        }"
      >
        <RouterView />
      </main>
    </div>
  </div>
</template>
