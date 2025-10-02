<template>
  <div class="app-header">
    <div class="logo-container">
      <img src="/tomato.svg" alt="番茄书城" class="logo" />
      <h1 class="site-title">番茄线上书城</h1>
    </div>
    <div class="user-actions" v-if="hasToken">
      <el-button type="text" @click="goToProfile">个人中心</el-button>
      <el-button type="text" @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const hasToken = computed(() => {
  return localStorage.getItem('token')
})

const goToProfile = () => {
  router.push('/profile')
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  width: 40px;
  height: 40px;
  margin-right: 10px;
}

.site-title {
  font-size: 20px;
  font-weight: bold;
  color: #e53935;
  margin: 0;
}

.user-actions {
  display: flex;
  gap: 10px;
}
</style> 