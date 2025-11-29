<template>
  <div class="loading-wave">
    <span>加</span><span>载</span><span>中</span><span>.</span><span>.</span><span>.</span>
  </div>
</template>

<script setup>
// No script logic needed for this visual component
</script>

<style scoped>
.loading-wave {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 1.2rem;
  color: var(--primary);
  font-weight: 500;
}

.loading-wave span {
  display: inline-block;
  animation: wave 1.2s ease-in-out infinite;
}

.loading-wave span:nth-child(1) { animation-delay: 0.0s; }
.loading-wave span:nth-child(2) { animation-delay: 0.1s; }
.loading-wave span:nth-child(3) { animation-delay: 0.2s; }
.loading-wave span:nth-child(4) { animation-delay: 0.3s; }
.loading-wave span:nth-child(5) { animation-delay: 0.4s; }
.loading-wave span:nth-child(6) { animation-delay: 0.5s; }

@keyframes wave {
  0%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-6px);
    opacity: 1;
    text-shadow: 0 0 8px rgba(61, 174, 252, 0.6);
  }
}
</style>
