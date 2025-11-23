

<template>
  <div class="card item-card" @click="goToDetail">
    <img :src="item.img" class="item-img">

    <h3>{{ item.name }}</h3>

    <p class="price">￥{{ item.price }}</p>

    <p
      class="change"
      :class="item.change >= 0 ? 'up' : 'down'"
    >
      {{ item.change >= 0 ? '+' : '' }}{{ Number(item.change).toFixed(2) }}%
    </p>

    <button class="button-primary">查看详情</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  item: Object
})

const router = useRouter()

const goToDetail = () => {
  router.push(`/item/${props.item.id}`)
}
</script>

<style scoped>
.item-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}

.item-img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 6px;
  margin-bottom: 10px;
}

.price {
  font-size: 22px;
  margin: 5px 0;
}

.up {
  color: var(--up);
}
.down {
  color: var(--down);
}
</style>
