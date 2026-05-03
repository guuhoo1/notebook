import { ref, onMounted, onUnmounted } from 'vue'
import { useLocalStorage } from '@vueuse/core'

interface HistoryState {
  content: string
  timestamp: number
}

const MAX_HISTORY_SIZE = 100

export function useEditorHistory(initialContent: string = '') {
  const history = ref<HistoryState[]>([])
  const currentIndex = ref(-1)
  const isUndoRedo = ref(false)

  const canUndo = ref(false)
  const canRedo = ref(false)

  function updateButtons() {
    canUndo.value = currentIndex.value > 0
    canRedo.value = currentIndex.value < history.value.length - 1
  }

  function pushState(content: string) {
    if (isUndoRedo.value) return

    if (currentIndex.value >= 0 && history.value[currentIndex.value]?.content === content) {
      return
    }

    if (currentIndex.value < history.value.length - 1) {
      history.value = history.value.slice(0, currentIndex.value + 1)
    }

    history.value.push({
      content,
      timestamp: Date.now()
    })

    if (history.value.length > MAX_HISTORY_SIZE) {
      history.value = history.value.slice(-MAX_HISTORY_SIZE)
    }

    currentIndex.value = history.value.length - 1
    updateButtons()
  }

  function undo(): string | null {
    if (currentIndex.value <= 0) return null

    isUndoRedo.value = true
    currentIndex.value--
    updateButtons()

    const state = history.value[currentIndex.value]
    return state?.content ?? null
  }

  function redo(): string | null {
    if (currentIndex.value >= history.value.length - 1) return null

    isUndoRedo.value = true
    currentIndex.value++
    updateButtons()

    const state = history.value[currentIndex.value]
    return state?.content ?? null
  }

  function resetUndoRedoFlag() {
    isUndoRedo.value = false
  }

  function clear() {
    history.value = []
    currentIndex.value = -1
    updateButtons()
  }

  if (initialContent) {
    pushState(initialContent)
  }

  return {
    canUndo,
    canRedo,
    pushState,
    undo,
    redo,
    resetUndoRedoFlag,
    clear,
    historyLength: () => history.value.length
  }
}

export function useAutoSave(
  content: () => string,
  saveCallback: (content: string) => Promise<void> | void,
  options: {
    key?: string
    interval?: number
    debounce?: number
  } = {}
) {
  const {
    key = 'markdown-editor-autosave',
    interval = 30000,
    debounce = 1000
  } = options

  const lastSaved = ref<Date | null>(null)
  const isSaving = ref(false)
  const hasUnsavedChanges = ref(false)
  const savedContent = useLocalStorage<string>(key, '')

  let debounceTimer: ReturnType<typeof setTimeout> | null = null
  let intervalTimer: ReturnType<typeof setInterval> | null = null

  async function save() {
    const currentContent = content()
    if (!currentContent) return

    isSaving.value = true
    try {
      await saveCallback(currentContent)
      savedContent.value = currentContent
      lastSaved.value = new Date()
      hasUnsavedChanges.value = false
    } catch (error) {
      console.error('Auto-save failed:', error)
    } finally {
      isSaving.value = false
    }
  }

  function debouncedSave() {
    hasUnsavedChanges.value = true
    if (debounceTimer) {
      clearTimeout(debounceTimer)
    }
    debounceTimer = setTimeout(save, debounce)
  }

  function restore(): string | null {
    return savedContent.value || null
  }

  function clearSaved() {
    savedContent.value = ''
    lastSaved.value = null
    hasUnsavedChanges.value = false
  }

  onMounted(() => {
    intervalTimer = setInterval(() => {
      if (hasUnsavedChanges.value) {
        save()
      }
    }, interval)
  })

  onUnmounted(() => {
    if (debounceTimer) {
      clearTimeout(debounceTimer)
    }
    if (intervalTimer) {
      clearInterval(intervalTimer)
    }
  })

  return {
    lastSaved,
    isSaving,
    hasUnsavedChanges,
    save,
    debouncedSave,
    restore,
    clearSaved
  }
}

export function useEditorLayout() {
  const layoutMode = useLocalStorage<'split' | 'edit' | 'preview'>('editor-layout-mode', 'split')
  const splitRatio = useLocalStorage<number>('editor-split-ratio', 50)
  const isDragging = ref(false)

  const MIN_RATIO = 20
  const MAX_RATIO = 80

  function setLayoutMode(mode: 'split' | 'edit' | 'preview') {
    layoutMode.value = mode
  }

  function setSplitRatio(ratio: number) {
    splitRatio.value = Math.max(MIN_RATIO, Math.min(MAX_RATIO, ratio))
  }

  function startDrag() {
    isDragging.value = true
  }

  function stopDrag() {
    isDragging.value = false
  }

  function handleDrag(clientX: number, containerRect: DOMRect) {
    if (!isDragging.value) return

    const containerWidth = containerRect.width
    const newRatio = ((clientX - containerRect.left) / containerWidth) * 100
    setSplitRatio(newRatio)
  }

  return {
    layoutMode,
    splitRatio,
    isDragging,
    setLayoutMode,
    setSplitRatio,
    startDrag,
    stopDrag,
    handleDrag,
    MIN_RATIO,
    MAX_RATIO
  }
}
