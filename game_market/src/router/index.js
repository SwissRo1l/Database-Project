<<<<<<< HEAD
import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Market from '../views/Market.vue'
import ItemDetail from '../views/ItemDetail.vue'
import Trade from '../views/Trade.vue'
import Inventory from '../views/Inventory.vue'
import Profile from '../views/Profile.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const routes = [
  { path: '/', name: 'Home', component: Market },
  { path: '/market', name: 'Market', component: Market },
  { path: '/item/:id', name: 'ItemDetail', component: ItemDetail, props: true },
  { path: '/trade', name: 'Trade', component: Trade },
  { path: '/inventory', name: 'Inventory', component: Inventory },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  // Removed invalid duplicate root route that referenced an undefined `HomeView`.
]

const router = createRouter({ history: createWebHistory(), routes })
export default router
=======
import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Market from '../views/Market.vue'
import ItemDetail from '../views/ItemDetail.vue'
import Trade from '../views/Trade.vue'
import Inventory from '../views/Inventory.vue'
import Profile from '../views/Profile.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const routes = [
  { path: '/', name: 'Home', component: Market },
  { path: '/market', name: 'Market', component: Market },
  { path: '/item/:id', name: 'ItemDetail', component: ItemDetail, props: true },
  { path: '/trade', name: 'Trade', component: Trade },
  { path: '/inventory', name: 'Inventory', component: Inventory },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  // Removed invalid duplicate root route that referenced an undefined `HomeView`.
]

const router = createRouter({ history: createWebHistory(), routes })
export default router
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
