<template>
  <div class="page-card analysis-page">
    <div class="page-header">
      <div>
        <h2>AI 简历深度分析</h2>
        <p class="subtitle">上传简历，获取全方位能力画像，并自动匹配最适合的在招岗位</p>
      </div>
      <el-tag v-if="result?.aiPowered" type="success" effect="dark">DeepSeek AI 驱动</el-tag>
      <el-tag v-else type="info" effect="plain">规则引擎分析（配置 API Key 可启用 AI）</el-tag>
    </div>

    <!-- 上传区 -->
    <el-card v-if="!result" shadow="never" class="upload-card">
      <el-upload
        drag
        :auto-upload="false"
        :limit="1"
        accept=".txt,.pdf,.doc,.docx"
        :on-change="handleFile"
        :disabled="analyzing"
      >
        <div class="upload-icon">📋</div>
        <div class="upload-title">拖拽简历到此处，或点击上传</div>
        <div class="upload-hint">支持 PDF / TXT，AI 将解析技能、经历、优劣势并匹配岗位</div>
      </el-upload>
      <el-divider>或粘贴简历文本</el-divider>
      <el-input
        v-model="pasteText"
        type="textarea"
        :rows="6"
        placeholder="将简历全文粘贴到此处..."
        :disabled="analyzing"
      />
      <div class="upload-actions">
        <el-button type="primary" size="large" :loading="analyzing" :disabled="!canAnalyze" @click="startAnalyze">
          {{ analyzing ? 'AI 分析中，请稍候...' : '开始深度分析' }}
        </el-button>
      </div>
    </el-card>

    <!-- 分析结果 -->
    <template v-if="result">
      <div class="result-toolbar">
        <el-button @click="resetAnalysis">重新分析</el-button>
      </div>

      <!-- 综合评分 -->
      <el-row :gutter="20" class="score-row">
        <el-col :span="8">
          <el-card shadow="hover" class="score-card">
            <div class="score-label">简历综合评分</div>
            <el-progress
              type="dashboard"
              :percentage="result.overallScore || 0"
              :color="scoreColor(result.overallScore)"
              :width="140"
            />
            <div class="score-hint">{{ scoreLabel(result.overallScore) }}</div>
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card shadow="hover" class="summary-card">
            <template #header><strong>AI 综合评价</strong></template>
            <p class="summary-text">{{ result.analysisSummary }}</p>
            <div class="direction">
              <el-icon><Compass /></el-icon>
              <span>职业方向建议：<strong>{{ result.careerDirection }}</strong></span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 基本信息 -->
      <el-card shadow="never" class="section-card">
        <template #header><strong>候选人画像</strong></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="姓名">{{ result.profile?.name || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ result.profile?.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ result.profile?.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="工作年限">
            {{ result.profile?.yearsOfExperience != null ? result.profile.yearsOfExperience + ' 年' : '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="学历">{{ result.profile?.education || '—' }}</el-descriptions-item>
          <el-descriptions-item label="当前职位">{{ result.profile?.currentTitle || '—' }}</el-descriptions-item>
          <el-descriptions-item label="职业摘要" :span="3">{{ result.profile?.summary || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 技能矩阵 -->
      <el-card shadow="never" class="section-card">
        <template #header><strong>技能矩阵</strong></template>
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="skill-group">
              <div class="skill-title">核心技能</div>
              <el-tag v-for="s in result.skills?.core || []" :key="s" type="primary" class="skill-tag">{{ s }}</el-tag>
              <span v-if="!(result.skills?.core?.length)" class="empty-skill">暂无识别</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="skill-group">
              <div class="skill-title">工具 / 技术栈</div>
              <el-tag v-for="s in result.skills?.tools || []" :key="s" type="success" class="skill-tag">{{ s }}</el-tag>
              <span v-if="!(result.skills?.tools?.length)" class="empty-skill">暂无识别</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="skill-group">
              <div class="skill-title">软实力</div>
              <el-tag v-for="s in result.skills?.soft || []" :key="s" type="warning" class="skill-tag">{{ s }}</el-tag>
              <span v-if="!(result.skills?.soft?.length)" class="empty-skill">暂无识别</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 工作经历 -->
      <el-card v-if="result.experiences?.length" shadow="never" class="section-card">
        <template #header><strong>工作经历解析</strong></template>
        <el-timeline>
          <el-timeline-item
            v-for="(exp, i) in result.experiences"
            :key="i"
            :timestamp="exp.duration"
            placement="top"
          >
            <div class="exp-title">{{ exp.role }} · {{ exp.company }}</div>
            <div class="exp-highlights">{{ exp.highlights || '—' }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 优劣势与建议 -->
      <el-row :gutter="20" class="section-card">
        <el-col :span="8">
          <el-card shadow="never" class="insight-card strength">
            <template #header><strong>✅ 核心优势</strong></template>
            <ul>
              <li v-for="(s, i) in result.strengths" :key="i">{{ s }}</li>
            </ul>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="insight-card weakness">
            <template #header><strong>⚠️ 待提升项</strong></template>
            <ul>
              <li v-for="(s, i) in result.weaknesses" :key="i">{{ s }}</li>
            </ul>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="insight-card suggestion">
            <template #header><strong>💡 优化建议</strong></template>
            <ul>
              <li v-for="(s, i) in result.suggestions" :key="i">{{ s }}</li>
            </ul>
          </el-card>
        </el-col>
      </el-row>

      <!-- 岗位匹配 -->
      <el-card shadow="never" class="section-card match-section">
        <template #header>
          <div class="match-header">
            <strong>🎯 智能岗位匹配（{{ result.matchedPositions?.length || 0 }} 个在招岗位）</strong>
            <span class="match-hint">按匹配度从高到低排序，点击可一键投递</span>
          </div>
        </template>

        <el-empty v-if="!(result.matchedPositions?.length)" description="暂无已发布岗位可匹配" />

        <div v-for="(pos, idx) in result.matchedPositions" :key="pos.positionId" class="match-card" :class="pos.recommendation">
          <div class="match-rank">#{{ idx + 1 }}</div>
          <div class="match-body">
            <div class="match-top">
              <h3>{{ pos.title }}</h3>
              <el-tag :type="recommendType(pos.recommendation)" size="small">{{ recommendLabel(pos.recommendation) }}</el-tag>
              <span class="dept">{{ pos.department }}</span>
            </div>
            <p class="match-desc">{{ pos.description }}</p>
            <div class="match-score-row">
              <span>匹配度</span>
              <el-progress
                :percentage="pos.matchScore || 0"
                :color="scoreColor(pos.matchScore)"
                :stroke-width="14"
                style="flex:1; margin: 0 12px"
              />
              <strong class="match-pct">{{ pos.matchScore }}%</strong>
            </div>
            <div class="match-detail">
              <div class="match-highlight">
                <span class="label">匹配亮点</span>
                <p>{{ pos.highlights }}</p>
              </div>
              <div v-if="pos.risks" class="match-risk">
                <span class="label">潜在风险</span>
                <p>{{ pos.risks }}</p>
              </div>
            </div>
            <div class="skill-compare">
              <div v-if="pos.matchedSkills?.length">
                <span class="label">已具备</span>
                <el-tag v-for="s in pos.matchedSkills" :key="s" type="success" size="small" class="skill-tag">{{ s }}</el-tag>
              </div>
              <div v-if="pos.gapSkills?.length" style="margin-top:8px">
                <span class="label">待补充</span>
                <el-tag v-for="s in pos.gapSkills" :key="s" type="danger" size="small" effect="plain" class="skill-tag">{{ s }}</el-tag>
              </div>
            </div>
          </div>
          <div class="match-actions">
            <el-button type="primary" @click="goApply(pos.positionId)">一键投递</el-button>
            <el-button link @click="$router.push(`/public/${pos.positionId}`)">查看 JD</el-button>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Compass } from '@element-plus/icons-vue'
import { analyzeResume, analyzeResumeText } from '../../api/candidate'

const router = useRouter()
const analyzing = ref(false)
const result = ref(null)
const resumeFile = ref(null)
const pasteText = ref('')

const canAnalyze = computed(() => !!resumeFile.value || pasteText.value.trim().length > 30)

const handleFile = (file) => {
  resumeFile.value = file.raw
  pasteText.value = ''
}

const startAnalyze = async () => {
  analyzing.value = true
  try {
    if (resumeFile.value) {
      result.value = await analyzeResume(resumeFile.value)
    } else {
      result.value = await analyzeResumeText(pasteText.value.trim())
    }
    ElMessage.success('简历分析完成')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (e) {
    ElMessage.error(e?.message || '分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

const resetAnalysis = () => {
  result.value = null
  resumeFile.value = null
  pasteText.value = ''
}

const goApply = (positionId) => {
  router.push(`/candidate/apply/${positionId}`)
}

const scoreColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const scoreLabel = (score) => {
  if (score >= 85) return '优秀，竞争力强'
  if (score >= 70) return '良好，有提升空间'
  if (score >= 50) return '一般，建议优化简历'
  return '较弱，需重点改进'
}

const recommendType = (level) => ({ HIGH: 'success', MEDIUM: 'warning', LOW: 'info' }[level] || 'info')
const recommendLabel = (level) => ({ HIGH: '强烈推荐', MEDIUM: '值得考虑', LOW: '匹配度偏低' }[level] || '—')
</script>

<style scoped>
.analysis-page { max-width: 1100px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.subtitle { color: #909399; margin-top: 8px; }
.upload-card { text-align: center; padding: 20px; }
.upload-icon { font-size: 48px; margin-bottom: 12px; }
.upload-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.upload-hint { font-size: 13px; color: #909399; }
.upload-actions { margin-top: 20px; }
.result-toolbar { margin-bottom: 16px; }
.score-row { margin-bottom: 20px; }
.score-card { text-align: center; padding: 16px 0; }
.score-label { font-size: 14px; color: #606266; margin-bottom: 12px; }
.score-hint { margin-top: 8px; font-size: 13px; color: #909399; }
.summary-card .summary-text { line-height: 1.8; color: #303133; font-size: 15px; }
.direction { display: flex; align-items: center; gap: 8px; margin-top: 16px; color: #409eff; font-size: 14px; }
.section-card { margin-bottom: 20px; }
.skill-group { min-height: 80px; }
.skill-title { font-weight: 600; margin-bottom: 10px; color: #606266; font-size: 14px; }
.skill-tag { margin: 0 6px 6px 0; }
.empty-skill { color: #c0c4cc; font-size: 13px; }
.exp-title { font-weight: 600; font-size: 15px; }
.exp-highlights { color: #606266; margin-top: 4px; line-height: 1.6; }
.insight-card ul { padding-left: 18px; margin: 0; line-height: 2; color: #303133; }
.insight-card.strength :deep(.el-card__header) { background: #f0f9eb; }
.insight-card.weakness :deep(.el-card__header) { background: #fdf6ec; }
.insight-card.suggestion :deep(.el-card__header) { background: #ecf5ff; }
.match-header { display: flex; align-items: baseline; gap: 12px; flex-wrap: wrap; }
.match-hint { font-size: 13px; color: #909399; font-weight: normal; }
.match-card {
  display: flex; gap: 16px; padding: 20px; margin-bottom: 16px;
  border: 1px solid #ebeef5; border-radius: 10px; background: #fafafa;
  transition: box-shadow 0.2s;
}
.match-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.match-card.HIGH { border-left: 4px solid #67c23a; }
.match-card.MEDIUM { border-left: 4px solid #e6a23c; }
.match-card.LOW { border-left: 4px solid #c0c4cc; }
.match-rank { font-size: 28px; font-weight: 700; color: #dcdfe6; min-width: 40px; }
.match-body { flex: 1; }
.match-top { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 8px; }
.match-top h3 { margin: 0; font-size: 17px; }
.dept { color: #909399; font-size: 13px; }
.match-desc { color: #606266; font-size: 14px; line-height: 1.6; margin-bottom: 12px; }
.match-score-row { display: flex; align-items: center; font-size: 14px; margin-bottom: 12px; }
.match-pct { font-size: 18px; color: #409eff; min-width: 48px; }
.match-detail { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px; }
.match-highlight, .match-risk { font-size: 14px; }
.label { font-weight: 600; color: #909399; font-size: 12px; display: block; margin-bottom: 4px; }
.match-highlight p, .match-risk p { margin: 0; line-height: 1.6; color: #303133; }
.match-actions { display: flex; flex-direction: column; gap: 8px; justify-content: center; }
</style>
