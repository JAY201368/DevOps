<template>
  <div class="app-header">
    <div class="logo-container">
      <img src="/tomato.svg" alt="番茄书城" class="logo" />
      <h1 class="site-title">番茄线上书城</h1>
    </div>
    <div class="nav-buttons" v-if="logined">
      <el-button type="text" @click="goToHome">
        <el-icon><HomeFilled /></el-icon> 首页
      </el-button>
      <el-button type="text" @click="goToProducts">
        <el-icon><Goods /></el-icon> 商品列表
      </el-button>
      
      <!-- 个人中心下拉菜单 -->
      <el-dropdown trigger="click" class="user-center-dropdown">
        <el-button type="text" class="user-center-button">
          <el-icon><UserFilled /></el-icon> 个人中心
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goToCart">
              <el-icon class="cart-icon"><ShoppingCart /></el-icon> 购物车
              <el-badge v-if="cartCount > 0" :value="cartCount" class="cart-badge" />
            </el-dropdown-item>
            <el-dropdown-item v-if="!isAdmin" @click="goToWishList">
              <el-icon class="wishlist-icon"><Star /></el-icon> 我的愿望单
              <el-badge v-if="wishlistStore.wishlistCount > 0" :value="wishlistStore.wishlistCount" class="wishlist-badge" />
            </el-dropdown-item>
            <el-dropdown-item @click="goToOrders">
              <el-icon><Document /></el-icon> 我的订单
            </el-dropdown-item>
            <el-dropdown-item v-if="!isAdmin" @click="goToUserCoupons">
              <el-icon><Ticket /></el-icon> 我的促销券
            </el-dropdown-item>
            <el-dropdown-item @click="goToProfile">
              <el-icon><User /></el-icon> 个人信息
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      
      <!-- 根据用户角色显示不同的广告相关菜单 -->
      <el-button v-if="isAdmin" type="text" @click="goToAdvertisements">
        <el-icon><Setting /></el-icon> 广告管理
      </el-button>
      <el-button v-else type="text" @click="goToAdsRecommend">
        <el-icon><Bell /></el-icon> 广告推荐
      </el-button>
      
      <!-- 促销券管理入口 -->
      <el-button v-if="isAdmin" type="text" @click="goToCouponManagement">
        <el-icon><Discount /></el-icon> 促销券管理
      </el-button>

      <!-- 轮播图管理入口 -->
      <el-button v-if="isAdmin" type="text" @click="goToBannerManagement">
        <el-icon><Picture /></el-icon> 轮播图管理
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
import { ShoppingCart, Star, Goods, Document, User, UserFilled, Setting, Bell, SwitchButton, Discount, Ticket, Picture, HomeFilled, ArrowDown } from '@element-plus/icons-vue'
import { useWishListStore } from '../store/wishlist'
import { clearCache } from '../api/request'

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
  // 清除API缓存
  clearCache();
  
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
const goToCouponManagement = () => router.push('/coupons')
const goToUserCoupons = () => router.push('/user-coupons')
const goToHome = () => router.push('/')
const goToBannerManagement = () => router.push('/banners')

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

.user-center-dropdown {
  margin: 0;
}

.user-center-button {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: color 0.3s;
}

.user-center-button:hover {
  color: #409eff;
}

.user-center-dropdown :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  line-height: 1.5;
  font-size: 14px;
  transition: all 0.3s;
}

.user-center-dropdown :deep(.el-dropdown-menu__item:hover) {
  background-color: #ecf5ff;
  color: #409eff;
}

.user-center-dropdown :deep(.el-dropdown-menu) {
  min-width: 180px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 5px 0;
}

.el-dropdown-menu__item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.el-dropdown-menu__item .cart-badge,
.el-dropdown-menu__item .wishlist-badge {
  margin-left: 8px;
}

.el-dropdown-menu__item .el-badge :deep(.el-badge__content) {
  transform: scale(0.8);
  height: 16px;
  line-height: 16px;
  padding: 0 4px;
}

@media (max-width: 768px) {
  .nav-buttons {
    margin-left: 0;
    gap: 10px;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .app-header {
    flex-direction: column;
    height: auto;
    padding: 15px;
  }
  
  .logo-container {
    margin-bottom: 15px;
  }
  
  .user-actions {
    margin-top: 15px;
  }
  
  .nav-buttons .el-button {
    font-size: 14px;
    padding: 0 8px;
  }
}

@media (max-width: 480px) {
  .nav-buttons {
    gap: 5px;
  }
  
  .site-title {
    font-size: 20px;
  }
  
  .logo {
    width: 40px;
    height: 40px;
  }
}
</style>