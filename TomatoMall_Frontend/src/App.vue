<template>
  <div class="app">
    <AppHeader ref="appHeaderRef" />
    <div class="main-content">
      <router-view />
    </div>
    
    <!-- 在全局添加Live2D组件，确保在所有页面都可见 -->
    <div class="global-live2d" v-if="!isLoginPage">
      <Live2D />
    </div>
  </div>
</template>

<script setup>
import { ref, provide, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import Live2D from './components/Live2D.vue'
import { useWishListStore } from './store/wishlist'
import env from './config/env'


const appHeaderRef = ref(null)
provide('appHeaderRef', appHeaderRef)

const route = useRoute()
const router = useRouter()
const wishlistStore = useWishListStore()

// 判断当前是否是登录页面，登录页面已经包含了Live2D组件，所以不需要在全局再添加
const isLoginPage = computed(() => {
  return route.path === '/login'
})

// 监听路由变化，触发自定义事件通知Live2D组件
watch(() => route.path, (newPath, oldPath) => {
  if (newPath !== '/login' && oldPath !== undefined) {
    // 如果不是登录页面，且用户已登录，且是真正的路由切换而不是初始加载
    const token = localStorage.getItem('token')
    const username = localStorage.getItem('username')
    if (token && username) {
      setTimeout(() => {
        // 页面切换时发送特定的页面切换事件
        window.dispatchEvent(new CustomEvent('page-changed', {
          detail: { newPath, oldPath }
        }))
      }, 100)
    }
  }
  
  // 处理愿望单同步
  if (newPath === '/wishlist') {
    console.log('进入愿望单页面，检查是否需要刷新数据')
    const wishlistUpdated = localStorage.getItem('wishlist_updated') === 'true'
    if (wishlistUpdated) {
      console.log('检测到愿望单有更新，强制刷新数据')
      // 强制刷新愿望单数据
      setTimeout(() => {
        wishlistStore.fetchWishListCount(true)
        // 触发自定义事件
        window.dispatchEvent(new CustomEvent('wishlist-updated'))
      }, 100)
    }
  }
})

// 组件挂载时也进行一次检查
onMounted(() => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  const logined = sessionStorage.getItem('logined') === 'true'
  
  // 如果有token但没有设置登录状态，设置登录状态
  if (token && username && !logined) {
    sessionStorage.setItem('logined', 'true')
  }
  
  // 如果已登录且不在登录页面
  if (token && username && route.path !== '/login') {
    // 设置Header组件的登录状态
    if (appHeaderRef.value) {
      appHeaderRef.value.setLogined(true)
    }
    
    // 加载用户愿望单数据
    wishlistStore.fetchWishListCount()
    
    setTimeout(() => {
      window.dispatchEvent(new CustomEvent('user-logged-in'))
    }, 100)
  }
  
  // 全局监听愿望单更新事件
  window.addEventListener('storage', (event) => {
    if (event.key === 'wishlist_updated' && event.newValue === 'true') {
      console.log('检测到localStorage中愿望单状态更新')
      wishlistStore.fetchWishListCount(true)
    }
  })

  // 初始化AI聊天插件
  if (window._Ai) {
    // 获取用户ID
    let userId = null;
    if (username) {
      // 从localStorage获取用户ID，如果没有则从后端获取
      userId = localStorage.getItem('userId');
      if (!userId) {
        // 这里可以添加获取用户ID的逻辑
        // 例如调用后端API获取用户ID
        // 暂时使用username作为userId
        userId = username;
      }
    }

    window._Ai.Init({
      model: env.VITE_AI_MODEL,
      key: env.VITE_AI_API_KEY,
      img: env.VITE_AI_AVATAR_IMG,
      userId: userId // 添加用户ID
    });

    // 监听登录状态变化
    window.addEventListener('storage', (event) => {
      if (event.key === 'token' || event.key === 'username') {
        const newUsername = localStorage.getItem('username');
        if (newUsername) {
          // 更新AI聊天插件的用户ID
          window._Ai.setUserId(newUsername);
        } else {
          // 用户登出，清除用户ID
          window._Ai.setUserId(null);
        }
      }
    });
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative; /* 添加相对定位，为Live2D提供定位上下文 */
  background: linear-gradient(135deg, #f6f9fc, #e9f1f9, #dce9f5);
}

.main-content {
  flex: 1;
  padding: 20px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

/* 全局Live2D定位样式 */
.global-live2d {
  position: fixed;
  bottom: 20px; /* 增加底部距离 */
  right: 20px; /* 增加右侧距离 */
  z-index: 999; /* 降低层级，确保不会遮挡重要按钮 */
  pointer-events: none; /* 防止Live2D阻挡下方内容的点击 */
  width: 200px; /* 减小宽度 */
  height: 250px; /* 减小高度 */
  transform: scale(0.8); /* 进一步缩小整体大小 */
  transform-origin: bottom right;
  opacity: 0.9; /* 略微增加透明度 */
}

.global-live2d > * {
  pointer-events: auto; /* 恢复Live2D组件本身的交互性 */
}
</style>
