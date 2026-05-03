<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import TaskList from '@tiptap/extension-task-list'
import TaskItem from '@tiptap/extension-task-item'
import { parseMarkdown } from '@/utils/markdown'
import { useDebounceFn, useLocalStorage } from '@vueuse/core'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const isMarkdownMode = defineModel<boolean>('markdownMode', { default: false })

// 双栏布局配置
const showPreview = useLocalStorage<boolean>('editor-show-preview', true)
const editorWidthPercent = useLocalStorage<number>('editor-width-percent', 50)
const isDragging = ref(false)
const containerRef = ref<HTMLElement | null>(null)

// 最小/最大宽度百分比
const MIN_WIDTH_PERCENT = 20
const MAX_WIDTH_PERCENT = 80

// 编辑器实例
const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({
      heading: {
        levels: [1, 2, 3],
      },
    }),
    TaskList,
    TaskItem.configure({
      nested: true,
    }),
  ],
  onUpdate: ({ editor }) => {
    const content = editor.getHTML()
    emitUpdate(content)
  },
})

// 低延迟更新（使用微任务）
let updateTimeout: ReturnType<typeof queueMicrotask> | null = null
function emitUpdate(content: string) {
  if (updateTimeout) return
  updateTimeout = queueMicrotask(() => {
    updateTimeout = null
    emit('update:modelValue', content)
  })
}

// 预览内容
const markdownPreview = ref('')

// 防抖更新预览（更长的延迟，不影响输入）
const updatePreview = useDebounceFn((content: string) => {
  markdownPreview.value = parseMarkdown(content)
}, 500)

// 监听内容变化更新预览
watch(
  () => props.modelValue,
  (newValue) => {
    updatePreview(newValue)
  },
  { immediate: true }
)

// 自动补全相关
const showSuggestions = ref(false)
const suggestions = ref<string[]>([])
const selectedSuggestionIndex = ref(0)

// 常用 Markdown 语法提示
const markdownSuggestions = [
  { trigger: '# ', title: '标题 1', template: '# ' },
  { trigger: '## ', title: '标题 2', template: '## ' },
  { trigger: '### ', title: '标题 3', template: '### ' },
  { trigger: '- ', title: '无序列表', template: '- ' },
  { trigger: '1. ', title: '有序列表', template: '1. ' },
  { trigger: '**', title: '粗体', template: '****', cursorOffset: 2 },
  { trigger: '*', title: '斜体', template: '**', cursorOffset: 1 },
  { trigger: '`', title: '行内代码', template: '``', cursorOffset: 1 },
  { trigger: '```', title: '代码块', template: '```\n\n```', cursorOffset: 4 },
  { trigger: '> ', title: '引用', template: '> ' },
  { trigger: '- [ ]', title: '待办事项', template: '- [ ] ' },
  { trigger: '- [x]', title: '已完成', template: '- [x] ' },
]

// 处理文本输入
function handleTextareaInput(e: Event) {
  const target = e.target as HTMLTextAreaElement
  emit('update:modelValue', target.value)
  checkSuggestions()
}

// 检查是否需要显示自动补全
function checkSuggestions() {
  if (!editor.value || !isMarkdownMode.value) {
    showSuggestions.value = false
    return
  }

  const selection = editor.value.state.selection
  const pos = selection.from
  const textBefore = editor.value.state.doc.textBetween(Math.max(0, pos - 10), pos)
  
  // 查找匹配的提示
  const matched = markdownSuggestions.filter(s => 
    textBefore.endsWith(s.trigger) || textBefore.endsWith(s.trigger.slice(0, -1))
  )

  if (matched.length > 0) {
    suggestions.value = matched.map(s => s.title)
    selectedSuggestionIndex.value = 0
    showSuggestions.value = true
  } else {
    showSuggestions.value = false
  }
}

