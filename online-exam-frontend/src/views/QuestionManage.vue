<template>
  <div class="page-container">
    <div class="card-box">
      <!-- 搜索栏 -->
      <div class="table-toolbar">
        <div class="search-area">
          <el-select v-model="queryParams.type" placeholder="题型" clearable style="width: 120px" @change="loadData">
            <el-option label="单选题" value="SINGLE" />
            <el-option label="多选题" value="MULTIPLE" />
            <el-option label="判断题" value="JUDGE" />
          </el-select>
          <el-select v-model="queryParams.categoryId" placeholder="分类" clearable filterable style="width: 150px" @change="loadData">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-input v-model="queryParams.keyword" placeholder="搜索题目内容" clearable style="width: 220px" @keyup.enter="loadData" @clear="loadData" />
          <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        </div>
        <div>
          <el-button type="success" :icon="Files" @click="categoryDialogVisible = true">分类管理</el-button>
          <el-button type="primary" :icon="Plus" @click="openDialog()">新增题目</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column label="题型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" effect="light">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" align="center" />
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新增/编辑题目对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题目' : '新增题目'" width="700px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="题型" prop="type">
          <el-radio-group v-model="form.type" @change="onTypeChange">
            <el-radio-button value="SINGLE">单选题</el-radio-button>
            <el-radio-button value="MULTIPLE">多选题</el-radio-button>
            <el-radio-button value="JUDGE">判断题</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" clearable filterable style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题干内容" />
        </el-form-item>

        <!-- 选项（单选/多选） -->
        <template v-if="form.type === 'SINGLE' || form.type === 'MULTIPLE'">
          <el-form-item label="选项设置">
            <div class="options-box">
              <div v-for="(opt, idx) in options" :key="idx" class="option-row">
                <span class="option-label">{{ String.fromCharCode(65 + idx) }}.</span>
                <el-input v-model="opt.text" :placeholder="'选项 ' + String.fromCharCode(65 + idx)" />
                <el-button type="danger" :icon="Delete" circle link @click="removeOption(idx)" v-if="options.length > 2" />
              </div>
              <el-button type="primary" link :icon="Plus" @click="addOption" v-if="options.length < 8">添加选项</el-button>
            </div>
          </el-form-item>
          <el-form-item :label="form.type === 'SINGLE' ? '正确答案' : '正确答案'" prop="answer">
            <el-select v-if="form.type === 'SINGLE'" v-model="form.answer" placeholder="选择正确选项" style="width: 200px">
              <el-option v-for="(opt, idx) in options" :key="idx" :label="String.fromCharCode(65 + idx)" :value="String.fromCharCode(65 + idx)" />
            </el-select>
            <el-checkbox-group v-else v-model="multiAnswer">
              <el-checkbox v-for="(opt, idx) in options" :key="idx" :value="String.fromCharCode(65 + idx)">
                {{ String.fromCharCode(65 + idx) }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </template>

        <!-- 判断题 -->
        <template v-if="form.type === 'JUDGE'">
          <el-form-item label="正确答案" prop="answer">
            <el-radio-group v-model="form.answer">
              <el-radio-button value="T">正确</el-radio-button>
              <el-radio-button value="F">错误</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </template>

        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="答案解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分类管理对话框 -->
    <el-dialog v-model="categoryDialogVisible" title="分类管理" width="450px">
      <div class="category-add">
        <el-input v-model="newCategory" placeholder="输入分类名称" @keyup.enter="handleAddCategory" />
        <el-button type="primary" :icon="Plus" @click="handleAddCategory">添加</el-button>
      </div>
      <el-table :data="categories" border style="margin-top: 12px">
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button type="danger" link :icon="Delete" @click="handleDeleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Search, Plus, Edit, Delete, Files } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getQuestionPage, addQuestion, updateQuestion, deleteQuestion,
  getCategoryList, addCategory, deleteCategory
} from '../api/question'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  type: '',
  categoryId: null,
  keyword: ''
})

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const options = ref([{ text: '' }, { text: '' }, { text: '' }, { text: '' }])
const multiAnswer = ref([])

const form = reactive({
  type: 'SINGLE',
  categoryId: null,
  content: '',
  options: '',
  answer: '',
  score: 5,
  analysis: ''
})

const rules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请设置正确答案', trigger: 'change' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }]
}

const categoryDialogVisible = ref(false)
const newCategory = ref('')

// 多选答案变化时同步到 form.answer
watch(multiAnswer, (val) => {
  if (form.type === 'MULTIPLE') {
    form.answer = val.slice().sort().join(',')
  }
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
    const res = await getQuestionPage(queryParams)
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

function addOption() {
  options.value.push({ text: '' })
}
function removeOption(idx) {
  options.value.splice(idx, 1)
}

function onTypeChange() {
  form.answer = ''
  multiAnswer.value = []
  if (form.type === 'JUDGE') {
    options.value = []
  } else if (options.value.length === 0) {
    options.value = [{ text: '' }, { text: '' }, { text: '' }, { text: '' }]
  }
}

function openDialog(row) {
  editingId.value = row?.id || null
  if (row) {
    // 编辑：回填
    form.type = row.type
    form.categoryId = row.categoryId
    form.content = row.content
    form.score = parseInt(row.score)
    form.analysis = row.analysis
    form.answer = row.answer

    if (row.type === 'SINGLE' || row.type === 'MULTIPLE') {
      try {
        const optArr = JSON.parse(row.options || '[]')
        options.value = optArr.map(o => ({ text: o.replace(/^[A-H]\.\s*/, '') }))
      } catch {
        options.value = [{ text: '' }, { text: '' }, { text: '' }, { text: '' }]
      }
      if (row.type === 'MULTIPLE') {
        multiAnswer.value = row.answer.split(',')
      }
    }
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(form, {
    type: 'SINGLE', categoryId: null, content: '', options: '', answer: '', score: 5, analysis: ''
  })
  options.value = [{ text: '' }, { text: '' }, { text: '' }, { text: '' }]
  multiAnswer.value = []
}

async function handleSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 校验选项
    if (form.type === 'SINGLE' || form.type === 'MULTIPLE') {
      const validOpts = options.value.filter(o => o.text.trim())
      if (validOpts.length < 2) {
        ElMessage.warning('请至少填写2个选项')
        return
      }
      form.options = JSON.stringify(validOpts.map((o, i) => String.fromCharCode(65 + i) + '. ' + o.text.trim()))
      // 重新计算多选答案
      if (form.type === 'MULTIPLE') {
        form.answer = multiAnswer.value.slice().sort().join(',')
        if (!form.answer) {
          ElMessage.warning('请选择正确答案')
          return
        }
      }
    } else {
      form.options = null
    }

    submitting.value = true
    try {
      if (editingId.value) {
        form.id = editingId.value
        await updateQuestion(form)
        ElMessage.success('修改成功')
      } else {
        await addQuestion(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除该题目吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteQuestion(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

async function handleAddCategory() {
  if (!newCategory.value.trim()) return
  await addCategory(newCategory.value.trim())
  ElMessage.success('添加成功')
  newCategory.value = ''
  loadCategories()
}

function handleDeleteCategory(row) {
  ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadCategories()
  }).catch(() => {})
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.search-area {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.options-box {
  width: 100%;
}
.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.option-label {
  width: 24px;
  font-weight: 600;
  color: #2c2c2c;
}
.category-add {
  display: flex;
  gap: 10px;
}
</style>
