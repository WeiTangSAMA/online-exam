<template>
  <div class="login-container">
    <section class="login-showcase">
      <div class="showcase-badge">
        <el-icon><Reading /></el-icon>
        在线考试工作台
      </div>
      <h1>让考试管理从组题到成绩闭环更清晰。</h1>
      <p>面向教师、管理员和学生的考试系统，集中处理题库、试卷、答题、评分与统计分析。</p>

      <div class="preview-panel">
        <div class="preview-header">
          <span />
          <span />
          <span />
        </div>
        <div class="preview-row strong">
          <div>
            <small>今日待处理</small>
            <strong>试卷发布检查</strong>
          </div>
          <el-tag type="success" effect="light">进行中</el-tag>
        </div>
        <div class="preview-grid">
          <div>
            <strong>128</strong>
            <span>题库题目</span>
          </div>
          <div>
            <strong>24</strong>
            <span>成绩记录</span>
          </div>
        </div>
      </div>
    </section>

    <section class="login-card">
      <div class="login-header">
        <div class="logo">
          <el-icon><Reading /></el-icon>
        </div>
        <div>
          <h2>在线考试系统</h2>
          <p>登录后进入你的考试工作台</p>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登录系统
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item prop="name">
              <el-input v-model="registerForm.name" placeholder="请输入姓名" :prefix-icon="EditPen" clearable />
            </el-form-item>
            <el-form-item prop="role">
              <el-select v-model="registerForm.role" placeholder="请选择角色" style="width: 100%">
                <el-option label="学生" value="STUDENT" />
                <el-option label="教师" value="TEACHER" />
              </el-select>
            </el-form-item>
            <el-button type="primary" class="login-btn" :loading="loading" @click="handleRegister">
              创建账号
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="test-accounts">
        <p class="tip-title">测试账号，密码均为 123456</p>
        <div class="account-list">
          <el-button text class="account-tag" @click="quickFill('admin')">管理员 admin</el-button>
          <el-button text class="account-tag" @click="quickFill('teacher')">教师 teacher</el-button>
          <el-button text class="account-tag" @click="quickFill('student1')">学生 student1</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, EditPen, Reading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  name: '',
  role: 'STUDENT'
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

function quickFill(username) {
  loginForm.username = username
  loginForm.password = '123456'
  activeTab.value = 'login'
}

async function handleLogin() {
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(loginForm)
      userStore.setLoginInfo(res.data)
      ElMessage.success('登录成功')
      router.push('/')
    } finally {
      loading.value = false
    }
  })
}

async function handleRegister() {
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await register(registerForm)
      ElMessage.success('注册成功，请登录')
      loginForm.username = registerForm.username
      loginForm.password = registerForm.password
      activeTab.value = 'login'
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 480px;
  align-items: stretch;
  background: #f4f6fb;
}

.login-showcase {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 64px clamp(36px, 7vw, 96px);
  color: #fff;
  background: linear-gradient(135deg, #111827 0%, #20327a 58%, #0f766e 100%);
}

.showcase-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.86);
  font-size: 13px;
  font-weight: 700;
}

.login-showcase h1 {
  max-width: 12em;
  margin: 22px 0 16px;
  font-size: 42px;
  line-height: 1.12;
  font-weight: 850;
  letter-spacing: 0;
  text-wrap: balance;
}

.login-showcase p {
  max-width: 58ch;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.75;
}

.preview-panel {
  width: min(420px, 100%);
  margin-top: 42px;
  padding: 18px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.preview-header {
  display: flex;
  gap: 7px;
  margin-bottom: 18px;
}

.preview-header span {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
}

.preview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.10);
}

.preview-row small,
.preview-grid span {
  display: block;
  color: rgba(255, 255, 255, 0.64);
  font-size: 12px;
}

.preview-row strong,
.preview-grid strong {
  display: block;
  margin-top: 5px;
  color: #fff;
  font-size: 18px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.preview-grid > div {
  padding: 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
}

.login-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 56px;
  background: #fff;
  box-shadow: -12px 0 36px rgba(23, 32, 51, 0.08);
}

.login-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 30px;
}

.logo {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: var(--primary-color);
  font-size: 22px;
}

.login-header h2 {
  font-size: 24px;
  color: var(--text-primary);
  font-weight: 850;
  line-height: 1.2;
}

.login-header p {
  margin-top: 5px;
  color: var(--text-secondary);
  font-size: 13px;
}

.login-tabs {
  width: 100%;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--border-lighter);
}

.login-btn {
  width: 100%;
  height: 46px;
  margin-top: 8px;
  border-radius: 8px;
  font-weight: 750;
}

.test-accounts {
  margin-top: 26px;
  padding-top: 18px;
  border-top: 1px solid var(--border-lighter);
}

.tip-title {
  margin-bottom: 10px;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 650;
}

.account-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.account-tag {
  min-height: 30px;
  padding: 0 10px;
  color: var(--primary-dark);
  background: var(--primary-light);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.account-tag:hover {
  color: var(--primary-color);
  background: #e3e9ff;
}

@media (max-width: 980px) {
  .login-container {
    grid-template-columns: 1fr;
  }

  .login-showcase {
    min-height: 42vh;
    padding: 42px 28px;
  }

  .login-showcase h1 {
    font-size: 32px;
  }

  .login-card {
    padding: 34px 24px 42px;
    box-shadow: none;
  }
}

@media (max-width: 520px) {
  .login-showcase {
    min-height: auto;
  }

  .preview-panel {
    display: none;
  }

  .login-header {
    align-items: flex-start;
  }
}
</style>