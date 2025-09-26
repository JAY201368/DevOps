<template>
  <div class="app-header">
    <div class="logo-container">
      <img src="/tomato.svg" alt="番茄书城" class="logo" />
      <h1 class="site-title">番茄线上书城</h1>
    </div>
    <div class="nav-buttons" v-if="logined">
      <el-button type="text" @click="goToProducts">商品列表</el-button>
      <el-button type="text" @click="goToCart">
        <el-icon class="cart-icon"><ShoppingCart /></el-icon> 购物车
        <el-badge v-if="cartCount > 0" :value="cartCount" class="cart-badge" />
      </el-button>
      <el-button type="text" @click="goToProfile">个人信息</el-button>
    </div>
    <div class="user-actions" v-if="logined">
      <el-button type="text" @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCartItems } from '../api/cart'
import { ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const logined = ref(false)
const cartCount = ref(0)

// 组件挂载时检查sessionStorage中的登录状态
onMounted(() => {
  const loginStatus = sessionStorage.getItem('logined')
  logined.value = loginStatus === 'true'
  
  if (logined.value) {
    fetchCartCount();
  }
})

// 监听路由变化，在每次路由变化时更新购物车数量
watch(() => route.path, () => {
  if (logined.value) {
    fetchCartCount();
  }
})

// 获取购物车商品数量
const fetchCartCount = async () => {
  try {
    const response = await getCartItems();
    if (response.code === '200' && response.data && response.data.items) {
      cartCount.value = response.data.items.length;
    }
  } catch (error) {
    console.error('获取购物车数量失败:', error);
  }
}

const goToProducts = () => {
  router.push('/products')
}

const goToCart = () => {
  router.push('/cart')
}

const goToProfile = () => {
  router.push('/profile')
}

const logout = () => {
  logined.value = false
  sessionStorage.removeItem('logined')
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('userRole')
  router.push('/login')
}

// 暴露方法给父组件
defineExpose({
  setLogined: (value) => {
    logined.value = value
    sessionStorage.setItem('logined', value.toString())
    
    if (value) {
      fetchCartCount();
    }
  }
})
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 70px;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  width: 50px;
  height: 50px;
  margin-right: 15px;
}

.site-title {
  font-size: 24px;
  font-weight: bold;
  color: #e53935;
  margin: 0;
}

.nav-buttons {
  display: flex;
  gap: 30px;
  margin-left: -150px;
}

.nav-buttons .el-button {
  font-size: 16px;
  padding: 0 10px;
  display: flex;
  align-items: center;
}

.cart-icon {
  margin-right: 5px;
}

.cart-badge {
  margin-left: 5px;
}

.user-actions {
  display: flex;
  gap: 10px;
}

.user-actions .el-button {
  font-size: 16px;
}
</style> 