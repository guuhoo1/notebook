import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { requiresAuth: false, title: '注册' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/note/NoteList.vue'),
        meta: { title: '全部笔记' }
      },
      {
        path: 'note/:id',
        name: 'NoteDetail',
        component: () => import('@/views/note/NoteDetail.vue'),
        meta: { title: '笔记详情' }
      },
      {
        path: 'note/:id/edit',
        name: 'NoteEdit',
        component: () => import('@/views/note/NoteEditor.vue'),
        meta: { title: '编辑笔记' }
      },
      {
        path: 'note/new',
        name: 'NoteNew',
        component: () => import('@/views/note/NoteEditor.vue'),
        meta: { title: '新建笔记' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/CategoryList.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/Settings.vue'),
        meta: { title: '设置' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '记事本'} - 个人记事本`
  
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  
  if (requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && token) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
