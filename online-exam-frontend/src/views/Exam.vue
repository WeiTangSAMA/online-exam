<template>
  <div class="exam-page" v-loading="loading">
    <!-- 顶部栏：考试名 + 倒计时 -->
    <div class="exam-topbar">
      <div class="topbar-left">
        <el-button text :icon="Back" @click="confirmExit">退出</el-button>
        <span class="exam-title">{{ paperInfo.title }}</span>
      </div>
      <div class="countdown" :class="{ warning: remainSeconds < 300 }">
        <el-icon><AlarmClock /></el-icon>
        <span>{{ formattedTime }}</span>
      </div>
    </div>

    <div class="exam-body" v-if="paperInfo.questions?.length">
      <div class="exam-layout">
        <!-- 左侧题号导航 -->
        <div class="question-nav">
          <h4 class="nav-title">答题卡</h4>
          <p class="nav-tip">
            <el-tag type="success" size="small" effect="plain">已答 {{ answeredCount }}</el-tag>
            <el-tag type="info" size="small" effect="plain">共 {{ paperInfo.questions.length }} 题</el-tag>
          </p>
          <div class="nav-grid">
            <div
              v-for="(q, idx) in paperInfo.questions"
              :key="q.id"
              class="nav-item"
              :class="{
                answered: isAnswered(q.id),
                current: currentIndex === idx
              }"
              @click="goTo(idx)"
            >
              {{ idx + 1 }}
            </div>
          </div>
          <el-button type="primary" class="submit-all-btn" @click="confirmSubmit">
            交卷
          </el-button>
        </div>

        <!-- 中间题目内容 -->
        <div class="question-content">
          <div class="question-card" v-if="currentQuestion">
            <div class="question-header">
              <el-tag :type="typeTag(currentQuestion.type)" size="large">
                {{ typeText(currentQuestion.type) }}
              </el-tag>
              <span class="question-no">第 {{ currentIndex + 1 }} 题 / 共 {{ paperInfo.questions.length }} 题</span>
              <span class="question-score">{{ currentQuestion.score }} 分</span>
            </div>
            <div class="question-text">{{ currentQuestion.content }}</div>

            <!-- 选项区 -->
            <div class="options-area">
              <!-- 单选题 -->
              <el-radio-group v-if="currentQuestion.type === 'SINGLE'" v-model="answers[currentQuestion.id]">
                <el-radio
                  v-for="opt in parsedOptions(currentQuestion.options)"
                  :key="opt.value"
                  :value="opt.value"
                  class="option-item"
                >
                  {{ opt.value }}. {{ opt.text }}
                </el-radio>
              </el-radio-group>

              <!-- 多选题 -->
              <el-checkbox-group v-else-if="currentQuestion.type === 'MULTIPLE'" v-model="multiAnswers[currentQuestion.id]">
                <el-checkbox
                  v-for="opt in parsedOptions(currentQuestion.options)"
                  :key="opt.value"
                  :value="opt.value"
                  class="option-item"
                >
                  {{ opt.value }}. {{ opt.text }}
                </el-checkbox>
              </el-checkbox-group>

              <!-- 判断题 -->
              <el-radio-group v-else-if="currentQuestion.type === 'JUDGE'" v-model="answers[currentQuestion.id]">
                <el-radio value="T" class="option-item">✓ 正确</el-radio>
                <el-radio value="F" class="option-item">✗ 错误</el-radio>
              </el-radio-group>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div class="question-footer">
            <el-button :icon="ArrowLeft" :disabled="currentIndex === 0" @click="prev">上一题</el-button>
            <span class="footer-progress">{{ currentIndex + 1 }} / {{ paperInfo.questions.length }}</span>
            <el-button v-if="currentIndex < paperInfo.questions.length - 1" type="primary" @click="next">
              下一题 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
            <el-button v-else type="success" @click="confirmSubmit">交卷</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 成绩结果对话框 -->
    <el-dialog v-model="resultVisible" title="考试结果" width="420px" :close-on-click-modal="false" :show-close="false">
      <div class="result-box">
        <div class="result-emoji">{{ finalScore >= paperInfo.passScore ? '🎉' : '😢' }}</div>
        <div class="result-score" :class="{ pass: finalScore >= paperInfo.passScore }">{{ finalScore }}</div>
        <div class="result-total">总分 {{ paperInfo.totalScore }} 分 / 及格 {{ paperInfo.passScore }} 分</div>
        <el-tag :type="finalScore >= paperInfo.passScore ? 'success' : 'danger'" size="large" effect="dark">
          {{ finalScore >= paperInfo.passScore ? '恭喜通过' : '未通过' }}
        </el-tag>
      </div>
      <template #footer>
        <el-button type="primary" @click="goScore">查看成绩</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Back, ArrowLeft, ArrowRight, AlarmClock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamPaper, startExam, submitExam } from '../api/exam'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const paperInfo = reactive({ id: null, title: '', totalScore: 0, duration: 60, passScore: 60, questions: [] })
