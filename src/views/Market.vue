<template>
  <div class="market-page">
    <NavBar />

    <!-- Hero Carousel -->
    <div class="hero-carousel">
      <div 
        class="carousel-slide" 
        v-for="(slide, index) in slides" 
        :key="index"
        :class="{ active: currentSlide === index }"
        :style="{ backgroundImage: `url(${slide.image})` }"
      >
        <div class="hero-overlay"></div>
        <div class="hero-content">
          <h1 class="hero-title">{{ slide.title }}</h1>
          <p class="hero-subtitle">{{ slide.subtitle }}</p>
          <div class="hero-stats" v-if="index === 0">
            <div class="stat-item">
              <span class="stat-value">24k+</span>
              <span class="stat-label">在线物品</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">1.2M</span>
              <span class="stat-label">日交易额</span>
            </div>
          </div>
          <button class="button-primary hero-btn" v-if="slide.action" @click="scrollToGrid">
            {{ slide.action }}
          </button>
        </div>
      </div>

      <!-- Indicators -->
      <div class="carousel-indicators">
        <span 
          v-for="(slide, index) in slides" 
          :key="index"
          class="indicator"
          :class="{ active: currentSlide === index }"
          @click="setSlide(index)"
        ></span>
      </div>
    </div>

    <div class="container" id="market-grid">
      <!-- Search Header -->
      <div v-if="isSearching" class="search-header">
        <h2>搜索结果: "{{ route.query.q }}"</h2>
        <button class="text-btn" @click="router.push('/market')">清除搜索</button>
      </div>

      <!-- Tabs -->
      <div v-else class="market-tabs">
        <div 
          class="tab-item" 
          :class="{ active: currentTab === 'hot' }"
          @click="currentTab = 'hot'"
        >
          🔥 热门推荐
        </div>
        <div 
          class="tab-item" 
          :class="{ active: currentTab === 'gainers' }"
          @click="currentTab = 'gainers'"
        >
          📈 24h 涨幅榜
        </div>
        <div 
          class="tab-item" 
          :class="{ active: currentTab === 'all' }"
          @click="currentTab = 'all'"
        >
          📦 全部商品
        </div>
      </div>

      <!-- Filter Bar (Only for All Items) -->
      <div v-if="currentTab === 'all' && !isSearching" class="filter-bar">
        <div class="filter-group">
          <label>排序:</label>
          <select v-model="filterSort">
            <option value="default">默认</option>
            <option value="price_asc">价格: 低到高</option>
            <option value="price_desc">价格: 高到低</option>
            <option value="newest">最新上架</option>
          </select>
        </div>
        <div class="filter-group">
          <label>分类:</label>
          <select v-model="filterCategory">
            <option value="All">全部</option>
            <option value="Rifle">步枪 (Rifle)</option>
            <option value="Sniper">狙击枪 (Sniper)</option>
            <option value="Pistol">手枪 (Pistol)</option>
            <option value="Knife">刀具 (Knife)</option>
          </select>
        </div>
      </div>

      <!-- Item Grid -->
      <div v-if="isLoading" class="loading-state">
        <LoadingWave />
      </div>
      <div v-else class="item-grid">
        <ItemCard 
          v-for="item in displayItems" 
          :key="item.id" 
          :item="item" 
        />
      </div>

      <!-- Pagination (Only for All Items tab) -->
      <div class="pagination" v-if="currentTab === 'all' && !isLoading && totalPages > 1">
        <button 
          :disabled="currentPage === 0" 
          @click="changePage(currentPage - 1)"
          class="page-btn"
        >
          上一页
        </button>
        <span class="page-info">第 {{ currentPage + 1 }} 页 / 共 {{ totalPages }} 页</span>
        <button 
          :disabled="currentPage >= totalPages - 1" 
          @click="changePage(currentPage + 1)"
          class="page-btn"
        >
          下一页
        </button>
      </div>
      
      <!-- Empty State -->
      <div v-if="!isLoading && displayItems.length === 0" class="empty-state">
        暂无数据
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from "../components/NavBar.vue"
import ItemCard from "../components/ItemCard.vue"
import LoadingWave from "../components/LoadingWave.vue"
import { fetchListings } from "../api/market"
import { normalizeItemFields, getSales24, getChangeVal } from '../utils/marketHelpers'

const route = useRoute()
const router = useRouter()
const hotItems = ref([])
const topGainers = ref([])
const allItems = ref([])
const searchResults = ref([])
const isSearching = ref(false)
const isLoading = ref(false)
const currentTab = ref('hot')

// Pagination
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 12

const filterSort = ref('default')
const filterCategory = ref('All')

