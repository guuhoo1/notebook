<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import TaskList from '@tiptap/extension-task-list'
import TaskItem from '@tiptap/extension-task-item'
import { parseMarkdown } from '@/utils/markdown'
import { useEditorHistory, useAutoSave, useEditorLayout } from '@/composables/useEditor'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'save': []
}>()

const isMarkdownMode = defineModel<boolean>('markdownMode', { default: false })

const {
  layoutMode,
  splitRatio,
  isDragging,
  setLayoutMode,
  startDrag,
  stopDrag,
  handleDrag
} = useEditorLayout()

const containerRef = ref<HTMLElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const editorRef = ref<HTMLElement | null>(null)
const isEditorFocused = ref(false)

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({
      heading: { levels: [1, 2, 3, 4, 5, 6] },
    }),
    TaskList,
    TaskItem.configure({ nested: true }),
  ],
  onUpdate: ({ editor }) => {
    const content = editor.getHTML()
    emitUpdate(content)
    scheduleHistoryPush(content)
    autoSave.debouncedSave()
  },
  onFocus: () => {
    isEditorFocused.value = true
  },
  onBlur: () => {
    isEditorFocused.value = false
  },
})

const history = useEditorHistory(props.modelValue)

const autoSave = useAutoSave(
  () => props.modelValue,
  async () => {
    emit('save')
  },
  { key: 'note-editor-autosave', debounce: 2000 }
)

let updateScheduled = false
function emitUpdate(content: string) {
  if (updateScheduled) return
  updateScheduled = true
  queueMicrotask(() => {
    updateScheduled = false
    emit('update:modelValue', content)
  })
}

let historyTimer: ReturnType<typeof setTimeout> | null = null
function scheduleHistoryPush(content: string) {
  if (historyTimer) {
    clearTimeout(historyTimer)
  }
  historyTimer = setTimeout(() => {
    history.pushState(content)
    historyTimer = null
  }, 300)
}

const markdownPreview = ref('')
const renderTime = ref(0)

function renderMarkdown(content: string) {
  const start = performance.now()
  markdownPreview.value = parseMarkdown(content)
  renderTime.value = performance.now() - start
}

let renderScheduled = false
function scheduleRender(content: string) {
  if (renderScheduled) return
  renderScheduled = true
  queueMicrotask(() => {
    renderMarkdown(content)
    renderScheduled = false
  })
}

watch(
  () => props.modelValue,
  (newValue) => {
    scheduleRender(newValue)
  },
  { immediate: true }
)

const showSuggestions = ref(false)
const suggestions = ref<string[]>([])
const selectedSuggestionIndex = ref(0)

const markdownSuggestions = [
  { trigger: '# ', title: '标题 1', template: '# ' },
  { trigger: '## ', title: '标题 2', template: '## ' },
  { trigger: '### ', title: '标题 3', template: '### ' },
  { trigger: '#### ', title: '标题 4', template: '#### ' },
  { trigger: '##### ', title: '标题 5', template: '##### ' },
  { trigger: '###### ', title: '标题 6', template: '###### ' },
  { trigger: '- ', title: '无序列表', template: '- ' },
  { trigger: '* ', title: '无序列表', template: '* ' },
  { trigger: '1. ', title: '有序列表', template: '1. ' },
  { trigger: '**', title: '粗体', template: '****', cursorOffset: 2 },
  { trigger: '__', title: '粗体', template: '____', cursorOffset: 2 },
  { trigger: '*', title: '斜体', template: '**', cursorOffset: 1 },
  { trigger: '_', title: '斜体', template: '__', cursorOffset: 1 },
  { trigger: '~~', title: '删除线', template: '~~~~', cursorOffset: 2 },
  { trigger: '`', title: '行内代码', template: '``', cursorOffset: 1 },
  { trigger: '```', title: '代码块', template: '```\n\n```', cursorOffset: 4 },
  { trigger: '> ', title: '引用', template: '> ' },
  { trigger: '- [ ]', title: '待办事项', template: '- [ ] ' },
  { trigger: '- [x]', title: '已完成', template: '- [x] ' },
  { trigger: '---', title: '分割线', template: '\n---\n' },
  { trigger: '***', title: '分割线', template: '\n***\n' },
]

