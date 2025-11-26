<template>
  <div class="page inventory">
    <NavBar />
    <div class="container">
      <div class="header">
        <h2>我的库存</h2>
        <div class="stats">
          <div class="stat-item">
            <span class="label">总价值</span>
            <span class="value">{{ totalValue.toLocaleString() }} G</span>
          </div>
          <div class="stat-item">
            <span class="label">物品数量</span>
            <span class="value">{{ itemCount }}</span>
          </div>
        </div>
      </div>

      <div>
        <div v-if="inventoryItems.length === 0" class="no-inventory" style="padding:40px; color:var(--text-light);">
          你的背包目前是空的。
        </div>

        <div v-else class="inventory-grid">
          <div class="item-card" v-for="item in inventoryItems" :key="item.id">
            <div class="card-image">
              <img :src="item.img || 'https://via.placeholder.com/150'" alt="Item">
            </div>
            <div class="card-info">
              <h4>{{ item.name }}</h4>
              <p class="rarity">稀有度: {{ item.rarity || '普通' }}</p>
              <div class="card-actions">
                <button class="sell-btn">出售</button>
                <button class="inspect-btn">查看</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NavBar from '../components/NavBar.vue'
import { fetchInventory } from '../api/user'

const inventoryItems = ref([])
const totalValue = ref(0)
const itemCount = ref(0)

onMounted(async () => {
  try {
    // Assuming user ID 1 for now
    const res = await fetchInventory(1)
    inventoryItems.value = res || []
    
    // Calculate stats
    itemCount.value = inventoryItems.value.length
    totalValue.value = inventoryItems.value.reduce((sum, item) => sum + (item.price || 0), 0)
  } catch (e) {
    console.error(e)
    // Fallback mock
    inventoryItems.value = Array.from({ length: 12 }, (_, i) => ({
      id: i,
      name: `神秘物品 #${i + 1}`,
      rarity: '传说',
      img: 'https://via.placeholder.com/150',
      price: 1000
    }))
    itemCount.value = 12
    totalValue.value = 12000
  }
})
</script>

<style scoped>
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
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
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
  margin-bottom: 15px;
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
