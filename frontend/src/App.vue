<template>
  <div class="app">
    <header class="cute-header">
      <div class="brand">🌸 Star Market</div>
      <nav>
        <router-link to="/">首页</router-link>
        <router-link to="/market">市场</router-link>
        <router-link to="/me">个人主页</router-link>
      </nav>
    </header>
    <router-view />
  </div>
</template>

<script setup lang="ts">
// shell app with router-view only
</script>

<style>
:root{
  --bg: #fff7fb;
  --card: #ffeef7;
  --accent: #ff86b9;
  --accent-2: #a389f4;
  --text: #41354a;
}
*{ box-sizing: border-box }
body{ margin:0; background: var(--bg); color: var(--text); font-family: 'Nunito', system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial }
.cute-header{ position: sticky; top:0; z-index:10; backdrop-filter: blur(6px); display:flex; align-items:center; justify-content:space-between; padding:12px 18px; border-bottom: 1px solid #f3dbe7; background: linear-gradient(90deg, #fff7fb, #f9f5ff) }
.brand{ font-weight:800; letter-spacing:.5px; color: var(--accent); display:flex; align-items:center; gap:8px }
nav a{ margin-left:16px; text-decoration:none; color: var(--text); padding:6px 10px; border-radius: 999px }
nav a.router-link-active{ background: var(--card); color: var(--accent-2); border:1px solid #f0e4f9 }

.card{ background: var(--card); border:1px solid #f3dbe7; border-radius:16px; padding:16px; box-shadow: 0 2px 6px rgba(255,134,185,.12) }
.btn{ background: var(--accent); color:white; border:none; padding:10px 14px; border-radius:12px; cursor:pointer }
.btn.secondary{ background: var(--accent-2) }
.input{ padding:10px 12px; border-radius:12px; border:1px solid #eed7e6; background: white }
.grid{ display:grid; gap:16px }
.grid.cols-2{ grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)) }
</style>

