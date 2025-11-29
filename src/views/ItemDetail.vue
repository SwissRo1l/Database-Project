<template>
  <div class="page item-detail">
    <NavBar />
    <div class="container">
      <div v-if="isLoading" class="loading-container">
        <LoadingWave />
      </div>
      <div v-else class="detail-card">
        <div class="item-image">
          <img :src="displayImage" alt="Item Image" />
        </div>
        <div class="item-info">
          <h1>{{ item.name }}</h1>
          <p class="price">{{ item.price }} G</p>
          <p class="change" :class="{ up: item.change > 0, down: item.change < 0 }">
            {{ item.change > 0 ? '+' : '' }}{{ Number(item.change).toFixed(2) }}% (24h)
          </p>
          <p class="description">
            这是一个非常稀有的物品，拥有强大的属性。
            (这里是物品描述占位符)
          </p>
          <div class="actions">
            <button class="button-primary" @click="buyItem">立即购买</button>
            <button class="button-secondary" @click="goBack">返回市场</button>
          </div>
        </div>
      </div>
      
      <div class="chart-section">
        <h3>价格走势 (Daily Min Price)</h3>
        <div class="chart-container" ref="chartRef"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import LoadingWave from '../components/LoadingWave.vue'
import { fetchItem } from '../api/item'
import { fetchDailyHistory } from '../api/market'
import * as echarts from 'echarts'
import { getItemImage } from '../utils/itemImages'

const route = useRoute()
const router = useRouter()
const item = ref({})
const isLoading = ref(true)
const chartRef = ref(null)
let chartInstance = null
const chartData = ref([])

const displayImage = computed(() => {
  return getItemImage(item.value.name || '')
})

onMounted(async () => {
  const id = route.params.id
  isLoading.value = true
  try {
    const res = await fetchItem(id)
    item.value = res || {}
    
    // Fetch chart data
    await loadChartData(id)
  } catch (error) {
    console.error('Failed to fetch item:', error)
    // Fallback
    item.value = {
      id: id,
      name: id == 1 ? "AK47 | 火蛇" : "未知物品",
      price: 456,
      change: 3.2,
      img: "https://via.placeholder.com/400x300"
    }
  } finally {
    isLoading.value = false
    await nextTick()
    if (chartData.value.length > 0) {
      renderChart(chartData.value)
    }
  }
  
  window.addEventListener('resize', () => {
    chartInstance && chartInstance.resize()
  })
})

const loadChartData = async (itemId) => {
  try {
    const data = await fetchDailyHistory(itemId)
    if (data && data.length > 0) {
      chartData.value = data
    }
  } catch (e) {
    console.error("Failed to load chart data", e)
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartRef.value)
  
  const dates = data.map(item => item.date)
  const prices = data.map(item => item.price)
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#888' } },
      axisLabel: { color: '#888' }
    },
    yAxis: {
      type: 'value',
      scale: true,
      splitLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#888' }
    },
    series: [
      {
        name: 'Min Price',
        type: 'line',
        smooth: true,
        data: prices,
        itemStyle: { color: '#fcd535' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(252, 213, 53, 0.5)' },
            { offset: 1, color: 'rgba(252, 213, 53, 0.0)' }
          ])
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const buyItem = () => {
  router.push({ path: '/trade', query: { itemId: item.value.id, action: 'buy' } })
}

const goBack = () => {
  router.push('/market')
}
</script>

<style scoped>
.container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-card {
  display: flex;
  gap: 40px;
  background: var(--panel);
  padding: 40px;
  border-radius: 12px;
  margin-bottom: 40px;
}

.item-image img {
  width: 400px;
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info h1 {
  font-size: 32px;
  margin-bottom: 10px;
  color: var(--primary);
}

.price {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.change {
  font-size: 18px;
  margin-bottom: 20px;
}

.change.up { color: var(--up); }
.change.down { color: var(--down); }

.description {
  color: var(--text-light);
  line-height: 1.6;
  margin-bottom: 30px;
}

.actions {
  display: flex;
  gap: 20px;
}

.button-secondary {
  background: transparent;
  border: 1px solid var(--text-light);
  color: var(--text);
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.button-secondary:hover {
  border-color: var(--text);
}

.chart-section {
  background: var(--panel);
  padding: 30px;
  border-radius: 12px;
}

.chart-section h3 {
  margin-bottom: 20px;
  color: var(--text);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.chart-container {
  height: 400px;
  width: 100%;
}
</style>
