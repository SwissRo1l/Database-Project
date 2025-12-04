import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { fetchNotifications, markAllAsRead as apiMarkAllRead } from '../api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const notification = ref(null)
  const history = ref([])
  const backendHistory = ref([])

  const show = (message, type = 'info') => {
    const newNotif = { 
      id: Date.now(), 
      message, 
      type, 
      time: new Date().toISOString(),
      read: false 
    }
    notification.value = newNotif
    history.value.unshift(newNotif)
  }

  const clear = () => {
    notification.value = null
  }

  const fetchBackend = async (userId) => {
    if (!userId) return
    try {
      const res = await fetchNotifications(userId)
      backendHistory.value = res
    } catch (e) {
      console.error('Failed to fetch notifications:', e)
    }
  }

  const markAllRead = async (userId) => {
    // Mark local read
    history.value.forEach(n => n.read = true)
    
    // Mark backend read
    if (userId) {
      try {
        await apiMarkAllRead(userId)
        backendHistory.value.forEach(n => n.isRead = true)
      } catch (e) {
        console.error(e)
      }
    }
  }

  const unreadCount = computed(() => {
    const localUnread = history.value.filter(n => !n.read).length
    const backendUnread = backendHistory.value.filter(n => !n.isRead).length
    return localUnread + backendUnread
  })

  return { 
    notification, 
    history, 
    backendHistory,
    show, 
    clear, 
    markAllRead, 
    fetchBackend,
    unreadCount 
  }
})
