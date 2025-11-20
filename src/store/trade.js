import { defineStore } from 'pinia'

export const useTradeStore = defineStore('trade', {
  state: () => ({ orders: [] }),
  actions: {
    setOrders(o) { this.orders = o }
  }
})
