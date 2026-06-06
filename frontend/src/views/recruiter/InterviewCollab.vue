<template>
  <div class="page-card">
    <h2>面试协同 · WebIDE</h2>
    <p class="tip">技术岗实时协同编程（支持录码回放演示）· 应用 #{{ applicationId }}</p>
    <el-input v-model="code" type="textarea" :rows="18" font-family="monospace" @input="save" />
    <div class="actions">
      <el-button type="primary" @click="loadSummary">AI 面试摘要</el-button>
      <el-button @click="save">保存代码</el-button>
    </div>
    <el-alert v-if="summary" :title="summary" type="info" :closable="false" style="margin-top:16px" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getCollabCode, saveCollabCode, getMeetingSummary } from '../../api/recruiter'

const route = useRoute()
const applicationId = route.params.id
const code = ref('')
const summary = ref('')

const load = async () => {
  const res = await getCollabCode(applicationId)
  code.value = res.code
}

const save = async () => {
  await saveCollabCode(applicationId, code.value)
}

const loadSummary = async () => {
  const res = await getMeetingSummary(applicationId)
  summary.value = res.summary
}

onMounted(load)
</script>

<style scoped>
.tip { color: #909399; margin-bottom: 16px; }
.actions { margin-top: 12px; display: flex; gap: 8px; }
</style>
