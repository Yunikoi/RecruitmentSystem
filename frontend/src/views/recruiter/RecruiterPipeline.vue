<template>
  <div class="page-card">
    <div class="toolbar">
      <h2>招聘漏斗 · AI 智能筛选</h2>
      <el-select v-model="filterStage" placeholder="按阶段筛选" clearable style="width:160px" @change="loadData">
        <el-option v-for="s in stages" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </div>

    <el-alert v-if="isAdmin" type="info" :closable="false" show-icon title="HR 功能入口：请展开下方「HR 高级工具」面板（工作流、人才复活、查重、盲筛、Offer、背调）；表格中可「协同编程」「发面试邀请」" style="margin-bottom:12px" />

    <el-collapse v-if="isAdmin" v-model="hrToolsOpen" style="margin-bottom:16px">
      <el-collapse-item name="hr-tools" title="HR 高级工具：工作流 / 人才复活 / 查重 / 合规 / 集成">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-button @click="openWorkflow">自定义工作流</el-button>
            <el-button @click="scanTalent">人才库 AI 匹配</el-button>
            <el-button @click="loadDuplicates">查重合并</el-button>
          </el-col>
          <el-col :span="8">
            <el-switch v-model="blindHiring" active-text="盲筛" @change="saveCompliance" />
            <el-switch v-model="blindReview" active-text="匿名面评" style="margin-left:12px" @change="saveCompliance" />
          </el-col>
          <el-col :span="8">
            <el-button type="success" @click="sendOffer">发 Offer (e-Sign)</el-button>
            <el-button type="warning" @click="startBgCheck">发起背调</el-button>
            <el-button link @click="$router.push('/management/dashboard')">审计日志→驾驶舱</el-button>
          </el-col>
        </el-row>
      </el-collapse-item>
    </el-collapse>

    <el-row :gutter="16" class="funnel-cards">
      <el-col :span="4" v-for="s in stages" :key="s.value">
        <el-card shadow="hover" class="funnel-card">
          <div class="funnel-count">{{ countByStage(s.value) }}</div>
          <div class="funnel-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="list" stripe border v-loading="loading" style="margin-top:20px">
      <el-table-column prop="candidateName" label="候选人" width="100" />
      <el-table-column prop="positionTitle" label="岗位" min-width="140" />
      <el-table-column prop="channelLabel" label="渠道" width="90" />
      <el-table-column prop="matchScore" label="AI匹配" width="90">
        <template #default="{ row }">
          <el-progress :percentage="row.matchScore || 0" :color="scoreColor(row.matchScore)" />
        </template>
      </el-table-column>
      <el-table-column prop="matchHighlights" label="亮点" min-width="160" show-overflow-tooltip />
      <el-table-column label="AI初试" min-width="140">
        <template #default="{ row }">
          <template v-if="row.aiInterviewScore != null">
            <el-tag :type="row.aiInterviewPass ? 'success' : 'danger'" size="small">
              {{ row.aiInterviewScore }} 分 · {{ row.aiInterviewPass ? '建议通过' : '不建议' }}
            </el-tag>
            <el-button link type="primary" @click="openAiFeedback(row)">查看面评</el-button>
          </template>
          <span v-else class="muted">未完成</span>
        </template>
      </el-table-column>
      <el-table-column prop="matchRisks" label="风险" min-width="120" show-overflow-tooltip />
      <el-table-column prop="stageLabel" label="阶段" width="100">
        <template #default="{ row }"><el-tag>{{ row.stageLabel }}</el-tag></template>
      </el-table-column>
      <el-table-column label="面试邀请" min-width="160">
        <template #default="{ row }">
          <template v-if="row.interviews?.length">
            <el-tag
              v-for="iv in row.interviews.slice(0, 2)"
              :key="iv.id"
              size="small"
              :type="inviteStatusType(iv.status)"
              style="margin:2px"
            >
              {{ iv.typeLabel }}·{{ iv.statusLabel }}
            </el-tag>
            <el-button link type="primary" @click="openInterviews(row)">详情</el-button>
          </template>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="380" fixed="right">
        <template #default="{ row }">
            <el-button link type="primary" @click="openResume(row)">查看简历</el-button>
            <el-button link type="info" v-if="isInterviewer || isAdmin" @click="$router.push(`/recruiter/collab/${row.id}`)">协同编程</el-button>
            <el-button link type="info" v-if="isInterviewer || isAdmin" @click="openEvaluation(row)">提交面评</el-button>
          <template v-if="isAdmin">
            <el-button link type="success" v-if="row.stage === 'SCREENING'" @click="advance(row, 'AI_INTERVIEW')">进AI面</el-button>
            <el-button link type="primary" v-if="row.stage === 'AI_INTERVIEW' && row.aiInterviewScore != null" @click="advance(row, 'BUSINESS_INTERVIEW')">进业务面</el-button>
            <el-button link type="warning" v-if="row.stage === 'BUSINESS_INTERVIEW'" @click="advance(row, 'HR_INTERVIEW')">进HR面</el-button>
            <el-button link type="success" v-if="row.stage === 'HR_INTERVIEW'" @click="advance(row, 'OFFER')">发Offer</el-button>
            <el-button link type="success" v-if="row.stage === 'OFFER'" @click="advance(row, 'HIRED')">确认录用</el-button>
            <el-button link type="danger" @click="reject(row)">淘汰</el-button>
            <el-button link @click="openSchedule(row)" :disabled="!canScheduleInterview(row)">安排面试</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="interviewVisible" title="面试邀请记录" width="640px">
      <p class="eval-target">候选人：{{ interviewRow?.candidateName }} · {{ interviewRow?.positionTitle }}</p>
      <el-empty v-if="!interviewRow?.interviews?.length" description="暂无面试邀请" />
      <div v-for="iv in interviewRow?.interviews || []" :key="iv.id" class="interview-record">
        <div class="record-head">
          <el-tag type="warning">{{ iv.typeLabel }}</el-tag>
          <el-tag :type="inviteStatusType(iv.status)" size="small">{{ iv.statusLabel }}</el-tag>
        </div>
        <p>时间：{{ formatTime(iv.scheduledAt) }} · 面试官：{{ iv.interviewerName }}</p>
        <p>地点：{{ iv.location }}</p>
        <p v-if="iv.responseNote" class="response-line">
          <strong>{{ responseNoteLabel(iv.status) }}：</strong>{{ iv.responseNote }}
        </p>
        <p v-if="iv.respondedAt" class="muted">响应于 {{ formatTime(iv.respondedAt) }}</p>
        <el-button
          v-if="isAdmin && ['PENDING', 'ACCEPTED', 'RESCHEDULE_REQUESTED'].includes(iv.status)"
          link
          type="danger"
          @click="handleCancelInterview(iv)"
        >
          取消邀请
        </el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="scheduleVisible" title="发送面试邀请" width="480px">
      <el-alert
        title="邀请发出后，求职者需在 72 小时内接受/拒绝/申请改期"
        type="info"
        :closable="false"
        style="margin-bottom:16px"
      />
      <el-form :model="scheduleForm" label-width="90px">
        <el-form-item label="面试类型">
          <el-select v-model="scheduleForm.type">
            <el-option label="AI初试" value="AI" />
            <el-option label="业务面试" value="BUSINESS" />
            <el-option label="HR面试" value="HR" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试官ID"><el-input v-model="scheduleForm.interviewerId" placeholder="如: 5" /></el-form-item>
        <el-form-item label="时间"><el-date-picker v-model="scheduleForm.scheduledAt" type="datetime" /></el-form-item>
        <el-form-item label="地点/链接"><el-input v-model="scheduleForm.location" placeholder="会议室/腾讯会议链接" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSchedule">发送邀请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="evalVisible" title="结构化面评" width="520px">
      <p class="eval-target">候选人：{{ evalRow?.candidateName }} · {{ evalRow?.positionTitle }}</p>
      <el-form :model="evalForm" label-width="100px">
        <el-form-item label="技术能力">
          <el-slider v-model="evalForm.technicalScore" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="沟通表达">
          <el-slider v-model="evalForm.communicationScore" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="文化匹配">
          <el-slider v-model="evalForm.cultureScore" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="优势"><el-input v-model="evalForm.strengths" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="待提升"><el-input v-model="evalForm.weaknesses" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="推荐意见"><el-input v-model="evalForm.recommendation" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="结论">
          <el-radio-group v-model="evalForm.result">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="HOLD">待定</el-radio>
            <el-radio value="FAIL">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalVisible = false">取消</el-button>
        <el-button type="primary" :loading="evalSubmitting" @click="submitEvaluation">提交面评</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resumeVisible" :title="`简历 · ${resumeRow?.candidateName || ''}`" width="820px" top="4vh" @closed="clearResumePreview">
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
        <el-descriptions-item label="姓名">{{ resumeRow?.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ resumeRow?.candidateEmail }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ resumeRow?.candidatePhone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="技能标签">{{ resumeRow?.parsedSkills || '—' }}</el-descriptions-item>
        <el-descriptions-item label="AI匹配" :span="2">
          <el-tag type="success">{{ resumeRow?.matchScore }}%</el-tag>
          {{ resumeRow?.matchHighlights }}
        </el-descriptions-item>
        <el-descriptions-item v-if="resumeRow?.aiInterviewScore != null" label="AI初试" :span="2">
          <el-tag :type="resumeRow.aiInterviewPass ? 'success' : 'danger'">
            {{ resumeRow.aiInterviewScore }} 分 · {{ resumeRow.aiInterviewPass ? '建议进入下一轮' : '不建议进入下一轮' }}
          </el-tag>
          <p class="ai-feedback-preview">{{ resumeRow.aiInterviewFeedback }}</p>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="resumeRow?.hasResumeFile" class="resume-actions">
        <el-button type="primary" :loading="resumeLoading" @click="loadOriginalFile">加载原文件</el-button>
        <el-button v-if="resumeBlobUrl" @click="downloadOriginal">下载原文件</el-button>
        <span class="file-name">{{ resumeRow?.resumeFileName }}</span>
      </div>
      <iframe v-if="resumeBlobUrl && isPdf" class="pdf-preview" :src="resumeBlobUrl" />

      <div class="resume-box">
        <div class="resume-box-title">解析文本</div>
        <pre v-if="resumeRow?.resumeText">{{ resumeRow.resumeText }}</pre>
        <el-empty v-else description="暂无简历文本" />
      </div>
    </el-dialog>

    <el-dialog v-model="aiFeedbackVisible" title="AI 初试面评" width="560px">
      <p class="eval-target">{{ aiFeedbackRow?.candidateName }} · {{ aiFeedbackRow?.positionTitle }}</p>
      <div v-if="aiFeedbackRow?.aiInterviewScore != null" class="ai-feedback-panel">
        <div class="ai-feedback-head">
          <el-tag :type="aiFeedbackRow.aiInterviewPass ? 'success' : 'danger'" size="large">
            得分 {{ aiFeedbackRow.aiInterviewScore }} · {{ aiFeedbackRow.aiInterviewPass ? '建议进入下一轮' : '不建议进入下一轮' }}
          </el-tag>
          <span v-if="aiFeedbackRow.aiInterviewAt" class="muted">
            {{ formatTime(aiFeedbackRow.aiInterviewAt) }}
          </span>
        </div>
        <pre class="ai-feedback-text">{{ aiFeedbackRow.aiInterviewFeedback }}</pre>
      </div>
      <el-empty v-else description="该候选人尚未完成 AI 初试" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authState } from '../../utils/auth'