let suggestionTimer: ReturnType<typeof setTimeout> | null = null
function scheduleCheckSuggestions() {
  if (suggestionTimer) {
    clearTimeout(suggestionTimer)
  }
  suggestionTimer = setTimeout(() => {
    checkSuggestions()
    suggestionTimer = null
  }, 50)
}

function checkSuggestions() {
  if (!isMarkdownMode.value || !textareaRef.value) {
    showSuggestions.value = false
    return
  }

  const textarea = textareaRef.value
  const pos = textarea.selectionStart
  const textBefore = textarea.value.substring(Math.max(0, pos - 10), pos)

  const matched = markdownSuggestions.filter(s => textBefore.endsWith(s.trigger))
  
  if (matched.length > 0) {
    suggestions.value = matched.map(s => s.title)
    selectedSuggestionIndex.value = 0
    showSuggestions.value = true
  } else {
    showSuggestions.value = false
  }
}

function applySuggestion(index: number) {
  if (!textareaRef.value) return

  const matched = markdownSuggestions.find(s => suggestions.value[index] === s.title)
  if (!matched) return

  const textarea = textareaRef.value
  const pos = textarea.selectionStart
  const triggerLength = matched.trigger.length
  const startPos = pos - triggerLength

  const before = textarea.value.substring(0, startPos)
  const after = textarea.value.substring(pos)
  const newValue = before + matched.template + after

  emit('update:modelValue', newValue)

  nextTick(() => {
    if (matched.cursorOffset !== undefined) {
      const newPos = startPos + matched.template.length - matched.cursorOffset
      textarea.setSelectionRange(newPos, newPos)
    } else {
      const newPos = startPos + matched.template.length
      textarea.setSelectionRange(newPos, newPos)
    }
    textarea.focus()
  })

  showSuggestions.value = false
}

function handleTextareaInput(e: Event) {
  const target = e.target as HTMLTextAreaElement
  const newValue = target.value
  emit('update:modelValue', newValue)
  scheduleHistoryPush(newValue)
  autoSave.debouncedSave()
  scheduleCheckSuggestions()
  scheduleRender(newValue)
}

function handleTextareaKeydown(e: KeyboardEvent) {
  if (showSuggestions.value) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      e.stopPropagation()
      selectedSuggestionIndex.value = (selectedSuggestionIndex.value + 1) % suggestions.value.length
      return
    }
    if (e.key === 'ArrowUp') {
      e.preventDefault()
      e.stopPropagation()
      selectedSuggestionIndex.value = (selectedSuggestionIndex.value - 1 + suggestions.value.length) % suggestions.value.length
      return
    }
    if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault()
      e.stopPropagation()
      applySuggestion(selectedSuggestionIndex.value)
      return
    }
    if (e.key === 'Escape') {
      e.preventDefault()
      e.stopPropagation()
      showSuggestions.value = false
      return
    }
  }

  if ((e.ctrlKey || e.metaKey) && e.key === 'z' && !e.shiftKey) {
    e.preventDefault()
    e.stopPropagation()
    const content = history.undo()
    if (content !== null) {
      emit('update:modelValue', content)
      history.resetUndoRedoFlag()
    }
    return
  }

  if ((e.ctrlKey || e.metaKey) && (e.key === 'y' || (e.key === 'z' && e.shiftKey))) {
    e.preventDefault()
    e.stopPropagation()
    const content = history.redo()
    if (content !== null) {
      emit('update:modelValue', content)
      history.resetUndoRedoFlag()
    }
    return
  }

  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    e.stopPropagation()
    autoSave.save()
    return
  }

  if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
    e.preventDefault()
    e.stopPropagation()
    wrapSelection('**', '**')
    return
  }

  if ((e.ctrlKey || e.metaKey) && e.key === 'i') {
    e.preventDefault()
    e.stopPropagation()
    wrapSelection('*', '*')
    return
  }

  if (e.key === 'Tab') {
    e.preventDefault()
    e.stopPropagation()
    insertText('\t')
    return
  }
}

