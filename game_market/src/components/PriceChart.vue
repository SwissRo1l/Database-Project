<<<<<<< HEAD
<template>
  <div class="chart-container">
    <h3>📊 当日价格</h3>
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let chartInstance = null

// 生成24小时模拟数据
const generateData = () => {
  const data = []
  const now = new Date()
  let value = 1000
  
  for (let i = 0; i < 24; i++) {
    const time = new Date(now.getTime() - (23 - i) * 3600 * 1000)
    // 随机波动
    value = value + (Math.random() - 0.5) * 50
    data.push({
      time: `${time.getHours()}:00`,
      value: Math.floor(value)
    })
  }
  return data
}

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  const data = generateData()
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' }
    },
    grid: {
      top: '15%',
      left: '5%',
      right: '5%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.time),
      axisLine: { lineStyle: { color: '#555' } },
      axisLabel: { color: '#888' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#888' }
    },
    series: [
      {
        name: '市场指数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(61, 174, 252, 0.5)' },
            { offset: 1, color: 'rgba(61, 174, 252, 0)' }
          ])
        },
        lineStyle: {
          color: '#3DAEFC',
          width: 2
        },
        data: data.map(item => item.value)
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-container {
  background: var(--panel);
  border-radius: 8px;
  padding: 20px;
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(10px);
}

h3 {
  margin: 0 0 15px 0;
  color: var(--text);
  font-size: 18px;
  text-align: center;
}

.chart {
  flex: 1;
  min-height: 300px;
  width: 100%;
}
</style>
=======
<template>
  <div class="chart-container">
    <h3>📊 当日价格</h3>
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let chartInstance = null

// 生成24小时模拟数据
const generateData = () => {
  const data = []
  const now = new Date()
  let value = 1000
  
  for (let i = 0; i < 24; i++) {
    const time = new Date(now.getTime() - (23 - i) * 3600 * 1000)
    // 随机波动
    value = value + (Math.random() - 0.5) * 50
    data.push({
      time: `${time.getHours()}:00`,
      value: Math.floor(value)
    })
  }
  return data
}

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  const data = generateData()
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' }
    },
    grid: {
      top: '15%',
      left: '5%',
      right: '5%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.time),
      axisLine: { lineStyle: { color: '#555' } },
      axisLabel: { color: '#888' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#888' }
    },
    series: [
      {
        name: '市场指数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(61, 174, 252, 0.5)' },
            { offset: 1, color: 'rgba(61, 174, 252, 0)' }
          ])
        },
        lineStyle: {
          color: '#3DAEFC',
          width: 2
        },
        data: data.map(item => item.value)
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-container {
  background: var(--panel);
  border-radius: 8px;
  padding: 20px;
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(10px);
}

h3 {
  margin: 0 0 15px 0;
  color: var(--text);
  font-size: 18px;
  text-align: center;
}

.chart {
  flex: 1;
  min-height: 300px;
  width: 100%;
}
</style>
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