import { getApplications, updateStage, scheduleInterview, getResumeFile, submitEvaluation as submitEvaluationApi, cancelInterview, getWorkflow, updateWorkflow, matchTalentPool, activateTalent, getDuplicates, mergeDuplicates, createOffer, startBackgroundCheck, getBackgroundCheck } from '../../api/recruiter'
import { getComplianceSettings, updateComplianceSettings } from '../../api/compliance'

const isAdmin = computed(() => authState.user?.role === 'ADMIN')
const isInterviewer = computed(() => authState.user?.role === 'INTERVIEWER')

const loading = ref(false)
const list = ref([])
const allList = ref([])
const filterStage = ref('')
const scheduleVisible = ref(false)
const interviewVisible = ref(false)
const interviewRow = ref(null)
const evalVisible = ref(false)
const evalSubmitting = ref(false)
const evalRow = ref(null)
const evalForm = reactive({
  technicalScore: 80,
  communicationScore: 80,
  cultureScore: 80,
  strengths: '',
  weaknesses: '',
  recommendation: '',
  result: 'PASS'
})
const resumeVisible = ref(false)
const resumeRow = ref(null)
const resumeLoading = ref(false)
const resumeBlobUrl = ref('')
const resumeBlob = ref(null)
const isPdf = ref(false)
const aiFeedbackVisible = ref(false)
const aiFeedbackRow = ref(null)
const currentRow = ref(null)
const scheduleForm = reactive({ type: 'BUSINESS', interviewerId: '5', scheduledAt: '', location: '' })
const blindHiring = ref(false)
const blindReview = ref(false)
const hrToolsOpen = ref(['hr-tools'])
const hrTargetRow = ref(null)

