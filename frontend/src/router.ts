import { createRouter, createWebHistory } from 'vue-router'
import HomeView from './views/HomeView.vue'
import MarketView from './views/MarketView.vue'
import ProfileView from './views/ProfileView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/market', name: 'market', component: MarketView },
  { path: '/me', name: 'me', component: ProfileView }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})
