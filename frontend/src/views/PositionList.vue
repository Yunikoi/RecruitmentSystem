<template>
  <div class="page-card">
    <div class="toolbar">
      <el-form :inline="true" :model="query">
        <el-form-item label="岗位名称">
          <el-input v-model="query.title" placeholder="模糊搜索" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div v-if="isDepartment">
        <el-button type="primary" @click="$router.push('/positions/create')">新增岗位</el-button>
        <el-button @click="$router.push('/import')">Excel导入</el-button>
      </div>
    </div>

    <el-alert
      v-if="isAdmin"
      title="招聘管理员：可对待审批岗位进行通过/驳回，对已发布岗位进行关闭"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />
    <el-alert
      v-if="isDepartment"
      title="部门账号：创建岗位后保存为草稿，确认无误后提交审批，由管理员审核发布"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="岗位名称" min-width="160" />
      <el-table-column v-if="isAdmin" prop="department" label="提交部门" width="120" />
      <el-table-column v-if="isAdmin" prop="createdByName" label="提交人" width="110" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" :width="isAdmin ? 280 : 260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/positions/${row.id}`)">详情</el-button>
          <template v-if="isDepartment">
            <el-button link type="primary" :disabled="!canEdit(row)" @click="$router.push(`/positions/${row.id}/edit`)">编辑</el-button>
            <el-button link type="warning" v-if="row.status === 'DRAFT'" @click="handleSubmit(row)">提交审批</el-button>
            <el-button link type="info" v-if="row.status === 'PENDING'" @click="handleRemind(row)">催办HR</el-button>
            <el-button link type="danger" :disabled="!canDelete(row)" @click="handleDelete(row)">删除</el-button>
          </template>
          <template v-if="isAdmin">
            <el-button link type="success" v-if="row.status === 'PENDING'" @click="openApproval(row, 'approve')">通过</el-button>
            <el-button link type="danger" v-if="row.status === 'PENDING'" @click="openApproval(row, 'reject')">驳回</el-button>
            <el-button link type="warning" v-if="row.status === 'PUBLISHED'" @click="handleClose(row)">关闭</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="approvalVisible" :title="approvalType === 'approve' ? '审批通过' : '审批驳回'" width="480px">
      <el-form :model="approvalForm" label-width="90px">
        <el-form-item label="审批意见">
          <el-input v-model="approvalForm.approvalComment" type="textarea" :rows="4" placeholder="请输入审批意见（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApproval">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isAdmin as checkAdmin, isDepartment as checkDepartment } from '../utils/auth'
import {
  getPositions, deletePosition, submitPosition, approvePosition, rejectPosition, closePosition, remindApproval
} from '../api/position'

const loading = ref(false)
const tableData = ref([])
const query = reactive({ title: '', status: '' })
const isAdmin = computed(() => checkAdmin())
const isDepartment = computed(() => checkDepartment())

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待审批', value: 'PENDING' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已关闭', value: 'CLOSED' }
]

const approvalVisible = ref(false)
const approvalType = ref('approve')
const currentRow = ref(null)
const approvalForm = reactive({ approvalComment: '' })

const statusLabel = (s) => ({ DRAFT: '草稿', PENDING: '待审批', PUBLISHED: '已发布', CLOSED: '已关闭' }[s] || s)
const statusTagType = (s) => ({ DRAFT: 'info', PENDING: 'warning', PUBLISHED: 'success', CLOSED: 'danger' }[s] || '')
const formatTime = (t) => (t ? t.replace('T', ' ').slice(0, 19) : '-')
const canEdit = (row) => row.status === 'DRAFT'
const canDelete = (row) => row.status === 'DRAFT'

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (query.title) params.title = query.title
    if (query.status) params.status = query.status
    tableData.value = await getPositions(params)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.title = ''
  query.status = ''
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除岗位「${row.title}」吗？`, '提示', { type: 'warning' })
  await deletePosition(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async (row) => {
  await ElMessageBox.confirm(`确定提交「${row.title}」进入审批流程吗？`, '提示', { type: 'info' })
  await submitPosition(row.id)
  ElMessage.success('已提交审批，等待管理员审核')
  loadData()
}

const handleRemind = async (row) => {
  const msg = await remindApproval(row.id)
  ElMessage.success(msg)
}

const handleClose = async (row) => {
  await ElMessageBox.confirm(`确定关闭岗位「${row.title}」吗？`, '提示', { type: 'warning' })
  await closePosition(row.id)
  ElMessage.success('岗位已关闭')
  loadData()
}

const openApproval = (row, type) => {
  currentRow.value = row
  approvalType.value = type
  approvalForm.approvalComment = ''
  approvalVisible.value = true
}

const submitApproval = async () => {
  const data = { approvalComment: approvalForm.approvalComment }
  if (approvalType.value === 'approve') {
    await approvePosition(currentRow.value.id, data)
    ElMessage.success('审批通过，岗位已发布')
  } else {
    await rejectPosition(currentRow.value.id, data)
    ElMessage.success('已驳回，岗位退回草稿')
  }
  approvalVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
