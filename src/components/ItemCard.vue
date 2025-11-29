

<template>
  <div class="item-card" @click="goToDetail">
    <div class="img-container">
      <img :src="displayImage" class="item-img">
      <div class="discount-badge" :class="item.change >= 0 ? 'up-bg' : 'down-bg'">
        {{ item.change >= 0 ? '+' : '' }}{{ Number(item.change).toFixed(2) }}%
      </div>
    </div>

    <div class="card-content">
      <h3 class="item-name">{{ item.name }}</h3>
      <div class="price-row">
        <span class="price">￥{{ item.price }}</span>
      </div>
      <button class="button-primary full-width">查看详情</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { getItemImage } from '../utils/itemImages'

const props = defineProps({
  item: Object
})

const router = useRouter()

const displayImage = computed(() => {
  return getItemImage(props.item.name)
})

const goToDetail = () => {
  router.push(`/item/${props.item.id}`)
}
</script>

<style scoped>
.item-card {
  background: var(--panel);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(10px);
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.3);
  border-color: var(--primary);
  background: rgba(255, 255, 255, 0.08);
}

.img-container {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.item-card:hover .item-img {
  transform: scale(1.05);
}

.discount-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  color: #fff;
  font-size: 0.8rem;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.up-bg { background-color: var(--up); }
.down-bg { background-color: var(--down); }

.card-content {
  padding: 15px;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.item-name {
  font-size: 1rem;
  margin: 0 0 10px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text);
}

.price-row {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  font-size: 1.2rem;
  font-weight: bold;
  color: #fff;
}

.full-width {
  width: 100%;
  margin-top: auto;
  padding: 8px 0;
  font-size: 0.9rem;
}
</style>
