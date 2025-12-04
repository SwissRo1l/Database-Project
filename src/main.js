import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/main.css'
import { fetchProfile } from './api/user'
import { useUserStore } from './store/user'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// On app start, populate user store if we have a userId
const userStore = useUserStore()
const userId = localStorage.getItem('userId')
if (userId) {
	fetchProfile(userId).then(profile => {
		if (profile) userStore.setUser(profile)
	}).catch(() => {})
}

app.mount('#app')
