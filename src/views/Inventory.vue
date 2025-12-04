<template>
  <div class="page inventory">
    <NavBar></NavBar>
    <div class="container">
      <div class="header">
        <h2>我的库存</h2>
        <div class="stats">
          <div class="stat-item">
            <span class="label">总价值 (估算)</span>
            <span class="value">{{ totalValue }} G</span>
          </div>
          <div class="stat-item">
            <span class="label">物品数量</span>
            <span class="value">{{ itemCount }}</span>
          </div>
        </div>
      </div>

      <div v-if="isLoading" class="loading-container">
        <LoadingWave></LoadingWave>
      </div>
      <div v-else>
        <div class="inventory-grid">
          <div class="item-card" v-for="item in paginatedItems" :key="item.id">
            <div class="card-image">
              <img :src="getItemImage(item.name)" alt="Item" />
            </div>
            <div class="card-info">
              <h4>{{ item.name }}</h4>
              <p class="rarity">稀有度: {{ item.rarity || '普通' }}</p>
              
              <div class="item-details">
                <div class="detail-row">
                  <span class="label">购买日期:</span>
                  <span class="val">{{ formatDate(item.purchaseDate) }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">市场最低价:</span>
                  <span class="val price">{{ item.price }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">拥有数量:</span>
                  <span class="val">{{ item.quantity }}</span>
                </div>
                <div class="detail-row" v-if="item.reserved > 0">
                  <span class="label">挂单中:</span>
                  <span class="val reserved">{{ item.reserved }}</span>
                </div>
              </div>

              <div class="card-actions">
                <button class="sell-btn" @click="goToTrade(item)">出售</button>
                <button class="inspect-btn" @click="goToDetail(item)">查看</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination" v-if="totalPages > 1">
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import LoadingWave from '../components/LoadingWave.vue'
import { fetchInventory } from '../api/user'
import { getItemImage } from '../utils/itemImages'

const router = useRouter()
const inventoryItems = ref([])
const totalValue = ref(0)
const itemCount = ref(0)
const currentPage = ref(0)
const pageSize = 12
const isLoading = ref(true)

const totalPages = computed(() => Math.ceil(inventoryItems.value.length / pageSize))

const paginatedItems = computed(() => {
  const start = currentPage.value * pageSize
  const end = start + pageSize
  return inventoryItems.value.slice(start, end)
})

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const formatDate = (dateStr) => {
  if (!dateStr || dateStr === '未知') return '未知'
  try {
    return new Date(dateStr).toLocaleDateString()
  } catch (e) {
    return dateStr
  }
}

const goToTrade = (item) => {
  router.push({ name: 'Trade', query: { itemId: item.id, type: 'sell' } })
}

const goToDetail = (item) => {
  router.push({ name: 'ItemDetail', params: { id: item.id } })
}

onMounted(async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('请先登录')
    return
  }

  isLoading.value = true
  try {
    const res = await fetchInventory(userId)
    // Filter out items with 0 quantity
    inventoryItems.value = (res || []).filter(item => item.quantity > 0)
    
    // Calculate stats
    itemCount.value = inventoryItems.value.reduce((sum, item) => sum + (item.quantity || 0), 0)
    totalValue.value = inventoryItems.value.reduce((sum, item) => {
      const price = typeof item.price === 'number' ? item.price : 0
      return sum + (price * (item.quantity || 0))
    }, 0)
  } catch (e) {
    console.error(e)
  } finally {
    isLoading.value = false
  }
})
</script>

<style scoped>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.stat-item .label {
  font-size: 12px;
  color: var(--text-light);
}

.stat-item .value {
  font-size: 20px;
  color: var(--primary);
  font-weight: bold;
}

.inventory-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.item-card {
  background: var(--panel);
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}

.item-card:hover {
  transform: translateY(-5px);
}

.card-image img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.card-info {
  padding: 15px;
}

.card-info h4 {
  margin-bottom: 5px;
  font-size: 16px;
}

.rarity {
  font-size: 12px;
  color: #ffd700; /* Gold for legendary */
  margin-bottom: 10px;
}

.item-details {
  margin-bottom: 15px;
  font-size: 12px;
  color: var(--text-light);
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  gap: 20px;
}

.page-btn {
  background: var(--panel);
  border: 1px solid rgba(255,255,255,0.1);
  color: var(--text);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:not(:disabled):hover {
  background: rgba(255,255,255,0.1);
  border-color: var(--primary);
  color: var(--primary);
}

.page-info {
  color: var(--text-light);
  font-size: 0.9rem;
}

.detail-row .price {
  color: #4dff4d;
}

.detail-row .reserved {
  color: #f6465d;
  font-weight: bold;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.card-actions button {
  flex: 1;
  padding: 6px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  border: none;
}

.sell-btn {
  background: var(--primary);
  color: white;
}

.inspect-btn {
  background: rgba(255,255,255,0.1);
  color: var(--text);
}
</style>
