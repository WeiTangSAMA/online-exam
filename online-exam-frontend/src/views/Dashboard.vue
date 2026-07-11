<template>
  <div class="page-container dashboard-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <div class="hero-kicker">{{ roleText }}工作台</div>
        <h2>欢迎回来，{{ userStore.userInfo?.name || '用户' }}</h2>
        <p>{{ today }}。{{ heroMessage }}</p>
        <div class="hero-actions">
          <el-button type="primary" :icon="primaryAction.icon" @click="router.push(primaryAction.path)">
            {{ primaryAction.label }}
          </el-button>
          <el-button :icon="secondaryAction.icon" @click="router.push(secondaryAction.path)">
            {{ secondaryAction.label }}
          </el-button>
        </div>
      </div>
      <div class="hero-summary" aria-label="核心数据概览">
        <div v-for="item in statCards.slice(0, 2)" :key="item.label" class="hero-metric">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </section>

    <section class="stat-grid">
      <div v-for="item in statCards" :key="item.label" class="stat-card">
        <div class="stat-icon" :class="item.tone">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </section>

    <section class="dashboard-grid">
      <div class="card-box quick-box">
        <div class="section-head">
          <h3 class="section-title">快捷入口</h3>
          <span>按当前角色显示常用任务</span>
        </div>
        <div class="shortcut-grid">
          <button v-for="shortcut in shortcuts" :key="shortcut.title" class="shortcut-item" type="button" @click="router.push(shortcut.path)">
            <span class="shortcut-icon" :class="shortcut.tone">
              <el-icon><component :is="shortcut.icon" /></el-icon>
            </span>
            <span class="shortcut-copy">
              <strong>{{ shortcut.title }}</strong>
              <small>{{ shortcut.desc }}</small>
            </span>
            <el-icon class="shortcut-arrow"><ArrowRight /></el-icon>
          </button>
        </div>
      </div>

      <div class="card-box system-card">
        <div class="section-head">
          <h3 class="section-title">系统概览</h3>
          <span>完整考试闭环</span>
        </div>
        <div class="timeline-list">
          <div v-for="item in overviewItems" :key="item.title" class="timeline-item">
            <span class="timeline-dot" />
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, EditPen, Notebook, DocumentChecked, Medal, DataAnalysis, Reading, TrendCharts } from '@element-plus/icons-vue'
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
  return map[userStore.role] || '用户'
})

const heroMessage = computed(() => {
  if (userStore.isStudent) return '从可参加考试开始，完成答题后可在成绩管理查看详情。'
  return '题库、组卷、发布、成绩与分析都在这里集中处理。'
})

const primaryAction = computed(() => {
  if (userStore.isStudent) return { label: '进入考试', path: '/exam', icon: EditPen }
  return { label: '管理试卷', path: '/paper', icon: Notebook }
})

const secondaryAction = computed(() => {
  if (userStore.isStudent) return { label: '查看成绩', path: '/score', icon: Medal }
  return { label: '查看分析', path: '/stats', icon: DataAnalysis }
})

const statCards = computed(() => {
  if (userStore.isStudent) {
    return [
      { label: '可参加考试', value: examCount.value, icon: 'EditPen', tone: 'blue' },
      { label: '已考次数', value: myScoreCount.value, icon: 'Medal', tone: 'green' },
      { label: '题目总数', value: questionCount.value, icon: 'Reading', tone: 'amber' },
      { label: '试卷总数', value: paperCount.value, icon: 'DocumentChecked', tone: 'slate' }
    ]
  }
  return [
    { label: '题目总数', value: questionCount.value, icon: 'Reading', tone: 'blue' },
    { label: '试卷总数', value: paperCount.value, icon: 'Notebook', tone: 'green' },
    { label: '已发布试卷', value: examCount.value, icon: 'DocumentChecked', tone: 'amber' },
    { label: '成绩记录', value: myScoreCount.value, icon: 'TrendCharts', tone: 'slate' }
  ]
})

