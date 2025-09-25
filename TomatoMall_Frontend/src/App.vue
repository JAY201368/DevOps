<template>
  <div class="app">
    <AppHeader ref="appHeaderRef" />
    <router-view />
    
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

const appHeaderRef = ref(null)
provide('appHeaderRef', appHeaderRef)

const route = useRoute()
const router = useRouter()

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
})

// 组件挂载时也进行一次检查
onMounted(() => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  if (token && username && route.path !== '/login') {
    setTimeout(() => {
      window.dispatchEvent(new CustomEvent('user-logged-in'))
    }, 100)
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
  background-color: #f5f7fa;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative; /* 添加相对定位，为Live2D提供定位上下文 */
}

.main-content {
  flex: 1;
  padding: 20px;
}

/* 全局Live2D定位样式 */
.global-live2d {
  position: fixed;
  bottom: 0;
  right: 30px;
  z-index: 1000;
  pointer-events: none; /* 防止Live2D阻挡下方内容的点击 */
  width: 280px;
  height: 350px;
  transform: scale(0.95); /* 额外微调整体大小 */
  transform-origin: bottom right;
  opacity: 0.95; /* 略微透明以减少视觉干扰 */
}

.global-live2d > * {
  pointer-events: auto; /* 恢复Live2D组件本身的交互性 */
}
</style>
