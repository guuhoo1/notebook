<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { useAuthStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const shareCode = ref('')
const note = ref<any>(null)
const authorName = ref('')
const authorAvatar = ref('')
const loading = ref(true)
const error = ref('')
const shareUrl = ref('')
const showBackTop = ref(false)

async function loadNote() {
  shareCode.value = route.params.shareCode as string
  shareUrl.value = `${window.location.origin}/share/${shareCode.value}`
  
  try {
    const res = await fetch(`/api/share/${shareCode.value}`)
    const data = await res.json()
    
    if (data.code === 200) {
      note.value = data.data.note
      authorName.value = data.data.authorName || '匿名用户'
      authorAvatar.value = data.data.authorAvatar
      
      // 保存到最近浏览历史
      saveToRecentHistory({
        noteId: data.data.note.id,
        shareCode: shareCode.value,
        title: data.data.note.title,
        authorName: authorName.value,
        authorAvatar: authorAvatar.value,
        visitTime: new Date().toISOString(),
        shareViewCount: data.data.note.shareViewCount
      })
    } else {
      error.value = data.msg || '获取笔记失败'
    }
  } catch (e) {
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

function saveToRecentHistory(item: any) {
  try {
    const key = 'notebook_recent_shares'
    let history = []
    const existing = localStorage.getItem(key)
    if (existing) {
      history = JSON.parse(existing)
    }
    
    // 移除重复记录
    history = history.filter((h: any) => h.shareCode !== item.shareCode)
    
    // 插入到最前面
    history.unshift(item)
    
    // 只保留最近20条
    if (history.length > 20) {
      history = history.slice(0, 20)
    }
    
    localStorage.setItem(key, JSON.stringify(history))
  } catch (e) {
    console.error('保存浏览历史失败', e)
  }
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function handleBackHome() {
  if (!authStore.token) {
    if (confirm('暂未登录，是否返回登录页？')) {
      router.push({ name: 'Login' })
    }
  } else {
    router.push({ name: 'Home' })
  }
}

function handleScroll() {
  showBackTop.value = window.scrollY > 300
}

function scrollToTop() {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  loadNote()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="share-page min-h-screen bg-surface">
    <div class="max-w-3xl mx-auto px-4 py-6">
      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center h-64">
        <div class="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="text-center py-20">
        <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-red-50 flex items-center justify-center">
          <svg class="w-10 h-10 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h2 class="text-xl font-semibold text-ink mb-2">分享链接无效</h2>
        <p class="text-muted">{{ error }}</p>
        <button class="mt-6 btn-primary" @click="loadNote">
          重试
        </button>
      </div>

      <!-- 笔记内容 -->
      <div v-else class="bg-canvas rounded-xl shadow-sm overflow-hidden">
        <!-- 头部 -->
        <header class="border-b border-hairline">
          <div class="flex items-center justify-between mb-4">
            <span class="px-3 py-1 text-xs font-medium text-green-600 bg-green-50 rounded-full">
              公开分享
            </span>
            <span class="text-sm text-muted">浏览 {{ note.shareViewCount || 0 }} 次</span>
          </div>
          <!-- 分享人信息 -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center overflow-hidden">
                <img v-if="authorAvatar" :src="authorAvatar" alt="头像" class="w-full h-full object-cover" />
                <div v-else class="text-primary font-bold">{{ authorName.charAt(0).toUpperCase() }}</div>
              </div>
              <div>
                <div class="font-medium text-ink">{{ authorName }}</div>
                <div class="text-xs text-muted">分享于 {{ formatDate(note.createTime) }}</div>
              </div>
            </div>
            <button class="text-primary hover:underline text-sm" @click="handleBackHome">
              返回记事本首页
            </button>
          </div>
          <h1 class="px-6 py-4 text-2xl font-bold text-ink">{{ note.title }}</h1>
        </header>

        <!-- 内容区域 -->
        <article>
          <MdPreview :modelValue="note.mdContent" class="prose prose-gray max-w-none" />
        </article>

        <!-- 底部 -->
        <footer class="px-6 py-4 border-t border-hairline bg-surface-soft">
          <div class="flex items-center justify-center gap-4 text-sm text-muted">
            <span>记事本分享</span>
            <span class="w-1 h-1 rounded-full bg-muted"></span>
            <span>分享链接: {{ shareUrl }}</span>
          </div>
        </footer>
      </div>
    </div>

    <!-- 回到顶部按钮 -->
    <Transition name="fade">
      <button
        v-if="showBackTop"
        class="back-top-btn"
        @click="scrollToTop"
        title="返回顶部"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18" />
        </svg>
      </button>
    </Transition>
  </div>
</template>

<style scoped>
.share-page {
  padding-top: 1rem;
}

@media (min-width: 768px) {
  .share-page {
    padding-top: 2rem;
  }
}

.prose h1,
.prose h2,
.prose h3,
.prose h4,
.prose h5,
.prose h6 {
  color: #1f2937;
  font-weight: 600;
}

.prose p {
  color: #4b5563;
  line-height: 1.8;
}

.prose code {
  background-color: #f3f4f6;
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  font-size: 0.875em;
}

.prose pre {
  background-color: #1f2937;
  border-radius: 0.5rem;
  padding: 1rem;
  overflow-x: auto;
}

.prose pre code {
  background-color: transparent;
  padding: 0;
  color: #e5e7eb;
}

.prose blockquote {
  border-left: 3px solid #3b82f6;
  padding-left: 1rem;
  color: #6b7280;
  font-style: italic;
}

.prose ul,
.prose ol {
  padding-left: 1.5rem;
}

.prose li {
  margin-bottom: 0.25rem;
}

.prose a {
  color: #3b82f6;
  text-decoration: underline;
}

.prose img {
  max-width: 100%;
  border-radius: 0.5rem;
}

.prose table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

.prose th,
.prose td {
  border: 1px solid #e5e7eb;
  padding: 0.5rem;
  text-align: left;
}

.prose th {
  background-color: #f9fafb;
  font-weight: 600;
}

:deep(.md-editor-preview-wrapper) {
  padding: 0;
  margin: 0;
}

.back-top-btn {
  position: fixed;
  right: 1rem;
  bottom: 3rem;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
  z-index: 100;
}

.back-top-btn:hover {
  background-color: #3b82f6;
  color: #ffffff;
  border-color: #3b82f6;
  transform: translateY(-2px);
}

.back-top-btn:active {
  transform: translateY(0);
}

@media (min-width: 768px) {
  .back-top-btn {
    right: calc((100% - 48rem) / 2 + 1rem);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

</style>
