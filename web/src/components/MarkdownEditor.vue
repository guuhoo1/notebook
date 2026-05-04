<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import { MdEditor, config, type ToolbarNames } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import hljs from 'highlight.js'
import { useEditorHistory, useAutoSave, useEditorLayout } from '@/composables/useEditor'
import { parseMarkdown } from '@/utils/markdown'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'save': []
}>()

const {
  layoutMode,
  setLayoutMode
} = useEditorLayout()

const editorRef = ref()
const localValue = ref(props.modelValue)
const compiledHtml = ref('')

const history = useEditorHistory(props.modelValue)

const autoSave = useAutoSave(
  () => localValue.value,
  async () => {
    emit('save')
  },
  { key: 'note-editor-autosave', debounce: 2000 }
)

const renderTime = ref(0)
const isEditorFocused = ref(false)
const startTime = ref(performance.now())

let historyTimer: ReturnType<typeof setTimeout> | null = null

function handleChange(text: string) {
  localValue.value = text
  emit('update:modelValue', text)
  scheduleHistoryPush(text)
  autoSave.debouncedSave()
}

function scheduleHistoryPush(content: string) {
  if (historyTimer) {
    clearTimeout(historyTimer)
  }
  historyTimer = setTimeout(() => {
    history.pushState(content)
    historyTimer = null
  }, 300)
}

function onGetHtml(html: string) {
  const end = performance.now()
  renderTime.value = end - startTime.value
  compiledHtml.value = html
}

function getCompiledHtml(): string {
  if (compiledHtml.value) {
    return compiledHtml.value
  }
  return parseMarkdown(localValue.value)
}

defineExpose({
  getCompiledHtml
})

function onUploadImg(files: FileList, callback: (urls: string[]) => void) {
  const fileList: File[] = Array.from(files)
  Promise.all(
    fileList.map(file => {
      return new Promise<string>((resolve) => {
        const reader = new FileReader()
        reader.onload = (e) => {
          resolve(e.target?.result as string)
        }
        reader.readAsDataURL(file)
      })
    })
  ).then(urls => {
    callback(urls)
  })
}

const previewMode = computed(() => {
  switch (layoutMode.value) {
    case 'edit':
      return false
    case 'preview':
      return true
    default:
      return undefined
  }
})

const toolbars: ToolbarNames[] = [
  'bold',
  'underline',
  'italic',
  '-',
  'strikeThrough',
  'title',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  '-',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'mermaid',
  'katex',
  '-',
  'revoke',
  'next',
  'save',
  '=',
  'fullscreen',
  'htmlPreview',
  'catalog',
  'github'
]

watch(() => props.modelValue, (newValue) => {
  if (newValue !== localValue.value) {
    localValue.value = newValue
  }
})

onMounted(() => {
  config({
    editorExtensions: {
      highlight: {
        instance: hljs
      }
    }
  })
  startTime.value = performance.now()
})

onUnmounted(() => {
  if (historyTimer) {
    clearTimeout(historyTimer)
  }
})

function onFocus() {
  isEditorFocused.value = true
}

function onBlur() {
  isEditorFocused.value = false
}

function onSaved() {
  emit('save')
}
</script>

<template>
  <div class="markdown-editor h-full flex flex-col">
    <div class="flex items-center justify-between px-3 py-2 border-b border-hairline bg-canvas overflow-x-auto">
      <div class="flex items-center gap-1">
        <span class="text-xs text-muted px-2">Markdown 编辑器</span>
      </div>
      
      <div class="flex items-center gap-1">
        <button
          class="px-2 py-1 text-xs rounded transition-colors"
          :class="layoutMode === 'edit' ? 'bg-primary text-white' : 'text-muted hover:bg-surface-soft'"
          @click="setLayoutMode('edit')"
          title="仅编辑"
        >
          编辑
        </button>
        <button
          class="px-2 py-1 text-xs rounded transition-colors"
          :class="layoutMode === 'split' ? 'bg-primary text-white' : 'text-muted hover:bg-surface-soft'"
          @click="setLayoutMode('split')"
          title="双栏"
        >
          双栏
        </button>
        <button
          class="px-2 py-1 text-xs rounded transition-colors"
          :class="layoutMode === 'preview' ? 'bg-primary text-white' : 'text-muted hover:bg-surface-soft'"
          @click="setLayoutMode('preview')"
          title="仅预览"
        >
          预览
        </button>
      </div>
    </div>
    
    <div class="flex-1 overflow-hidden">
      <MdEditor
        ref="editorRef"
        v-model="localValue"
        editor-id="notebook-editor"
        :toolbars="toolbars"
        theme="light"
        preview-theme="vuepress"
        language="zh-CN"
        placeholder="请输入内容..."
        code-theme="atom-one-dark"
        :md-it-extend="null"
        :preview-theme-extend="null"
        :no-mermaid="false"
        :no-katex="false"
        :no-highlight="false"
        :no-iconfont="false"
        :no-prettier="false"
        :no-copy-code="false"
        :no-image-zoom="false"
        :no-ml-lang-badge="false"
        line-warp="true"
        screen-full="true"
        code-style-insert="true"
        :auto-split="layoutMode === 'split'"
        auto-scroll="true"
        :preview="previewMode"
        @on-change="handleChange"
        @on-html-changed="onGetHtml"
        @on-upload-img="onUploadImg"
        @on-save="onSaved"
        @on-focus="onFocus"
        @on-blur="onBlur"
        class="h-full"
      />
    </div>
    
    <div class="px-3 py-1 border-t border-hairline bg-surface-soft flex items-center justify-between text-xs text-muted">
      <div class="flex items-center gap-3">
        <span>Markdown</span>
        <span v-if="autoSave.hasUnsavedChanges" class="text-amber-600">未保存</span>
        <span v-else-if="autoSave.lastSaved" class="text-green-600">
          已保存 {{ autoSave.lastSaved.value?.toLocaleTimeString() }}
        </span>
      </div>
      <div class="flex items-center gap-3">
        <span>渲染: {{ renderTime.toFixed(1) }}ms</span>
        <span>Ctrl+Z 撤销 | Ctrl+S 保存</span>
      </div>
    </div>
  </div>
