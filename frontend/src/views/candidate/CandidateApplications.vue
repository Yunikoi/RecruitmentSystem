<template>
  <div class="page-card">
    <h2>我的投递 · 进度追踪</h2>
    <p class="subtitle">多岗位独立追踪 · 每条投递的数据（匹配分、面试、评价）相互隔离</p>

    <!-- 功能入口总览：始终可见，避免“找不到功能” -->
    <el-card shadow="never" class="feature-hub">
      <template #header><strong>求职者功能中心</strong> <span class="hub-hint">以下为本系统 C 端能力，按当前投递状态自动启用</span></template>
      <el-row :gutter="12">
        <el-col :span="8" v-for="f in featureList" :key="f.key">
          <div class="feature-item" :class="{ active: f.active }">
            <span class="f-icon">{{ f.active ? '✓' : '○' }}</span>
            <div>
              <div class="f-title">{{ f.title }}</div>
              <div class="f-desc">{{ f.desc }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-alert v-if="pendingCount > 0" :title="`您有 ${pendingCount} 条待确认的面试邀请，请向下滚动在「面试邀请」处接受/拒绝`" type="warning" show-icon :closable="false" style="margin: 16px 0" />

    <el-empty v-if="!loading && list.length === 0" description="暂无投递记录">
      <el-button type="primary" @click="$router.push('/public')">去投递岗位</el-button>
    </el-empty>

    <div v-for="item in list" :key="item.id" class="track-card">
      <div class="track-header">
        <div>
          <h3>{{ item.positionTitle }}</h3>
          <span class="dept">{{ item.department }}</span>
          <el-tag size="small" type="info" style="margin-left:8px">独立追踪 #{{ item.id }}</el-tag>
        </div>
        <el-tag :type="stageType(item.stage)">{{ item.stageLabel }}</el-tag>
      </div>

      <el-steps :active="stageIndex(item.stage)" finish-status="success" align-center style="margin: 24px 0">
        <el-step title="已投递" /><el-step title="初筛" /><el-step title="AI初试" />
        <el-step title="业务面" /><el-step title="HR面" /><el-step title="Offer" />
      </el-steps>

      <div class="meta">
        <el-tag v-if="item.matchScore" type="success">本岗位 AI 匹配 {{ item.matchScore }}%</el-tag>
        <span>渠道：{{ item.channelLabel }}</span>
        <span>投递：{{ formatTime(item.appliedAt) }}</span>
      </div>

      <!-- 模拟面试：始终展示，非初筛阶段显示历史/说明 -->
      <div class="mock-box">
        <div class="mock-head">
          <strong class="mock-title">AI 模拟面试</strong>
          <el-tag v-if="mockStatus[item.id]?.usedCount" type="info" size="small">
            已练 {{ mockStatus[item.id].usedCount }}/2 次
          </el-tag>
        </div>
        <p class="mock-desc" v-if="['APPLIED','SCREENING'].includes(item.stage)">
          正式 AI 初试前可进行最多 2 次模拟，熟悉答题节奏与环境。
        </p>
        <p class="mock-desc" v-else>
          当前阶段：{{ item.stageLabel }}。模拟面试仅在「已投递 / 初筛」开放，历史记录如下。
        </p>
        <el-button
          v-if="['APPLIED','SCREENING'].includes(item.stage)"
          type="primary"
          :disabled="!(mockStatus[item.id]?.canStart)"
          @click="openMock(item)"
        >
          开始模拟面试
        </el-button>

        <div v-if="mockStatus[item.id]?.history?.length" class="mock-history-wrap">
          <div class="mock-history-title">练习记录</div>
          <div
            v-for="(h, idx) in mockStatus[item.id].history"
            :key="h.sessionId"
            class="mock-record"
          >
            <div class="mock-record-head">
              <span class="mock-record-label">第 {{ mockStatus[item.id].history.length - idx }} 次</span>
              <el-tag :type="h.score >= 75 ? 'success' : h.score >= 60 ? 'warning' : 'danger'" size="small">
                得分 {{ h.score ?? '—' }}
              </el-tag>
              <el-button link type="primary" @click="openMockFeedback(h)">查看完整反馈</el-button>
            </div>
            <p class="mock-record-preview">{{ h.feedback || '暂无反馈' }}</p>
          </div>
        </div>
      </div>

      <!-- 日程协同 -->
      <div v-if="['BUSINESS_INTERVIEW','HR_INTERVIEW'].includes(item.stage)" class="calendar-box">
        <strong>可预约时间看板</strong>
        <el-empty v-if="!(calendarSlots[item.id]?.length)" description="暂无可预约时段，请联系 HR" :image-size="60" />
        <div v-else class="slot-list">
          <div v-for="slot in calendarSlots[item.id]" :key="slot.id" class="slot-item">
            <span>{{ formatTime(slot.startTime) }} · {{ slot.interviewerName }}</span>
            <el-button size="small" type="success" @click="bookSlot(item.id, slot.id)">一键预约</el-button>
          </div>
        </div>
      </div>

      <div v-if="item.interviews?.length" class="interview-invites">
        <strong>面试邀请</strong>
        <p v-if="!item.interviews.some(iv => iv.canRespond)" class="hint-text">当前邀请均已处理。HR 新发邀请后会显示「接受 / 拒绝 / 申请改期」按钮。</p>
        <div v-for="iv in item.interviews" :key="iv.id" class="invite-card" :class="{ pending: iv.canRespond, accepted: iv.status === 'ACCEPTED' }">
          <el-tag type="warning">{{ iv.typeLabel }}</el-tag>
          <el-tag :type="inviteStatusType(iv.status)" size="small">{{ iv.statusLabel }}</el-tag>
          <p>时间：{{ formatTime(iv.scheduledAt) }} · {{ iv.location }}</p>
          <p v-if="iv.canRespond && iv.expireAt" class="expire-tip">请在 {{ formatTime(iv.expireAt) }} 前响应</p>
          <div v-if="iv.canRespond" class="invite-actions">
            <el-button type="success" size="small" @click="handleAccept(iv)">接受邀请</el-button>
            <el-button type="warning" size="small" plain @click="openReschedule(iv)">申请改期</el-button>
            <el-button type="danger" size="small" plain @click="openDecline(iv)">拒绝邀请</el-button>
          </div>
        </div>
      </div>
      <div v-else class="interview-invites empty-invite">
        <strong>面试邀请</strong>
        <p class="hint-text">HR 安排面试后，将在此显示邀请，您可接受、拒绝或申请改期。</p>
      </div>

      <div class="actions">
        <el-button v-if="canAiInterview(item)" type="primary" @click="openAiInterview(item)">进入 AI 语音初试</el-button>
        <el-button v-if="item.stage === 'OFFER'" type="success" @click="handleSignOffer(item)">签署 Offer</el-button>
        <el-button @click="openChat(item)">智能答疑</el-button>
      </div>
    </div>

    <el-dialog v-model="mockFeedbackVisible" title="模拟面试 · AI 反馈" width="560px">
      <div v-if="mockFeedbackDetail" class="mock-feedback-dialog">
        <div class="mock-feedback-score">
          得分：<strong>{{ mockFeedbackDetail.score ?? '—' }}</strong>
        </div>
        <div class="mock-feedback-body">{{ mockFeedbackDetail.feedback || '暂无反馈内容' }}</div>
      </div>
    </el-dialog>

    <el-dialog v-model="mockVisible" title="AI 模拟面试" width="560px">
      <ol v-if="mockQuestions.length"><li v-for="(q,i) in mockQuestions" :key="i">{{ q }}</li></ol>
      <el-input v-model="mockAnswers" type="textarea" :rows="5" placeholder="请输入模拟回答..." style="margin-top:12px" />
      <template #footer>
        <el-button @click="mockVisible=false">取消</el-button>
        <el-button type="primary" :loading="mockSubmitting" @click="submitMock">提交获取反馈</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rescheduleVisible" title="申请改期" width="480px">
      <el-input v-model="rescheduleForm.note" type="textarea" :rows="4" placeholder="改期原因（必填）" />
      <el-date-picker v-model="rescheduleForm.preferredTime" type="datetime" placeholder="期望时间（选填）" style="width:100%;margin-top:12px" />
      <template #footer>
        <el-button @click="rescheduleVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReschedule">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="declineVisible" title="拒绝面试邀请" width="480px">
      <el-input v-model="declineForm.note" type="textarea" :rows="4" placeholder="拒绝原因（必填）" />
      <template #footer>
        <el-button @click="declineVisible=false">取消</el-button>
        <el-button type="danger" @click="submitDecline">确认</el-button>
      </template>
    </el-dialog>

    <AiInterviewRoom
      v-model:visible="aiVisible"
      :application-id="aiAppId"
      @finished="onAiInterviewFinished"
    />

    <el-dialog v-model="chatVisible" title="智能答疑" width="500px">
      <div class="chat-box"><div v-for="(m,i) in chatHistory" :key="i" :class="['chat-msg', m.role]">{{ m.text }}</div></div>
      <el-input v-model="chatQuestion" @keyup.enter="sendChat"><template #append><el-button @click="sendChat">发送</el-button></template></el-input>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMyApplications, askAi, acceptInterview, declineInterview, rescheduleInterview,
  getMockInterviewStatus, startMockInterview, submitMockInterview,
  getCalendarSlots, bookCalendarSlot, signOffer
} from '../../api/candidate'
import AiInterviewRoom from '../../components/AiInterviewRoom.vue'

