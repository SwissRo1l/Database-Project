<template>
  <div class="page trade">
    <NavBar />
    <div class="container">
      <div class="trade-panel">
        <div class="tabs">
          <button 
            :class="{ active: activeTab === 'buy' }" 
            @click="activeTab = 'buy'"
          >
            购买
          </button>
          <button 
            :class="{ active: activeTab === 'sell' }" 
            @click="activeTab = 'sell'"
          >
            出售
          </button>
        </div>

        <div class="trade-content">
          <div class="form-group">
            <label>选择物品</label>
            <div class="custom-select" ref="dropdownRef">
              <button class="select-toggle" @click.stop="toggleDropdown">
                <span v-if="selectedItemName">{{ selectedItemName }}</span>
                <span v-else class="placeholder">请选择...</span>
                <span class="arrow">▾</span>
              </button>

              <ul v-show="showDropdown" class="select-list" @click.stop>
                <li class="select-placeholder" @click="clearSelection">
                  请选择...
                </li>
                <li v-for="item in items" :key="item.id"
                    :class="{ 'is-selected': item.id === selectedItem }
                    "
                    @click="selectItem(item.id)">
                  {{ item.name }}
                </li>
              </ul>
            </div>
          </div>

          <div class="form-group">
            <label>价格 (G)</label>
            <input type="number" v-model="price" placeholder="0.00">
          </div>

          <div class="form-group">
            <label>数量</label>
            <input type="number" v-model="amount" placeholder="1">
          </div>

          <div class="summary">
            <p>总计: <span>{{ total }} G</span></p>
          </div>

          <button class="button-primary full-width" @click="executeTrade">
            {{ activeTab === 'buy' ? '确认购买' : '确认出售' }}
          </button>
        </div>
      </div>

      <div class="order-book">
        <PriceChart v-if="selectedItem" :base-price="currentPrice" class="mb-4" />
        <h3>市场挂单</h3>
        <div class="book-header">
          <span>价格</span>
          <span>数量</span>
          <span>时间</span>
        </div>
        <div class="book-list">
          <div class="book-item" v-for="order in marketOrders" :key="order.id">
            <span class="text-up">{{ order.price.toFixed(2) }}</span>
            <span>1</span>
            <span class="time">刚刚</span>
          </div>
          <div v-if="marketOrders.length === 0" class="no-data">
            暂无挂单
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import PriceChart from '../components/PriceChart.vue'
import { fetchListings } from '../api/market'
import { createOrder } from '../api/trade'

const route = useRoute()
const activeTab = ref('buy')
const selectedItem = ref('')
const price = ref('')
const amount = ref(1)
const items = ref([])
const marketOrders = ref([])
const currentPrice = ref(1000)

const fetchMarketOrders = async () => {
  if (!selectedItem.value) return
  
  try {
    const res = await fetchListings({ itemId: selectedItem.value, limit: 20 })
    marketOrders.value = res || []
    
    // Update current price based on the lowest sell order or item base price
    if (marketOrders.value.length > 0) {
      // Assuming listings are sorted by price ascending (cheapest first)
      // If not, we should sort them or pick the first one
      currentPrice.value = marketOrders.value[0].price
      price.value = currentPrice.value // Auto-fill price
    } else {
      // Fallback to item base price if available in items list
      const item = items.value.find(i => i.id === selectedItem.value)
      if (item) {
        currentPrice.value = item.price
        price.value = item.price
      }
    }
  } catch (e) {
    console.error('Failed to fetch market orders:', e)
  }
}

watch(selectedItem, () => {
  fetchMarketOrders()
})

// Custom dropdown state & helpers
const showDropdown = ref(false)
const dropdownRef = ref(null)
const selectedItemName = computed(() => {
  const itm = items.value.find(i => i.id === selectedItem.value)
  return itm ? itm.name : ''
})

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const selectItem = (id) => {
  selectedItem.value = id
  showDropdown.value = false
}

const clearSelection = () => {
  selectedItem.value = ''
  showDropdown.value = false
}

const handleDocClick = (e) => {
  if (!dropdownRef.value) return
  if (!dropdownRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleDocClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocClick)
})