const stages = [
  { value: 'APPLIED', label: '已投递' }, { value: 'SCREENING', label: '初筛' },
  { value: 'AI_INTERVIEW', label: 'AI面' }, { value: 'BUSINESS_INTERVIEW', label: '业务面' },
  { value: 'HR_INTERVIEW', label: 'HR面' }, { value: 'OFFER', label: 'Offer' },
  { value: 'HIRED', label: '已录用' }, { value: 'REJECTED', label: '淘汰' }
]

const scoreColor = (s) => s >= 80 ? '#67c23a' : s >= 60 ? '#e6a23c' : '#f56c6c'
const countByStage = (stage) => allList.value.filter(a => a.stage === stage).length
const formatTime = (t) => t ? t.replace('T', ' ').slice(0, 19) : '-'

const inviteStatusType = (status) => ({
  PENDING: 'warning',
  ACCEPTED: 'success',
  DECLINED: 'danger',
  RESCHEDULE_REQUESTED: 'info',
  CANCELLED: 'info',
  COMPLETED: ''
}[status] || '')

const responseNoteLabel = (status) => ({
  DECLINED: '拒绝原因',
  RESCHEDULE_REQUESTED: '改期说明',
  ACCEPTED: '备注'
}[status] || '说明')

const loadData = async () => {
  loading.value = true
  try {
    const params = filterStage.value ? { stage: filterStage.value } : {}
    allList.value = await getApplications(params)
    list.value = allList.value
  } finally { loading.value = false }
}

