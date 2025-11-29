import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const notification = ref(null)
  const history = ref([])

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

  const markAllRead = () => {
    history.value.forEach(n => n.read = true)
  }

  return { notification, history, show, clear, markAllRead }
})
