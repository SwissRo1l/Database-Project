<template>
  <main style="padding:20px">
    <section class="grid cols-2">
      <div class="card">
        <h2>创建账号 ✨</h2>
        <p>输入你的昵称，进入星星小市场。</p>
        <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap">
          <input class="input" v-model.trim="name" placeholder="你的昵称，例如 luna" />
          <button class="btn" @click="create">创建</button>
        </div>
        <div v-if="msg" style="margin-top:8px">{{ msg }}</div>
      </div>
      <div class="card">
        <h2>小贴士 🐾</h2>
        <ul>
          <li>创建后可前往“市场”查看在售装备</li>
          <li>在“个人主页”里能查看资产与在售物品</li>
        </ul>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const name = ref('')
const msg = ref('')

async function create(){
  if(!name.value){ msg.value='请输入昵称'; return }
  try{
    const { data } = await axios.post('/api/players', { username: name.value })
    msg.value = `欢迎 ${data.username} !`
    localStorage.setItem('gm_username', data.username)
    setTimeout(()=> router.push('/market'), 600)
  }catch(e:any){
    const err = e?.response?.data?.error || '创建失败'
    msg.value = err
    // 如果已存在，继续写入本地并跳转
    if(err.includes('exists') || err.includes('存在')){
      localStorage.setItem('gm_username', name.value)
      setTimeout(()=> router.push('/market'), 600)
    }
  }
}
</script>

<style scoped>
ul{ padding-left: 18px }
li{ line-height: 1.8 }
</style>