// Carousel State
const currentSlide = ref(0)
const slides = [
  {
    title: "游戏资产交易市场",
    subtitle: "发现、购买和出售稀有游戏道具，把握市场脉搏",
    image: "https://images.unsplash.com/photo-1542751371-adc38448a05e?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80",
    action: "立即探索"
  },
  {
    title: "今日热门装备",
    subtitle: "查看全服最受关注的稀有饰品，即刻入手",
    image: "https://images.unsplash.com/photo-1552820728-8b83bb6b773f?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80",
    action: "查看热门"
  },
  {
    title: "涨幅榜首",
    subtitle: "追踪市场动态，发现最具投资潜力的资产",
    image: "https://images.unsplash.com/photo-1611974765270-ca1258634369?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80",
    action: "查看涨幅"
  }
]

let slideInterval = null

const startSlideShow = () => {
  slideInterval = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % slides.length
  }, 5000)
}

const stopSlideShow = () => {
  if (slideInterval) clearInterval(slideInterval)
}

const setSlide = (index) => {
  stopSlideShow()
  currentSlide.value = index
  startSlideShow()
}

const scrollToGrid = () => {
  document.getElementById('market-grid').scrollIntoView({ behavior: 'smooth' })
}

const displayItems = computed(() => {
  if (isSearching.value) {
    return searchResults.value
  }
  if (currentTab.value === 'all') {
    return allItems.value
  }
  return currentTab.value === 'hot' ? hotItems.value : topGainers.value
})

const fetchAllItems = async (page = 0) => {
  isLoading.value = true
  try {
    const params = { 
      page: page,
      size: pageSize
    }
    if (filterSort.value !== 'default') {
      params.sort = filterSort.value
    }
    if (filterCategory.value !== 'All') {
      params.category = filterCategory.value
    }
    const res = await fetchListings(params)
    
    if (res && res.content) {
      // Normalize fields on each item
      let pageItems = res.content.map(i => normalizeItemFields(i))

      // For each item on this page, ensure it has at least one SELL order in the market (match trade page behavior)
      try {
        const checks = await Promise.all(pageItems.map(async it => {
          try {
            const orders = await fetchListings({ itemId: it.id, limit: 1 })
            // orders could be an array of orders or an object with content
            if (!orders) return false
            if (Array.isArray(orders)) {
              return orders.some(o => o && (o.type === 'SELL' || o.type === 'sell'))
            }
            if (orders.content && Array.isArray(orders.content)) {
              return orders.content.some(o => o && (o.type === 'SELL' || o.type === 'sell'))
            }
            return false
          } catch (e) {
            return false
          }
        }))

        pageItems = pageItems.filter((it, idx) => checks[idx])
      } catch (e) {
        console.warn('Failed to verify sell orders for page items', e)
      }

      allItems.value = pageItems
      totalPages.value = res.totalPages
      currentPage.value = res.number
    } else {
      // Fallback for non-paged response
      allItems.value = (res || []).map(i => normalizeItemFields(i))
      totalPages.value = 1
      currentPage.value = 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    isLoading.value = false
  }
}

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    fetchAllItems(page)
    scrollToGrid()
  }
}

watch([filterSort, filterCategory], () => {
  if (currentTab.value === 'all') {
    fetchAllItems(0)
  }
})

watch(currentTab, (newTab) => {
  if (newTab === 'all' && allItems.value.length === 0) {
    fetchAllItems()
  }
})

const performSearch = async (query) => {
  if (!query) {
    isSearching.value = false
    return
  }
  isSearching.value = true
  try {
    const res = await fetchListings({ keyword: query })
    searchResults.value = res || []
    // Scroll to grid when searching
    setTimeout(scrollToGrid, 100)
  } catch (e) {
    console.error(e)
  }
}

watch(() => route.query.q, (newQuery) => {
  performSearch(newQuery)
}, { immediate: true })

