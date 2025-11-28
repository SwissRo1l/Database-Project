<template>
  <div class="page trade">
    <NavBar />
    <div class="container">
      
      <!-- Top Bar: Item Selection -->
      <div class="top-bar">
        <div class="item-selector">
          <label>当前交易物品:</label>
          <select v-model="selectedItem" @change="handleItemChange">
            <option value="" disabled>请选择物品...</option>
            <option v-for="item in items" :key="item.id" :value="item.id">
              {{ item.name }}
            </option>
          </select>
        </div>
        <div class="user-balance" v-if="userBalance !== null">
          余额: <span>{{ userBalance }} G</span>
        </div>
      </div>

      <div class="trade-layout" v-if="selectedItem">
        
        <!-- Left: Order Book (Vertical Stack) -->
        <div class="trade-panel order-book-panel">
          <h3>🏛️ 盘口 (Order Book)</h3>
          
          <div class="order-book-vertical">
            <div class="order-row header">
              <span>价格 (Price)</span>
              <span>数量 (Qty)</span>
              <span>操作</span>
            </div>

            <!-- Sell Orders (Red) - Reversed so lowest price is at bottom -->
            <div class="order-list sells">
              <div v-for="order in sellOrdersReversed" :key="order.orderId" class="order-row item sell">
                <span class="price">{{ order.price }}</span>
                <span class="qty">{{ order.quantity }}</span>
                <button class="btn-action btn-buy-mini" @click="handleTrade(order)">买入</button>
              </div>
              <div v-if="sellOrders.length === 0" class="empty">暂无卖单</div>
            </div>

            <!-- Current Price / Spread -->
            <div class="spread-bar">
              <span class="current-price">{{ currentPrice || '---' }}</span>
              <span class="spread-label">最新成交价</span>
            </div>

            <!-- Buy Orders (Green) - Highest price at top -->
            <div class="order-list buys">
              <div v-for="order in buyOrders" :key="order.orderId" class="order-row item buy">
                <span class="price">{{ order.price }}</span>
                <span class="qty">{{ order.quantity }}</span>
                <button class="btn-action btn-sell-mini" @click="handleTrade(order)">卖出</button>
              </div>
              <div v-if="buyOrders.length === 0" class="empty">暂无买单</div>
            </div>
          </div>
        </div>

        <!-- Center: Chart & History -->
        <div class="trade-panel chart-panel">
          <h3>📈 价格走势</h3>
          <div class="chart-container" ref="chartRef"></div>
          
          <div class="recent-trades">
             <h4>最新成交</h4>
             <div class="trade-history-list">
               <div class="trade-row header">
                 <span>时间</span>
                 <span>价格</span>
                 <span>数量</span>
               </div>
               <div v-for="trade in recentTrades" :key="trade.time" class="trade-row">
                 <span>{{ formatTime(trade.time) }}</span>
                 <span :class="trade.price >= (trade.prevPrice || trade.price) ? 'green' : 'red'">{{ trade.price }}</span>
                 <span>{{ trade.quantity }}</span>
               </div>
               <div v-if="recentTrades.length === 0" class="empty-history">暂无成交记录</div>
             </div>
          </div>
        </div>

        <!-- Right: Place Limit Order -->
        <div class="trade-panel place-order-panel">
          <h3>📝 下单交易</h3>
          <div class="tabs">
            <button :class="{ active: activeTab === 'buy', 'tab-buy': true }" @click="activeTab = 'buy'">买入 (Buy)</button>
            <button :class="{ active: activeTab === 'sell', 'tab-sell': true }" @click="activeTab = 'sell'">卖出 (Sell)</button>
          </div>

          <div class="order-form">
            <div class="form-group">
              <label>价格 (Price)</label>
              <div class="input-wrapper">
                <input type="number" v-model="formPrice" placeholder="0.00">
                <span class="suffix">G</span>
              </div>
            </div>
            <div class="form-group">
              <label>数量 (Amount)</label>
              <div class="input-wrapper">
                <input type="number" v-model="formAmount" placeholder="0">
                <span class="suffix">个</span>
              </div>
            </div>
            
            <div class="slider-group">
              <!-- Mock slider -->
              <input type="range" min="0" max="100" value="0" class="slider">
            </div>

            <div class="summary">
              <div class="row">
                <span>可用余额:</span>
                <span>{{ userBalance }} G</span>
              </div>
              <div class="row total">
                <span>交易总额:</span>
                <span>{{ (formPrice * formAmount).toFixed(2) }} G</span>
              </div>
            </div>

            <button 
              class="button-primary full-width" 
              :class="activeTab === 'buy' ? 'btn-buy-main' : 'btn-sell-main'"
              @click="executeCreateOrder"
            >
              {{ activeTab === 'buy' ? '买入 (Buy)' : '卖出 (Sell)' }}
            </button>
          </div>
        </div>

      </div>

      <div v-else class="select-prompt">
        <p>请先在上方选择一个物品开始交易</p>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import { fetchListings, executeTrade, fetchTradeHistory } from '../api/market'
