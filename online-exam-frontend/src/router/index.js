import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  // 考试答题页 —— 独立全屏页面，不嵌套 Layout
  {
    path: '/exam/take/:paperId',
    name: 'Exam',
    component: () => import('../views/Exam.vue'),
    meta: { title: '答题', roles: ['STUDENT'] }
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '系统首页', icon: 'HomeFilled' }
      },
      {
        path: 'question',
        name: 'QuestionManage',
        component: () => import('../views/QuestionManage.vue'),
        meta: { title: '题库管理', icon: 'Document', roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'paper',
        name: 'PaperManage',
        component: () => import('../views/PaperManage.vue'),
        meta: { title: '试卷管理', icon: 'Notebook', roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'exam',
        name: 'ExamList',
        component: () => import('../views/ExamList.vue'),
        meta: { title: '在线考试', icon: 'EditPen', roles: ['STUDENT'] }
      },
      {
        path: 'score',
        name: 'ScoreList',
        component: () => import('../views/ScoreList.vue'),
        meta: { title: '成绩管理', icon: 'TrophyBase' }
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/Stats.vue'),
        meta: { title: '数据分析', icon: 'DataAnalysis', roles: ['ADMIN', 'TEACHER'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 公开页面（登录页）直接放行
  if (to.meta.public) {
    if (userStore.isLoggedIn && to.path === '/login') {
      next('/')
    } else {
      next()
    }
    return
  }

  // 未登录跳转登录页
  if (!userStore.isLoggedIn) {
    next('/login')
    return
  }

  // 角色权限校验
  if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    next('/')
    return
  }
  next()
})

export default router
