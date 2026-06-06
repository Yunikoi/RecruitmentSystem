<template>
  <div class="page-card">
    <h2 class="title">岗位数据统计</h2>

    <el-row :gutter="20" class="cards">
      <el-col :span="6" v-for="item in cardList" :key="item.key">
        <el-card shadow="hover">
          <div class="card-label">{{ item.label }}</div>
          <div class="card-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getStatistics } from '../api/position'

const chartRef = ref()
const stats = ref({ total: 0, byStatus: {} })
let chartInstance = null

const statusMap = {
  DRAFT: '草稿',
  PENDING: '待审批',
  PUBLISHED: '已发布',
  CLOSED: '已关闭'
}

const cardList = computed(() => {
  const byStatus = stats.value.byStatus || {}
  return [
    { key: 'total', label: '岗位总数', value: stats.value.total || 0 },
    { key: 'DRAFT', label: '草稿', value: byStatus.DRAFT || 0 },
    { key: 'PENDING', label: '待审批', value: byStatus.PENDING || 0 },
    { key: 'PUBLISHED', label: '已发布', value: byStatus.PUBLISHED || 0 }
  ]
})

const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const byStatus = stats.value.byStatus || {}
  const labels = Object.keys(statusMap).map((key) => statusMap[key])
  const values = Object.keys(statusMap).map((key) => byStatus[key] || 0)

  chartInstance.setOption({
    title: { text: '岗位状态分布', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: values,
        itemStyle: { color: '#409EFF' },
        barWidth: 48
      }
    ]
  })
}

const loadData = async () => {
  stats.value = await getStatistics()
  renderChart()
}

const handleResize = () => chartInstance?.resize()

onMounted(async () => {
  await loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.title {
  margin-bottom: 24px;
}

.cards {
  margin-bottom: 32px;
}

.card-label {
  color: #909399;
  font-size: 14px;
}

.card-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.chart {
  width: 100%;
  height: 420px;
}
</style>