const rescheduleVisible = ref(false)
const rescheduleForm = reactive({ note: '', preferredTime: null })

const loading = ref(false)
const list = ref([])
const mockStatus = ref({})
const calendarSlots = ref({})
const mockVisible = ref(false)
const mockSubmitting = ref(false)
const mockQuestions = ref([])
const mockAnswers = ref('')
const mockSessionId = ref(null)
const mockAppId = ref(null)
const mockFeedbackVisible = ref(false)
const mockFeedbackDetail = ref(null)
const aiVisible = ref(false)
const aiAppId = ref(null)
const chatVisible = ref(false)
const declineVisible = ref(false)
const currentInvite = ref(null)
const chatQuestion = ref('')
const chatHistory = ref([])
const currentPositionId = ref(null)
const declineForm = reactive({ note: '' })

const pendingCount = computed(() =>
  list.value.reduce((n, item) => n + (item.interviews?.filter(iv => iv.canRespond).length || 0), 0)
)

const featureList = computed(() => {
  const hasScreening = list.value.some(i => ['APPLIED', 'SCREENING'].includes(i.stage))
  const hasInterview = list.value.some(i => ['BUSINESS_INTERVIEW', 'HR_INTERVIEW'].includes(i.stage))
  const hasPending = pendingCount.value > 0
  const hasOffer = list.value.some(i => i.stage === 'OFFER')
  return [
    { key: 'mock', title: 'AI 模拟面试', desc: hasScreening ? '初筛阶段可练 2 次' : '需处于初筛阶段', active: hasScreening },
    { key: 'calendar', title: '日程自助预约', desc: hasInterview ? '业务/HR 面可约时段' : '进入面试阶段后开放', active: hasInterview },
    { key: 'invite', title: '邀请接受/拒绝', desc: hasPending ? '有待确认邀请' : '邀请已处理或无新邀请', active: hasPending },
    { key: 'ai', title: 'AI 语音初试', desc: '逐题语音/文字对话', active: list.value.some(i => ['SCREENING', 'AI_INTERVIEW'].includes(i.stage)) },
    { key: 'offer', title: 'Offer e-Sign', desc: hasOffer ? '可在线签署' : '到达 Offer 阶段开放', active: hasOffer },
    { key: 'chat', title: '智能答疑', desc: '24h 岗位问答', active: list.value.length > 0 }
  ]
})

