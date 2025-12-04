import { defineStore } from 'pinia'

export const useMarketStore = defineStore('market', {
  state: () => ({ listings: [], categories: [] }),
  actions: {
    setListings(listings) { this.listings = listings },
    setCategories(categories) { this.categories = categories }
  }
})