function handleGlobalKeydown(e: KeyboardEvent) {
  if (!isEditorFocused.value && document.activeElement !== textareaRef.value) {
    return
  }

  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    e.stopPropagation()
    autoSave.save()
    return
  }
}

function wrapSelection(before: string, after: string) {
  if (!textareaRef.value) return
  const textarea = textareaRef.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = textarea.value.substring(start, end)
  const newText = before + selectedText + after
  const newValue = textarea.value.substring(0, start) + newText + textarea.value.substring(end)
  emit('update:modelValue', newValue)
  nextTick(() => {
    textarea.setSelectionRange(start + before.length, end + before.length)
    textarea.focus()
  })
}

function insertText(text: string) {
  if (!textareaRef.value) return
  const textarea = textareaRef.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const newValue = textarea.value.substring(0, start) + text + textarea.value.substring(end)
  emit('update:modelValue', newValue)
  nextTick(() => {
    textarea.setSelectionRange(start + text.length, start + text.length)
    textarea.focus()
  })
}

function handleLink() {
  const url = window.prompt('请输入链接地址：')
  if (url && editor.value) {
    editor.value.chain().focus().setLink({ href: url }).run()
  }
}

function toggleMarkdownMode() {
  isMarkdownMode.value = !isMarkdownMode.value
}

function onDragStart(_e: MouseEvent | TouchEvent) {
  startDrag()
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', onDragEnd)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', onDragEnd)
}

function onDrag(e: MouseEvent | TouchEvent) {
  if (!containerRef.value) return
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const rect = containerRef.value.getBoundingClientRect()
  handleDrag(clientX, rect)
}

function onDragEnd() {
  stopDrag()
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', onDragEnd)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', onDragEnd)
}

const editPanelStyle = computed(() => {
  if (layoutMode.value === 'preview') return { width: '0%' }
  if (layoutMode.value === 'edit') return { width: '100%' }
  return { width: `${splitRatio.value}%` }
})

const previewPanelStyle = computed(() => {
  if (layoutMode.value === 'edit') return { width: '0%' }
  if (layoutMode.value === 'preview') return { width: '100%' }
  return { width: `${100 - splitRatio.value}%` }
})

watch(
  () => props.modelValue,
  (newValue) => {
    if (editor.value && editor.value.getHTML() !== newValue && !isMarkdownMode.value) {
      editor.value.commands.setContent(newValue)
    }
  }
)

function handleTextareaFocus() {
  isEditorFocused.value = true
}

function handleTextareaBlur() {
  isEditorFocused.value = false
}

onMounted(() => {
  document.addEventListener('keydown', handleGlobalKeydown, true)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleGlobalKeydown, true)
  if (historyTimer) {
    clearTimeout(historyTimer)
  }
  if (suggestionTimer) {
    clearTimeout(suggestionTimer)
  }
})
</script>

