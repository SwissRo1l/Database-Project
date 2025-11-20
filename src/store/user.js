import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: null,
    name: null,
    token: null
  }),
  actions: {
    setUser(user) { Object.assign(this, user) }
  }
})
