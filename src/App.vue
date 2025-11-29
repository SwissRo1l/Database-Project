<template>
  <Notification />
  <router-view />
</template>

<script setup>
import Notification from './components/Notification.vue'
// App.vue acts as the app shell; routing will render the current page.
</script>

<style>
html,body,#app{height:100%;margin:0}
</style>
