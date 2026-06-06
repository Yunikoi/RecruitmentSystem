<template>
  <div class="page-card">
    <h2 class="title">{{ isEdit ? '编辑岗位' : '新增岗位' }}</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 720px">
      <el-form-item label="岗位名称" prop="title">
        <el-input v-model="form.title" placeholder="如：Java 后端开发实习生" />
      </el-form-item>
      <el-form-item label="岗位描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="8" placeholder="岗位职责：..." />
      </el-form-item>
      <el-form-item label="JD Copilot">
        <el-input v-model="copilotBrief" placeholder="如：3年经验、懂 Web3 和 Java 的后端" />
        <el-button style="margin-top:8px" :loading="copilotLoading" @click="runCopilot">AI 一键生成 JD</el-button>
      </el-form-item>
      <el-form-item label="岗位类型">
        <el-select v-model="form.positionType" style="width:100%">
          <el-option label="通用" value="GENERAL" />
          <el-option label="技术岗" value="TECH" />
          <el-option label="高级岗" value="SENIOR" />
        </el-select>
      </el-form-item>
      <el-form-item label="技能标签"><el-input v-model="form.skillTags" placeholder="Java,Spring,Vue" /></el-form-item>
      <el-alert
        title="保存后为草稿状态，需在列表中点击「提交审批」后由管理员审核发布"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPosition, getPosition, updatePosition, generateJd } from '../api/position'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = reactive({ title: '', description: '', positionType: 'GENERAL', skillTags: '' })
const copilotBrief = ref('')
const copilotLoading = ref(false)
const rules = {
  title: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入岗位描述', trigger: 'blur' }]
}

const loadDetail = async () => {
  const data = await getPosition(route.params.id)
  form.title = data.title
  form.description = data.description
  form.positionType = data.positionType || 'GENERAL'
  form.skillTags = data.skillTags || ''
}

const runCopilot = async () => {
  if (!copilotBrief.value.trim()) return ElMessage.warning('请先描述岗位需求')
  copilotLoading.value = true
  try {
    const res = await generateJd(copilotBrief.value.trim())
    form.title = res.title
    form.description = res.description
    form.skillTags = res.skillTags
    form.positionType = res.positionType || 'GENERAL'
    ElMessage.success('JD 已生成，可继续编辑')
  } finally { copilotLoading.value = false }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { title: form.title, description: form.description }
    if (isEdit.value) {
      await updatePosition(route.params.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createPosition(payload)
      ElMessage.success('创建成功，请提交审批')
    }
    router.push('/positions')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEdit.value) loadDetail()
})
</script>

<style scoped>
.title {
  margin-bottom: 24px;
  font-size: 20px;
}
</style>