const loadData = async () => {
  try {
    // 获取热门商品
    const hotRes = await fetchListings({ sort: 'hot', limit: 8 })
    // Prefer server ordering but enforce client-side fallback sorting by 24h sales (desc)
    hotItems.value = hotRes || []
    const getSalesCount = (it) => {
      // try several possible fields that backend might use
      return Number(it?.sales24 ?? it?.sales ?? it?.volume ?? it?.tradeCount ?? it?.transactions ?? it?.sold ?? 0) || 0
    }
    if (hotItems.value && hotItems.value.length > 1) {
      hotItems.value = [...hotItems.value].sort((a, b) => getSalesCount(b) - getSalesCount(a))
    }

    // 获取涨幅榜
    const gainersRes = await fetchListings({ sort: 'gainers', limit: 8 })
    topGainers.value = gainersRes || []
    const getChangeVal = (it) => {
      // support multiple possible property names for percent change
      return Number(it?.change ?? it?.changePercent ?? it?.percentChange ?? it?.priceChangePercent ?? 0) || 0
    }
    if (topGainers.value && topGainers.value.length > 1) {
      topGainers.value = [...topGainers.value].sort((a, b) => getChangeVal(b) - getChangeVal(a))
    }
    // Normalize change values: if same item appears in both lists, prefer change from gainers dataset
    try {
      const changeMap = new Map()
      topGainers.value.forEach(it => {
        if (it && it.id !== undefined) changeMap.set(String(it.id), getChangeVal(it))
      })
      hotItems.value = hotItems.value.map(it => {
        if (!it || it.id === undefined) return it
        const mapped = { ...it }
        const mappedChange = changeMap.get(String(it.id))
        if (mappedChange !== undefined && !Number.isNaN(mappedChange)) {
          mapped.change = mappedChange
        }
        return mapped
      })
    } catch (e) {
      // ignore normalization errors
      console.warn('Failed to normalize change values between hot and gainers', e)
    }
  } catch (error) {
    console.error('Failed to load market data:', error)
    // Fallback to mock data if API fails
    hotItems.value = [
      { id: 1, name: "AK47 | 火蛇", price: 45600, change: 3.2, img: "https://via.placeholder.com/300x200" },
      { id: 2, name: "AWP | 二西莫夫", price: 82000, change: -1.5, img: "https://via.placeholder.com/300x200" },
      { id: 5, name: "Karambit | 渐变之色", price: 125000, change: 5.4, img: "https://via.placeholder.com/300x200" },
      { id: 6, name: "M9 刺刀 | 虎牙", price: 98000, change: 1.2, img: "https://via.placeholder.com/300x200" }
    ]
    topGainers.value = [
      { id: 3, name: "M4A1 | 消音版", price: 28000, change: -1.1, img: "https://via.placeholder.com/300x200" },
      { id: 4, name: "Ak47 | 红线", price: 39000, change: 2.8, img: "https://via.placeholder.com/300x200" },
      { id: 7, name: "USP-S | 枪响人亡", price: 15000, change: 8.5, img: "https://via.placeholder.com/300x200" },
      { id: 8, name: "Glock-18 | 水灵", price: 5600, change: 12.3, img: "https://via.placeholder.com/300x200" }
    ]
  }
}

onMounted(() => {
  loadData()
  startSlideShow()
})

onUnmounted(() => {
  stopSlideShow()
})
</script>

<style scoped>
.market-page {
  min-height: 100vh;
  /* background-color: var(--bg); Removed to show global dynamic background */
}

/* Hero Carousel */
.hero-carousel {
  position: relative;
  height: 400px;
  overflow: hidden;
  margin-bottom: 40px;
}

.carousel-slide {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  opacity: 0;
  transition: opacity 1s ease-in-out;
  z-index: 1;
}

.carousel-slide.active {
  opacity: 1;
  z-index: 2;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  background: linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.8));
}

.hero-content {
  position: relative;
  z-index: 3;
  color: #fff;
  padding: 0 20px;
  max-width: 800px;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  margin-bottom: 15px;
  text-shadow: 0 4px 8px rgba(0,0,0,0.6);
  letter-spacing: 1px;
}

.hero-subtitle {
  font-size: 1.4rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 30px;
  font-weight: 300;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
  margin-top: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: var(--primary);
  text-shadow: 0 0 10px rgba(61, 174, 252, 0.4);
}

.stat-label {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 5px;
}

.hero-btn {
  font-size: 1.1rem;
  padding: 12px 30px;
  margin-top: 20px;
  box-shadow: 0 4px 15px rgba(61, 174, 252, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;
}

.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(61, 174, 252, 0.6);
}

/* Indicators */
.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 10;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s;
}

.indicator.active {
  background-color: var(--primary);
  transform: scale(1.2);
}

/* Container */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}


/* Container */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* Tabs */
.market-tabs {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tab-item {
  padding: 15px 0;
  font-size: 1.1rem;
  color: var(--text-light);
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
}

.tab-item:hover {
  color: var(--text);
}

.tab-item.active {
  color: var(--primary);
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: var(--primary);
  box-shadow: 0 -2px 10px rgba(61, 174, 252, 0.5);
}

/* Grid */
.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 25px;
}

.empty-state {
  text-align: center;
  padding: 50px;
  color: var(--text-light);
  font-size: 1.2rem;
}

.loading-state {
  text-align: center;
  padding: 50px;
  color: var(--primary);
  font-size: 1.2rem;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }
  .item-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 15px;
  }
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.text-btn {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  font-size: 0.9rem;
}

.text-btn:hover {
  color: var(--primary);
  text-decoration: underline;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.05);
  padding: 15px;
  border-radius: 8px;
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-group label {
  color: var(--text-light);
  font-size: 0.9rem;
}

.filter-group select {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--text);
  padding: 8px 12px;
  border-radius: 4px;
  outline: none;
  cursor: pointer;
}

.filter-group select:focus {
  border-color: var(--primary);
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  gap: 20px;
}

.page-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--text);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:not(:disabled):hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--primary);
}

.page-info {
  color: var(--text-light);
  font-size: 0.9rem;
}
</style>