import { createOrder } from '../api/trade'
import { fetchProfile } from '../api/user'
import { searchItems } from '../api/item'
import * as echarts from 'echarts'

const route = useRoute()
const selectedItem = ref('')
const items = ref([])
const activeTab = ref('buy')

const sellOrders = ref([])
const buyOrders = ref([])
const userBalance = ref(null)
const currentPrice = ref(null)
const recentTrades = ref([])

const formPrice = ref('')
const formAmount = ref(1)
const chartRef = ref(null)
let chartInstance = null

// Computed property to reverse sell orders for display (Lowest price at bottom)
const sellOrdersReversed = computed(() => {
  return [...sellOrders.value].reverse()
})

// Load initial data
onMounted(async () => {
  await loadItems()
  await loadUserBalance()
  
  if (route.query.itemId) {
    selectedItem.value = Number(route.query.itemId)
    handleItemChange()
  }
  
  window.addEventListener('resize', () => {
    chartInstance && chartInstance.resize()
  })
})

const loadItems = async () => {
  try {
    const res = await searchItems('')
    items.value = res.map(item => ({
      id: item.assetId,
      name: item.assetName
    }))
  } catch (e) {
    console.error(e)
  }
}

const loadUserBalance = async () => {
  const userId = localStorage.getItem('userId')
  if (userId) {
    try {
      const res = await fetchProfile(userId)
      userBalance.value = res.balance
    } catch (e) {
      console.error(e)
    }
  }
}

const handleItemChange = async () => {
  if (!selectedItem.value) return
  
  try {
    const res = await fetchListings({ itemId: selectedItem.value, limit: 100 })
    const allOrders = res || []
    
    // Split orders
    // Sells: Sort ASC (Lowest price first)
    sellOrders.value = allOrders.filter(o => o.type === 'SELL').sort((a, b) => a.price - b.price)
    // Buys: Sort DESC (Highest price first)
    buyOrders.value = allOrders.filter(o => o.type === 'BUY').sort((a, b) => b.price - a.price)
    
    // Determine current price (mid price or last trade)
    if (sellOrders.value.length > 0 && buyOrders.value.length > 0) {
      currentPrice.value = ((sellOrders.value[0].price + buyOrders.value[0].price) / 2).toFixed(2)
    } else if (sellOrders.value.length > 0) {
      currentPrice.value = sellOrders.value[0].price
    } else if (buyOrders.value.length > 0) {
      currentPrice.value = buyOrders.value[0].price
    } else {
      currentPrice.value = '---'
    }

    // Auto-fill price
    if (currentPrice.value !== '---') {
      formPrice.value = currentPrice.value
    }

    // Load History and Chart
    await loadHistoryAndRenderChart()

  } catch (e) {
    console.error(e)
  }
}

const loadHistoryAndRenderChart = async () => {
  try {
    const history = await fetchTradeHistory(selectedItem.value)
    recentTrades.value = history.reverse().slice(0, 20) // Show last 20 trades in list
    
    // Process data for candles
    // Simple aggregation: Group by minute
    const candles = []
    const rawData = history.reverse() // Back to chronological order
    
    if (rawData.length === 0) {
        if (chartInstance) chartInstance.dispose()
        return
    }

    let currentCandle = null
    
    rawData.forEach(trade => {
      const date = new Date(trade.time)
      // Round down to nearest minute
      date.setSeconds(0, 0)
      const timeStr = date.getTime()
      
      if (!currentCandle || currentCandle.time !== timeStr) {
        if (currentCandle) candles.push(currentCandle)
        currentCandle = {
          time: timeStr,
          open: trade.price,
          high: trade.price,
          low: trade.price,
          close: trade.price,
          volume: trade.quantity
        }
      } else {
        currentCandle.high = Math.max(currentCandle.high, trade.price)
        currentCandle.low = Math.min(currentCandle.low, trade.price)
        currentCandle.close = trade.price
        currentCandle.volume += trade.quantity
      }
    })
    if (currentCandle) candles.push(currentCandle)

    renderChart(candles)
  } catch (e) {
    console.error("Failed to load history", e)
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartRef.value)
  
  const dates = data.map(item => new Date(item.time).toLocaleTimeString())
  const values = data.map(item => [item.open, item.close, item.low, item.high])
  
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
      data: dates,
      scale: true,
      boundaryGap: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#848e9c' },
      splitLine: { show: false }
    },
    yAxis: {
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
        itemStyle: {
          color: '#3b82f6'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.5)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.0)' }
          ])
        },
        data: values.map(v => v[1]) // Use Close price
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const formatTime = (isoString) => {
  const date = new Date(isoString)
  return date.toLocaleTimeString()
}

// Refresh data periodically or after actions
const refreshData = () => {
  handleItemChange()
  loadUserBalance()
}

