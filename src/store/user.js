import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: null,
    name: null,
    token: null,
    uid: null,
    balance: 0,
    reserved: 0,
    available: 0,
    avatar: null
  }),
  actions: {
    setUser(user = {}) {
      // Accept user object from API: { username, email, balance, reserved, available, uid, avatar }
      if (user.username) this.name = user.username
      if (user.email) this.email = user.email
      if (user.uid) this.uid = user.uid
      if (user.balance !== undefined) this.balance = Number(user.balance)
      if (user.reserved !== undefined) this.reserved = Number(user.reserved)
      if (user.available !== undefined) this.available = Number(user.available)
      if (user.id) this.id = user.id
      if (user.avatar !== undefined) this.avatar = user.avatar
    },
    setBalance(bal, reserved = 0) {
      this.balance = Number(bal)
      this.reserved = Number(reserved)
      this.available = Number(bal) - Number(reserved)
    }
  }
})
