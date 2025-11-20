<<<<<<< HEAD


<template>
  <nav class="navbar">
    <div class="logo">Game Market</div>

    <div class="nav-links">
      <router-link to="/market">市场</router-link>
      <router-link to="/trade">交易大厅</router-link>
      <router-link to="/inventory">背包</router-link>
      <router-link to="/profile">我的账户</router-link>
    </div>

    <input type="text" class="search" placeholder="搜索装备...">
  </nav>
</template>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  background: var(--panel);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.logo {
  color: var(--primary);
  font-size: 24px;
  font-weight: bold;
}

.nav-links a {
  color: var(--text);
  margin: 0 15px;
  text-decoration: none;
}

.nav-links a:hover {
  color: var(--primary);
}

.search {
  padding: 8px 15px;
  background: #2c2c2e;
  border: 1px solid #444;
  border-radius: 6px;
  color: var(--text);
}
</style>
=======


<template>
  <nav class="navbar">
    <div class="logo">Game Market</div>

    <div class="nav-links">
      <router-link to="/market">市场</router-link>
      <router-link to="/trade">交易大厅</router-link>
      <router-link to="/inventory">背包</router-link>
      <router-link to="/profile">我的账户</router-link>
    </div>

    <input type="text" class="search" placeholder="搜索装备...">
  </nav>
</template>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  background: var(--panel);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.logo {
  color: var(--primary);
  font-size: 24px;
  font-weight: bold;
}

.nav-links a {
  color: var(--text);
  margin: 0 15px;
  text-decoration: none;
}

.nav-links a:hover {
  color: var(--primary);
}

.search {
  padding: 8px 15px;
  background: #2c2c2e;
  border: 1px solid #444;
  border-radius: 6px;
  color: var(--text);
}
</style>
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
