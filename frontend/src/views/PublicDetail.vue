<template>
  <div class="page-card" v-loading="loading">
    <el-button link @click="$router.push('/public')">← 返回岗位列表</el-button>
    <div class="header-row">
      <h2>{{ detail.title }}</h2>
      <el-tag type="success">已发布</el-tag>
    </div>
    <el-descriptions :column="2" border style="margin-bottom: 24px">
      <el-descriptions-item label="招聘部门">{{ detail.department || '-' }}</el-descriptions-item>
      <el-descriptions-item label="发布时间">{{ formatTime(detail.publishedAt) }}</el-descriptions-item>
    </el-descriptions>
    <h3>岗位描述</h3>
    <div class="description">{{ detail.description }}</div>
    <el-button type="primary" size="large" style="margin-top:24px" @click="handleApply">一键智能投递</el-button>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicPosition } from '../api/position'
import { authState } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const isLoggedIn = computed(() => !!authState.token)
const isCandidate = computed(() => authState.user?.role === 'CANDIDATE')
const loading = ref(false)
const detail = reactive({
  title: '',
  description: '',
  department: '',
  publishedAt: ''
})

const formatTime = (time) => (time ? time.replace('T', ' ').slice(0, 19) : '-')

const handleApply = () => {
  if (!isLoggedIn.value) { ElMessage.info('请先登录'); router.push('/login'); return }
  if (!isCandidate.value) { ElMessage.warning('请使用求职者账号'); return }
  router.push(`/candidate/apply/${route.params.id}`)
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await getPublicPosition(route.params.id)
    Object.assign(detail, data)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0 20px;
}

.description {
  margin-top: 12px;
  padding: 16px;
  background: #f8f9fb;
  border-radius: 6px;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
