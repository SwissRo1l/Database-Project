<template>
  <main style="padding:20px" class="grid cols-2">
    <div class="card">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <h2>在售商品 🛒</h2>
        <button class="btn secondary" @click="load">刷新</button>
      </div>
      <div v-if="!list.length" style="opacity:.7">暂无在售，稍后再来看看～</div>
      <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); margin-top:12px">
        <div v-for="l in list" :key="l.listing_id" class="card" style="padding:12px">
          <div style="font-size:36px">🗡️</div>
          <div style="font-weight:700">{{ l.item_type }}</div>
          <div>价格：<b>{{ l.price }}</b> {{ l.currency_code }}</div>
          <div>卖家：{{ l.seller }}</div>
          <div style="display:flex; gap:8px; margin-top:8px">
            <input class="input" v-model.trim="buyer" placeholder="购买者昵称" />
            <button class="btn" @click="buy(l.listing_id)">购买</button>
          </div>
        </div>
      </div>
      <div v-if="msg" style="margin-top:8px">{{ msg }}</div>
    </div>

    <div class="card">
      <h2>快速铸造与上架 ✨</h2>
      <div style="display:flex; gap:8px; flex-wrap:wrap">
        <input class="input" v-model.trim="seller" placeholder="你的昵称（卖家）" />
        <select class="input" v-model="type">
          <option>Iron Sword</option>
          <option>Dragon Blade</option>
        </select>
        <input class="input" v-model.number="price" type="number" placeholder="价格（最小单位）" />
        <button class="btn" @click="mintAndList">一键铸造并上架</button>
      </div>
      <div v-if="sideMsg" style="margin-top:8px">{{ sideMsg }}</div>
    </div>
  </main>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ref, onMounted } from 'vue'

type Listing = { listing_id:number; price:number; currency_code:string; item_id:number; item_type:string; seller:string }
const list = ref<Listing[]>([])
const buyer = ref(localStorage.getItem('gm_username') || '')
const seller = ref(localStorage.getItem('gm_username') || '')
const msg = ref('')
const sideMsg = ref('')
const type = ref('Iron Sword')
const price = ref<number|undefined>(1500)

async function load(){
  const { data } = await axios.get('/api/listings')
  list.value = data
}

async function buy(id:number){
  try{
    const { data } = await axios.post('/api/orders/purchase', { username: buyer.value, listingId: id })
    msg.value = `购买成功：listing ${data.listing_id}`
    await load()
  }catch(e:any){
    msg.value = e?.response?.data?.error || '购买失败'
  }
}

async function mintAndList(){
  try{
    if(!seller.value){ sideMsg.value = '请填写你的昵称（卖家）'; return }
    const p = Number(price.value||0)
    if(!Number.isFinite(p) || p < 1){ sideMsg.value = '请输入有效价格（最小单位且 >= 1）'; return }
    const minted = await axios.post('/api/items/mint', { username: seller.value, itemType: type.value, metadata: '{"durability":100}' })
    const itemId = minted.data.item_id
    const listing = await axios.post('/api/listings', { username: seller.value, itemId, currency: 'GOLD', price: p })
    sideMsg.value = `已上架：listing ${listing.data.listing_id}`
    await load()
  }catch(e:any){
    sideMsg.value = e?.response?.data?.error || '上架失败'
  }
}

onMounted(load)
</script>

<style scoped>
</style>
