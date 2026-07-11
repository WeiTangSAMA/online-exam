<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '72px' : '248px'" class="sidebar">
      <div class="logo-box" :class="{ collapsed: isCollapse }">
        <div class="logo-mark">
          <el-icon><Reading /></el-icon>
        </div>
        <div v-show="!isCollapse" class="logo-copy">
          <span class="logo-text">在线考试系统</span>
          <span class="logo-subtitle">Exam Workspace</span>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item :index="'/' + route.path">
            <el-icon><component :is="route.meta.icon" /></el-icon>
            <template #title>{{ route.meta.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="workspace">
      <el-header class="header">
        <div class="header-left">
          <el-button class="collapse-btn" text circle @click="isCollapse = !isCollapse">
            <el-icon>
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <div class="route-heading">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
            <h1>{{ currentTitle || '工作台' }}</h1>
          </div>
        </div>

        <div class="header-right">
          <div class="role-pill">
            <span class="role-dot" />
            {{ roleText }}
          </div>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="34" class="user-avatar">
                {{ userStore.userInfo?.name?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.name }}</span>
              <el-icon class="chevron"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const menuRoutes = computed(() => {
  const rootRoute = router.options.routes.find(r => r.path === '/')
  if (!rootRoute || !rootRoute.children) return []
  return rootRoute.children.filter(child => {
    if (child.meta?.hidden) return false
    if (child.meta?.roles) {
      return child.meta.roles.includes(userStore.role)
    }
    return true
  })
})

const activeMenu = computed(() => '/' + (route.path.split('/')[1] || 'dashboard'))
const currentTitle = computed(() => route.meta.title || '')

const roleText = computed(() => {
  const map = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[userStore.role] || ''
})

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: var(--bg-color);
}

.sidebar {
  position: relative;
  background: #111827;
  transition: width 0.24s ease;
  overflow: hidden;
  box-shadow: 8px 0 26px rgba(17, 24, 39, 0.12);
}

.sidebar::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(52, 84, 209, 0.18), transparent 42%);
}

.logo-box {
  position: relative;
  z-index: 1;
  height: 76px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-box.collapsed {
  justify-content: center;
  padding: 0;
}

.logo-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: var(--primary-color);
  box-shadow: 0 8px 18px rgba(52, 84, 209, 0.28);
  flex: 0 0 auto;
}

.logo-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 800;
  line-height: 1.2;
  white-space: nowrap;
}

.logo-subtitle {
  margin-top: 3px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.56);
  letter-spacing: 0.04em;
}

.sidebar-menu {
  position: relative;
  z-index: 1;
  border-right: none;
  padding: 14px 10px;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(255, 255, 255, 0.68);
  --el-menu-active-color: #fff;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
  --el-menu-item-font-size: 14px;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 248px;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 44px;
  margin: 4px 0;
  border-radius: 8px;
  font-weight: 650;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(52, 84, 209, 0.95);
  box-shadow: 0 8px 18px rgba(52, 84, 209, 0.24);
}

.workspace {
  min-width: 0;
}

.header {
  height: 76px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(14px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 26px;
  border-bottom: 1px solid var(--border-lighter);
  box-shadow: var(--shadow-sm);
}

.header-left,
.header-right,
.user-info {
  display: flex;
  align-items: center;
}

.header-left {
  gap: 14px;
  min-width: 0;
}

.collapse-btn {
  color: var(--text-secondary);
  background: #f5f7fb;
}

.route-heading {
  min-width: 0;
}

.route-heading h1 {
  margin-top: 4px;
  font-size: 20px;
  line-height: 1.1;
  color: var(--text-primary);
  font-weight: 800;
}

.header-right {
  gap: 12px;
}

.role-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  color: var(--primary-dark);
  background: var(--primary-light);
  font-size: 13px;
  font-weight: 700;
}

.role-dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: var(--accent-color);
}

.user-info {
  gap: 10px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.18s ease;
}

.user-info:hover {
  background: #f3f6fb;
}

.user-avatar {
  background: #172033;
  color: #fff;
  font-weight: 750;
}

.user-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 700;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chevron {
  color: var(--text-secondary);
}

.main-content {
  background: var(--bg-color);
  padding: 0;
  overflow-y: auto;
}

@media (max-width: 860px) {
  .header {
    padding: 0 16px;
  }

  .route-heading h1,
  .role-pill,
  .user-name {
    display: none;
  }
}
</style>