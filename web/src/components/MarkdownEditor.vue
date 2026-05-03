<script setup lang="ts">
import { computed, watch } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'

import TaskList from '@tiptap/extension-task-list'
import TaskItem from '@tiptap/extension-task-item'
import { parseMarkdown } from '@/utils/markdown'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const isMarkdownMode = defineModel<boolean>('markdownMode', { default: false })

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
    emit('update:modelValue', content)
  },
})

const markdownPreview = computed(() => {
  return parseMarkdown(props.modelValue)
})

function toggleMarkdownMode() {
  isMarkdownMode.value = !isMarkdownMode.value
}

function handleLink() {
  const url = window.prompt('请输入链接地址：')
  if (url && editor.value) {
    editor.value.chain().focus().setLink({ href: url }).run()
  }
}

watch(
  () => props.modelValue,
  (newValue) => {
    if (editor.value && editor.value.getHTML() !== newValue) {
      editor.value.commands.setContent(newValue)
    }
  }
)
</script>

<template>
  <div class="markdown-editor h-full flex flex-col">
    <div class="flex items-center justify-between px-4 py-2 border-b border-hairline">
      <div class="flex items-center gap-1">
        <button
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
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="粗体"
          @click="editor?.chain().focus().toggleBold().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 4h8a4 4 0 014 4 4 4 0 01-4 4H6z M6 12h9a4 4 0 014 4 4 4 0 01-4 4H6z" />
          </svg>
        </button>
        <button
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="斜体"
          @click="editor?.chain().focus().toggleItalic().run()"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 4h4m-2 0v16m4-16h-4m0 16h4" transform="skewX(-10)" />
          </svg>
        </button>
        <button
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
          class="p-2 rounded hover:bg-surface-soft transition-colors"
          title="链接"
          @click="handleLink"
          :disabled="!editor"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
          </svg>
        </button>
      </div>
      
      <button
        class="px-3 py-1 text-sm rounded hover:bg-surface-soft transition-colors"
        :class="isMarkdownMode ? 'bg-primary text-white' : 'text-muted'"
        @click="toggleMarkdownMode"
      >
        {{ isMarkdownMode ? '富文本' : 'Markdown' }}
      </button>
    </div>
    
    <div class="flex-1 flex overflow-hidden">
      <div class="flex-1 overflow-hidden">
        <div v-if="!isMarkdownMode" class="h-full">
          <EditorContent :editor="editor" class="h-full" />
        </div>
        <textarea
          v-else
          :value="modelValue"
          @input="emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
          class="w-full h-full p-4 bg-transparent outline-none resize-none"
          placeholder="使用 Markdown 语法编辑..."
        ></textarea>
      </div>
      
      <div class="hidden md:block w-1/2 border-l border-hairline overflow-auto">
        <div class="p-4">
          <h4 class="text-sm font-medium text-muted mb-3">预览</h4>
          <div v-html="markdownPreview" class="markdown-preview"></div>
        </div>
      </div>
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
