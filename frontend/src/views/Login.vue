<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h2>TalentFlow ATS</h2>
      <p class="subtitle">互联网顶尖人才生态与组织发展引擎</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password size="large" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>
      <div class="tips">
        <p><strong>测试账号：</strong></p>
        <p>求职者：candidate / candidate123</p>
        <p>招聘HR：admin / admin123</p>
        <p>面试官：interviewer / interview123</p>
        <p>管理层：executive / exec123</p>
        <p>部门：dept_hr / dept123 · dept_tech / dept123</p>
        <el-button link type="primary" @click="$router.push('/public')">游客浏览已发布岗位 →</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { setAuth, getHomeRoute } from '../utils/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await login(form)
    setAuth(data.token, data)
    ElMessage.success(`欢迎，${data.displayName}`)
    router.push(getHomeRoute(data.role))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 12px 8px 20px;
}

h2 {
  text-align: center;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 28px;
  font-size: 14px;
}

.tips {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}
</style>
