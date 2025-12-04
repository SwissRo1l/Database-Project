<template>
  <div class="page dashboard">
    <h2>Dashboard</h2>
    <p>Redirecting to Market...</p>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  router.push('/market')
})
</script>

<style scoped></style>
