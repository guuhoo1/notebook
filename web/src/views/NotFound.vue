<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push({ name: 'Home' })
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-canvas">
    <div class="text-center">
      <h1 class="text-display-lg text-ink mb-4">404</h1>
      <p class="text-body text-muted mb-6">页面不存在</p>
      <button class="btn-primary" @click="goHome">返回首页</button>
    </div>
  </div>
</template>