// Taker Action (Trade against existing order)
const handleTrade = async (order) => {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('请先登录')
    return
  }

  const quantity = prompt(`请输入交易数量 (最大 ${order.quantity}):`, "1")
  if (!quantity) return
  
  const qty = parseInt(quantity)
  if (isNaN(qty) || qty <= 0 || qty > order.quantity) {
    alert('无效的数量')
    return
  }

  try {
    await executeTrade({
      orderId: order.orderId,
      userId: userId,
      quantity: qty
    })
    alert('交易成功！')
    refreshData()
  } catch (error) {
    console.error(error)
    const msg = error.response?.data?.message || '交易失败'
    if (msg.includes("Insufficient funds")) {
      alert('余额不足！请充值。')
    } else {
      alert('错误: ' + msg)
    }
  }
}

// Maker Action (Create new limit order)
const executeCreateOrder = async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('请先登录')
    return
  }
  
  if (!formPrice.value || !formAmount.value) {
    alert('请填写价格和数量')
    return
  }

  try {
    await createOrder({
      itemId: selectedItem.value,
      price: Number(formPrice.value),
      amount: Number(formAmount.value),
      type: activeTab.value.toUpperCase(),
      userId: userId // Pass userId explicitly if backend requires it
    })
    alert('挂单发布成功！')
    formPrice.value = ''
    formAmount.value = 1
    refreshData()
  } catch (error) {
    console.error(error)
    alert('发布失败: ' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--panel);
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.item-selector select {
  margin-left: 10px;
  padding: 8px;
  border-radius: 4px;
  background: #2b3139;
  color: white;
  border: 1px solid #444;
  min-width: 200px;
}

.user-balance span {
  color: #fcd535;
  font-weight: bold;
  font-size: 16px;
}

/* 3-Column Layout */
.trade-layout {
  display: grid;
  grid-template-columns: 300px 1fr 320px;
  gap: 10px;
  height: 600px;
}

.trade-panel {
  background: var(--panel);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.trade-panel h3 {
  font-size: 16px;
  padding: 15px;
  margin: 0;
  border-bottom: 1px solid #2b3139;
  background: #1e2329;
}

/* Order Book Styles */
.order-book-vertical {
  flex: 1;
  display: flex;
  flex-direction: column;
  font-size: 12px;
  overflow-y: auto;
}

.order-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  padding: 4px 10px;
  align-items: center;
  cursor: pointer;
}

.order-row.header {
  color: #848e9c;
  padding: 10px;
  font-size: 12px;
}

.order-row.item:hover {
  background: #2b3139;
}

.sell .price { color: #f6465d; }
.buy .price { color: #0ecb81; }
.qty { color: #eaecef; text-align: right; }

.spread-bar {
  padding: 10px;
  text-align: center;
  border-top: 1px solid #2b3139;
  border-bottom: 1px solid #2b3139;
  margin: 5px 0;
}

.current-price {
  font-size: 20px;
  font-weight: bold;
  color: #eaecef;
  margin-right: 10px;
}

.spread-label {
  font-size: 12px;
  color: #848e9c;
}

.btn-action {
  border: none;
  border-radius: 2px;
  padding: 2px 6px;
  font-size: 10px;
  cursor: pointer;
  justify-self: end;
}
.btn-buy-mini { background: #f6465d; color: white; }
.btn-sell-mini { background: #0ecb81; color: black; }

/* Chart Panel */
.chart-panel {
  position: relative;
}

.chart-container {
  flex: 1;
  min-height: 300px;
  background: #161a1e;
}

.recent-trades {
  height: 200px;
  border-top: 1px solid #2b3139;
  padding: 10px;
  overflow-y: auto;
}

.recent-trades h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #eaecef;
}

.trade-history-list {
  font-size: 12px;
}

.trade-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  padding: 2px 0;
  color: #848e9c;
}
.trade-row .green { color: #0ecb81; }
.trade-row .red { color: #f6465d; }

.empty-history {
  text-align: center;
  padding: 20px;
  color: #848e9c;
}

/* Place Order Panel */
.place-order-panel {
  padding: 0;
}

.tabs {
  display: flex;
}

.tabs button {
  flex: 1;
  padding: 15px;
  background: #1e2329;
  border: none;
  color: #848e9c;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
}

.tabs button.active {
  color: white;
}

.tabs button.tab-buy.active { background: #0ecb81; }
.tabs button.tab-sell.active { background: #f6465d; }

.order-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #848e9c;
}

.input-wrapper {
  display: flex;
  background: #2b3139;
  border-radius: 4px;
  border: 1px solid #444;
  align-items: center;
  padding-right: 10px;
}

.input-wrapper input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 10px;
  color: white;
  outline: none;
}

.suffix {
  color: #848e9c;
  font-size: 12px;
}

.slider-group {
  margin: 20px 0;
}

.slider {
  width: 100%;
}

.summary {
  margin-bottom: 20px;
  font-size: 12px;
  color: #848e9c;
}

.summary .row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.summary .total span:last-child {
  font-size: 16px;
  color: white;
  font-weight: bold;
}

.btn-buy-main { background: #0ecb81; color: white; border: none; }
.btn-sell-main { background: #f6465d; color: white; border: none; }

.full-width {
  width: 100%;
  padding: 12px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

@media (max-width: 1024px) {
  .trade-layout {
    grid-template-columns: 1fr;
    height: auto;
  }
  .trade-panel {
    height: auto;
    min-height: 400px;
  }
}
</style>