const stages = ['APPLIED', 'SCREENING', 'AI_INTERVIEW', 'BUSINESS_INTERVIEW', 'HR_INTERVIEW', 'OFFER']
const stageIndex = (s) => { const i = stages.indexOf(s); return i >= 0 ? i : 0 }
const stageType = (s) => s === 'REJECTED' ? 'danger' : s === 'HIRED' ? 'success' : 'warning'
const formatTime = (t) => t ? t.replace('T', ' ').slice(0, 19) : '-'
const canAiInterview = (item) => ['SCREENING', 'AI_INTERVIEW'].includes(item.stage)
const inviteStatusType = (s) => ({ PENDING: 'warning', ACCEPTED: 'success', DECLINED: 'danger' }[s] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    list.value = await getMyApplications()
    for (const item of list.value) {
      try { mockStatus.value[item.id] = await getMockInterviewStatus(item.id) } catch {}
      if (['BUSINESS_INTERVIEW', 'HR_INTERVIEW'].includes(item.stage)) {
        try { calendarSlots.value[item.id] = await getCalendarSlots(item.id) } catch { calendarSlots.value[item.id] = [] }
      }
    }
  } finally { loading.value = false }
}

const openMock = async (item) => {
  const st = await getMockInterviewStatus(item.id)
  if (!st.canStart) { ElMessage.warning('模拟次数已用完或不可模拟'); return }
  const res = await startMockInterview(item.id)
  mockAppId.value = item.id
  mockSessionId.value = res.sessionId
  mockQuestions.value = res.questions || []
  mockAnswers.value = ''
  mockVisible.value = true
}

