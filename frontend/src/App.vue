<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo" @click="goHome">TalentFlow ATS</div>
      <el-menu mode="horizontal" :default-active="activeMenu" router background-color="#1a1a2e" text-color="#fff" active-text-color="#00d4ff" class="nav-menu">
        <el-menu-item index="/public">公开岗位</el-menu-item>
        <template v-if="isLoggedIn">
          <!-- C端 -->
          <template v-if="isCandidate">
            <el-menu-item index="/candidate/applications">我的投递</el-menu-item>
          </template>
          <!-- B端 HR -->
          <template v-if="isAdmin || isInterviewer">
            <el-menu-item index="/recruiter/pipeline">招聘漏斗</el-menu-item>
            <el-menu-item index="/positions">岗位管理</el-menu-item>
          </template>
          <!-- 部门 -->
          <template v-if="isDepartment">
            <el-menu-item index="/positions">我的岗位</el-menu-item>
            <el-menu-item index="/import">Excel导入</el-menu-item>
          </template>
          <!-- M端 / HR 驾驶舱 -->
          <template v-if="isExecutive || isAdmin">
            <el-menu-item index="/management/dashboard">{{ isAdmin ? 'HR驾驶舱' : '管理驾驶舱' }}</el-menu-item>
          </template>
        </template>
      </el-menu>
      <div class="user-area">
        <template v-if="isLoggedIn">
          <el-tag size="small" effect="dark">{{ user?.displayName }}（{{ roleLabel }}）</el-tag>
          <el-button size="small" @click="handleLogout">退出</el-button>
        </template>
        <el-button v-else type="warning" size="small" @click="$router.push('/login')">登录</el-button>
      </div>
    </el-header>
    <el-main class="main"><router-view /></el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authState, clearAuth, getRoleLabel, getHomeRoute } from './utils/auth'
import { logout } from './api/auth'

const route = useRoute()
const router = useRouter()
const user = computed(() => authState.user)
const isLoggedIn = computed(() => !!authState.token)
const isAdmin = computed(() => authState.user?.role === 'ADMIN')
const isDepartment = computed(() => authState.user?.role === 'DEPARTMENT')
const isCandidate = computed(() => authState.user?.role === 'CANDIDATE')
const isInterviewer = computed(() => authState.user?.role === 'INTERVIEWER')
const isExecutive = computed(() => authState.user?.role === 'EXECUTIVE')
const roleLabel = computed(() => getRoleLabel(authState.user?.role))

const activeMenu = computed(() => {
  if (route.path.startsWith('/candidate')) return '/candidate/applications'
  if (route.path.startsWith('/recruiter')) return '/recruiter/pipeline'
  if (route.path.startsWith('/management')) return '/management/dashboard'
  if (route.path.startsWith('/public')) return '/public'
  if (route.path.startsWith('/positions')) return '/positions'
  return route.path
})

const goHome = () => router.push(isLoggedIn.value ? getHomeRoute(authState.user?.role) : '/public')

const handleLogout = async () => {
  try { await logout() } catch { /* ignore */ }
  clearAuth()
  ElMessage.success('已退出')
  router.push('/public')
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif; background: #f0f2f5; }
.layout { min-height: 100vh; }
.header { display: flex; align-items: center; padding: 0 24px; background: #1a1a2e; color: #fff; }
.logo { font-size: 20px; font-weight: bold; margin-right: 24px; cursor: pointer; color: #00d4ff; }
.nav-menu { border-bottom: none !important; flex: 1; background: transparent !important; }
.user-area { display: flex; align-items: center; gap: 12px; margin-left: 16px; }
.main { padding: 24px; max-width: 1400px; margin: 0 auto; width: 100%; }
.page-card { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
</style>
