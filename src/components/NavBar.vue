<template>
  <nav class="navbar">
    <div class="nav-left">
      <div class="menu-icon">☰</div>
      <div class="logo">Game Market</div>
    </div>

    <div class="nav-center">
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <input 
          type="text" 
          placeholder="搜索装备..." 
          v-model="searchQuery"
          @keyup.enter="handleSearch"
        >
      </div>
    </div>

    <div class="nav-right">
      <div class="nav-links">
        <router-link to="/market">市场</router-link>
        <router-link to="/trade">交易</router-link>
        <router-link to="/inventory">背包</router-link>
        <router-link to="/profile">账户</router-link>
      </div>
      
      <div class="actions">
        <div class="balance" v-if="userStore.available">
          {{ formattedBalance }} G
        </div>
        
        <div class="icon-btn notification-btn" @click="goToNotifications">
          🔔
          <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
        </div>
        
        <div class="avatar-circle">
          <img v-if="userStore.avatar" :src="userStore.avatar" alt="Avatar" class="nav-avatar-img">
          <span v-else>{{ userStore.name ? userStore.name.charAt(0).toUpperCase() : 'U' }}</span>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { useNotificationStore } from '../store/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notifStore = useNotificationStore()

const searchQuery = ref('')
let pollInterval = null

const startPolling = (userId) => {
  if (pollInterval) clearInterval(pollInterval)
  if (!userId) return
  
  // Initial fetch
  notifStore.fetchBackend(userId)
  
  // Poll every 10 seconds
  pollInterval = setInterval(() => {
    notifStore.fetchBackend(userId)
  }, 10000)
}

onMounted(() => {
  const userId = userStore.uid || localStorage.getItem('userId')
  if (userId) {
    startPolling(userId)
  }
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

watch(() => userStore.uid, (newUid) => {
  startPolling(newUid)
})

watch(() => route.query.q, (newQ) => {
  searchQuery.value = newQ || ''
}, { immediate: true })

const handleSearch = () => {
  router.push({ path: '/market', query: { q: searchQuery.value } })
}

const formattedBalance = computed(() => {
  try {
    return (Number(userStore.available) || 0).toLocaleString()
  } catch (e) {
    return '0'
  }
})

const unreadCount = computed(() => {
  return notifStore.unreadCount
})

const goToNotifications = () => {
  router.push('/notifications')
}
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: var(--panel);
  border-bottom: 1px solid rgba(255,255,255,0.1);
  height: 64px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 200px;
}

.menu-icon {
  font-size: 20px;
  cursor: pointer;
  color: var(--text);
}

.logo {
  color: var(--primary);
  font-size: 22px;
  font-weight: bold;
  letter-spacing: 0.5px;
}

.nav-center {
  flex: 1;
  max-width: 720px;
  margin: 0 20px;
}

.search-bar {
  background: rgba(255,255,255,0.1);
  border-radius: 8px;
  padding: 8px 15px;
  display: flex;
  align-items: center;
  transition: background 0.2s;
}

.search-bar:focus-within {
  background: #fff;
}

.search-bar:focus-within input {
  color: #000;
}

.search-bar:focus-within .search-icon {
  filter: invert(1);
}

.search-icon {
  margin-right: 10px;
  opacity: 0.7;
}

.search-bar input {
  background: transparent;
  border: none;
  color: var(--text);
  width: 100%;
  font-size: 16px;
  outline: none;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 200px;
  justify-content: flex-end;
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-links a {
  color: var(--text);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-links a:hover, .nav-links a.router-link-active {
  color: var(--primary);
}

.actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.balance {
  color: #fcd535;
  font-weight: bold;
  font-size: 14px;
}

.icon-btn {
  position: relative;
  cursor: pointer;
  font-size: 20px;
  padding: 8px;
  border-radius: 50%;
  transition: background 0.2s;
}

.icon-btn:hover {
  background: rgba(255,255,255,0.1);
}

.badge {
  position: absolute;
  top: 0;
  right: 0;
  background: var(--up); /* Red */
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  background: purple;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  cursor: pointer;
  overflow: hidden;
}

.nav-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
