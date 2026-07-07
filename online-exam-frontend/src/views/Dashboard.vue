<template>
  <div class="page-container">
    <!-- 欢迎卡片 -->
    <div class="welcome-card card-box">
      <div class="welcome-text">
        <h2>欢迎回来，{{ userStore.userInfo?.name }} 👋</h2>
        <p>今天是 {{ today }}，祝你工作顺利！</p>
      </div>
      <div class="welcome-icon">📊</div>
    </div>

    <!-- 数据统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <div class="stat-card card-box" :style="{ background: item.bg }">
          <div class="stat-icon">{{ item.icon }}</div>
          <div class="stat-info">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <div class="card-box quick-box">
      <h3 class="section-title">快捷入口</h3>
      <el-row :gutter="16">
        <el-col :span="6" v-for="shortcut in shortcuts" :key="shortcut.title">
          <div class="shortcut-item" @click="router.push(shortcut.path)">
            <div class="shortcut-icon" :style="{ background: shortcut.color }">{{ shortcut.icon }}</div>
            <div class="shortcut-title">{{ shortcut.title }}</div>
            <div class="shortcut-desc">{{ shortcut.desc }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 角色说明 -->
    <div class="card-box">
      <h3 class="section-title">系统介绍</h3>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="系统名称">在线考试系统 (Online Examination System)</el-descriptions-item>
        <el-descriptions-item label="当前角色">
          <el-tag :type="roleTagType">{{ roleText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="主要功能">题库管理、试卷组卷、在线考试、自动评分、成绩管理、数据可视化</el-descriptions-item>
        <el-descriptions-item label="技术栈">Spring Boot + MyBatis-Plus + Vue 3 + Element Plus + ECharts</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getQuestionPage } from '../api/question'
import { getPaperPage, getPublishedPapers } from '../api/paper'
import { getMyScores } from '../api/score'

const router = useRouter()
const userStore = useUserStore()

const questionCount = ref(0)
const paperCount = ref(0)
const examCount = ref(0)
const myScoreCount = ref(0)

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
})

const roleText = computed(() => {
  const map = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[userStore.role] || ''
})

const roleTagType = computed(() => {
  const map = { ADMIN: 'danger', TEACHER: 'success', STUDENT: 'warning' }
  return map[userStore.role] || ''
})

const statCards = computed(() => {
  if (userStore.isStudent) {
    return [
      { label: '可参加考试', value: examCount.value, icon: '📝', bg: 'linear-gradient(135deg, #e8e8e8, #d5d5d5)' },
      { label: '已考次数', value: myScoreCount.value, icon: '✅', bg: 'linear-gradient(135deg, #e0e8e4, #c8d8cc)' },
      { label: '题目总数', value: questionCount.value, icon: '📚', bg: 'linear-gradient(135deg, #e4e8ec, #d0d8e0)' },
      { label: '试卷总数', value: paperCount.value, icon: '📄', bg: 'linear-gradient(135deg, #eae4e0, #d8d0c8)' }
    ]
  }
  return [
    { label: '题目总数', value: questionCount.value, icon: '📚', bg: 'linear-gradient(135deg, #e8e8e8, #d5d5d5)' },
    { label: '试卷总数', value: paperCount.value, icon: '📄', bg: 'linear-gradient(135deg, #e0e8e4, #c8d8cc)' },
    { label: '已发布试卷', value: examCount.value, icon: '✅', bg: 'linear-gradient(135deg, #e4e8ec, #d0d8e0)' },
    { label: '成绩记录', value: myScoreCount.value, icon: '🏆', bg: 'linear-gradient(135deg, #eae4e0, #d8d0c8)' }
  ]
})

const shortcuts = computed(() => {
  if (userStore.isStudent) {
    return [
      { title: '在线考试', desc: '参加考试', icon: '📝', color: '#555', path: '/exam' },
      { title: '我的成绩', desc: '查看成绩', icon: '🏆', color: '#888', path: '/score' }
    ]
  }
  return [
    { title: '题库管理', desc: '管理题目', icon: '📚', color: '#555', path: '/question' },
    { title: '试卷管理', desc: '组卷发布', icon: '📄', color: '#888', path: '/paper' },
    { title: '成绩管理', desc: '查看成绩', icon: '🏆', color: '#aaa', path: '/score' },
    { title: '数据分析', desc: '统计分析', icon: '📊', color: '#777', path: '/stats' }
  ]
})

onMounted(async () => {
  try {
    if (userStore.isStudent) {
      const eRes = await getPublishedPapers({ pageNum: 1, pageSize: 1 })
      examCount.value = eRes.data.total
      paperCount.value = eRes.data.total
      const sRes = await getMyScores({ pageNum: 1, pageSize: 1 })
      myScoreCount.value = sRes.data.total
      return
    }

    const qRes = await getQuestionPage({ pageNum: 1, pageSize: 1 })
    questionCount.value = qRes.data.total
    const pRes = await getPaperPage({ pageNum: 1, pageSize: 1 })
    paperCount.value = pRes.data.total
    const eRes = await getPaperPage({ pageNum: 1, pageSize: 1, status: 1 })
    examCount.value = eRes.data.total
    const sRes = await getMyScores({ pageNum: 1, pageSize: 1 })
    myScoreCount.value = sRes.data.total
  } catch (e) {
    // 忽略统计错误
  }
})
</script>

<style scoped>
.welcome-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #f8f8f8 0%, #eee 100%);
  color: #1a1a1a;
  margin-bottom: 20px;
  border: 1px solid #e8e8e8;
}

.welcome-text h2 {
  font-size: 22px;
  margin-bottom: 8px;
  font-weight: 700;
  color: #1a1a1a;
}

.welcome-text p {
  color: #999;
  font-size: 14px;
}

.welcome-icon {
  font-size: 56px;
  opacity: 0.8;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #1a1a1a;
  border: 1px solid #e8e8e8;
}

.stat-icon {
  font-size: 38px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}

.quick-box {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid #2c2c2c;
}

.shortcut-item {
  text-align: center;
  padding: 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #eee;
  background: #fafafa;
}

.shortcut-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  border-color: #ddd;
}

.shortcut-icon {
  width: 52px;
  height: 52px;
  line-height: 52px;
  margin: 0 auto 12px;
  border-radius: 14px;
  font-size: 26px;
  color: #fff;
}

.shortcut-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.shortcut-desc {
  font-size: 12px;
  color: #aaa;
}
</style>
