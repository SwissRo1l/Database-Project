<template>
  <div class="page notifications-page">
    <NavBar></NavBar>
    <div class="container">
      <div class="header">
        <h2>通知中心</h2>
        <button class="mark-read-btn" @click="markAllRead">全部标记为已读</button>
      </div>

      <div class="notification-list">
        <div 
          v-for="notif in history" 
          :key="notif.id" 
          class="notif-item"
          :class="{ unread: !notif.read }"
        >
          <div class="icon" :class="notif.type">
            {{ getIcon(notif.type) }}
          </div>
          <div class="content">
            <p class="message">{{ notif.message }}</p>
            <span class="time">{{ formatTime(notif.time) }}</span>
          </div>
        </div>
        
        <div v-if="history.length === 0" class="empty-state">
          暂无通知
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import NavBar from '../components/NavBar.vue'
import { useNotificationStore } from '../store/notification'

const store = useNotificationStore()
const { history } = storeToRefs(store)

const markAllRead = () => {
  store.markAllRead()
}

const getIcon = (type) => {
  switch(type) {
    case 'success': return '✅'
    case 'error': return '❌'
    default: return 'ℹ️'
  }
}

const formatTime = (isoStr) => {
  return new Date(isoStr).toLocaleString()
}
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.mark-read-btn {
  background: transparent;
  border: 1px solid var(--primary);
  color: var(--primary);
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.notif-item {
  display: flex;
  align-items: center;
  background: var(--panel);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 15px;
  border-left: 4px solid transparent;
  transition: background 0.2s;
}

.notif-item.unread {
  background: rgba(61, 174, 252, 0.1);
  border-left-color: var(--primary);
}

.icon {
  font-size: 24px;
  margin-right: 20px;
}

.content {
  flex: 1;
}

.message {
  font-size: 16px;
  margin-bottom: 5px;
}

.time {
  font-size: 12px;
  color: var(--text-light);
}

.empty-state {
  text-align: center;
  color: var(--text-light);
  padding: 50px;
}
</style>
