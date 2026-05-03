import { defineStore } from 'pinia'
import { ref } from 'vue'
import { categoryApi } from '@/api'
import type { Category, CategoryParams } from '@/types'

/**
 * 分类状态管理Store
 */
export const useCategoryStore = defineStore(
  'category',
  () => {
    const categories = ref<Category[]>([])
    const activeCategory = ref<Category | null>(null)
    const loading = ref(false)

    /**
     * 获取分类列表
     */
    async function fetchCategories() {
      loading.value = true
      try {
        const res = await categoryApi.getList()
        if (res.code === 200 && res.data) {
          categories.value = res.data
        }
        return res
      } finally {
        loading.value = false
      }
    }

    /**
     * 创建分类
     */
    async function createCategory(data: CategoryParams) {
      const res = await categoryApi.create(data)
      if (res.code === 200 && res.data) {
        categories.value.push(res.data)
      }
      return res
    }

    /**
     * 更新分类
     */
    async function updateCategory(id: number, data: Partial<CategoryParams>) {
      const res = await categoryApi.update(id, data)
      if (res.code === 200 && res.data) {
        const index = categories.value.findIndex((c) => c.id === id)
        if (index !== -1) {
          categories.value[index] = res.data
        }
      }
      return res
    }

    /**
     * 删除分类
     */
    async function deleteCategory(id: number) {
      const res = await categoryApi.delete(id)
      if (res.code === 200) {
        categories.value = categories.value.filter((c) => c.id !== id)
        if (activeCategory.value?.id === id) {
          activeCategory.value = null
        }
      }
      return res
    }

    /**
     * 设置当前分类
     */
    function setActiveCategory(category: Category | null) {
      activeCategory.value = category
    }

    return {
      categories,
      activeCategory,
      loading,
      fetchCategories,
      createCategory,
      updateCategory,
      deleteCategory,
      setActiveCategory,
    }
  },
  {
    persist: {
      key: 'category-store',
      paths: ['activeCategory'],
    },
  }
)