const shortcuts = computed(() => {
  if (userStore.isStudent) {
    return [
      { title: '在线考试', desc: '查看并参加已发布考试', icon: 'EditPen', tone: 'blue', path: '/exam' },
      { title: '我的成绩', desc: '查看得分、答案与解析', icon: 'Medal', tone: 'green', path: '/score' }
    ]
  }
  return [
    { title: '题库管理', desc: '维护题目、分类与答案', icon: 'Reading', tone: 'blue', path: '/question' },
    { title: '试卷管理', desc: '组卷、编辑并发布考试', icon: 'Notebook', tone: 'green', path: '/paper' },
    { title: '成绩管理', desc: '查看学生成绩与答题详情', icon: 'Medal', tone: 'amber', path: '/score' },
    { title: '数据分析', desc: '查看分布、通过率与排行', icon: 'DataAnalysis', tone: 'slate', path: '/stats' }
  ]
})

const overviewItems = [
  { title: '题库沉淀', desc: '按题型、分类和分值维护可复用题目。' },
  { title: '试卷发布', desc: '组卷后发布，学生端即可参加考试。' },
  { title: '自动评分', desc: '提交后自动判分，成绩同步进入统计。' }
]

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
    // 统计失败不阻塞工作台主流程
  }
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 22px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 24px;
  align-items: stretch;
  padding: 30px;
  border-radius: 8px;
  color: #fff;
  background: linear-gradient(135deg, #172033 0%, #20327a 58%, #0f766e 100%);
  box-shadow: var(--shadow-lg);
}

.hero-kicker {
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.86);
  font-size: 13px;
  font-weight: 700;
}

.hero-copy h2 {
  margin: 16px 0 10px;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 850;
  letter-spacing: 0;
}

.hero-copy p {
  max-width: 58ch;
  color: rgba(255, 255, 255, 0.76);
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
  flex-wrap: wrap;
}

.hero-summary {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.hero-metric {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 86px;
  padding: 18px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.hero-metric span {
  color: rgba(255, 255, 255, 0.72);
  font-weight: 650;
}

.hero-metric strong {
  font-size: 34px;
  line-height: 1;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 116px;
  padding: 20px;
  background: #fff;
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.stat-icon,
.shortcut-icon {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  border-radius: 8px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  font-size: 22px;
}

.blue { color: #3454d1; background: #eef2ff; }
.green { color: #0f766e; background: #e7f7f4; }
.amber { color: #a16207; background: #fff7e0; }
.slate { color: #405066; background: #eef2f7; }

.stat-value {
  font-size: 30px;
  font-weight: 850;
  line-height: 1;
  color: var(--text-primary);
}

.stat-label {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 650;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
  gap: 22px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 18px;
}

.section-head > span {
  color: var(--text-secondary);
  font-size: 13px;
  white-space: nowrap;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  min-height: 86px;
  padding: 16px;
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: #fbfcff;
  cursor: pointer;
  text-align: left;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.shortcut-item:hover {
  transform: translateY(-2px);
  border-color: #cbd7ff;
  box-shadow: var(--shadow-md);
}

.shortcut-icon {
  width: 42px;
  height: 42px;
  font-size: 20px;
}

.shortcut-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.shortcut-copy strong {
  color: var(--text-primary);
  font-size: 15px;
}

.shortcut-copy small {
  margin-top: 5px;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.4;
}

.shortcut-arrow {
  color: var(--text-placeholder);
}

.timeline-list {
  display: grid;
  gap: 18px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 14px 1fr;
  gap: 12px;
  align-items: start;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  margin-top: 5px;
  border-radius: 999px;
  background: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(52, 84, 209, 0.12);
}

.timeline-item strong {
  color: var(--text-primary);
  font-size: 14px;
}

.timeline-item p {
  margin-top: 5px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 980px) {
  .hero-panel,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .hero-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .hero-panel {
    padding: 22px;
  }

  .hero-copy h2 {
    font-size: 24px;
  }

  .hero-summary {
    grid-template-columns: 1fr;
  }

  .section-head {
    display: block;
  }

  .section-head > span {
    display: block;
    margin-top: 8px;
    white-space: normal;
  }
}
</style>