<template>
  <div class="page-card">
    <h2 class="title">互联网热招岗位</h2>
    <p class="subtitle">AI 智能匹配 · 一键投递 · 进度全透明</p>
    <div class="top-actions">
      <el-button v-if="isCandidate" type="success" @click="$router.push('/candidate/resume-analysis')">AI 简历分析 · 智能匹配岗位</el-button>
      <el-button v-if="!isLoggedIn" type="primary" link @click="$router.push('/login')">登录投递 →</el-button>
    </div>

    <el-form :inline="true" :model="query" style="margin-bottom: 20px">
      <el-form-item label="岗位名称">
        <el-input v-model="query.title" placeholder="搜索岗位" clearable @keyup.enter="loadData" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!loading && list.length === 0" description="暂无已发布岗位" />

    <el-row :gutter="20">
      <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom: 20px">
        <el-card shadow="hover" class="job-card">
          <h3>{{ item.title }}</h3>
          <p class="desc">{{ item.description }}</p>
          <div class="meta">{{ item.department }} · {{ formatTime(item.publishedAt || item.updatedAt) }}</div>
          <el-button type="primary" @click="handleApply(item)">一键投递</el-button>
          <el-button link @click="$router.push(`/public/${item.id}`)">查看详情</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublishedPositions } from '../api/position'
import { authState } from '../utils/auth'

const router = useRouter()
const isLoggedIn = computed(() => !!authState.token)
const isCandidate = computed(() => authState.user?.role === 'CANDIDATE')
const loading = ref(false)
const list = ref([])
const query = reactive({ title: '' })

const formatTime = (time) => (time ? time.replace('T', ' ').slice(0, 19) : '-')

const handleApply = (item) => {
  if (!isLoggedIn.value) {
    ElMessage.info('请先以求职者身份登录')
    router.push('/login')
    return
  }
  if (!isCandidate.value) {
    ElMessage.warning('请使用求职者账号登录后投递')
    return
  }
  router.push(`/candidate/apply/${item.id}`)
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (query.title) params.title = query.title
    list.value = await getPublishedPositions(params)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.title { margin-bottom: 8px; }
.subtitle { color: #909399; margin-bottom: 12px; }
.top-actions { display: flex; gap: 12px; align-items: center; margin-bottom: 20px; flex-wrap: wrap; }
.job-card h3 { margin-bottom: 12px; }
.desc {
  color: #606266; line-height: 1.6; height: 72px; overflow: hidden;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; margin-bottom: 12px;
}
.meta { color: #909399; font-size: 13px; margin-bottom: 12px; }
</style>
