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
  <div class="app-layout">
    <TopNav @toggle-sidebar="toggleSidebar" />
    
    <div 
      class="sidebar-overlay" 
      :class="{ active: sidebarOpen }"
      @click="closeSidebar"
    ></div>
    
    <div class="app-body">
      <Sidebar :open="sidebarOpen" @close="closeSidebar" />
      <main
        class="app-main"
        :class="{ 'md:ml-60': authStore.isAuthenticated }"
      >
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  background-color: #ffffff;
  max-width: 100vw;
  overflow-x: hidden;
}

.sidebar-overlay {
  position: fixed;
  inset: 0;
  z-index: 30;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
}

.sidebar-overlay.active {
  opacity: 1;
  pointer-events: auto;
}

@media (min-width: 768px) {
  .sidebar-overlay {
    display: none;
  }
}

.app-body {
  display: flex;
  padding-top: 64px;
  max-width: 100vw;
  overflow-x: hidden;
}

.app-main {
  flex: 1;
  min-height: calc(100vh - 64px);
  min-width: 0;
  max-width: 100vw;
  overflow-x: hidden;
  transition: margin-left 0.3s;
}
</style>
