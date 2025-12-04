<template>
  <div class="page profile">
    <NavBar />
    <div class="container">
      <div v-if="isLoading" class="loading-container">
        <LoadingWave />
      </div>
      <div v-else>
        <div class="profile-header">
          <div class="avatar" @click="triggerFileInput" title="点击修改头像">
            <img :src="userStore.avatar || 'https://via.placeholder.com/100'" alt="Avatar" />
            <div class="avatar-overlay">✏️</div>
            <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" accept="image/*" />
          </div>
          <div class="user-info">
            <h2>{{ userStore.name }}<span class="uid-tag">#{{ userStore.uid }}</span></h2>
            <div class="tags">
              <span class="tag vip">VIP {{ Math.floor(userStore.available / 10000) }}</span>
              <span class="tag verified">已认证</span>
            </div>
          </div>
          <div class="balance-card">
            <p class="label">账户余额</p>
            <p class="amount">{{ (userStore.available || 0).toLocaleString() }} G</p>
            <button class="deposit-btn" @click="deposit">充值</button>
          </div>
        </div>

        <div class="content-grid">

          <div class="section history">
            <div class="section-header">
              <h3>交易历史</h3>
              <router-link to="/history" class="view-all">查看全部</router-link>
            </div>
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
                          <input type="text" v-model="editName" :placeholder="userStore.name" :disabled="!isEditingName" />
                          <button class="btn-small" @click="updateName">{{ isEditingName ? '保存' : '修改' }}</button>
                          <button v-if="isEditingName" class="btn-small cancel-btn" @click="cancelEditName">取消</button>
                        </div>
            </div>
            
            <div class="setting-item">
              <label>邮箱</label>
              <input type="email" :value="userStore.email" disabled />
            </div>
            <div class="setting-item">
              <label>密码</label>
              <button class="change-pwd" @click="changePassword">修改密码</button>
            </div>
            <button class="logout-btn" @click="logout">退出登录</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import LoadingWave from '../components/LoadingWave.vue'
import { fetchProfile, updateProfile, validatePassword } from '../api/user'
import { fetchOrders } from '../api/trade'
import { recharge } from '../api/wallet'
import { useUserStore } from '../store/user'
import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()
const history = ref([])
const editName = ref('')
const editAvatar = ref('')
const showAvatarInput = ref(false)
const isEditingName = ref(false)
const fileInput = ref(null)
const isLoading = ref(true)

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)
  const uid = userStore.id || localStorage.getItem('userId')
  formData.append('userId', uid)

  try {
    const res = await request.post('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res && res.url) {
      userStore.avatar = res.url
      alert('头像上传成功')
    }
  } catch (e) {
    console.error(e)
    alert('头像上传失败')
  }
}

onMounted(async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    router.push('/login')
    return
  }

  isLoading.value = true
  try {
    // Fetch Profile
    const profileRes = await fetchProfile(userId)
    if (profileRes) {
      userStore.setUser(profileRes)
      editName.value = userStore.name
      editAvatar.value = userStore.avatar || ''
    }

    // Fetch History
    const ordersRes = await fetchOrders({ userId: userId, page: 0, size: 5 })
    // If backend returns paged object, extract content
    if (ordersRes && ordersRes.content) {
      history.value = ordersRes.content
    } else {
      history.value = ordersRes || []
    }

  } catch (e) {
    console.error(e)
  } finally {
    isLoading.value = false
  }
})

const updateName = async () => {
  // Toggle edit mode or save
  if (!isEditingName.value) {
    isEditingName.value = true
    return
  }

  // Save
  if (!editName.value || editName.value === userStore.name) {
    isEditingName.value = false
    return
  }

  try {
    await updateProfile(userStore.uid, { username: editName.value })
    userStore.name = editName.value
    localStorage.setItem('username', editName.value)
    alert('用户名修改成功')
  } catch (e) {
    console.error(e)
    alert('修改失败')
  } finally {
    isEditingName.value = false
  }
}

const cancelEditName = () => {
  // revert value and disable edit
  editName.value = userStore.name
  isEditingName.value = false
}

// removed avatar text-input update: avatar can be updated by clicking the avatar image in the header

const changePassword = async () => {
  const current = window.prompt('请输入当前密码:')
  if (current === null) return
  if (!current) {
    alert('当前密码不能为空')
    return
  }

  try {
    await validatePassword(userStore.uid, current)
  } catch (e) {
    console.error(e)
    const msg = e?.response?.data?.message || e?.message || '当前密码不正确'
    alert(msg)
    return
  }

  const next = window.prompt('请输入新密码:')
  if (next === null) return
  if (!next) {
    alert('新密码不能为空')
    return
  }

  if (next === current) {
    alert('新密码不能与旧密码相同')
    return
  }

  try {
    const res = await updateProfile(userStore.uid, { currentPassword: current, password: next })
    alert(res?.message || '密码修改成功')
  } catch (e) {
    console.error(e)
    const msg = e?.response?.data?.message || e?.message || '修改密码失败，请稍后重试'
    alert(msg)
  }
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

const deposit = async () => {
  // Ask user for an amount to recharge
  const input = window.prompt('请输入充值金额（单位 G）:')
  if (!input) return
  const value = Number(input)
  if (Number.isNaN(value) || value <= 0) {
    alert('请输入有效的正数金额')
    return
  }

  try {
    const playerId = Number(userStore.uid) || Number(localStorage.getItem('userId'))
    if (!playerId) {
      alert('未找到用户 ID，请重新登录')
      return
    }

    const res = await recharge(playerId, { amount: value })
    if (res) {
      // Refresh profile to get updated reserved/available fields
      const profileRes = await fetchProfile(playerId)
      if (profileRes) {
        userStore.setUser(profileRes)
      }
      alert(res.message || '充值成功')
    }
  } catch (e) {
    console.error(e)
    alert('充值失败，请稍后重试')
  }
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
  margin: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding-bottom: 10px;
}

.view-all {
  font-size: 12px;
  color: var(--primary);
  text-decoration: none;
}

.view-all:hover {
  text-decoration: underline;
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

.icon.buy { background: rgba(244, 67, 54, 0.2); color: var(--up); } /* Red bg for Buy (Up) */
.icon.sell { background: rgba(76, 175, 80, 0.2); color: var(--down); } /* Green bg for Sell (Down) */

.details {
  flex: 1;
}

.details .name { font-weight: bold; }
.details .date { font-size: 12px; color: var(--text-light); }

.amount.negative { color: var(--up); } /* Red for spending money */
.amount.positive { color: var(--down); } /* Green for earning money */

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
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.btn-small {
  padding: 5px 10px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 12px;
  background: var(--primary);
  color: white;
}

.cancel-btn {
  background: rgba(244, 67, 54, 0.2);
  color: var(--up);
  border: 1px solid var(--up);
  margin-left: 10px;
}

.cancel-btn:hover {
  background: rgba(244, 67, 54, 0.4);
}
</style>
