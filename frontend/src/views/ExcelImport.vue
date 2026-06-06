<template>
  <div class="page-card">
    <h2 class="title">Excel 批量导入</h2>
    <el-alert
      title="模板说明：Excel 第一行必须包含「岗位名称」「岗位描述」两列，从第二行开始填写数据。"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />

    <el-upload
      ref="uploadRef"
      drag
      :auto-upload="false"
      :limit="1"
      accept=".xlsx,.xls"
      :on-change="handleFileChange"
      :on-exceed="handleExceed"
    >
      <div class="upload-icon">📄</div>
      <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip">仅支持 .xlsx / .xls 格式，文件大小不超过 10MB</div>
      </template>
    </el-upload>

    <div class="actions">
      <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="handleUpload">开始导入</el-button>
      <el-button @click="downloadTemplateFile">下载模板</el-button>
    </div>

    <el-result v-if="result" icon="success" title="导入完成" style="margin-top: 24px">
      <template #sub-title>
        共 {{ result.total }} 条，成功 {{ result.successCount }} 条，失败 {{ result.failCount }} 条
      </template>
      <template #extra>
        <el-alert
          v-if="result.errors?.length"
          type="warning"
          :closable="false"
          :title="'失败详情：' + result.errors.join('；')"
          style="margin-bottom: 16px"
        />
        <el-button type="primary" @click="$router.push('/positions')">查看岗位列表</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { importPositions, downloadTemplate } from '../api/position'

const uploadRef = ref()
const selectedFile = ref(null)
const uploading = ref(false)
const result = ref(null)

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  result.value = null
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先移除当前文件')
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  try {
    result.value = await importPositions(selectedFile.value)
    ElMessage.success('导入完成')
  } finally {
    uploading.value = false
  }
}

const downloadTemplateFile = async () => {
  const blob = await downloadTemplate()
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '岗位导入模板.xlsx'
  link.click()
  URL.revokeObjectURL(link.href)
}
</script>

<style scoped>
.title {
  margin-bottom: 20px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
</style>
