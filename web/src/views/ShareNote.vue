<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

const route = useRoute()
const shareCode = ref('')
const note = ref<any>(null)
const loading = ref(true)
const error = ref('')
const shareUrl = ref('')

async function loadNote() {
  shareCode.value = route.params.shareCode as string
  shareUrl.value = `${window.location.origin}/share/${shareCode.value}`
  
  try {
    const res = await fetch(`/api/share/${shareCode.value}`)
    const data = await res.json()
    
    if (data.code === 200) {
      note.value = data.data
    } else {
      error.value = data.msg || '获取笔记失败'
    }
  } catch (e) {
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
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

onMounted(() => {
  loadNote()
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
        <header class="px-6 py-6 border-b border-hairline">
          <div class="flex items-center justify-between mb-4">
            <span class="px-3 py-1 text-xs font-medium text-green-600 bg-green-50 rounded-full">
              公开分享
            </span>
            <span class="text-sm text-muted">浏览 {{ note.shareViewCount || 0 }} 次</span>
          </div>
          <h1 class="text-2xl font-bold text-ink">{{ note.title }}</h1>
          <p class="mt-2 text-sm text-muted">{{ formatDate(note.createTime) }}</p>
        </header>

        <!-- 内容区域 -->
        <article class="px-6 py-6">
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

      <!-- 返回首页按钮 -->
      <div class="mt-6 text-center">
        <a href="/" class="text-primary hover:underline text-sm">
          返回记事本首页
        </a>
      </div>
    </div>
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

</style>