<template>
  <div ref="editorRef" class="markdown-editor h-full flex flex-col">
    <div class="flex items-center justify-between px-3 py-2 border-b border-hairline bg-canvas overflow-x-auto">
      <div class="flex items-center gap-1 flex-shrink-0">
        <template v-if="!isMarkdownMode">
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors min-h-[40px] min-w-[40px] flex items-center justify-center"
            title="标题 1"
            @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
          >
            <span class="text-sm font-bold">H1</span>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors min-h-[40px] min-w-[40px] flex items-center justify-center"
            title="标题 2"
            @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
          >
            <span class="text-sm font-bold">H2</span>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors min-h-[40px] min-w-[40px] flex items-center justify-center"
            title="标题 3"
            @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
          >
            <span class="text-sm font-bold">H3</span>
          </button>
          <div class="hidden sm:block w-px h-4 bg-hairline mx-1"></div>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors min-h-[40px] min-w-[40px] flex items-center justify-center"
            title="粗体 (Ctrl+B)"
            @click="editor?.chain().focus().toggleBold().run()"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 4h8a4 4 0 014 4 4 4 0 01-4 4H6z M6 12h9a4 4 0 014 4 4 4 0 01-4 4H6z" />
            </svg>
          </button>
          <button
            class="p-2 rounded hover:bg-surface-soft transition-colors min-h-[40px] min-w-[40px] flex items-center justify-center"
            title="斜体 (Ctrl+I)"
            @click="editor?.chain().focus().toggleItalic().run()"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 4h4m-2 0v16m4-16h-4m0 16h4" transform="skewX(-10)" />
            </svg>
          </button>
          <button
            class="hidden sm:flex p-2 rounded hover:bg-surface-soft transition-colors min-h-[40px] min-w-[40px] items-center justify-center"
            title="删除线"
            @click="editor?.chain().focus().toggleStrike().run()"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 10H7m10 4H7m5-8v12" />
            </svg>
          </button>
          <div class="hidden sm:block w-px h-4 bg-hairline mx-1"></div>
          <button
            class="hidden sm:flex p-2 rounded hover:bg-surface-soft transition-colors min-h-[40px] min-w-[40px] items-center justify-center"
            title="无序列表"
            @click="editor?.chain().focus().toggleBulletList().run()"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <button
            class="p-1.5 rounded hover:bg-surface-soft transition-colors"
            title="有序列表"
            @click="editor?.chain().focus().toggleOrderedList().run()"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 20h14M7 12h14M7 4h14M3 20h.01M3 12h.01M3 4h.01" />
            </svg>
          </button>
          <button
            class="p-1.5 rounded hover:bg-surface-soft transition-colors"
            title="任务列表"
            @click="editor?.chain().focus().toggleTaskList().run()"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </button>
          <div class="w-px h-4 bg-hairline mx-1"></div>
          <button
            class="p-1.5 rounded hover:bg-surface-soft transition-colors"
            title="引用"
            @click="editor?.chain().focus().toggleBlockquote().run()"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </button>
          <button
            class="p-1.5 rounded hover:bg-surface-soft transition-colors"
            title="代码块"
            @click="editor?.chain().focus().toggleCodeBlock().run()"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
            </svg>
          </button>
          <button
            class="p-1.5 rounded hover:bg-surface-soft transition-colors"
            title="链接"
            @click="handleLink"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l1.1 1.1" />
            </svg>
          </button>
        </template>
        <span v-else class="text-xs text-muted px-2">Markdown 模式</span>
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
        <div class="w-px h-4 bg-hairline mx-1"></div>
        <button
          class="px-2 py-1 text-xs rounded transition-colors"
          :class="isMarkdownMode ? 'bg-primary text-white' : 'text-muted hover:bg-surface-soft'"
          @click="toggleMarkdownMode"
        >
          {{ isMarkdownMode ? '富文本' : 'MD' }}
        </button>
      </div>
    </div>
    
    <div ref="containerRef" class="flex-1 flex overflow-hidden relative">
      <div
        v-if="showSuggestions && suggestions.length > 0"
        class="absolute z-50 bg-canvas border border-hairline rounded-lg shadow-lg py-1 min-w-[140px]"
        style="top: 8px; left: 8px;"
      >
        <button
          v-for="(suggestion, index) in suggestions"
          :key="suggestion"
          class="w-full px-3 py-1.5 text-left text-xs hover:bg-surface-soft transition-colors"
          :class="{ 'bg-surface-soft': index === selectedSuggestionIndex }"
          @click="applySuggestion(index)"
        >
          {{ suggestion }}
        </button>
      </div>
      
      <div
        v-show="layoutMode !== 'preview'"
        class="overflow-auto transition-all duration-200"
        :style="editPanelStyle"
      >
        <div v-if="!isMarkdownMode" class="h-full overflow-auto">
          <EditorContent :editor="editor" class="h-full prose prose-sm max-w-none" />
        </div>
        <textarea
          v-else
          ref="textareaRef"
          :value="modelValue"
          @input="handleTextareaInput"
          @keydown="handleTextareaKeydown"
          @focus="handleTextareaFocus"
          @blur="handleTextareaBlur"
          class="w-full h-full p-4 bg-transparent outline-none resize-none font-mono text-sm leading-relaxed overflow-auto"
          placeholder="使用 Markdown 语法编辑..."
          spellcheck="false"
        ></textarea>
      </div>
      
      <div
        v-if="layoutMode === 'split'"
        class="w-1 bg-hairline cursor-col-resize hover:bg-primary transition-colors duration-200 select-none flex-shrink-0"
        :class="{ 'bg-primary': isDragging }"
        @mousedown="onDragStart"
        @touchstart="onDragStart"
      >
        <div class="h-full flex items-center justify-center">
          <div class="w-0.5 h-6 bg-surface-strong rounded-full"></div>
        </div>
      </div>
      
      <div
        v-show="layoutMode !== 'edit'"
        class="border-l border-hairline overflow-auto transition-all duration-200"
        :style="previewPanelStyle"
      >
        <div class="p-4 h-full overflow-auto">
          <div v-html="markdownPreview" class="markdown-preview"></div>
        </div>
      </div>
    </div>
    
    <div class="px-3 py-1 border-t border-hairline bg-surface-soft flex items-center justify-between text-xs text-muted">
      <div class="flex items-center gap-3">
        <span>{{ isMarkdownMode ? 'Markdown' : '富文本' }}</span>
        <span v-if="autoSave.hasUnsavedChanges.value" class="text-amber-600">未保存</span>
        <span v-else-if="autoSave.lastSaved.value" class="text-green-600">
          已保存 {{ autoSave.lastSaved.value.toLocaleTimeString() }}
        </span>
      </div>
      <div class="flex items-center gap-3">
        <span>渲染: {{ renderTime.toFixed(1) }}ms</span>
        <span>Ctrl+Z 撤销 | Ctrl+S 保存</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.markdown-preview {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.7;
  color: #1f2937;
}

