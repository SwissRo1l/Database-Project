<template>
  <nav class="navbar">
    <div class="logo">Game Market</div>

    <div class="nav-links">
      <router-link to="/market">市场</router-link>
      <router-link to="/trade">交易大厅</router-link>
      <router-link to="/inventory">背包</router-link>
      <router-link to="/profile">我的账户</router-link>
    </div>

    <div class="nav-right">
      <div class="balance">余额: <span class="bal-amount">{{ formattedBalance }} G</span></div>
      <input type="text" class="search" placeholder="搜索装备...">
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const formattedBalance = computed(() => {
  try {
    return (Number(userStore.available) || 0).toLocaleString()
  } catch (e) {
    return '0'
  }
})
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  background: var(--panel);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.logo {
  color: var(--primary);
  font-size: 24px;
  font-weight: bold;
}

.nav-links a {
  color: var(--text);
  margin: 0 15px;
  text-decoration: none;
}

.nav-links a:hover {
  color: var(--primary);
}

.search {
  padding: 8px 15px;
  background: #2c2c2e;
  border: 1px solid #444;
  border-radius: 6px;
  color: var(--text);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.balance {
  color: var(--text);
  font-weight: bold;
}
.bal-amount { color: var(--primary); }
</style>