const advance = async (row, stage) => {
  if (stage === 'BUSINESS_INTERVIEW' && row.aiInterviewScore == null) {
    return ElMessage.warning('须先完成 AI 初试后再进入业务面')
  }
  await updateStage(row.id, { stage })
  ElMessage.success('阶段已更新')
  loadData()
}

/** 工作流含 AI 初试时，未完成 AI 不可安排人工面试 */
const canScheduleInterview = (row) => {
  if (['REJECTED', 'HIRED', 'APPLIED'].includes(row.stage)) return false
  if (row.aiInterviewScore != null) return true
  return ['BUSINESS_INTERVIEW', 'HR_INTERVIEW', 'OFFER'].includes(row.stage)
}

const reject = async (row) => {
  await ElMessageBox.confirm(`确定淘汰「${row.candidateName}」？AI将自动生成反馈信`, '提示', { type: 'warning' })
  await updateStage(row.id, { stage: 'REJECTED' })
  ElMessage.success('已淘汰并加入人才库')
  loadData()
}

const openSchedule = (row) => { currentRow.value = row; scheduleVisible.value = true }

const openInterviews = (row) => {
  interviewRow.value = row
  interviewVisible.value = true
}

const handleCancelInterview = async (iv) => {
  await ElMessageBox.confirm('确定取消该面试邀请？', '提示', { type: 'warning' })
  await cancelInterview(iv.id)
  ElMessage.success('邀请已取消')
  interviewVisible.value = false
  loadData()
}

const openEvaluation = (row) => {
  evalRow.value = row
  evalForm.technicalScore = 80
  evalForm.communicationScore = 80
  evalForm.cultureScore = 80
  evalForm.strengths = row.matchHighlights || ''
  evalForm.weaknesses = row.matchRisks || ''
  evalForm.recommendation = '建议进入下一轮'
  evalForm.result = 'PASS'
  evalVisible.value = true
}

const submitEvaluation = async () => {
  evalSubmitting.value = true
  try {
    await submitEvaluationApi({
      applicationId: evalRow.value.id,
      technicalScore: evalForm.technicalScore,
      communicationScore: evalForm.communicationScore,
      cultureScore: evalForm.cultureScore,
      strengths: evalForm.strengths,
      weaknesses: evalForm.weaknesses,
      recommendation: evalForm.recommendation,
      result: evalForm.result
    })
    ElMessage.success('面评已提交')
    evalVisible.value = false
  } finally {
    evalSubmitting.value = false
  }
}

const openResume = async (row) => {
  resumeRow.value = row
  resumeVisible.value = true
  clearResumePreview()
  if (row.hasResumeFile) {
    await loadOriginalFile()
  }
}

const openAiFeedback = (row) => {
  aiFeedbackRow.value = row
  aiFeedbackVisible.value = true
}

const loadOriginalFile = async () => {
  if (!resumeRow.value?.id) return
  resumeLoading.value = true
  try {
    const blob = await getResumeFile(resumeRow.value.id)
    resumeBlob.value = blob
    resumeBlobUrl.value = URL.createObjectURL(blob)
    isPdf.value = (resumeRow.value.resumeContentType || blob.type || '').includes('pdf')
  } finally {
    resumeLoading.value = false
  }
}

const downloadOriginal = () => {
  if (!resumeBlobUrl.value) return
  const a = document.createElement('a')
  a.href = resumeBlobUrl.value
  a.download = resumeRow.value?.resumeFileName || 'resume.pdf'
  a.click()
}