onMounted(async () => {
  // Load items for dropdown
  try {
    const res = await fetchListings({ limit: 100 })
    // Deduplicate items by ID
    const uniqueItems = []
    const seenIds = new Set()
    for (const item of res) {
      if (!seenIds.has(item.id)) {
        seenIds.add(item.id)
        uniqueItems.push(item)
      }
    }
    items.value = uniqueItems
  } catch (e) {
    console.error(e)
    items.value = [
      { id: 1, name: 'AK47 | 火蛇', price: 45600 },
      { id: 2, name: 'M4A4 | 咆哮', price: 120000 },
      { id: 3, name: 'AWP | 巨龙传说', price: 850000 }
    ]
  }

  // Pre-select item if passed in query
  if (route.query.itemId) {
    selectedItem.value = Number(route.query.itemId)
  }
  if (route.query.action) {
    activeTab.value = route.query.action
  }
})

const total = computed(() => {
  return (Number(price.value) * Number(amount.value)).toFixed(2)
})

const executeTrade = async () => {
  if (!selectedItem.value || !price.value || !amount.value) {
    alert('请填写完整信息')
    return
  }

  try {
    await createOrder({
      itemId: selectedItem.value,
      price: Number(price.value),
      amount: Number(amount.value),
      type: activeTab.value
    })
    alert(`${activeTab.value === 'buy' ? '购买' : '出售'} 订单已提交`)
  } catch (error) {
    console.error(error)
    alert('交易失败: ' + (error.message || '未知错误'))
  }
}
</script>

<style scoped>
.container {
  display: flex;
  gap: 20px;
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.trade-panel, .order-book {
  background: var(--panel);
  border-radius: 12px;
  padding: 20px;
}

.trade-panel {
  flex: 1;
}

.order-book {
  flex: 1;
}

.tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.tabs button {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-light);
  padding: 15px;
  cursor: pointer;
  font-size: 16px;
  border-bottom: 2px solid transparent;
}

.tabs button.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-light);
}

.form-group input, .form-group select {
  width: 100%;
  padding: 12px;
  background: rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 6px;
  color: var(--text);
}

/* Make dropdown options readable and style the selected option */
.form-group select option {
  color: var(--text);
  background: var(--panel);
}

/* When option is selected in the native dropdown, give it a darker background */
.form-group select option:checked {
  background: rgba(0,0,0,0.6);
  color: var(--text-light);
}

/* Hover state in dropdown (may be honored by some browsers) */
.form-group select option:hover {
  background: rgba(255,255,255,0.03);
}

/* Custom select styles */
.custom-select {
  position: relative;
}
.select-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 12px;
  background: rgba(0,0,0,0.2); /* restore original dark background */
  border: 1px solid rgba(255,255,255,0.1); /* original border */
  border-radius: 6px;
  color: var(--text);
  cursor: pointer;
}
.select-toggle .placeholder { color: rgba(255,255,255,0.4); }
.select-toggle .arrow { color: rgba(255,255,255,0.6); }
.select-list {
  position: absolute;
  left: 0;
  right: 0;
  max-height: 320px;
  overflow: auto;
  background: #ffffff; /* white dropdown background */
  border: 1px solid rgba(0,0,0,0.08);
  margin-top: 8px;
  border-radius: 6px;
  z-index: 50;
  box-shadow: 0 6px 18px rgba(0,0,0,0.25);
}
.select-list li {
  padding: 12px;
  color: #111; /* dark text for items */
  cursor: pointer;
}
.select-list li.is-selected {
  background: #000000; /* black for selected item */
  color: #ffffff; /* white text when selected */
}
.select-list li.select-placeholder { color: rgba(0,0,0,0.45); font-style: italic; }
.select-list li:hover { background: rgba(0,0,0,0.06); }

.summary {
  margin: 20px 0;
  text-align: right;
  font-size: 18px;
}

.summary span {
  color: var(--primary);
  font-weight: bold;
}

.full-width {
  width: 100%;
  padding: 15px;
  font-size: 16px;
}

.book-header, .book-item {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  padding: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.book-header {
  color: var(--text-light);
  font-size: 14px;
}

.text-up { color: var(--up); }
.text-down { color: var(--down); }
.time { color: var(--text-light); }

.mb-4 {
  margin-bottom: 20px;
}

.no-data {
  padding: 20px;
  text-align: center;
  color: var(--text-light);
}
</style>
