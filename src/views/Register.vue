<template>
  <div class="page register-page">
    <div class="register-container">
      <div class="logo">
        <h1>GAME MARKET</h1>
        <p>创建您的账户</p>
      </div>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <input type="text" v-model="username" placeholder="设置用户名" required>
        </div>

        <div class="form-group">
          <label>邮箱地址</label>
          <input type="email" v-model="email" placeholder="example@mail.com" required>
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <input type="password" v-model="password" placeholder="设置密码" required>
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <input type="password" v-model="confirmPassword" placeholder="再次输入密码" required>
        </div>
        
        <button type="submit" class="button-primary full-width">立即注册</button>
      </form>
      
      <div class="footer-links">
        <span>已有账号? <router-link to="/login">立即登录</router-link></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'

const router = useRouter()
const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')

const handleRegister = async () => {
  if (password.value !== confirmPassword.value) {
    alert('两次输入的密码不一致')
    return
  }

  try {
    const res = await register({
      username: username.value,
      email: email.value,
      password: password.value
    })
    
    if (res && res.token) {
      // Auto login
      localStorage.setItem('token', res.token)
      localStorage.setItem('userId', res.userId)
      localStorage.setItem('username', res.username)
      alert('注册成功，即将进入市场')
      router.push('/market')
    } else {
      // Fallback if no token returned (should not happen with new backend)
      alert('注册成功，请登录')
      router.push('/login')
    }
  } catch (error) {
    console.error(error)
    const message = error.response?.data?.message || error.message || '未知错误'
    alert('注册失败: ' + message)
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--bg);
}

.register-container {
  background: var(--panel);
  padding: 50px;
  border-radius: 16px;
  width: 100%;
  max-width: 450px;
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
  margin-bottom: 20px;
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
  text-align: center;
  font-size: 14px;
  color: var(--text-light);
}

.footer-links a {
  color: var(--primary);
  text-decoration: none;
  font-weight: bold;
}
</style>
