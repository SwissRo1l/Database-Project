<<<<<<< HEAD
import { defineStore } from 'pinia'

export const useMarketStore = defineStore('market', {
  state: () => ({ listings: [], categories: [] }),
  actions: {
    setListings(listings) { this.listings = listings },
    setCategories(categories) { this.categories = categories }
  }
})
=======
import { defineStore } from 'pinia'

export const useMarketStore = defineStore('market', {
  state: () => ({ listings: [], categories: [] }),
  actions: {
    setListings(listings) { this.listings = listings },
    setCategories(categories) { this.categories = categories }
  }
})
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