// 应用自动补全
function applySuggestion(index: number) {
  if (!editor.value || !isMarkdownMode.value) return
  
  const matched = markdownSuggestions.filter(s => 
    suggestions.value[index] === s.title
  )[0]

  if (matched) {
    const selection = editor.value.state.selection
    const pos = selection.from
    const textBefore = editor.value.state.doc.textBetween(Math.max(0, pos - 10), pos)
    
    // 删除触发文本
    const triggerLength = matched.trigger.length
    const startPos = pos - (textBefore.endsWith(matched.trigger) ? triggerLength : triggerLength - 1)
    
    editor.value.chain()
      .focus()
      .deleteRange({ from: startPos, to: pos })
      .insertContent(matched.template)
      .run()

    // 设置光标位置
    if (matched.cursorOffset !== undefined) {
      nextTick(() => {
        const newPos = editor.value?.state.selection.from
        if (newPos && matched.cursorOffset !== undefined) {
          editor.value?.chain()
            .focus()
            .setTextSelection(newPos - (matched.template.length - matched.cursorOffset))
            .run()
        }
      })
    }

    showSuggestions.value = false
  }
}

// 键盘快捷键处理
function handleKeydown(e: KeyboardEvent) {
  if (!editor.value) return

  // Tab 键：插入制表符
  if (e.key === 'Tab') {
    e.preventDefault()
    editor.value.chain().focus().insertContent('\t').run()
    return
  }

  // Ctrl/Cmd + B：粗体
  if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
    e.preventDefault()
    editor.value.chain().focus().toggleBold().run()
    return
  }

  // Ctrl/Cmd + I：斜体
  if ((e.ctrlKey || e.metaKey) && e.key === 'i') {
    e.preventDefault()
    editor.value.chain().focus().toggleItalic().run()
    return
  }

  // Ctrl/Cmd + S：保存（向上冒泡）
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    return
  }

  // Ctrl/Cmd + Z：撤销
  if ((e.ctrlKey || e.metaKey) && e.key === 'z' && !e.shiftKey) {
    e.preventDefault()
    editor.value.chain().focus().undo().run()
    return
  }

  // Ctrl/Cmd + Shift + Z：重做
  if ((e.ctrlKey || e.metaKey) && e.key === 'z' && e.shiftKey) {
    e.preventDefault()
    editor.value.chain().focus().redo().run()
    return
  }

  // Ctrl/Cmd + D：删除选中内容或当前行
  if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
    e.preventDefault()
    const state = editor.value.state
    if (state.selection.from !== state.selection.to) {
      editor.value.chain().focus().deleteSelection().run()
    } else {
      const textBefore = state.doc.textBetween(Math.max(0, state.selection.from - 100), state.selection.from)
      const textAfter = state.doc.textBetween(state.selection.to, Math.min(state.doc.content.size, state.selection.to + 100))
      const lineBreakBefore = textBefore.lastIndexOf('\n')
      const lineBreakAfter = textAfter.indexOf('\n')
      const from = lineBreakBefore >= 0 ? state.selection.from - (textBefore.length - lineBreakBefore) : 0
      const to = lineBreakAfter >= 0 ? state.selection.to + lineBreakAfter : state.doc.content.size
      editor.value.chain().focus().deleteRange({ from, to }).run()
    }
    return
  }

  // Ctrl/Cmd + Backspace：删除单词
  if ((e.ctrlKey || e.metaKey) && e.key === 'Backspace') {
    e.preventDefault()
    const state = editor.value.state
    const pos = state.selection.from
    const textBefore = state.doc.textBetween(Math.max(0, pos - 50), pos)
    const wordStart = textBefore.lastIndexOf(' ') + 1
    const deleteFrom = pos - (textBefore.length - wordStart)
    editor.value.chain().focus().deleteRange({ from: deleteFrom, to: pos }).run()
    return
  }

  // Ctrl/Cmd + Delete：删除单词
  if ((e.ctrlKey || e.metaKey) && e.key === 'Delete') {
    e.preventDefault()
    const state = editor.value.state
    const pos = state.selection.to
    const textAfter = state.doc.textBetween(pos, Math.min(state.doc.content.size, pos + 50))
    const wordEnd = textAfter.search(/\s|$/)
    const deleteTo = pos + wordEnd
    editor.value.chain().focus().deleteRange({ from: pos, to: deleteTo }).run()
    return
  }

  // 自动补全导航
  if (showSuggestions.value) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      selectedSuggestionIndex.value = (selectedSuggestionIndex.value + 1) % suggestions.value.length
      return
    }
    if (e.key === 'ArrowUp') {
      e.preventDefault()
      selectedSuggestionIndex.value = (selectedSuggestionIndex.value - 1 + suggestions.value.length) % suggestions.value.length
      return
    }
    if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault()
      applySuggestion(selectedSuggestionIndex.value)
      return
    }
    if (e.key === 'Escape') {
      e.preventDefault()
      showSuggestions.value = false
      return
    }
  }
}

