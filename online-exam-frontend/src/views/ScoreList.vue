<template>
  <div class="page-container">
    <div class="card-box">
      <div class="table-toolbar">
        <div class="search-area">
          <template v-if="!userStore.isStudent">
            <el-select v-model="paperFilter" placeholder="选择试卷筛选" clearable filterable style="width: 220px" @change="loadData">
              <el-option v-for="p in allPapers" :key="p.id" :label="p.title" :value="p.id" />
            </el-select>
          </template>
        </div>
        <div v-if="!userStore.isStudent" class="tab-switch">
          <el-radio-group v-model="scoreTab" @change="loadData">
            <el-radio-button value="all">全部成绩</el-radio-button>
            <el-radio-button value="rank">成绩排行</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column type="index" label="排名" width="70" align="center" v-if="scoreTab === 'rank'" />
        <el-table-column prop="name" label="学生" width="120" align="center" v-if="!userStore.isStudent">
          <template #default="{ row }">
            {{ row.name }} <span style="color:#bbb">({{ row.username }})</span>
          </template>
        </el-table-column>
        <el-table-column prop="paperTitle" label="试卷" min-width="200" show-overflow-tooltip />
        <el-table-column label="成绩" width="140" align="center" sortable :sort-method="sortByScore">
          <template #default="{ row }">
            <span class="score-text" :class="{ pass: row.passed, fail: !row.passed }">
              {{ row.score }}
            </span>
            <span class="score-total"> / {{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.passed ? 'success' : 'danger'" effect="light">
              {{ row.passed ? '及格' : '不及格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" align="center" sortable />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 答题详情对话框 -->
    <el-dialog v-model="detailVisible" title="答题详情" width="750px" top="5vh">
      <div v-loading="detailLoading">
        <div class="detail-header">
          <h3>{{ detailData.paperTitle }}</h3>
          <div class="detail-score-box">
            <span>得分：</span>
            <span class="detail-score" :class="{ pass: detailData.score >= (detailData.totalScore * 0.6) }">
              {{ detailData.score }}
            </span>
            <span> / {{ detailData.totalScore }}</span>
          </div>
        </div>
        <div class="detail-list">
          <div v-for="(ans, idx) in detailData.answers" :key="ans.questionId" class="detail-item">
            <div class="detail-q-header">
              <el-tag :type="typeTag(ans.type)" size="small">{{ typeText(ans.type) }}</el-tag>
              <span class="detail-q-no">第 {{ idx + 1 }} 题</span>
              <span class="detail-q-score" :class="ans.isCorrect ? 'right' : 'wrong'">
                {{ ans.isCorrect ? `+${ans.score}分 ✓` : '0分 ✗' }}
              </span>
            </div>
            <div class="detail-q-content">{{ ans.content }}</div>
            <div class="detail-ans-row">
              <span class="ans-label">你的答案：</span>
              <span class="ans-value" :class="ans.isCorrect ? 'right' : 'wrong'">
                {{ formatAnswer(ans.userAnswer) || '未作答' }}
              </span>
            </div>
            <div class="detail-ans-row" v-if="!ans.isCorrect">
              <span class="ans-label">正确答案：</span>
              <span class="ans-value right">{{ formatAnswer(ans.correctAnswer) }}</span>
            </div>
            <div class="detail-analysis" v-if="ans.analysis">
              <el-icon><InfoFilled /></el-icon> {{ ans.analysis }}
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { View, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import { getMyScores, getAllScores, getRanking } from '../api/score'
import { getExamDetail } from '../api/exam'
import { getPaperPage } from '../api/paper'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const allPapers = ref([])
const paperFilter = ref(null)
const scoreTab = ref('all')

const queryParams = reactive({ pageNum: 1, pageSize: 10 })

// 详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = reactive({ paperTitle: '', score: 0, totalScore: 100, answers: [] })

function typeText(type) {
  return { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断' }[type] || ''
}
function typeTag(type) {
  return { SINGLE: 'primary', MULTIPLE: 'success', JUDGE: 'warning' }[type] || ''
}

function formatAnswer(ans) {
  if (!ans) return ''
  const map = { T: '正确', F: '错误' }
  if (map[ans]) return map[ans]
  return ans
}

function sortByScore(a, b) {
  return a.score - b.score
}

async function loadData() {
  loading.value = true
  try {
    let res
    if (userStore.isStudent) {
      res = await getMyScores(queryParams)
    } else if (scoreTab.value === 'rank') {
      res = await getRanking({ ...queryParams, paperId: paperFilter.value })
    } else {
      res = await getAllScores({ ...queryParams, paperId: paperFilter.value })
    }
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadPapers() {
  if (userStore.isStudent) return
  const res = await getPaperPage({ pageNum: 1, pageSize: 100 })
  allPapers.value = res.data.records
}

async function viewDetail(row) {
  if (!row.recordId) {
    ElMessage.warning('该成绩缺少答题记录，无法查看详情')
    return
  }
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getExamDetail(row.recordId)
    Object.assign(detailData, { paperTitle: '', score: 0, totalScore: 100, answers: [] }, res.data)
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadPapers()
  loadData()
})
</script>

<style scoped>
.search-area { display: flex; gap: 10px; align-items: center; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.score-text { font-size: 18px; font-weight: 700; }
.score-text.pass { color: #52c41a; }
.score-text.fail { color: #ff4d4f; }
.score-total { color: #bbb; font-size: 13px; }
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}
.detail-score-box { font-size: 16px; color: #888; }
.detail-score { font-size: 28px; font-weight: 700; color: #ff4d4f; }
.detail-score.pass { color: #52c41a; }
.detail-list {
  max-height: 60vh;
  overflow-y: auto;
}
.detail-item {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 12px;
}
.detail-q-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.detail-q-no { color: #aaa; font-size: 13px; }
.detail-q-score { margin-left: auto; font-weight: 600; }
.detail-q-score.right { color: #52c41a; }
.detail-q-score.wrong { color: #ff4d4f; }
.detail-q-content { color: #1a1a1a; margin-bottom: 10px; line-height: 1.6; }
.detail-ans-row { margin-bottom: 6px; }
.ans-label { color: #aaa; font-size: 13px; }
.ans-value { font-weight: 600; }
.ans-value.right { color: #52c41a; }
.ans-value.wrong { color: #ff4d4f; }
.detail-analysis {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f7f7f7;
  border-radius: 8px;
  color: #888;
  font-size: 13px;
  display: flex;
  align-items: flex-start;
  gap: 6px;
}
</style>
