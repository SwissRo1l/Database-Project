<template>
  <div class="trade-panel">
    <slot></slot>
  </div>
</template>

<script>
export default { name: 'TradePanel' }
</script>

<style scoped>
.trade-panel{border:1px solid #eee;padding:12px;border-radius:6px}
</style>