// 全局键盘监听
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

// 切换单/双栏
function togglePreview() {
  showPreview.value = !showPreview.value
}

// 拖拽开始
function startDrag(_e: MouseEvent | TouchEvent) {
  isDragging.value = true
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', handleDrag)
  document.addEventListener('touchend', stopDrag)
}

// 拖拽过程
function handleDrag(e: MouseEvent | TouchEvent) {
  if (!isDragging.value || !containerRef.value) return
  
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const containerRect = containerRef.value.getBoundingClientRect()
  const containerWidth = containerRect.width
  const newPercent = ((clientX - containerRect.left) / containerWidth) * 100
  
  editorWidthPercent.value = Math.max(
    MIN_WIDTH_PERCENT,
    Math.min(MAX_WIDTH_PERCENT, newPercent)
  )
}

// 拖拽结束
function stopDrag() {
  isDragging.value = false
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', handleDrag)
  document.removeEventListener('touchend', stopDrag)
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

// 监听外部内容变化
watch(
  () => props.modelValue,
  (newValue) => {
    if (editor.value && editor.value.getHTML() !== newValue && !isMarkdownMode.value) {
      editor.value.commands.setContent(newValue)
    }
  }
)
</script>

<template>
  <div class="markdown-editor h-full flex flex-col">
    <!-- 工具栏 -->
    <div class="flex items-center justify-between px-4 py-2 border-b border-hairline bg-canvas">
      <div class="flex items-center gap-1">
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="标题 1"
          @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
          </svg>
        </button>
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="标题 2"
          @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
          </svg>
        </button>
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="标题 3"
          @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
          </svg>
        </button>
        
        <div class="w-px h-5 bg-hairline mx-1"></div>
        
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="粗体 (Ctrl+B)"
          @click="editor?.chain().focus().toggleBold().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 4h8a4 4 0 014 4 4 4 0 01-4 4H6z M6 12h9a4 4 0 014 4 4 4 0 01-4 4H6z" />
          </svg>
        </button>
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="斜体 (Ctrl+I)"
          @click="editor?.chain().focus().toggleItalic().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 4h4m-2 0v16m4-16h-4m0 16h4" transform="skewX(-10)" />
          </svg>
        </button>
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="删除线"
          @click="editor?.chain().focus().toggleStrike().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 10H7m10 4H7m5-8v12" />
          </svg>
        </button>
        
        <div class="w-px h-5 bg-hairline mx-1"></div>
        
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="无序列表"
          @click="editor?.chain().focus().toggleBulletList().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="有序列表"
          @click="editor?.chain().focus().toggleOrderedList().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 20h14M7 12h14M7 4h14M3 20h.01M3 12h.01M3 4h.01" />
          </svg>
        </button>
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="任务列表"
          @click="editor?.chain().focus().toggleTaskList().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
        </button>
        
        <div class="w-px h-5 bg-hairline mx-1"></div>
        
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="引用"
          @click="editor?.chain().focus().toggleBlockquote().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </button>
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="代码块"
          @click="editor?.chain().focus().toggleCodeBlock().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
          </svg>
        </button>
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="行内代码"
          @click="editor?.chain().focus().toggleCode().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
          </svg>
        </button>
        
        <div class="w-px h-5 bg-hairline mx-1"></div>
        
        <button
          v-if="!isMarkdownMode"
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="链接"
          @click="handleLink"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l1.1 1.1" />
          </svg>
        </button>
      </div>
      
      <div class="flex items-center gap-2">
        <!-- 预览切换按钮 -->
        <button
          class="px-3 py-1 text-sm rounded hover:bg-surface-soft transition-colors"
          :class="showPreview ? 'bg-primary text-white' : 'text-muted'"
          @click="togglePreview"
        >
          {{ showPreview ? '单栏' : '双栏' }}
        </button>
        <!-- 编辑模式切换 -->
        <button
          class="px-3 py-1 text-sm rounded hover:bg-surface-soft transition-colors"
          :class="isMarkdownMode ? 'bg-primary text-white' : 'text-muted'"
          @click="toggleMarkdownMode"
        >
          {{ isMarkdownMode ? '富文本' : 'Markdown' }}
        </button>
      </div>
    </div>
    
    <!-- 编辑区域 -->
    <div 
      ref="containerRef"
      class="flex-1 flex overflow-hidden relative"
    >
      <!-- 自动补全提示 -->
      <div
        v-if="showSuggestions && suggestions.length > 0"
        class="absolute z-50 bg-canvas border border-hairline rounded-lg shadow-lg py-1 min-w-[160px]"
        style="top: 8px; left: 8px;"
      >
        <button
          v-for="(suggestion, index) in suggestions"
          :key="suggestion"
          class="w-full px-4 py-2 text-left text-sm hover:bg-surface-soft transition-colors flex items-center gap-2"
          :class="{ 'bg-surface-soft': index === selectedSuggestionIndex }"
          @click="applySuggestion(index)"
        >
          <span>{{ suggestion }}</span>
        </button>
      </div>
      
      <!-- 编辑面板 -->
      <div 
        class="overflow-hidden transition-all duration-200"
        :style="{ 
          width: showPreview ? `${editorWidthPercent}%` : '100%'
        }"
      >
        <div v-if="!isMarkdownMode" class="h-full">
          <EditorContent :editor="editor" class="h-full" />
        </div>
        <textarea
          v-else
          :value="modelValue"
          @input="handleTextareaInput"
          @keydown="handleKeydown"
          class="w-full h-full p-4 bg-transparent outline-none resize-none font-mono text-sm leading-relaxed"
          placeholder="使用 Markdown 语法编辑..."
          spellcheck="false"
        ></textarea>
      </div>
      
      <!-- 拖拽手柄 -->
      <div
        v-if="showPreview"
        class="w-1 bg-hairline cursor-col-resize hover:bg-primary transition-colors duration-200 select-none"
        :class="{ 'bg-primary': isDragging }"
        @mousedown="startDrag"
        @touchstart="startDrag"
      >
        <div class="h-full flex items-center justify-center">
          <div class="w-0.5 h-8 bg-surface-strong rounded-full mx-0.5"></div>
          <div class="w-0.5 h-8 bg-surface-strong rounded-full mx-0.5"></div>
        </div>
      </div>
      
      <!-- 预览面板 -->
      <div 
        v-if="showPreview"
        class="border-l border-hairline overflow-auto transition-all duration-200"
        :style="{ 
          width: `${100 - editorWidthPercent}%`
        }"
      >
        <div class="p-4">
          <h4 class="text-sm font-medium text-muted mb-3">预览</h4>
          <div v-html="markdownPreview" class="markdown-preview"></div>
        </div>
      </div>
    </div>
    
    <!-- 状态栏 -->
    <div class="px-4 py-1 border-t border-hairline bg-surface-soft flex items-center justify-between text-xs text-muted">
      <span>{{ isMarkdownMode ? 'Markdown' : '富文本' }} 模式</span>
      <span>快捷键: Ctrl+B 粗体 | Ctrl+I 斜体 | Ctrl+Z 撤销</span>
    </div>
  </div>
</template>

<style scoped>
.markdown-preview {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
}

.markdown-preview :deep(h1) {
  font-size: 1.875rem;
  font-weight: 700;
  margin-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.3em;
}

.markdown-preview :deep(h2) {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.3em;
}

.markdown-preview :deep(h3) {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.markdown-preview :deep(p) {
  margin-bottom: 1rem;
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  padding-left: 2rem;
  margin-bottom: 1rem;
}

.markdown-preview :deep(li) {
  margin-bottom: 0.25rem;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid #ef4444;
  padding-left: 1rem;
  font-style: italic;
  color: #6b7280;
  margin: 1rem 0;
}

.markdown-preview :deep(code) {
  background-color: #f3f4f6;
  padding: 0.2em 0.4em;
  border-radius: 0.25rem;
  font-size: 0.875em;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
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
}

.markdown-preview :deep(a) {
  color: #3b82f6;
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
  margin: 1rem 0;
}

.markdown-preview :deep(input[type="checkbox"]) {
  margin-right: 0.5rem;
}

.markdown-preview :deep(.contains-task-list) {
  list-style: none;
  padding-left: 0;
}
</style>
