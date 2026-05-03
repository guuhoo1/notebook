<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores'
import { onMounted } from 'vue'

const authStore = useAuthStore()

onMounted(async () => {
  if (authStore.isAuthenticated && !authStore.user) {
    await authStore.fetchUserInfo()
  }
})
</script>

<template>
  <RouterView />
</template>

<style scoped>
</style>
