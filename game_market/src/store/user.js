<<<<<<< HEAD
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
=======
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
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
