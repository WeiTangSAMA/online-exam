<template>
  <div class="page-container">
    <div class="card-box">
      <div class="table-toolbar">
        <div>
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 130px" @change="loadData">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </div>
        <el-button type="primary" :icon="Plus" @click="openPaperDialog()">新增试卷</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="title" label="试卷标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="passScore" label="及格分" width="90" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Setting" @click="openComposeDialog(row)">组卷</el-button>
            <el-button type="warning" link :icon="Edit" @click="openPaperDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link :icon="Promotion" @click="handlePublish(row)">发布</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑试卷 -->
    <el-dialog v-model="paperDialogVisible" :title="editingId ? '编辑试卷' : '新增试卷'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="试卷标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入试卷标题" />
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="300" /> 分钟
        </el-form-item>
        <el-form-item label="及格分数" prop="passScore">
          <el-input-number v-model="form.passScore" :min="0" :max="200" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paperDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handlePaperSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 组卷对话框 -->
    <el-dialog v-model="composeDialogVisible" title="试卷组卷" width="850px" top="5vh">
      <div class="compose-header">
        <h3>{{ currentPaper?.title }}</h3>
        <div class="compose-summary">
          <el-tag type="primary">已选 {{ selectedIds.length }} 题</el-tag>
          <el-tag type="success">合计 {{ totalSelectedScore }} 分</el-tag>
        </div>
      </div>

      <div class="compose-toolbar">
        <el-select v-model="questionQuery.type" placeholder="题型" clearable style="width: 110px" @change="loadQuestions">
          <el-option label="单选题" value="SINGLE" />
          <el-option label="多选题" value="MULTIPLE" />
          <el-option label="判断题" value="JUDGE" />
        </el-select>
        <el-select v-model="questionQuery.categoryId" placeholder="分类" clearable filterable style="width: 140px" @change="loadQuestions">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="questionQuery.keyword" placeholder="搜索题目" clearable style="width: 200px" @keyup.enter="loadQuestions" />
        <el-button type="primary" :icon="Search" @click="loadQuestions">查询</el-button>
      </div>

      <el-table :data="questionList" border height="400px" @selection-change="handleSelectionChange"
                :row-key="(row) => row.id" ref="composeTableRef">
        <el-table-column type="selection" width="45" reserve-selection />
        <el-table-column label="题型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        <el-table-column prop="score" label="分值" width="70" align="center" />
      </el-table>

      <template #footer>
        <el-button @click="composeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleComposeSubmit">保存组卷</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Edit, Delete, Setting, Search, Promotion } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPaperPage, savePaper, updatePaper, deletePaper, publishPaper, bindQuestions } from '../api/paper'
import { getQuestionPage, getCategoryList } from '../api/question'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, status: null })

// 试卷表单
const paperDialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const form = reactive({
  title: '', duration: 60, passScore: 60, status: 0
})
const rules = {
  title: [{ required: true, message: '请输入试卷标题', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'blur' }]
}

// 组卷
const composeDialogVisible = ref(false)
const currentPaper = ref(null)
const questionList = ref([])
const selectedRows = ref([])
const selectedIds = ref([])
const composeTableRef = ref()
const questionQuery = reactive({ type: '', categoryId: null, keyword: '', pageNum: 1, pageSize: 200 })

const totalSelectedScore = computed(() => {
  return selectedRows.value.reduce((sum, r) => sum + parseInt(r.score), 0)
})

function typeText(type) {
  return { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题' }[type] || ''
}
function typeTag(type) {
  return { SINGLE: 'primary', MULTIPLE: 'success', JUDGE: 'warning' }[type] || ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await getPaperPage(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  const res = await getCategoryList()
  categories.value = res.data
}

function openPaperDialog(row) {
  editingId.value = row?.id || null
  if (row) {
    Object.assign(form, {
      title: row.title, duration: row.duration, passScore: row.passScore, status: row.status
    })
  } else {
    Object.assign(form, { title: '', duration: 60, passScore: 60, status: 0 })
  }
  paperDialogVisible.value = true
}

async function handlePaperSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (editingId.value) {
        form.id = editingId.value
        await updatePaper(form)
        ElMessage.success('修改成功')
      } else {
        await savePaper(form)
        ElMessage.success('新增成功')
      }
      paperDialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

function handlePublish(row) {
  ElMessageBox.confirm(`确定发布试卷「${row.title}」吗？发布后学生即可参加考试。`, '提示', {
    type: 'warning'
  }).then(async () => {
    await publishPaper(row.id)
    ElMessage.success('发布成功')
    loadData()
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除试卷「${row.title}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deletePaper(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

// ===== 组卷 =====
async function openComposeDialog(row) {
  currentPaper.value = row
  composeDialogVisible.value = true
  // 重置查询
  Object.assign(questionQuery, { type: '', categoryId: null, keyword: '', pageNum: 1, pageSize: 200 })
  selectedIds.value = []
  selectedRows.value = []
  await loadQuestions()
  // 加载该试卷已绑定的题目并默认勾选
  await loadBoundQuestions(row.id)
}

async function loadQuestions() {
  const res = await getQuestionPage(questionQuery)
  questionList.value = res.data.records
  // 重新勾选已选中的
  setTimeout(() => {
    questionList.value.forEach(q => {
      if (selectedIds.value.includes(q.id)) {
        composeTableRef.value?.toggleRowSelection(q, true)
      }
    })
  }, 50)
}

async function loadBoundQuestions(paperId) {
  // 通过获取考试试卷来获得已绑定的题目
  const { getExamPaper } = await import('../api/exam')
  try {
    const res = await getExamPaper(paperId)
    const boundIds = (res.data.questions || []).map(q => q.id)
    selectedIds.value = boundIds
    // 勾选表格
    setTimeout(() => {
      questionList.value.forEach(q => {
        if (boundIds.includes(q.id)) {
          composeTableRef.value?.toggleRowSelection(q, true)
        }
      })
    }, 100)
  } catch {
    // 忽略
  }
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
  selectedIds.value = rows.map(r => r.id)
}

async function handleComposeSubmit() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一道题目')
    return
  }
  submitting.value = true
  try {
    await bindQuestions(currentPaper.value.id, selectedIds.value)
    ElMessage.success('组卷成功')
    composeDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.compose-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.compose-summary {
  display: flex;
  gap: 10px;
}
.compose-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
</style>
