<script setup lang="ts">
import { RouterView } from 'vue-router'
import TopNav from '@/components/layout/TopNav.vue'
import Sidebar from '@/components/layout/Sidebar.vue'
</script>

<template>
  <div class="min-h-screen bg-canvas">
    <TopNav />
    <div class="flex pt-16">
      <Sidebar />
      <main class="flex-1 min-h-[calc(100vh-64px)] ml-0 md:ml-60">
        <RouterView />
      </main>
    </div>
  </div>
</template>
