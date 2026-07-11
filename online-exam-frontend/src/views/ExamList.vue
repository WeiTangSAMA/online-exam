<template>
  <div class="page-container">
    <div class="card-box">
      <div class="table-toolbar">
        <h3 class="section-title">可参加的考试</h3>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="paper in paperList" :key="paper.id">
          <div class="paper-card">
            <div class="paper-card-header" :style="{ background: cardColors[paper.id % cardColors.length] }">
              <div class="paper-icon">📝</div>
              <el-tag effect="dark" round>进行中</el-tag>
            </div>
            <div class="paper-card-body">
              <h3 class="paper-title">{{ paper.title }}</h3>
              <div class="paper-meta">
                <span><el-icon><Clock /></el-icon> {{ paper.duration }} 分钟</span>
                <span><el-icon><TrophyBase /></el-icon> 总分 {{ paper.totalScore }} 分</span>
                <span><el-icon><Check /></el-icon> 及格 {{ paper.passScore }} 分</span>
              </div>
              <p class="paper-desc">共 {{ paper.questionCount || '-' }} 道题目，请在规定时间内完成作答。</p>
              <el-button type="primary" class="start-btn" @click="startExam(paper)">
                开始考试 <el-icon class="el-icon--right"><Right /></el-icon>
              </el-button>
            </div>
          </div>
        </el-col>
        <el-col :span="24" v-if="!loading && paperList.length === 0">
          <el-empty description="暂无可参加的考试" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, TrophyBase, Check, Right } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { getPublishedPapers } from '../api/paper'
import { getExamPaper } from '../api/exam'

const router = useRouter()
const loading = ref(false)
const paperList = ref([])

const cardColors = [
  'linear-gradient(135deg, #e8e8e8, #d8d8d8)',
  'linear-gradient(135deg, #e4ebe8, #d0ddd4)',
  'linear-gradient(135deg, #e6e8ec, #d2d6de)',
  'linear-gradient(135deg, #eae6e2, #d8d0c8)',
  'linear-gradient(135deg, #e0e0e0, #ccc)'
]

async function loadData() {
  loading.value = true
  try {
    const res = await getPublishedPapers({ pageNum: 1, pageSize: 100 })
    // 查询每张试卷的题目数量
    const papers = res.data.records
    for (const p of papers) {
      try {
        const examRes = await getExamPaper(p.id)
        p.questionCount = examRes.data.questions?.length || 0
      } catch {
        p.questionCount = 0
      }
    }
    paperList.value = papers
  } finally {
    loading.value = false
  }
}

async function startExam(paper) {
  ElMessageBox.confirm(
    `确定开始考试「${paper.title}」吗？\n考试时长 ${paper.duration} 分钟，开始后请专注作答。`,
    '开始考试',
    { confirmButtonText: '开始', cancelButtonText: '取消', type: 'info' }
  ).then(() => {
    router.push(`/exam/take/${paper.id}`)
  }).catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
.paper-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #eee;
}
.paper-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.07);
}
.paper-card-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  color: #1a1a1a;
}
.paper-icon {
  font-size: 34px;
}
.paper-card-body {
  padding: 20px;
}
.paper-title {
  font-size: 17px;
  color: #1a1a1a;
  margin-bottom: 12px;
  font-weight: 600;
}
.paper-meta {
  display: flex;
  gap: 16px;
  color: #888;
  font-size: 13px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.paper-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.paper-desc {
  color: #aaa;
  font-size: 13px;
  margin-bottom: 16px;
  line-height: 1.6;
}
.start-btn {
  width: 100%;
}
</style>
