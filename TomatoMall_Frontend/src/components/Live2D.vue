<template>
  <div class="live2d-container" v-if="isLoggedIn">
    <canvas id="live2d-canvas" @click="handleLive2DClick"></canvas>
  </div>
</template>

<script setup>
import { onMounted, ref, onBeforeUnmount, watch } from 'vue';
import { useRoute } from 'vue-router';
import * as live2d from 'live2d-render';
import env from '../config/env'

// 是否已登录
const isLoggedIn = ref(false);
const route = useRoute();

// 检查用户是否已登录
const checkLoginStatus = () => {
  const token = localStorage.getItem('token');
  const username = localStorage.getItem('username');
  isLoggedIn.value = !!(token && username);
};

// 显示消息
const showMessage = (message, duration = 3000) => {
  try {
    live2d.setMessageBox(message, duration);
  } catch (e) {
    console.log('显示消息失败:', e);
  }
};

// 设置随机表情
const setRandomExpression = () => {
  try {
    live2d.setRandomExpression();
  } catch (e) {
    console.log('设置随机表情失败:', e);
  }
};

// 根据路径获取页面相关消息
const getPageMessage = (path) => {
  if (path.includes('/products') && !path.includes('/products/')) {
    return '这里是商品列表，看看有什么喜欢的书籍吧！';
  } else if (path.includes('/products/')) {
    return '这是商品详情页面，这本书看起来不错哦！';
  } else if (path.includes('/cart')) {
    return '这是购物车，准备好结算了吗？';
  } else if (path.includes('/wishlist')) {
    return '这是您的愿望单，收藏了不少好书呢！';
  } else if (path.includes('/profile')) {
    return '这是您的个人信息页面，请保管好您的账户信息~';
  } else if (path.includes('/orders')) {
    return '这是您的订单列表，感谢您的购买！';
  } else if (path.includes('/ads-recommend')) {
    return '这里有专为您推荐的好书，不妨看看有没有感兴趣的！';
  } else if (path.includes('/user-coupons')) {
    return '这是您的促销券页面，使用这些优惠券可以省下不少钱哦！';
  } else if (path.includes('/advertisements')) {
    return '这是广告管理页面，您可以在这里管理所有广告内容。';
  } else if (path.includes('/coupons')) {
    return '这是促销券管理页面，您可以在这里创建和管理促销券。';
  } else if (path === '/') {
    return '欢迎来到番茄书城首页！有什么可以帮您找的吗？';
  } else {
    return '您正在浏览新页面，需要帮助吗？';
  }
};

// 点击Live2D时的处理
const handleLive2DClick = () => {
  setRandomExpression();
  showMessage('你好！有什么可以帮助你的吗？', 3000);
};

// 初始化Live2D
const initLive2D = async () => {
  try {
    await live2d.initializeLive2D({
      // live2d 所在区域的背景颜色
      BackgroundRGBA: [0.0, 0.0, 0.0, 0.0],

      // live2d 的 model3.json 文件的相对 根目录 的路径
      ResourcesPath: env.VITE_LIVE2D_MODEL_PATH,

      // live2d 的大小
      CanvasSize: {
        height: env.VITE_LIVE2D_CANVAS_HEIGHT,
        width: env.VITE_LIVE2D_CANVAS_WIDTH
      },

      // 展示工具箱（可以控制 live2d 的展出隐藏，使用特定表情）
      ShowToolBox: env.VITE_LIVE2D_SHOW_TOOLBOX,

      // 是否使用 indexDB 进行缓存优化
      LoadFromCache: env.VITE_LIVE2D_USE_CACHE,
      
      // 指定要使用的Canvas元素ID
      CanvasID: 'live2d-canvas'
    });

    console.log('Live2D 加载完成');
    
    // 初始化完成后显示欢迎消息
    setTimeout(() => {
      showMessage('欢迎来到番茄书城！', 3000);
    }, 1000);
  } catch (error) {
    console.error('Live2D 加载失败:', error);
  }
};

// 监听路由变化，根据不同页面显示不同消息
watch(() => route.path, (newPath, oldPath) => {
  if (isLoggedIn.value && newPath !== oldPath) {
    // 路径变化时，设置随机表情并显示页面相关消息
    setTimeout(() => {
      setRandomExpression();
      showMessage(getPageMessage(newPath), 4000);
    }, 500); // 延迟500ms，确保页面已经加载
  }
});

// 监听自定义页面切换事件
const handlePageChanged = (event) => {
  if (isLoggedIn.value) {
    const path = event.detail.newPath;
    setTimeout(() => {
      setRandomExpression();
      showMessage(getPageMessage(path), 4000);
    }, 500);
  }
};

onMounted(() => {
  // 初始检查登录状态
  checkLoginStatus();
  
  // 如果已登录，初始化Live2D
  if (isLoggedIn.value) {
    initLive2D();
    
    // 初始显示当前页面消息
    setTimeout(() => {
      showMessage(getPageMessage(route.path), 4000);
    }, 2000);
  }
  
  // 监听登录状态变化
  window.addEventListener('storage', (e) => {
    if (e.key === 'token' || e.key === 'username') {
      const wasLoggedIn = isLoggedIn.value;
      checkLoginStatus();
      
      // 如果从未登录变为已登录，初始化Live2D
      if (!wasLoggedIn && isLoggedIn.value) {
        initLive2D();
      }
    }
  });
  
  // 订阅自定义登录事件
  window.addEventListener('user-logged-in', () => {
    const wasLoggedIn = isLoggedIn.value;
    checkLoginStatus();
    
    // 如果从未登录变为已登录，初始化Live2D
    if (!wasLoggedIn && isLoggedIn.value) {
      initLive2D();
    }
  });
  
  // 订阅页面切换事件
  window.addEventListener('page-changed', handlePageChanged);
});

onBeforeUnmount(() => {
  // 隐藏消息框
  try {
    live2d.hideMessageBox();
  } catch (e) {
    console.log('隐藏消息框失败:', e);
  }
  
  // 移除事件监听
  window.removeEventListener('page-changed', handlePageChanged);
});
</script>

<style scoped>
.live2d-container {
  width: 280px;
  height: 350px;
  position: relative;
}

#live2d-canvas {
  width: 100%;
  height: 100%;
  cursor: pointer; /* 添加指针样式表明可点击 */
}

/* 自定义Live2D消息框样式 */
:global(#live2dMessageBox-content) {
  background-color: rgba(255, 245, 247, 0.95);
  color: #303133;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  padding: 10px;
  height: fit-content;
  border-radius: 12px;
  word-break: break-all;
  border: 1px solid rgba(255, 182, 193, 0.3);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  font-size: 12px;
}

:global(.live2dMessageBox-content-hidden) {
  opacity: 0;
  transform: scaleY(0.2);
  transition: all 0.35s ease-in;
}

:global(.live2dMessageBox-content-visible) {
  opacity: 1;
  transition: all 0.35s ease-out;
}
</style>
