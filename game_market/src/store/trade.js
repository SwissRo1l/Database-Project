<<<<<<< HEAD
import { defineStore } from 'pinia'

export const useTradeStore = defineStore('trade', {
  state: () => ({ orders: [] }),
  actions: {
    setOrders(o) { this.orders = o }
  }
})
=======
import { defineStore } from 'pinia'

export const useTradeStore = defineStore('trade', {
  state: () => ({ orders: [] }),
  actions: {
    setOrders(o) { this.orders = o }
  }
})
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
