<<<<<<< HEAD
<template>
  <div class="notification" v-if="message">{{ message }}</div>
</template>

<script>
export default {
  name: 'Notification',
  props: { message: String }
}
</script>

<style scoped>
.notification{background:#fffae6;padding:8px;border:1px solid #ffe58f;border-radius:4px}
</style>
=======
<template>
  <div class="notification" v-if="message">{{ message }}</div>
</template>

<script>
export default {
  name: 'Notification',
  props: { message: String }
}
</script>

<style scoped>
.notification{background:#fffae6;padding:8px;border:1px solid #ffe58f;border-radius:4px}
</style>
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
