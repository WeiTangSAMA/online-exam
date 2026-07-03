<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">📚</div>
        <h1 class="title">在线考试系统</h1>
        <p class="subtitle">Online Examination System</p>
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
              登 录
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
              注 册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="test-accounts">
        <p class="tip-title">测试账号（密码均为 123456）：</p>
        <div class="account-list">
          <el-tag @click="quickFill('admin')" class="account-tag" effect="plain">管理员 admin</el-tag>
          <el-tag @click="quickFill('teacher')" class="account-tag" type="success" effect="plain">教师 teacher</el-tag>
          <el-tag @click="quickFill('student1')" class="account-tag" type="warning" effect="plain">学生 student1</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, EditPen } from '@element-plus/icons-vue'
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
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #e8e8e8 0%, #f5f5f5 30%, #e0e0e0 70%, #f0f0f0 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -30%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(180,180,180,0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.login-container::after {
  content: '';
  position: absolute;
  bottom: -40%;
  left: -20%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(160,160,160,0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 18px;
  padding: 44px 38px 32px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.6);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo {
  font-size: 44px;
  margin-bottom: 10px;
}

.title {
  font-size: 24px;
  color: #1a1a1a;
  margin-bottom: 6px;
  font-weight: 700;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 12px;
  color: #aaa;
  letter-spacing: 3px;
  text-transform: uppercase;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  letter-spacing: 6px;
  margin-top: 10px;
  border-radius: 10px;
  font-weight: 600;
}

.test-accounts {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #eee;
  text-align: center;
}

.tip-title {
  font-size: 12px;
  color: #bbb;
  margin-bottom: 10px;
}

.account-list {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.account-tag {
  cursor: pointer;
  transition: all 0.25s;
  border-radius: 6px;
  font-size: 12px;
}
.account-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
</style>