const submitMock = async () => {
  mockSubmitting.value = true
  try {
    const res = await submitMockInterview(mockAppId.value, { sessionId: mockSessionId.value, answers: mockAnswers.value })
    mockVisible.value = false
    mockFeedbackDetail.value = res
    mockFeedbackVisible.value = true
    await loadData()
  } finally { mockSubmitting.value = false }
}

const openMockFeedback = (record) => {
  mockFeedbackDetail.value = record
  mockFeedbackVisible.value = true
}

const bookSlot = async (appId, slotId) => {
  await bookCalendarSlot(appId, slotId)
  ElMessage.success('时段已锁定，面试邀请已自动确认')
  await loadData()
}

const handleAccept = async (iv) => {
  await acceptInterview(iv.id)
  ElMessage.success('已接受邀请')
  loadData()
}

const openDecline = (iv) => { currentInvite.value = iv; declineForm.note = ''; declineVisible.value = true }
const submitDecline = async () => {
  if (!declineForm.note.trim()) return ElMessage.warning('请填写原因')
  await declineInterview(currentInvite.value.id, declineForm.note.trim())
  declineVisible.value = false
  loadData()
}

const openReschedule = (iv) => { currentInvite.value = iv; rescheduleForm.note = ''; rescheduleForm.preferredTime = null; rescheduleVisible.value = true }
const submitReschedule = async () => {
  if (!rescheduleForm.note.trim()) return ElMessage.warning('请说明改期原因')
  await rescheduleInterview(currentInvite.value.id, { note: rescheduleForm.note.trim(), preferredTime: rescheduleForm.preferredTime || undefined })
  rescheduleVisible.value = false
  ElMessage.success('改期申请已提交')
  loadData()
}

const handleSignOffer = async (item) => {
  await signOffer(item.id)
  ElMessage.success('Offer 已在线签署')
}

const openAiInterview = (item) => {
  aiAppId.value = item.id
  aiVisible.value = true
}

const onAiInterviewFinished = () => {
  ElMessage.success('AI 初试已完成')
  loadData()
}

const openChat = (item) => {
  currentPositionId.value = item.positionId
  chatHistory.value = [{ role: 'bot', text: '您好！有任何岗位问题都可以问我。' }]
  chatVisible.value = true
}

