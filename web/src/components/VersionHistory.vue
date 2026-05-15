<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { noteApi } from '@/api'
import type { NoteVersion } from '@/types'

const props = defineProps<{
  noteId: number
  visible: boolean
}>()

const emit = defineEmits<{
  'close': []
  'restore': [version: NoteVersion]
}>()

const versions = ref<NoteVersion[]>([])
const loading = ref(false)
const selectedVersion = ref<NoteVersion | null>(null)

async function loadVersions() {
  if (!props.noteId) return
  loading.value = true
  try {
    const result = await noteApi.getVersions(props.noteId)
    if (result.code === 200 && result.data) {
      versions.value = result.data
      if (versions.value.length > 0) {
        selectedVersion.value = versions.value[0]
      }
    }
  } catch (e) {
    console.error('加载版本列表失败', e)
  } finally {
    loading.value = false
  }
}

async function handleRestore(version: NoteVersion) {
  if (!confirm(`确定要恢复到版本 ${version.versionNumber} 吗？`)) {
    return
  }
  try {
    const result = await noteApi.restoreVersion(props.noteId, version.versionNumber)
    if (result.code === 200) {
      emit('restore', version)
      emit('close')
    }
  } catch (e) {
    console.error('恢复版本失败', e)
  }
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

watch(() => props.visible, (val) => {
  if (val) {
    loadVersions()
  }
})

const hasVersions = computed(() => versions.value.length > 0)
</script>

<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 z-50 flex items-center justify-center"
      style="background-color: rgba(0, 0, 0, 0.5);"
      @click.self="emit('close')"
    >
      <div class="bg-canvas rounded-lg w-full max-w-2xl max-h-[80vh] overflow-hidden shadow-xl">
        <div class="flex items-center justify-between px-6 py-4 border-b border-hairline">
          <h2 class="text-title-md text-ink">版本历史</h2>
          <button class="p-2 hover:bg-surface-soft rounded-lg" @click="emit('close')">
            <svg class="w-5 h-5 text-body" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="flex h-[calc(80vh-64px)]">
          <div class="w-1/2 border-r border-hairline overflow-y-auto">
            <div v-if="loading" class="flex justify-center py-12">
              <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
            </div>

            <div v-else-if="!hasVersions" class="flex flex-col items-center justify-center py-12 text-muted">
              <svg class="w-12 h-12 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p>暂无版本记录</p>
            </div>

            <div v-else class="divide-y divide-hairline">
              <button
                v-for="version in versions"
                :key="version.id"
                :class="[
                  'w-full px-4 py-3 text-left transition-colors',
                  selectedVersion?.id === version.id ? 'bg-surface-soft' : 'hover:bg-surface-soft/50',
                ]"
                @click="selectedVersion = version"
              >
                <div class="flex items-center justify-between mb-1">
                  <span class="text-label-md text-ink">版本 {{ version.versionNumber }}</span>
                  <span class="text-xs text-muted">{{ formatDate(version.createdAt) }}</span>
                </div>
                <p class="text-body text-muted text-sm truncate">{{ version.title }}</p>
              </button>
            </div>
          </div>

          <div class="w-1/2 overflow-y-auto p-4">
            <div v-if="!selectedVersion" class="flex flex-col items-center justify-center h-full text-muted">
              <svg class="w-12 h-12 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" />
              </svg>
              <p>选择一个版本查看</p>
            </div>

            <div v-else class="space-y-4">
              <div class="flex items-center justify-between">
                <h3 class="text-title-sm text-ink">版本 {{ selectedVersion.versionNumber }}</h3>
                <button
                  class="btn-primary text-sm"
                  @click="handleRestore(selectedVersion)"
                >
                  恢复此版本
                </button>
              </div>

              <div class="text-sm text-muted">
                创建时间：{{ formatDate(selectedVersion.createdAt) }}
              </div>

              <div class="border border-hairline rounded-lg p-4 bg-surface-soft">
                <p class="text-label-md text-ink mb-2">标题</p>
                <p class="text-body text-ink">{{ selectedVersion.title }}</p>
              </div>

              <div class="border border-hairline rounded-lg p-4 bg-surface-soft">
                <p class="text-label-md text-ink mb-2">内容预览</p>
                <div class="text-body text-ink whitespace-pre-wrap break-all max-h-48 overflow-y-auto">
                  {{ selectedVersion.summary || (selectedVersion.content ? selectedVersion.content.substring(0, 500) + '...' : '无内容') }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
