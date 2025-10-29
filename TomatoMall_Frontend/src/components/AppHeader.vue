<template>
  <div class="app-header">
    <div class="logo-container">
      <img src="/tomato.svg" alt="番茄书城" class="logo" />
      <h1 class="site-title">番茄线上书城</h1>
    </div>
    <div class="nav-buttons" v-if="logined">
      <el-button type="text" @click="goToProducts">
        <el-icon><Goods /></el-icon> 商品列表
      </el-button>
      <el-button type="text" @click="goToCart">
        <el-icon class="cart-icon"><ShoppingCart /></el-icon> 购物车
        <el-badge v-if="cartCount > 0" :value="cartCount" class="cart-badge" />
      </el-button>
      <el-button v-if="!isAdmin" type="text" @click="goToWishList">
        <el-icon class="wishlist-icon"><Star /></el-icon> 我的愿望单
        <el-badge v-if="wishlistStore.wishlistCount > 0" :value="wishlistStore.wishlistCount" class="wishlist-badge" />
      </el-button>
      <el-button type="text" @click="goToOrders">
        <el-icon><Document /></el-icon> 我的订单
      </el-button>
      <el-button type="text" @click="goToProfile">
        <el-icon><User /></el-icon> 个人信息
      </el-button>
      
      <!-- 根据用户角色显示不同的广告相关菜单 -->
      <el-button v-if="isAdmin" type="text" @click="goToAdvertisements">
        <el-icon><Setting /></el-icon> 广告管理
      </el-button>
      <el-button v-else type="text" @click="goToAdsRecommend">
        <el-icon><Bell /></el-icon> 广告推荐
      </el-button>
    </div>
    <div class="user-actions" v-if="logined">
      <el-button type="text" @click="logout">
        <el-icon><SwitchButton /></el-icon> 退出登录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCartItems } from '../api/cart'
import { ShoppingCart, Star, Goods, Document, User, Setting, Bell, SwitchButton } from '@element-plus/icons-vue'
import { useWishListStore } from '../store/wishlist'

const router = useRouter()
const route = useRoute()
const logined = ref(false)
const cartCount = ref(0)

const wishlistStore = useWishListStore()

// 从localStorage获取用户角色
const userRole = ref('')
const isAdmin = computed(() => userRole.value === 'admin')

// 组件挂载时检查sessionStorage中的登录状态
onMounted(() => {
  const loginStatus = sessionStorage.getItem('logined')
  logined.value = loginStatus === 'true'
  
  if (logined.value) {
    fetchCartCount();
    wishlistStore.fetchWishListCount();
    // 获取用户角色
    userRole.value = localStorage.getItem('userRole') || '';
    // 添加购物车更新事件监听
    window.addEventListener('cart-updated', handleCartUpdate);
  }
})

// 监听路由变化，在每次路由变化时更新购物车和愿望清单数量
watch(() => route.path, () => {
  if (logined.value) {
    fetchCartCount();
    wishlistStore.fetchWishListCount();
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

// 监听购物车更新事件
const handleCartUpdate = (event) => {
  cartCount.value = event.detail.count;
}

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('cart-updated', handleCartUpdate);
})

// 导航函数
const goToProducts = () => router.push('/products')
const goToCart = () => router.push('/cart')
const goToOrders = () => router.push('/orders')
const goToProfile = () => router.push('/profile')
const goToWishList = () => router.push('/wishlist')
const goToAdvertisements = () => router.push('/advertisements')
const goToAdsRecommend = () => router.push('/ads-recommend')

const logout = () => {
  logined.value = false
  userRole.value = ''
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
      // 登录时获取用户角色和更新数据
      userRole.value = localStorage.getItem('userRole') || '';
      fetchCartCount();
      wishlistStore.fetchWishListCount();
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
  gap: 5px;
}

.nav-buttons .el-icon {
  font-size: 18px;
}

.cart-icon {
  margin-right: 5px;
}

.cart-badge {
  margin-left: 5px;
}

.wishlist-icon {
  margin-right: 4px;
  font-size: 16px;
}

.wishlist-badge {
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