const clearResumePreview = () => {
  if (resumeBlobUrl.value) URL.revokeObjectURL(resumeBlobUrl.value)
  resumeBlobUrl.value = ''
  resumeBlob.value = null
  isPdf.value = false
}

const submitSchedule = async () => {
  if (!canScheduleInterview(currentRow.value)) {
    return ElMessage.warning('须先完成 AI 初试后再安排人工面试')
  }
  if (!scheduleForm.scheduledAt) {
    ElMessage.warning('请选择面试时间')
    return
  }
  if (!scheduleForm.location?.trim()) {
    ElMessage.warning('请填写面试地点或会议链接')
    return
  }
  await scheduleInterview({
    applicationId: currentRow.value.id,
    interviewerId: Number(scheduleForm.interviewerId),
    type: scheduleForm.type,
    scheduledAt: scheduleForm.scheduledAt,
    location: scheduleForm.location
  })
  ElMessage.success('面试邀请已发送，等待求职者确认')
  scheduleVisible.value = false
  loadData()
}

onMounted(async () => {
  loadData()
  if (isAdmin.value) {
    try {
      const s = await getComplianceSettings()
      blindHiring.value = s.blindHiringEnabled
      blindReview.value = s.blindReviewEnabled
    } catch {}
  }
})

const saveCompliance = async () => {
  await updateComplianceSettings({ blindHiringEnabled: blindHiring.value, blindReviewEnabled: blindReview.value })
  ElMessage.success('合规配置已更新')
}

const openWorkflow = async () => {
  const row = list.value[0]
  if (!row) return ElMessage.warning('暂无候选人')
  const steps = await getWorkflow(row.positionId)
  const { value } = await ElMessageBox.prompt(`当前工作流：${steps.join(' → ')}`, '自定义工作流', {
    inputValue: steps.join(',')
  })
  if (value) {
    await updateWorkflow(row.positionId, value.split(',').map(s => s.trim()))
    ElMessage.success('工作流已更新')
  }
}

const scanTalent = async () => {
  const row = list.value[0]
  if (!row) return
  const matches = await matchTalentPool(row.positionId)
  if (!matches.length) return ElMessage.info('人才库暂无高匹配候选人')
  const top = matches[0]
  await ElMessageBox.confirm(`发现 ${matches.length} 名高匹配人才，最高：${top.candidateName} (${top.matchScore}%)，是否激活？`)
  await activateTalent(top.applicationId, row.positionId)
  ElMessage.success('激活邀请已发送')
}

const loadDuplicates = async () => {
  const groups = await getDuplicates()
  if (!groups.length) return ElMessage.info('未发现重复档案')
  const g = groups[0]
  await mergeDuplicates({ primaryId: g.applicationIds[0], mergeIds: g.applicationIds.slice(1) })
  ElMessage.success('已合并重复档案')
  loadData()
}

const sendOffer = async () => {
  const row = list.value.find(r => r.stage === 'HR_INTERVIEW') || list.value[0]
  if (!row) return
  await createOffer(row.id)
  ElMessage.success('Offer 已发起，等待候选人 e-Sign 签署')
  loadData()
}

const startBgCheck = async () => {
  const row = list.value[0]
  if (!row) return
  await startBackgroundCheck(row.id)
  const report = await getBackgroundCheck(row.id)
  ElMessageBox.alert(report.reportSummary?.slice(0, 300) || '背调进行中', '背调报告（脱敏）')
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.funnel-cards { margin-bottom: 8px; }
.funnel-card { text-align: center; }
.funnel-count { font-size: 28px; font-weight: bold; color: #409eff; }
.funnel-label { font-size: 13px; color: #909399; margin-top: 4px; }
.muted { color: #909399; font-size: 13px; }
.interview-record {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  font-size: 13px;
  line-height: 1.8;
}
.record-head { display: flex; gap: 8px; margin-bottom: 4px; }
.response-line { background: #f4f4f5; padding: 6px 10px; border-radius: 4px; margin-top: 4px; }
.eval-target { margin: 0 0 16px; color: #606266; font-size: 14px; }
.resume-actions { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.file-name { color: #909399; font-size: 13px; }
.pdf-preview {
  width: 100%;
  height: 420px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  margin-bottom: 12px;
}
.resume-box-title { font-weight: 600; margin-bottom: 8px; color: #606266; }
.resume-box {
  max-height: 55vh;
  overflow: auto;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}
.resume-box pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
}
.ai-feedback-preview {
  margin: 8px 0 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.ai-feedback-panel { margin-top: 8px; }
.ai-feedback-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.ai-feedback-text {
  margin: 0;
  padding: 14px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  max-height: 360px;
  overflow: auto;
}
</style>
