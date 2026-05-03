<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores'
import type { LoginParams } from '@/types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = ref<LoginParams>({
  account: '',
  password: '',
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)

async function handleLogin() {
  if (!form.value.account.trim()) {
    errorMessage.value = '请输入邮箱或手机号'
    return
  }
  if (!form.value.password) {
    errorMessage.value = '请输入密码'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const res = await authStore.login(form.value)
    if (res.code === 200) {
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } else {
      errorMessage.value = res.msg || '登录失败'
    }
  } catch (e: any) {
    errorMessage.value = e.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goRegister() {
  router.push('/register')
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-canvas px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <h1 class="text-display-md text-ink mb-2">欢迎回来</h1>
        <p class="text-body text-muted">登录您的账号</p>
      </div>

      <div class="bg-canvas border border-hairline rounded-lg p-8">
        <form @submit.prevent="handleLogin">
          <div class="space-y-4">
            <div>
              <label class="block text-label-md text-ink mb-2">邮箱/手机号</label>
              <input
                v-model="form.account"
                type="text"
                class="input-base"
                placeholder="请输入邮箱或手机号"
                autocomplete="username"
              />
            </div>

            <div>
              <label class="block text-label-md text-ink mb-2">密码</label>
              <div class="relative">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="input-base pr-10"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-ink"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="!showPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                  </svg>
                </button>
              </div>
            </div>

            <div v-if="errorMessage" class="p-3 bg-red-50 border border-red-200 rounded-md">
              <p class="text-sm text-red-600">{{ errorMessage }}</p>
            </div>

            <button
              type="submit"
              class="btn-primary w-full"
              :disabled="loading"
            >
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </div>
        </form>

        <div class="mt-6 text-center">
          <span class="text-body text-muted">还没有账号？</span>
          <button class="text-link hover:underline ml-1" @click="goRegister">
            立即注册
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
