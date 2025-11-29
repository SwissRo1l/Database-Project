<template>
  <div class="page history-page">
    <NavBar></NavBar>
    <div class="container">
      <div class="header">
        <h2>全部交易记录</h2>
        <router-link to="/profile" class="back-link">返回个人中心</router-link>
      </div>

      <LoadingWave v-if="isLoading"></LoadingWave>

      <div v-else>
        <div class="history-list">
          <div class="history-item" v-for="record in history" :key="record.id">
            <div class="icon" :class="record.type === 'buy' ? 'buy' : 'sell'">
              {{ record.type === 'buy' ? '买' : '卖' }}
            </div>
            <div class="details">
              <p class="name">{{ record.itemName }}</p>
              <p class="date">{{ record.date }}</p>
            </div>
            <div class="amount" :class="record.type === 'buy' ? 'negative' : 'positive'">
              {{ record.type === 'buy' ? '-' : '+' }}{{ Math.abs(record.price) }} G
            </div>
          </div>
          <div v-if="history.length === 0" class="empty-state">暂无交易记录</div>
        </div>

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
import { ref, onMounted } from 'vue'
import NavBar from '../components/NavBar.vue'
import LoadingWave from '../components/LoadingWave.vue'
import { fetchOrders } from '../api/trade'

const history = ref([])
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 10
const isLoading = ref(false)

const loadHistory = async (page) => {
  const userId = localStorage.getItem('userId')
  if (!userId) return

  isLoading.value = true
  try {
    const res = await fetchOrders({ userId, page, size: pageSize })
    if (res && res.content) {
      history.value = res.content
      totalPages.value = res.totalPages
      currentPage.value = res.number
    } else {
      history.value = []
    }
  } catch (e) {
    console.error(e)
  } finally {
    isLoading.value = false
  }
}

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    loadHistory(page)
  }
}

onMounted(() => {
  loadHistory(0)
})
</script>

<style scoped>
.container {
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.back-link {
  color: var(--text-light);
  text-decoration: none;
}

.back-link:hover {
  color: var(--primary);
}

.history-list {
  background: var(--panel);
  border-radius: 12px;
  padding: 20px;
  min-height: 400px;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.history-item:last-child {
  border-bottom: none;
}

.icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-weight: bold;
}

.icon.buy { background: rgba(76, 175, 80, 0.2); color: var(--up); }
.icon.sell { background: rgba(255, 77, 77, 0.2); color: var(--down); }

.details {
  flex: 1;
}

.details .name { font-weight: bold; }
.details .date { font-size: 12px; color: var(--text-light); }

.amount.negative { color: var(--up); }
.amount.positive { color: var(--down); }

.empty-state {
  text-align: center;
  color: var(--text-light);
  padding: 40px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 20px;
}

.page-btn {
  background: var(--panel);
  border: 1px solid rgba(255,255,255,0.1);
  color: var(--text);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:not(:disabled):hover {
  background: rgba(255,255,255,0.1);
}

.page-info {
  color: var(--text-light);
}
</style>
