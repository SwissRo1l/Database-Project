<template>
  <main style="padding:20px" class="grid cols-2">
    <div class="card">
      <h2>个人资产 🌟</h2>
      <div style="display:flex; gap:8px; flex-wrap:wrap; align-items:center">
        <input class="input" v-model.trim="name" placeholder="你的昵称" />
        <button class="btn" @click="loadAll">查询</button>
      </div>
      <div style="margin-top:10px">
        <h3>钱包</h3>
        <div v-if="!wallets.length" style="opacity:.7">暂无数据</div>
        <ul>
          <li v-for="w in wallets" :key="w.currency">{{ w.currency }}：{{ w.balance }}</li>
        </ul>
      </div>
      <div>
        <h3>背包</h3>
        <div v-if="!items.length" style="opacity:.7">暂无道具</div>
        <ul>
          <li v-for="i in items" :key="i.id">#{{ i.id }} · {{ i.type }} · {{ i.status }}</li>
        </ul>
      </div>
    </div>

    <div class="card">
      <h2>我在售的商品 🧺</h2>
      <div v-if="!listings.length" style="opacity:.7">暂无在售</div>
      <ul>
        <li v-for="l in listings" :key="l.listing_id" style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
          <span>#{{ l.listing_id }} · {{ l.item_type }} · {{ l.price }} {{ l.currency_code }}</span>
          <button class="btn secondary" @click="cancelListing(l.listing_id)">撤销</button>
        </li>
      </ul>
      <div v-if="cancelMsg" style="margin-top:8px">{{ cancelMsg }}</div>
    </div>

    <div class="card" style="grid-column: 1 / -1">
      <h2>我的订单 📜</h2>
      <div v-if="!orders.length" style="opacity:.7">暂无订单</div>
      <ul>
        <li v-for="o in orders" :key="o.order_id">#{{ o.order_id }} · 购买 {{ o.item_type }} · {{ o.price }} {{ o.currency_code }} · 卖家：{{ o.seller }}</li>
      </ul>
    </div>
  </main>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ref, onMounted } from 'vue'

const name = ref(localStorage.getItem('gm_username') || '')
const wallets = ref<{currency:string; balance:number}[]>([])
const items = ref<{id:number; type:string; status:string}[]>([])
const listings = ref<any[]>([])
const orders = ref<any[]>([])
const cancelMsg = ref('')

async function loadAll(){
  if(!name.value) return
  const [wRes, iRes, lRes, oRes] = await Promise.all([
    axios.get(`/api/wallets/${encodeURIComponent(name.value)}`).catch(()=>({data:[]})),
    axios.get(`/api/items`, { params: { owner: name.value } }).catch(()=>({data:[]})),
    axios.get(`/api/listings/mine`, { params: { username: name.value } }).catch(()=>({data:[]})),
    axios.get(`/api/orders/mine`, { params: { username: name.value } }).catch(()=>({data:[]})),
  ])
  wallets.value = wRes.data
  items.value = iRes.data
  listings.value = lRes.data
  orders.value = oRes.data
}

onMounted(loadAll)

async function cancelListing(id:number){
  try{
    const { data } = await axios.post(`/api/listings/${id}/cancel`, { username: name.value })
    cancelMsg.value = `已撤销：listing ${data.listing_id}`
    await loadAll()
  }catch(e:any){
    cancelMsg.value = e?.response?.data?.error || '撤销失败'
  }
}
</script>

<style scoped>
ul{ padding-left: 18px }
li{ line-height: 1.8 }
</style>
