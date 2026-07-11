<template>
  <div class="page-container">
    <!-- 顶部筛选 -->
    <div class="card-box">
      <div class="table-toolbar">
        <h3 class="section-title">成绩数据分析</h3>
        <el-select v-model="paperFilter" placeholder="选择试卷" clearable style="width: 280px" @change="loadData">
          <el-option v-for="p in allPapers" :key="p.id" :label="p.title" :value="p.id" />
        </el-select>
      </div>

      <!-- 数据卡片 -->
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-mini" style="background: linear-gradient(135deg, #ececec, #d8d8d8)">
            <div class="stat-mini-label">参考人数</div>
            <div class="stat-mini-value">{{ stats.totalCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini" style="background: linear-gradient(135deg, #e8ece9, #d0d8d4)">
            <div class="stat-mini-label">及格率</div>
            <div class="stat-mini-value">{{ stats.passRate || 0 }}%</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini" style="background: linear-gradient(135deg, #eaeced, #d6d8dc)">
            <div class="stat-mini-label">平均分</div>
            <div class="stat-mini-value">{{ stats.avgScore || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini" style="background: linear-gradient(135deg, #eeecec, #dcd8d8)">
            <div class="stat-mini-label">最高/最低</div>
            <div class="stat-mini-value">{{ stats.maxScore || 0 }} / {{ stats.minScore || 0 }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <div class="card-box">
          <h4 class="section-title">成绩分布</h4>
          <div ref="distributionChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="card-box">
          <h4 class="section-title">及格率统计</h4>
          <div ref="passRateChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 排行榜 -->
    <div class="card-box">
      <h4 class="section-title">成绩排行榜 TOP 10</h4>
      <el-table :data="rankList" border stripe v-loading="rankLoading">
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <div class="rank-badge" :class="'rank-' + ($index + 1)" v-if="$index < 3">{{ $index + 1 }}</div>
            <span v-else>{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="学生" width="150">
          <template #default="{ row }">{{ row.name }} ({{ row.username }})</template>
        </el-table-column>
        <el-table-column prop="paperTitle" label="试卷" min-width="200" show-overflow-tooltip />
        <el-table-column label="成绩" width="160" align="center">
          <template #default="{ row }">
            <span class="rank-score">{{ row.score }}</span> / {{ row.totalScore }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.passed ? 'success' : 'danger'">{{ row.passed ? '及格' : '不及格' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" align="center" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStats } from '../api/stats'
import { getRanking } from '../api/score'
import { getPaperPage } from '../api/paper'

const paperFilter = ref(null)
const allPapers = ref([])
const stats = ref({})
const rankList = ref([])
const rankLoading = ref(false)

const distributionChartRef = ref(null)
const passRateChartRef = ref(null)
let distributionChart = null
let passRateChart = null

function initCharts() {
  if (distributionChartRef.value) {
    distributionChart = echarts.init(distributionChartRef.value)
  }
  if (passRateChartRef.value) {
    passRateChart = echarts.init(passRateChartRef.value)
  }
}

function renderDistribution() {
  if (!distributionChart) return
  const data = stats.value.distribution || []
  const option = {
    tooltip: { trigger: 'axis', formatter: '{b}: {c} 人' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.range),
      axisLine: { lineStyle: { color: '#ccc' } },
      axisLabel: { color: '#888' }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      minInterval: 1,
      axisLine: { show: false },
      axisLabel: { color: '#aaa' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.count),
      barWidth: '45%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: function (params) {
          // 高级灰白渐变：从浅到深
          const colors = ['#d0d0d0', '#bdbdbd', '#a8a8a8', '#949494', '#2c2c2c']
          return colors[params.dataIndex] || '#a8a8a8'
        }
      },
      label: { show: true, position: 'top', color: '#888', fontWeight: 600 }
    }]
  }
  distributionChart.setOption(option)
}

function renderPassRate() {
  if (!passRateChart) return
  const totalCount = stats.value.totalCount || 0
  const passRate = stats.value.passRate || 0
  const passCount = Math.round(totalCount * passRate / 100)
  const failCount = totalCount - passCount
  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { bottom: 10, left: 'center', textStyle: { color: '#888' } },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{d}%', fontSize: 16, fontWeight: 'bold', color: '#555' },
      data: [
        { value: passCount, name: '及格', itemStyle: { color: '#555' } },
        { value: failCount, name: '不及格', itemStyle: { color: '#d5d5d5' } }
      ]
    }]
  }
  passRateChart.setOption(option)
}

async function loadData() {
  const res = await getStats({ paperId: paperFilter.value })
  stats.value = res.data
  await nextTick()
  renderDistribution()
  renderPassRate()
  loadRank()
}

async function loadRank() {
  rankLoading.value = true
  try {
    let res
    if (paperFilter.value) {
      res = await getRanking({ paperId: paperFilter.value, pageNum: 1, pageSize: 10 })
    } else {
      // 全部成绩按分数降序（用 all 接口）
      const { getAllScores } = await import('../api/score')
      res = await getAllScores({ pageNum: 1, pageSize: 10 })
      // 手动按分数排序
      res.data.records.sort((a, b) => b.score - a.score)
    }
    rankList.value = res.data.records
  } finally {
    rankLoading.value = false
  }
}

async function loadPapers() {
  const res = await getPaperPage({ pageNum: 1, pageSize: 100 })
  allPapers.value = res.data.records
}

function handleResize() {
  distributionChart && distributionChart.resize()
  passRateChart && passRateChart.resize()
}

onMounted(async () => {
  await loadPapers()
  await nextTick()
  initCharts()
  await loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  distributionChart && distributionChart.dispose()
  passRateChart && passRateChart.dispose()
})
</script>

<style scoped>
.section-title {
  margin-bottom: 16px;
}
.stat-mini {
  border-radius: 12px;
  padding: 22px;
  color: #1a1a1a;
  text-align: center;
  border: 1px solid #e8e8e8;
}
.stat-mini-label {
  font-size: 13px;
  color: #777;
  margin-bottom: 8px;
}
.stat-mini-value {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
}
.chart-row {
  margin-top: 20px;
}
.chart-box {
  height: 320px;
}
.rank-badge {
  width: 28px;
  height: 28px;
  line-height: 28px;
  border-radius: 50%;
  color: #fff;
  font-weight: 700;
  margin: 0 auto;
}
.rank-1 { background: linear-gradient(135deg, #888, #555); }
.rank-2 { background: linear-gradient(135deg, #aaa, #888); }
.rank-3 { background: linear-gradient(135deg, #bbb, #999); }
.rank-score {
  font-size: 18px;
  font-weight: 700;
  color: #2c2c2c;
}
</style>
