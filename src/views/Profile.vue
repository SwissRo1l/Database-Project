<template>
  <div class="page profile">
    <NavBar />
    <div class="container">
      <div class="profile-header">
        <div class="avatar">
          <img src="https://via.placeholder.com/100" alt="Avatar">
        </div>
        <div class="user-info">
          <h2>{{ user.username }}<span class="uid-tag">#{{ user.uid }}</span></h2>
          <div class="tags">
            <span class="tag vip">VIP {{ Math.floor(user.balance / 10000) }}</span>
            <span class="tag verified">已认证</span>
          </div>
        </div>
        <div class="balance-card">
          <p class="label">账户余额</p>
          <p class="amount">{{ user.balance.toLocaleString() }} G</p>
          <button class="deposit-btn">充值</button>
        </div>
      </div>

      <div class="content-grid">
        <div class="section history">
          <h3>交易历史</h3>
          <div class="history-list">
            <div class="history-item" v-for="record in history" :key="record.id">
              <div class="icon" :class="record.type === 'buy' ? 'buy' : 'sell'">
                {{ record.type === 'buy' ? '买' : '卖' }}
              </div>
              <div class="details">
                <p class="name">{{ record.itemName }}</p>
                <p class="date">{{ record.date }}</p>
              </div>
              <div class="amount" :class="record.type === 'buy' ? 'negative' : 'positive'">
                {{ record.type === 'buy' ? '-' : '+' }}{{ Math.abs(record.price) }} G
              </div>
            </div>
          </div>
        </div>

        <div class="section settings">
          <h3>账户设置</h3>
          <div class="setting-item">
            <label>用户名</label>
            <div class="input-group">
              <input type="text" v-model="editName" :placeholder="user.username">
              <button class="btn-small" @click="updateName">修改</button>
            </div>
          </div>
          <div class="setting-item">
            <label>邮箱</label>
            <input type="email" :value="user.email" disabled>
          </div>
          <div class="setting-item">
            <label>密码</label>
            <button class="change-pwd">修改密码</button>
          </div>
          <button class="logout-btn" @click="logout">退出登录</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import { fetchProfile, updateProfile } from '../api/user'
import { fetchOrders } from '../api/trade'

const router = useRouter()
const user = ref({
  username: 'PlayerOne',
  email: 'player@example.com',
  balance: 0,
  uid: '0'
})
const history = ref([])
const editName = ref('')

onMounted(async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    router.push('/login')
    return
  }

  try {
    // Fetch Profile
    const profileRes = await fetchProfile(userId)
    if (profileRes) {
      user.value = { ...user.value, ...profileRes }
      editName.value = user.value.username
    }

    // Fetch History
    const ordersRes = await fetchOrders({ userId: userId, limit: 5 })
    history.value = ordersRes || []
  } catch (e) {
    console.error(e)
  }
})

const updateName = async () => {
  if (!editName.value || editName.value === user.value.username) return
  
  try {
    await updateProfile(user.value.uid, { username: editName.value })
    user.value.username = editName.value
    localStorage.setItem('username', editName.value)
    alert('用户名修改成功')
  } catch (e) {
    console.error(e)
    alert('修改失败')
  }
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.container {
  padding: 40px;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  background: var(--panel);
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 30px;
  gap: 30px;
}

.avatar img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid var(--primary);
}

.user-info {
  flex: 1;
}

.user-info h2 {
  font-size: 28px;
  margin-bottom: 5px;
}

.uid {
  color: var(--text-light);
  margin-bottom: 10px;
}

.tags {
  display: flex;
  gap: 10px;
}

.tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.tag.vip { background: #ffd700; color: #000; }
.tag.verified { background: var(--primary); color: #fff; }

.balance-card {
  text-align: right;
}

.balance-card .label {
  color: var(--text-light);
  font-size: 14px;
}

.balance-card .amount {
  font-size: 32px;
  color: var(--primary);
  font-weight: bold;
  margin: 5px 0 15px;
}

.deposit-btn {
  background: var(--primary);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
}

.section {
  background: var(--panel);
  padding: 25px;
  border-radius: 12px;
}

.section h3 {
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding-bottom: 10px;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-weight: bold;
}

.icon.buy { background: rgba(255, 77, 77, 0.2); color: var(--down); }
.icon.sell { background: rgba(76, 175, 80, 0.2); color: var(--up); }

.details {
  flex: 1;
}

.details .name { font-weight: bold; }
.details .date { font-size: 12px; color: var(--text-light); }

.amount.negative { color: var(--down); }
.amount.positive { color: var(--up); }

.setting-item {
  margin-bottom: 20px;
}

.setting-item label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-light);
}

.setting-item input {
  width: 100%;
  padding: 10px;
  background: rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 6px;
  color: var(--text);
}

.change-pwd {
  background: transparent;
  border: 1px solid var(--text-light);
  color: var(--text);
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
}

.logout-btn {
  width: 100%;
  background: rgba(255, 77, 77, 0.2);
  color: var(--down);
  border: 1px solid var(--down);
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 20px;
}
</style>