</template>

<style>
.md-editor {
  border: none !important;
  height: 100% !important;
}

.md-editor .md-editor-toolbar {
  border: none !important;
  border-bottom: 1px solid var(--md-editor-box-shadow-color) !important;
}

.md-editor .md-editor-content {
  height: calc(100% - 45px) !important;
}

.md-editor .md-editor-input-wrapper {
  height: 100% !important;
}

.md-editor .md-editor-preview-wrapper {
  height: 100% !important;
}

.md-editor .md-editor-toolbar-warp {
  height: 45px !important;
}

/* 自定义预览样式 */
.md-editor-preview-wrapper .md-editor-preview {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif !important;
  line-height: 1.7 !important;
  color: #1f2937 !important;
}

.md-editor-preview-wrapper .md-editor-preview h1 {
  font-size: 1.75rem !important;
  font-weight: 700 !important;
  margin: 1.5rem 0 1rem !important;
  padding-bottom: 0.3rem !important;
  border-bottom: 1px solid #e5e7eb !important;
}

.md-editor-preview-wrapper .md-editor-preview h2 {
  font-size: 1.5rem !important;
  font-weight: 600 !important;
  margin: 1.25rem 0 0.75rem !important;
  padding-bottom: 0.25rem !important;
  border-bottom: 1px solid #e5e7eb !important;
}

.md-editor-preview-wrapper .md-editor-preview h3 {
  font-size: 1.25rem !important;
  font-weight: 600 !important;
  margin: 1rem 0 0.5rem !important;
}

.md-editor-preview-wrapper .md-editor-preview p {
  margin: 0.75rem 0 !important;
}

.md-editor-preview-wrapper .md-editor-preview blockquote {
  border-left: 4px solid #3b82f6 !important;
  padding-left: 1rem !important;
  margin: 1rem 0 !important;
  color: #6b7280 !important;
  background: #f9fafb !important;
  padding: 0.5rem 1rem !important;
  border-radius: 0 0.375rem 0.375rem 0 !important;
}

.md-editor-preview-wrapper .md-editor-preview code {
  background-color: #f3f4f6 !important;
  padding: 0.125rem 0.375rem !important;
  border-radius: 0.25rem !important;
  font-size: 0.875em !important;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace !important;
  color: #ef4444 !important;
}

.md-editor-preview-wrapper .md-editor-preview pre {
  background-color: #1f2937 !important;
  color: #e5e7eb !important;
  padding: 1rem !important;
  border-radius: 0.5rem !important;
  overflow-x: auto !important;
  margin: 1rem 0 !important;
}

.md-editor-preview-wrapper .md-editor-preview pre code {
  background-color: transparent !important;
  padding: 0 !important;
  color: inherit !important;
}

.md-editor-preview-wrapper .md-editor-preview a {
  color: #3b82f6 !important;
  text-decoration: none !important;
}

.md-editor-preview-wrapper .md-editor-preview a:hover {
  text-decoration: underline !important;
}

.md-editor-preview-wrapper .md-editor-preview table {
  width: 100% !important;
  border-collapse: collapse !important;
  margin: 1rem 0 !important;
}

.md-editor-preview-wrapper .md-editor-preview th,
.md-editor-preview-wrapper .md-editor-preview td {
  border: 1px solid #e5e7eb !important;
  padding: 0.5rem 0.75rem !important;
  text-align: left !important;
}

.md-editor-preview-wrapper .md-editor-preview th {
  background-color: #f9fafb !important;
  font-weight: 600 !important;
}

.md-editor-preview-wrapper .md-editor-preview tr:nth-child(even) {
  background-color: #f9fafb !important;
}

.md-editor-preview-wrapper .md-editor-preview img {
  max-width: 100% !important;
  border-radius: 0.5rem !important;
  margin: 0.5rem 0 !important;
}

.md-editor-preview-wrapper .md-editor-preview hr {
  border: none !important;
  border-top: 2px solid #e5e7eb !important;
  margin: 1.5rem 0 !important;
}

.markdown-editor {
  --md-editor-box-shadow-color: #e5e7eb !important;
}
</style>
