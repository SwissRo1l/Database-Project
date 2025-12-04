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
        <h3>价格走势</h3>
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
import { fetchDailyHistory, fetchHourlyHistory, fetchTradeHistory } from '../api/market'
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
    const history = await fetchTradeHistory(itemId)
    if (history && history.length > 0) {
      const chrono = [...history].sort((a, b) => new Date(a.time) - new Date(b.time))

      const buckets = []
      let current = null

      chrono.forEach(trade => {
        const ts = new Date(trade.time)
        if (Number.isNaN(ts.getTime())) {
          return
        }
        ts.setSeconds(0, 0)
        const key = ts.getTime()

        if (!current || current.time !== key) {
          if (current) buckets.push(current)
          current = { time: key, price: Number(trade.price) }
        } else {
          current.price = Number(trade.price)
        }
      })
      if (current) buckets.push(current)

      chartData.value = buckets
      return
    }

    const hourly = await fetchHourlyHistory(itemId, 168)
    if (hourly && hourly.length > 0) {
      chartData.value = hourly
        .map(entry => ({
          time: new Date(entry.time).getTime(),
          price: Number(entry.price)
        }))
        .filter(point => !Number.isNaN(point.time))
        .sort((a, b) => a.time - b.time)
      return
    }

    const daily = await fetchDailyHistory(itemId)
    if (daily && daily.length > 0) {
      chartData.value = daily
        .map(entry => ({
          time: new Date(`${entry.date}T00:00:00Z`).getTime(),
          price: Number(entry.price)
        }))
        .filter(point => !Number.isNaN(point.time))
        .sort((a, b) => a.time - b.time)
    }
  } catch (e) {
    console.error('Failed to load chart data', e)
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartRef.value)
  
  const orderedPoints = data
    .map(point => {
      const timestamp = typeof point.time === 'number' ? point.time : new Date(point.time).getTime()
      if (Number.isNaN(timestamp)) return null
      return { timestamp, price: Number(point.price) }
    })
    .filter(Boolean)
    .sort((a, b) => a.timestamp - b.timestamp)

  if (orderedPoints.length === 0) {
    return
  }

  const labels = orderedPoints.map(point => {
    const date = new Date(point.timestamp)
    return Number.isNaN(date.getTime()) ? '' : date.toLocaleTimeString()
  })
  const prices = orderedPoints.map(point => point.price)
  
  const option = {
    backgroundColor: '#161a1e',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: labels,
      scale: true,
      boundaryGap: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#848e9c' },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      scale: true,
      splitLine: {
        show: true,
        lineStyle: { color: '#2b3139' }
      },
      axisLabel: { color: '#848e9c' }
    },
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      }
    ],
    series: [
      {
        name: 'Price',
        type: 'line',
        smooth: true,
        symbol: 'none',
        sampling: 'average',
        data: prices,
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.5)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.0)' }
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
