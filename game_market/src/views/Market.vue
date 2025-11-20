<<<<<<< HEAD


<template>
  <div class="market-container">

    <!-- 导航栏 -->
    <NavBar />

    <div class="content-layout">
      <!-- 左侧内容：商品列表 -->
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

      <!-- 右侧内容：图表 -->
      <aside class="side-content">
        <PriceChart />
      </aside>
    </div>

  </div>
</template>


<script setup>
import NavBar from "../components/NavBar.vue"
import ItemCard from "../components/ItemCard.vue"
import PriceChart from "../components/PriceChart.vue"

// 先做假数据，后端API连上后可以替换为真实数据库数据
const hotItems = [
// ...existing code...
  {
    id: 1,
    name: "AK47 | 火蛇",
    price: 456,
    change: 3.2,
    img: "https://via.placeholder.com/300x200"
  },
  {
    id: 2,
    name: "AWP | 二西莫夫",
    price: 820,
    change: -1.5,
    img: "https://via.placeholder.com/300x200"
  }
]

const topGainers = [
  {
    id: 3,
    name: "M4A1 | 消音版",
    price: 280,
    change: -1.1,
    img: "https://via.placeholder.com/300x200"
  },
  {
    id: 4,
    name: "Ak47 | 红线",
    price: 390,
    change: 2.8,
    img: "https://via.placeholder.com/300x200"
  }
]
</script>


<style scoped>
.market-container {
  padding: 0;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 450px;
  gap: 40px;
  padding: 20px 40px;
  max-width: 100%;
  margin: 0;
}

.side-content {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.section-title {
  font-size: 26px;
  margin: 20px 0;
  color: var(--primary);
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
  .side-content {
    height: 400px;
    position: static;
  }
}
</style>

=======


<template>
  <div class="market-container">

    <!-- 导航栏 -->
    <NavBar />

    <div class="content-layout">
      <!-- 左侧内容：商品列表 -->
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

      <!-- 右侧内容：图表 -->
      <aside class="side-content">
        <PriceChart />
      </aside>
    </div>

  </div>
</template>


<script setup>
import NavBar from "../components/NavBar.vue"
import ItemCard from "../components/ItemCard.vue"
import PriceChart from "../components/PriceChart.vue"

// 先做假数据，后端API连上后可以替换为真实数据库数据
const hotItems = [
// ...existing code...
  {
    id: 1,
    name: "AK47 | 火蛇",
    price: 456,
    change: 3.2,
    img: "https://via.placeholder.com/300x200"
  },
  {
    id: 2,
    name: "AWP | 二西莫夫",
    price: 820,
    change: -1.5,
    img: "https://via.placeholder.com/300x200"
  }
]

const topGainers = [
  {
    id: 3,
    name: "M4A1 | 消音版",
    price: 280,
    change: -1.1,
    img: "https://via.placeholder.com/300x200"
  },
  {
    id: 4,
    name: "Ak47 | 红线",
    price: 390,
    change: 2.8,
    img: "https://via.placeholder.com/300x200"
  }
]
</script>


<style scoped>
.market-container {
  padding: 0;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 450px;
  gap: 40px;
  padding: 20px 40px;
  max-width: 100%;
  margin: 0;
}

.side-content {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.section-title {
  font-size: 26px;
  margin: 20px 0;
  color: var(--primary);
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
  .side-content {
    height: 400px;
    position: static;
  }
}
</style>

>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
