<template>
  <div class="trade-panel">
    <slot></slot>
  </div>
</template>

<script>
export default { name: 'TradePanel' }
</script>

<style scoped>
.trade-panel {
  background: var(--panel);
  padding: 20px;
  border-radius: 12px;
}
</style>
