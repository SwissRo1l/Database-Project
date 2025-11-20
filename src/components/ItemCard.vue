

<template>
  <div class="card item-card">
    <img :src="item.img" class="item-img">

    <h3>{{ item.name }}</h3>

    <p class="price">￥{{ item.price }}</p>

    <p
      class="change"
      :class="item.change >= 0 ? 'up' : 'down'"
    >
      {{ item.change >= 0 ? '+' : '' }}{{ item.change }}%
    </p>

    <button class="button-primary">查看详情</button>
  </div>
</template>

<script setup>
defineProps({
  item: Object
})
</script>

<style scoped>
.item-card {
  text-align: center;
}

.item-img {
  width: 100%;
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
