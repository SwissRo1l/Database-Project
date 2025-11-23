<template>
  <div class="page login-page">
    <div class="login-container">
      <div class="logo">
        <h1>GAME MARKET</h1>
        <p>虚拟物品交易平台</p>
      </div>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名 / 邮箱</label>
          <input type="text" v-model="username" placeholder="请输入账号" required>
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <input type="password" v-model="password" placeholder="请输入密码" required>
        </div>
        
        <button type="submit" class="button-primary full-width">登录</button>
      </form>
      
      <div class="footer-links">
        <router-link to="/register">注册新账号</router-link>
        <a href="#">忘记密码?</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')

const handleLogin = async () => {
  try {
    const res = await login({ username: username.value, password: password.value })
    if (res && res.token) {
      localStorage.setItem('token', res.token)
      localStorage.setItem('userId', res.userId)
      localStorage.setItem('username', res.username)
      router.push('/market')
    } else {
      alert('登录失败，请检查用户名和密码')
    }
  } catch (error) {
    console.error(error)
    const message = error.response?.data?.message || error.message || '未知错误'
    alert('登录失败: ' + message)
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--bg);
}

.login-container {
  background: var(--panel);
  padding: 50px;
  border-radius: 16px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
}

.logo {
  text-align: center;
  margin-bottom: 40px;
}

.logo h1 {
  color: var(--primary);
  font-size: 32px;
  letter-spacing: 2px;
  margin-bottom: 5px;
}

.logo p {
  color: var(--text-light);
  font-size: 14px;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-light);
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px;
  background: rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 6px;
  color: var(--text);
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  border-color: var(--primary);
  outline: none;
}

.full-width {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  margin-top: 10px;
}

.footer-links {
  margin-top: 25px;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.footer-links a {
  color: var(--text-light);
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: var(--primary);
}
</style>
