<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores'
import type { RegisterParams } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const form = ref<RegisterParams & { confirmPassword: string }>({
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)

const passwordStrength = computed(() => {
  const pwd = form.value.password
  if (!pwd) return { level: 0, text: '', color: '' }
  if (pwd.length < 6) return { level: 1, text: '弱', color: 'bg-red-500' }
  if (pwd.length < 10 || !/\d/.test(pwd) || !/[a-zA-Z]/.test(pwd)) {
    return { level: 2, text: '中', color: 'bg-yellow-500' }
  }
  return { level: 3, text: '强', color: 'bg-green-500' }
})

function validateEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function validatePhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

async function handleRegister() {
  errorMessage.value = ''

  if (!form.value.nickname.trim()) {
    errorMessage.value = '请输入昵称'
    return
  }
  if (form.value.nickname.length < 2 || form.value.nickname.length > 20) {
    errorMessage.value = '昵称长度应为2-20个字符'
    return
  }

  if (!form.value.email && !form.value.phone) {
    errorMessage.value = '邮箱或手机号至少填写一项'
    return
  }

  if (form.value.email && !validateEmail(form.value.email)) {
    errorMessage.value = '邮箱格式不正确'
    return
  }

  if (form.value.phone && !validatePhone(form.value.phone)) {
    errorMessage.value = '手机号格式不正确'
    return
  }

  if (!form.value.password) {
    errorMessage.value = '请输入密码'
    return
  }
  if (form.value.password.length < 6 || form.value.password.length > 20) {
    errorMessage.value = '密码长度应为6-20个字符'
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  loading.value = true

  try {
    const res = await authStore.register({
      email: form.value.email || undefined,
      phone: form.value.phone || undefined,
      password: form.value.password,
      nickname: form.value.nickname,
    })

    if (res.code === 200) {
      router.push('/')
    } else {
      errorMessage.value = res.msg || '注册失败'
    }
  } catch (e: any) {
    errorMessage.value = e.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-canvas px-4 py-8">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <h1 class="text-display-md text-ink mb-2">创建账号</h1>
        <p class="text-body text-muted">开始记录生活</p>
      </div>

      <div class="bg-canvas border border-hairline rounded-lg p-8">
        <form @submit.prevent="handleRegister">
          <div class="space-y-4">
            <div>
              <label class="block text-label-md text-ink mb-2"
                >昵称 <span class="text-red-500">*</span></label
              >
              <input
                v-model="form.nickname"
                type="text"
                class="input-base"
                placeholder="请输入昵称（2-20字符）"
                maxlength="20"
              />
            </div>

            <div>
              <label class="block text-label-md text-ink mb-2">邮箱</label>
              <input
                v-model="form.email"
                type="email"
                class="input-base"
                placeholder="请输入邮箱"
                autocomplete="email"
              />
            </div>

            <div>
              <label class="block text-label-md text-ink mb-2">手机号</label>
              <input
                v-model="form.phone"
                type="tel"
                class="input-base"
                placeholder="请输入手机号"
                autocomplete="tel"
              />
              <p class="text-caption text-muted mt-1">邮箱或手机号至少填写一项</p>
            </div>

            <div>
              <label class="block text-label-md text-ink mb-2"
                >密码 <span class="text-red-500">*</span></label
              >
              <div class="relative">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="input-base pr-10"
                  placeholder="请输入密码（6-20字符）"
                  maxlength="20"
                  autocomplete="new-password"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-ink"
                  @click="showPassword = !showPassword"
                >
                  <svg
                    v-if="!showPassword"
                    class="w-5 h-5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                    />
                  </svg>
                  <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
                    />
                  </svg>
                </button>
              </div>
              <div v-if="form.password" class="flex items-center gap-2 mt-2">
                <div class="flex-1 h-1.5 bg-surface-strong rounded-full overflow-hidden">
                  <div
                    :class="['h-full transition-all', passwordStrength.color]"
                    :style="{ width: `${passwordStrength.level * 33.33}%` }"
                  />
                </div>
                <span class="text-caption text-muted">{{ passwordStrength.text }}</span>
              </div>
            </div>

            <div>
              <label class="block text-label-md text-ink mb-2"
                >确认密码 <span class="text-red-500">*</span></label
              >
              <input
                v-model="form.confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                class="input-base"
                placeholder="请再次输入密码"
                maxlength="20"
                autocomplete="new-password"
              />
            </div>

            <div v-if="errorMessage" class="p-3 bg-red-50 border border-red-200 rounded-md">
              <p class="text-sm text-red-600">{{ errorMessage }}</p>
            </div>

            <button type="submit" class="btn-primary w-full" :disabled="loading">
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </div>
        </form>

        <div class="mt-6 text-center">
          <span class="text-body text-muted">已有账号？</span>
          <button class="text-link hover:underline ml-1" @click="goLogin">立即登录</button>
        </div>
      </div>
    </div>
  </div>
</template>