const sendChat = async () => {
  if (!chatQuestion.value.trim()) return
  chatHistory.value.push({ role: 'user', text: chatQuestion.value })
  const q = chatQuestion.value
  chatQuestion.value = ''
  const res = await askAi(currentPositionId.value, q)
  chatHistory.value.push({ role: 'bot', text: res.answer })
}

onMounted(loadData)
</script>

<style scoped>
.subtitle { color: #909399; margin-bottom: 24px; }
.track-card { border: 1px solid #ebeef5; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
.track-header { display: flex; justify-content: space-between; align-items: center; }
.dept { color: #909399; font-size: 13px; margin-left: 8px; }
.meta { display: flex; gap: 16px; flex-wrap: wrap; font-size: 13px; margin: 12px 0; }
.mock-box, .calendar-box { margin: 12px 0; padding: 16px; background: #ecf5ff; border-radius: 8px; border: 1px solid #d9ecff; }
.mock-head { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.mock-title { font-size: 16px; color: #303133; }
.mock-desc { font-size: 14px; line-height: 1.6; color: #606266; margin: 0 0 12px; }
.mock-history-wrap { margin-top: 16px; }
.mock-history-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 10px; }
.mock-record {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 10px;
}
.mock-record-head { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 10px; }
.mock-record-label { font-size: 15px; font-weight: 600; color: #303133; }
.mock-record-preview {
  font-size: 14px;
  line-height: 1.75;
  color: #303133;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.mock-feedback-dialog { font-size: 15px; line-height: 1.8; color: #303133; }
.mock-feedback-score { font-size: 16px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #ebeef5; }
.mock-feedback-score strong { font-size: 22px; color: #409eff; }
.mock-feedback-body { white-space: pre-wrap; word-break: break-word; }
.feature-hub { margin-bottom: 20px; }
.hub-hint { font-size: 12px; color: #909399; font-weight: normal; margin-left: 8px; }
.feature-item { display: flex; gap: 8px; align-items: flex-start; padding: 10px; border-radius: 6px; background: #f5f7fa; margin-bottom: 8px; font-size: 13px; }
.feature-item.active { background: #f0f9eb; border: 1px solid #b3e19d; }
.f-title { font-weight: 600; color: #303133; }
.f-desc { color: #606266; font-size: 13px; margin-top: 4px; line-height: 1.5; }
.f-icon { font-size: 18px; color: #67c23a; line-height: 1.2; }
.feature-item:not(.active) .f-icon { color: #c0c4cc; }
.empty-invite { background: #fafafa; padding: 12px; border-radius: 6px; margin: 12px 0; }
.expire-tip { color: #e6a23c; font-size: 14px; }
.invite-card.accepted { border-left: 3px solid #67c23a; }
.hint-text { font-size: 14px; line-height: 1.6; color: #606266; margin: 6px 0; }
.slot-list { margin-top: 8px; }
.slot-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; font-size: 14px; color: #303133; }
.interview-invites { margin: 12px 0; padding: 14px; background: #fdf6ec; border-radius: 8px; font-size: 14px; color: #303133; }
.invite-card { margin-top: 10px; padding: 12px 14px; background: #fff; border-radius: 6px; font-size: 14px; line-height: 1.7; color: #303133; }
.invite-actions { margin-top: 8px; display: flex; gap: 8px; }
.actions { margin-top: 12px; }
.ai-questions { line-height: 2; padding-left: 20px; }
.ai-tips { color: #909399; font-size: 13px; }
.chat-box { max-height: 200px; overflow-y: auto; margin-bottom: 12px; }
.chat-msg { padding: 8px; margin: 4px 0; border-radius: 6px; font-size: 14px; }
.chat-msg.user { background: #409eff; color: #fff; margin-left: 30px; }
.chat-msg.bot { background: #f4f4f5; margin-right: 30px; }
</style>
