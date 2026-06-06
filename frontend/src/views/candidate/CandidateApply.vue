<template>
  <div class="page-card">
    <h2>一键智能投递</h2>
    <el-alert title="上传简历 PDF/TXT，AI 自动解析并填写，99% 字段免填" type="success" :closable="false" show-icon style="margin-bottom:20px" />

    <el-form label-width="100px" style="max-width:640px">
      <el-form-item label="投递渠道">
        <el-select v-model="channel" style="width:200px">
          <el-option label="官网" value="OFFICIAL" />
          <el-option label="Boss直聘" value="BOSS" />
          <el-option label="猎聘" value="LIEPIN" />
          <el-option label="LinkedIn" value="LINKEDIN" />
          <el-option label="内推" value="REFERRAL" />
        </el-select>
      </el-form-item>
      <el-form-item label="上传简历">
        <el-upload drag :auto-upload="false" :limit="1" accept=".txt,.pdf,.doc,.docx" :on-change="handleFile">
          <div class="upload-icon">📄</div>
          <div>拖拽简历到此处，或点击上传</div>
        </el-upload>
      </el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="技能标签"><el-input v-model="form.skills" type="textarea" :rows="2" /></el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleApply">确认投递</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applyJob, parseResume, applyWithResume } from '../../api/candidate'

const route = useRoute()
const router = useRouter()
const channel = ref('OFFICIAL')
const submitting = ref(false)
const resumeFile = ref(null)
const form = reactive({ name: '', email: '', phone: '', skills: '', resumeText: '' })

const handleFile = async (file) => {
  resumeFile.value = file.raw
  try {
    const parsed = await parseResume(file.raw)
    form.name = parsed.name
    form.email = parsed.email
    form.phone = parsed.phone
    form.skills = parsed.skills
    form.resumeText = parsed.rawText
    ElMessage.success('简历解析成功，已自动填表')
  } catch {
    ElMessage.warning('解析失败，请手动填写')
  }
}

const handleApply = async () => {
  submitting.value = true
  try {
    if (resumeFile.value) {
      await applyWithResume(route.params.id, resumeFile.value, channel.value)
    } else {
      await applyJob({
        positionId: Number(route.params.id),
        name: form.name, email: form.email, phone: form.phone,
        resumeText: form.resumeText || form.skills,
        channel: channel.value
      })
    }
    ElMessage.success('投递成功！可在「我的投递」追踪进度')
    router.push('/candidate/applications')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.upload-icon { font-size: 40px; margin-bottom: 8px; }
</style>
