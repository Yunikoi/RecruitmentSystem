<template>
  <div class="page-card" v-loading="loading">
    <h2>管理驾驶舱 · 预测与 ROI</h2>

    <el-row :gutter="20" class="kpi-row">
      <el-col :span="6" v-for="k in kpis" :key="k.label">
        <el-card shadow="hover"><div class="kpi-label">{{ k.label }}</div><div class="kpi-value">{{ k.value }}</div></el-card>
      </el-col>
    </el-row>

    <el-alert v-if="data?.biInsight" :title="data.biInsight" type="success" show-icon :closable="false" style="margin:16px 0" />

    <el-row :gutter="20" style="margin-top:24px">
      <el-col :span="12"><div ref="funnelChart" class="chart"></div></el-col>
      <el-col :span="12"><div ref="channelChart" class="chart"></div></el-col>
    </el-row>

    <h3 style="margin:24px 0 16px">招聘到岗预测 (Time-to-Hire Predictor)</h3>
    <el-table :data="data?.hirePredictions || []" stripe border>
      <el-table-column prop="department" label="部门" />
      <el-table-column prop="openPositions" label="在招岗位" />
      <el-table-column prop="estimatedDays" label="预计天数" />
      <el-table-column prop="estimatedBudgetWan" label="预算(万)">
        <template #default="{ row }">{{ row.estimatedBudgetWan?.toFixed(1) }}</template>
      </el-table-column>
      <el-table-column prop="suggestion" label="AI 建议" show-overflow-tooltip />
    </el-table>

    <h3 style="margin:24px 0 16px">离职画像反哺 · 渠道留存矩阵</h3>
    <el-table :data="data?.churnMatrix || []" stripe border>
      <el-table-column prop="channel" label="渠道" />
      <el-table-column prop="retentionScore" label="留存评分">
        <template #default="{ row }">{{ row.retentionScore?.toFixed(1) }}</template>
      </el-table-column>
      <el-table-column prop="hires" label="录用数" />
      <el-table-column prop="recommendation" label="预算建议" />
    </el-table>

    <h3 style="margin:24px 0 16px">渠道 ROI</h3>
    <el-table :data="data?.channelRoi || []" stripe border>
      <el-table-column prop="channel" label="渠道" />
      <el-table-column prop="applications" label="投递量" />
      <el-table-column prop="hires" label="录用数" />
      <el-table-column prop="hireRate" label="转化率">
        <template #default="{ row }">{{ row.hireRate?.toFixed(1) }}%</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '../../api/management'

const loading = ref(false)
const data = ref(null)
const funnelChart = ref()
const channelChart = ref()
let chart1, chart2

const kpis = computed(() => [
  { label: '总投递量', value: data.value?.totalApplications || 0 },
  { label: '在招岗位', value: data.value?.activePositions || 0 },
  { label: '平均招聘周期(天)', value: data.value?.avgTimeToHireDays || 0 },
  { label: 'Offer接受率', value: (data.value?.offerAcceptRate || 0).toFixed(1) + '%' }
])

const renderCharts = () => {
  if (!data.value) return
  if (!chart1) chart1 = echarts.init(funnelChart.value)
  if (!chart2) chart2 = echarts.init(channelChart.value)
  const funnel = data.value.funnelByStage || {}
  chart1.setOption({
    title: { text: '招聘漏斗', left: 'center' },
    xAxis: { type: 'category', data: Object.keys(funnel), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'bar', data: Object.values(funnel), itemStyle: { color: '#409EFF' } }]
  })
  const ch = data.value.byChannel || {}
  chart2.setOption({
    title: { text: '渠道分布', left: 'center' },
    series: [{ type: 'pie', radius: '60%', data: Object.entries(ch).map(([n, v]) => ({ name: n, value: v })) }]
  })
}

onMounted(async () => {
  loading.value = true
  try { data.value = await getDashboard(); renderCharts() } finally { loading.value = false }
  window.addEventListener('resize', () => { chart1?.resize(); chart2?.resize() })
})
onUnmounted(() => { chart1?.dispose(); chart2?.dispose() })
</script>

<style scoped>
.kpi-row { margin-bottom: 8px; }
.kpi-label { color: #909399; font-size: 13px; }
.kpi-value { font-size: 28px; font-weight: bold; color: #409eff; margin-top: 8px; }
.chart { height: 320px; }
</style>