const currentIndex = ref(0)
const answers = reactive({})      // 单选/判断题答案 { questionId: 'A' }
const multiAnswers = reactive({}) // 多选题答案 { questionId: ['A','B'] }
const recordId = ref(null)
const remainSeconds = ref(0)
let timer = null

const resultVisible = ref(false)
const finalScore = ref(0)

const currentQuestion = computed(() => paperInfo.questions[currentIndex.value])

const formattedTime = computed(() => {
  const m = Math.floor(remainSeconds.value / 60)
  const s = remainSeconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

const answeredCount = computed(() => {
  return paperInfo.questions.filter(q => isAnswered(q.id)).length
})

// 缓存 key（用于本地自动保存）
const cacheOwner = computed(() => userStore.userInfo?.userId || userStore.userInfo?.username || 'anonymous')
const cacheKey = computed(() => paperInfo.id ? `exam_cache_${cacheOwner.value}_${paperInfo.id}` : '')
const legacyCacheKey = computed(() => paperInfo.id ? `exam_cache_${paperInfo.id}` : '')

function typeText(type) {
  return { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题' }[type] || ''
}
function typeTag(type) {
  return { SINGLE: 'primary', MULTIPLE: 'success', JUDGE: 'warning' }[type] || ''
}

function parsedOptions(jsonStr) {
  try {
    const arr = JSON.parse(jsonStr || '[]')
    // 形如 ["A. xxx", "B. yyy"] 拆分
    return arr.map(item => {
      const match = item.match(/^([A-H])\.\s*(.*)$/)
      if (match) return { value: match[1], text: match[2] }
      return { value: item, text: item }
    })
  } catch {
    return []
  }
}

function isAnswered(qid) {
  if (multiAnswers[qid] !== undefined) {
    return multiAnswers[qid] && multiAnswers[qid].length > 0
  }
  return !!answers[qid]
}

// 监听答案变化，自动保存到本地
watch([answers, multiAnswers], () => {
  saveCache()
}, { deep: true })

function saveCache() {
  if (!cacheKey.value) return
  const data = {
    answers: { ...answers },
    multiAnswers: { ...multiAnswers }
  }
  localStorage.setItem(cacheKey.value, JSON.stringify(data))
}

function loadCache() {
  if (!cacheKey.value) return
  try {
    const data = JSON.parse(localStorage.getItem(cacheKey.value) || '{}')
    if (data.answers) Object.assign(answers, data.answers)
    if (data.multiAnswers) Object.assign(multiAnswers, data.multiAnswers)
  } catch {}
}

function clearCache() {
  if (cacheKey.value) localStorage.removeItem(cacheKey.value)
  if (legacyCacheKey.value) localStorage.removeItem(legacyCacheKey.value)
}

function goTo(idx) {
  currentIndex.value = idx
}
function prev() {
  if (currentIndex.value > 0) currentIndex.value--
}
function next() {
  if (currentIndex.value < paperInfo.questions.length - 1) currentIndex.value++
}

function startTimer(reset = true) {
  if (timer) clearInterval(timer)
  if (reset) remainSeconds.value = paperInfo.duration * 60
  timer = setInterval(() => {
    remainSeconds.value--
    if (remainSeconds.value <= 0) {
      clearInterval(timer)
      timer = null
      ElMessage.warning('考试时间到，系统自动交卷')
      doSubmit(true)
    }
  }, 1000)
}

function confirmExit() {
  ElMessageBox.confirm('退出后已答内容会保留在本地缓存，确定退出吗？', '提示', {
    type: 'warning'
  }).then(() => {
    clearInterval(timer)
    router.push('/exam')
  }).catch(() => {})
}

function confirmSubmit() {
  const unanswered = paperInfo.questions.length - answeredCount.value
  const msg = unanswered > 0
    ? `还有 ${unanswered} 题未作答，确定要交卷吗？`
    : '已完成全部题目，确定交卷吗？'
  ElMessageBox.confirm(msg, '交卷确认', {
    confirmButtonText: '确定交卷',
    cancelButtonText: '继续作答',
    type: 'warning'
  }).then(() => {
    doSubmit(false)
  }).catch(() => {})
}

async function doSubmit(auto) {
  if (timer) clearInterval(timer)
  // 组装答案
  const answerList = paperInfo.questions.map(q => {
    let ua = ''
    if (q.type === 'MULTIPLE') {
      ua = (multiAnswers[q.id] || []).slice().sort().join(',')
    } else {
      ua = answers[q.id] || ''
    }
    return { questionId: q.id, userAnswer: ua }
  })

  try {
    const res = await submitExam({ recordId: recordId.value, answers: answerList })
    finalScore.value = res.data
    clearCache()
    resultVisible.value = true
  } catch {
    // 恢复计时（如果不是自动交卷）
    if (!auto && remainSeconds.value > 0) startTimer(false)
  }
}

function goScore() {
  resultVisible.value = false
  router.push('/score')
}

onMounted(async () => {
  const paperId = route.params.paperId
  try {
    // 1. 获取试卷
    const res = await getExamPaper(paperId)
    Object.assign(paperInfo, res.data)
    // 初始化多选题答案数组
    paperInfo.questions.forEach(q => {
      if (q.type === 'MULTIPLE') {
        multiAnswers[q.id] = []
      }
    })
    // 加载本地缓存
    loadCache()
    // 2. 开始考试（创建记录）
    const startRes = await startExam(paperId)
    recordId.value = startRes.data
    // 3. 启动倒计时
    startTimer()
  } catch (e) {
    ElMessage.error('加载试卷失败')
    router.push('/exam')
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.exam-page {
  height: 100vh;
  background: #f4f5f7;
  display: flex;
  flex-direction: column;
}
.exam-topbar {
  height: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
}
.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.exam-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: 0.5px;
}
.countdown {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  background: #eee;
  padding: 8px 20px;
  border-radius: 10px;
  letter-spacing: 1px;
}
.countdown.warning {
  color: #fff;
  background: #ff4d4f;
  animation: blink 1s infinite;
}
@keyframes blink {
  50% { opacity: 0.7; }
}
.exam-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
.exam-layout {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 20px;
}
.question-nav {
  width: 260px;
  background: #fff;
  border-radius: 12px;
  padding: 22px;
  height: fit-content;
  position: sticky;
  top: 20px;
  border: 1px solid #eee;
}
.nav-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #1a1a1a;
}
.nav-tip {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}
.nav-item {
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  color: #aaa;
  background: #fafafa;
  transition: all 0.2s;
}
.nav-item:hover {
  border-color: #999;
  color: #555;
}
.nav-item.answered {
  background: #2c2c2c;
  color: #fff;
  border-color: #2c2c2c;
}
.nav-item.current {
  box-shadow: 0 0 0 2px #555;
}
.submit-all-btn {
  width: 100%;
}
.question-content {
  flex: 1;
  min-width: 0;
}
.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 28px;
  margin-bottom: 16px;
  border: 1px solid #eee;
}
.question-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.question-no {
  color: #aaa;
  font-size: 14px;
}
.question-score {
  margin-left: auto;
  color: #888;
  font-weight: 600;
}
.question-text {
  font-size: 17px;
  color: #1a1a1a;
  line-height: 1.8;
  margin-bottom: 24px;
}
.options-area {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.option-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-right: 0;
  height: auto;
  width: 100%;
  background: #fff;
  transition: all 0.2s;
}
.option-item:hover {
  border-color: #bbb;
  background: #f7f7f7;
}
.option-item.is-checked,
.option-item.el-radio.is-checked {
  border-color: #2c2c2c;
  background: #f0f0f0;
}
.question-footer {
  background: #fff;
  border-radius: 12px;
  padding: 16px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #eee;
}
.footer-progress {
  color: #aaa;
}
.result-box {
  text-align: center;
  padding: 20px 0;
}
.result-emoji {
  font-size: 60px;
}
.result-score {
  font-size: 56px;
  font-weight: 700;
  color: #ff4d4f;
  margin: 10px 0;
}
.result-score.pass {
  color: #52c41a;
}
.result-total {
  color: #aaa;
  margin-bottom: 16px;
}
</style>
