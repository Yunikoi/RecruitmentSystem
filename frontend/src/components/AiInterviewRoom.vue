<template>
  <el-dialog
    :model-value="visible"
    title="AI 智能初试 · 语音对话"
    width="680px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="loading" class="room-loading">
      <el-icon class="spin"><Loading /></el-icon>
      <span>正在连接 AI 面试官…</span>
    </div>

    <template v-else>
      <div class="room-header">
        <el-tag type="primary">第 {{ questionIndex }}/{{ totalQuestions }} 题</el-tag>
        <span class="room-tips">{{ tips }}</span>
      </div>

      <div ref="chatBox" class="chat-room">
        <div
          v-for="(msg, i) in messages"
          :key="i"
          :class="['bubble', msg.role]"
        >
          <span class="bubble-label">{{ msg.role === 'ai' ? 'AI 面试官' : '我' }}</span>
          <p>{{ msg.text }}</p>
        </div>
        <div v-if="interimText && listening" class="bubble user interim">
          <span class="bubble-label">识别中…</span>
          <p>{{ interimText }}</p>
        </div>
      </div>

      <div v-if="finished" class="finish-panel">
        <el-result icon="success" title="初试完成">
          <template #sub-title>
            <p v-if="score != null" class="score-line">综合得分：<strong>{{ score }}</strong> 分</p>
            <p class="summary-line">{{ summary }}</p>
          </template>
        </el-result>
      </div>

      <div v-else class="room-controls">
        <div class="status-bar">
          <span v-if="speaking" class="status speaking">🔊 AI 正在说话…</span>
          <span v-else-if="submitting" class="status">⏳ 正在提交回答…</span>
          <span v-else-if="listening" class="status listening">🎤 正在聆听，请作答</span>
          <span v-else class="status">点击麦克风语音回答，或直接打字</span>
        </div>

        <div class="control-row">
          <el-button
            circle
            size="large"
            :type="listening ? 'danger' : 'primary'"
            :disabled="submitting || speaking || finished"
            @click="toggleMic"
          >
            <span class="mic-icon">{{ listening ? '⏹' : '🎤' }}</span>
          </el-button>
          <el-input
            v-model="textAnswer"
            type="textarea"
            :rows="2"
            placeholder="也可在此输入回答，Enter 发送"
            :disabled="submitting || speaking || finished"
            @keydown.enter.exact.prevent="submitText"
          />
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="!textAnswer.trim() || speaking || finished"
            @click="submitText"
          >
            发送
          </el-button>
        </div>

        <p v-if="!recognitionSupported" class="voice-hint">
          当前浏览器不支持语音识别，请使用 Chrome/Edge，或直接文字作答。
        </p>
      </div>
    </template>

    <template #footer>
      <el-button v-if="finished" type="primary" @click="handleClose">完成</el-button>
      <el-button v-else @click="handleClose">退出</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { startAiInterview, replyAiInterview } from '../api/candidate'
import { useVoiceInterview } from '../composables/useVoiceInterview'

const props = defineProps({
  visible: Boolean,
  applicationId: { type: Number, default: null }
})

const emit = defineEmits(['update:visible', 'finished'])

const loading = ref(false)
const submitting = ref(false)
const sessionId = ref('')
const messages = ref([])
const tips = ref('')
const questionIndex = ref(0)
const totalQuestions = ref(0)
const textAnswer = ref('')
const finished = ref(false)
const score = ref(null)
const summary = ref('')
const chatBox = ref(null)

const {
  recognitionSupported,
  listening,
  speaking,
  interimText,
  speak,
  stopSpeak,
  listen,
  stopListen
} = useVoiceInterview()

const scrollBottom = async () => {
  await nextTick()
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
}

const pushAi = async (text) => {
  messages.value.push({ role: 'ai', text })
  await scrollBottom()
  await speak(text)
}

const reset = () => {
  stopSpeak()
  stopListen()
  sessionId.value = ''
  messages.value = []
  tips.value = ''
  questionIndex.value = 0
  totalQuestions.value = 0
  textAnswer.value = ''
  finished.value = false
  score.value = null
  summary.value = ''
}

const initSession = async () => {
  if (!props.applicationId) return
  reset()
  loading.value = true
  try {
    const data = await startAiInterview(props.applicationId)
    sessionId.value = data.sessionId
    tips.value = data.tips
    questionIndex.value = data.questionIndex
    totalQuestions.value = data.totalQuestions
    await pushAi(data.aiMessage)
  } catch (e) {
    ElMessage.error(e.message || '无法开始 AI 初试')
    emit('update:visible', false)
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.visible, props.applicationId],
  ([vis, id]) => {
    if (vis && id) initSession()
    if (!vis) reset()
  }
)

const submitAnswer = async (answer) => {
  const text = answer?.trim()
  if (!text || submitting.value || finished.value) return
  submitting.value = true
  messages.value.push({ role: 'user', text })
  textAnswer.value = ''
  await scrollBottom()
  try {
    const res = await replyAiInterview(props.applicationId, {
      sessionId: sessionId.value,
      answer: text
    })
    questionIndex.value = res.questionIndex
    totalQuestions.value = res.totalQuestions
    await pushAi(res.aiMessage)
    if (res.finished) {
      finished.value = true
      score.value = res.score
      summary.value = res.summary
      emit('finished', res)
    }
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const submitText = () => submitAnswer(textAnswer.value)

const toggleMic = async () => {
  if (listening.value) {
    stopListen()
    return
  }
  if (speaking.value || submitting.value || finished.value) return
  try {
    const text = await listen()
    if (text) await submitAnswer(text)
  } catch (e) {
    ElMessage.warning(e.message)
  }
}

const handleClose = async () => {
  if (!finished.value && messages.value.length > 1) {
    try {
      await ElMessageBox.confirm('面试尚未完成，确定退出吗？', '提示', { type: 'warning' })
    } catch {
      return
    }
  }
  stopSpeak()
  stopListen()
  emit('update:visible', false)
}
</script>

<style scoped>
.room-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 40px;
  color: #606266;
}
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.room-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.room-tips { font-size: 13px; color: #909399; }

.chat-room {
  height: 320px;
  overflow-y: auto;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.bubble {
  max-width: 88%;
  margin-bottom: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
}
.bubble.ai {
  background: #fff;
  border: 1px solid #dcdfe6;
  margin-right: auto;
}
.bubble.user {
  background: #409eff;
  color: #fff;
  margin-left: auto;
}
.bubble.interim { opacity: 0.75; }
.bubble-label {
  display: block;
  font-size: 12px;
  opacity: 0.75;
  margin-bottom: 4px;
}
.bubble p { margin: 0; }

.room-controls { margin-top: 8px; }
.status-bar { margin-bottom: 10px; font-size: 13px; color: #606266; }
.status.listening { color: #e6a23c; }
.status.speaking { color: #409eff; }

.control-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.mic-icon { font-size: 20px; }
.voice-hint { font-size: 12px; color: #909399; margin-top: 8px; }

.finish-panel { margin-top: -8px; }
.score-line { font-size: 16px; color: #303133; }
.summary-line { font-size: 14px; color: #606266; max-width: 480px; margin: 8px auto 0; line-height: 1.6; }
</style>
