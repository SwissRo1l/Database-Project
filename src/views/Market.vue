<template>
  <div class="market-container">

    <!-- 导航栏 -->
    <NavBar />

    <div class="content-layout">
      <main class="main-content">
        <!-- 热门装备 -->
        <section class="section">
          <h2 class="section-title">🔥 热门装备</h2>
          <div class="item-grid">
            <ItemCard
              v-for="item in hotItems"
              :key="item.id"
              :item="item"
            />
          </div>
        </section>

        <!-- 24小时涨幅榜 -->
        <section class="section">
          <h2 class="section-title">📈 24小时涨幅榜</h2>
          <div class="item-grid">
            <ItemCard
              v-for="item in topGainers"
              :key="item.id"
              :item="item"
            />
          </div>
        </section>
      </main>
    </div>

  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import NavBar from "../components/NavBar.vue"
import ItemCard from "../components/ItemCard.vue"
import { fetchListings } from "../api/market"

const hotItems = ref([])
const topGainers = ref([])

const loadData = async () => {
  try {
    const hotRes = await fetchListings({ sort: 'hot', limit: 8 })
    hotItems.value = hotRes || []

    const gainersRes = await fetchListings({ sort: 'gainers', limit: 8 })
    topGainers.value = gainersRes || []
  } catch (error) {
    console.error('Failed to load market data:', error)
    hotItems.value = [
      { id: 1, name: "AK47 | 火蛇", price: 45600, change: 3.2, img: "https://via.placeholder.com/300x200" },
      { id: 2, name: "AWP | 二西莫夫", price: 82000, change: -1.5, img: "https://via.placeholder.com/300x200" }
    ]
    topGainers.value = [
      { id: 3, name: "M4A1 | 消音版", price: 28000, change: -1.1, img: "https://via.placeholder.com/300x200" },
      { id: 4, name: "Ak47 | 红线", price: 39000, change: 2.8, img: "https://via.placeholder.com/300x200" }
    ]
  }
}

onMounted(() => {
  loadData()
})
</script>


<style scoped>
.market-container {
  padding: 0;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  padding: 20px 28px;
  max-width: 100%;
  margin: 0 auto;
}

.main-content {
  width: 100%;
}

.section-title {
  font-size: 26px;
  margin: 20px 0;
  color: var(--primary);
}

.item-grid {
  display: grid;
  /* use auto-fit so columns expand to fill available space */
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
    padding: 16px;
  }
}
</style>

