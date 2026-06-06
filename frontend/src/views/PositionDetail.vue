<template>
  <div class="page-card" v-loading="loading">
    <div class="header-row">
      <h2>{{ detail.title }}</h2>
      <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
    </div>

    <el-descriptions :column="2" border style="margin-bottom: 24px">
      <el-descriptions-item label="提交部门">{{ detail.department || '-' }}</el-descriptions-item>
      <el-descriptions-item label="提交人">{{ detail.createdByName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ formatTime(detail.updatedAt) }}</el-descriptions-item>
      <el-descriptions-item label="审批人">{{ detail.approver || '-' }}</el-descriptions-item>
      <el-descriptions-item label="发布时间">{{ formatTime(detail.publishedAt) }}</el-descriptions-item>
      <el-descriptions-item label="审批意见" :span="2">{{ detail.approvalComment || '-' }}</el-descriptions-item>
    </el-descriptions>

    <h3>岗位描述</h3>
    <div class="description">{{ detail.description }}</div>

    <div class="actions">
      <el-button @click="$router.push('/positions')">返回列表</el-button>
      <el-button type="primary" v-if="canEdit" @click="$router.push(`/positions/${detail.id}/edit`)">编辑</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getPosition } from '../api/position'
import { isDepartment as checkDepartment } from '../utils/auth'

const isDepartment = computed(() => checkDepartment())
const canEdit = computed(() => isDepartment.value && detail.status === 'DRAFT')

const route = useRoute()
const loading = ref(false)
const detail = reactive({
  id: null,
  title: '',
  description: '',
  status: '',
  createdAt: '',
  updatedAt: '',
  approver: '',
  approvalComment: '',
  publishedAt: ''
})

const statusLabel = (status) => {
  const map = { DRAFT: '草稿', PENDING: '待审批', PUBLISHED: '已发布', CLOSED: '已关闭' }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = { DRAFT: 'info', PENDING: 'warning', PUBLISHED: 'success', CLOSED: 'danger' }
  return map[status] || ''
}

const formatTime = (time) => (time ? time.replace('T', ' ').slice(0, 19) : '-')

const loadDetail = async () => {
  loading.value = true
  try {
    const data = await getPosition(route.params.id)
    Object.assign(detail, data)
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.description {
  margin: 12px 0 24px;
  padding: 16px;
  background: #f8f9fb;
  border-radius: 6px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.actions {
  margin-top: 24px;
}
</style>
