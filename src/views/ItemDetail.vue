<template>
  <div class="page item-detail">
    <NavBar />
    <div class="container">
      <div class="detail-card">
        <div class="item-image">
          <img :src="item.img || 'https://via.placeholder.com/400x300'" alt="Item Image">
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
        <div class="chart-placeholder">
          <!-- 这里可以放 ECharts 图表 -->
          <p>Price History Chart Placeholder</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import { fetchItem } from '../api/item'

const route = useRoute()
const router = useRouter()
const item = ref({})

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await fetchItem(id)
    item.value = res || {}
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
  }
})

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
  margin-top: 40px;
  background: var(--panel);
  padding: 20px;
  border-radius: 12px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.2);
  border-radius: 8px;
  color: var(--text-light);
}
</style>
