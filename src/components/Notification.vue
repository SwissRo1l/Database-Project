<template>
  <div v-if="notification" class="notification-overlay">
    <div class="notification-box" :class="notification.type">
      <div class="notification-content">
        <h3>提示</h3>
        <p>{{ notification.message }}</p>
      </div>
      <button class="confirm-btn" @click="close">确认</button>
    </div>
  </div>
</template>

<script setup>
import { useNotificationStore } from '../store/notification'
import { storeToRefs } from 'pinia'

const store = useNotificationStore()
const { notification } = storeToRefs(store)

const close = () => {
  store.clear()
}
</script>

<style scoped>
.notification-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}

.notification-box {
  background: #232428;
  border: 1px solid var(--primary);
  padding: 25px;
  border-radius: 12px;
  min-width: 320px;
  max-width: 90%;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
  text-align: center;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.notification-box.success {
  border-color: var(--down); /* Green */
}

.notification-box.error {
  border-color: var(--up); /* Red */
}

.notification-content h3 {
  margin-top: 0;
  color: #fff;
  font-size: 1.2rem;
  margin-bottom: 10px;
}

.notification-content p {
  color: var(--text);
  margin-bottom: 20px;
  font-size: 1rem;
  line-height: 1.5;
}

.confirm-btn {
  background: var(--primary);
  color: #fff;
  border: none;
  padding: 10px 30px;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: opacity 0.2s;
}

.confirm-btn:hover {
  opacity: 0.9;
}

@keyframes popIn {
  from { opacity: 0; transform: scale(0.8); }
  to { opacity: 1; transform: scale(1); }
}
</style>