.markdown-preview :deep(h1) {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 1.5rem 0 1rem;
  padding-bottom: 0.3rem;
  border-bottom: 1px solid #e5e7eb;
}

.markdown-preview :deep(h2) {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 1.25rem 0 0.75rem;
  padding-bottom: 0.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.markdown-preview :deep(h3) {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 1rem 0 0.5rem;
}

.markdown-preview :deep(h4),
.markdown-preview :deep(h5),
.markdown-preview :deep(h6) {
  font-size: 1rem;
  font-weight: 600;
  margin: 0.75rem 0 0.5rem;
}

.markdown-preview :deep(p) {
  margin: 0.75rem 0;
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  padding-left: 1.5rem;
  margin: 0.75rem 0;
}

.markdown-preview :deep(li) {
  margin: 0.25rem 0;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid #3b82f6;
  padding-left: 1rem;
  margin: 1rem 0;
  color: #6b7280;
  background: #f9fafb;
  padding: 0.5rem 1rem;
  border-radius: 0 0.375rem 0.375rem 0;
}

.markdown-preview :deep(code) {
  background-color: #f3f4f6;
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  font-size: 0.875em;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  color: #ef4444;
}

.markdown-preview :deep(pre) {
  background-color: #1f2937;
  color: #e5e7eb;
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  margin: 1rem 0;
}

.markdown-preview :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: inherit;
}

.markdown-preview :deep(a) {
  color: #3b82f6;
  text-decoration: none;
}

.markdown-preview :deep(a:hover) {
  text-decoration: underline;
}

.markdown-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
}

.markdown-preview :deep(th),
.markdown-preview :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 0.5rem 0.75rem;
  text-align: left;
}

.markdown-preview :deep(th) {
  background-color: #f9fafb;
  font-weight: 600;
}

.markdown-preview :deep(tr:nth-child(even)) {
  background-color: #f9fafb;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  border-radius: 0.5rem;
  margin: 0.5rem 0;
}

.markdown-preview :deep(hr) {
  border: none;
  border-top: 2px solid #e5e7eb;
  margin: 1.5rem 0;
}

.markdown-preview :deep(input[type="checkbox"]) {
  margin-right: 0.5rem;
}

.markdown-preview :deep(.contains-task-list) {
  list-style: none;
  padding-left: 0;
}

.markdown-preview :deep(.hljs) {
  background: #1f2937;
}

.prose :deep(.ProseMirror) {
  outline: none;
  min-height: 100%;
  padding: 1rem;
}

.prose :deep(.ProseMirror p.is-editor-empty:first-child::before) {
  color: #adb5bd;
  content: attr(data-placeholder);
  float: left;
  height: 0;
  pointer-events: none;
}
</style>
