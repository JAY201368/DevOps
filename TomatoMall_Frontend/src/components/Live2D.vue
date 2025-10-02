<template>
  <div class="live2d-container">
    <canvas id="live2d-canvas" @click="handleLive2DClick"></canvas>
    
    <!-- 添加悬浮提示语，根据用户角色和情境显示不同内容 -->
    <transition name="slide-fade">
      <div v-if="isLoggedIn && showTips" class="tips-bubble">
        {{ currentTip }}
        <div class="tips-arrow"></div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import * as live2d from 'live2d-render';
import { ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute();
const isLoggedIn = ref(false);
const showTips = ref(false);
const tipsTimer = ref(null);
const currentTip = ref('');
const isPageSpecificTip = ref(false);
const clickCount = ref(0);
const lastClickTime = ref(0);

// 检查用户是否已登录
const checkLoginStatus = () => {
  const token = localStorage.getItem('token');
  const username = localStorage.getItem('username');
  isLoggedIn.value = !!(token && username);
};

// 获取用户角色
const userRole = computed(() => {
  return localStorage.getItem('userRole') || 'user';
});

// 随机问候语
const greetings = [
  '今天天气真好呢~',
  '欢迎回来，有什么我能帮到你的吗？',
  '别戳我啦，我会害羞的！',
  '今天有什么新发现吗？',
  '你好呀，今天过得怎么样？',
  '想去看看有什么新商品吗？',
  '休息一下吧，别太劳累了~'
];

// 时间相关的问候语
const getTimeBasedGreeting = () => {
  const hour = new Date().getHours();
  if (hour >= 5 && hour < 12) {
    return '早上好！一日之计在于晨，今天也要元气满满哦！';
  } else if (hour >= 12 && hour < 14) {
    return '中午好！是时候吃午饭了，记得休息一下哦~';
  } else if (hour >= 14 && hour < 18) {
    return '下午好！来杯咖啡提提神怎么样？';
  } else if (hour >= 18 && hour < 22) {
    return '晚上好！今天过得怎么样？有什么有趣的发现吗？';
  } else {
    return '这么晚了还在浏览网页？记得注意休息哦，熬夜对身体不好~';
  }
};

// 根据角色获取默认提示语
const getDefaultTip = () => {
  if (userRole.value === 'admin') {
    return '欢迎回来，管理员大人！今天有什么需要处理的事情吗？';
  } else {
    return '欢迎光临！有什么感兴趣的商品吗？';
  }
};

// 获取连续点击的反应
const getClickResponse = () => {
  clickCount.value++;
  
  if (clickCount.value === 1) {
    return '你好啊！';
  } else if (clickCount.value === 2) {
    return '嗯？又点我？';
  } else if (clickCount.value === 3) {
    return '喂喂，不要一直戳我啦！';
  } else if (clickCount.value === 4) {
    return '再点我就要生气了哦！';
  } else if (clickCount.value === 5) {
    return '好吧，你赢了...我不生气了';
  } else if (clickCount.value > 5 && clickCount.value <= 10) {
    return `你已经点了我${clickCount.value}次了，真有耐心！`;
  } else {
    // 点击太多次后重置
    clickCount.value = 0;
    return '我累了，让我休息一下吧...';
  }
};

// 获取随机对话
const getRandomDialogue = () => {
  // 随机选择一个问候语
  return greetings[Math.floor(Math.random() * greetings.length)];
};

// 设置当前显示的提示文本
const setTipText = (text, isPageSpecific = false) => {
  currentTip.value = text;
  isPageSpecificTip.value = isPageSpecific;
};

// 重置提示文本为默认值
const resetTipText = () => {
  if (isPageSpecificTip.value) {
    currentTip.value = getDefaultTip();
    isPageSpecificTip.value = false;
  }
};

// 显示提示，并在一段时间后自动隐藏
const showTipsWithTimeout = (duration = 6000) => {
  // 如果没有设置特定提示，使用默认提示
  if (!isPageSpecificTip.value) {
    setTipText(getDefaultTip());
  }
  
  showTips.value = true;
  
  // 清除之前的定时器
  if (tipsTimer.value) {
    clearTimeout(tipsTimer.value);
  }
  
  // 设置新的定时器，指定时间后自动隐藏提示
  tipsTimer.value = setTimeout(() => {
    showTips.value = false;
    resetTipText();
  }, duration);
};

// 点击Live2D时的处理
const handleLive2DClick = () => {
  if (isLoggedIn.value) {
    // 检查是否是快速连续点击（1秒内）
    const now = Date.now();
    if (now - lastClickTime.value < 1000) {
      setTipText(getClickResponse(), true);
    } else {
      // 不是连续点击，重置计数
      clickCount.value = 0;
      
      // 随机决定显示哪种类型的对话
      const dialogType = Math.random();
      if (dialogType < 0.3) {
        // 30%的概率显示基于时间的问候
        setTipText(getTimeBasedGreeting(), true);
      } else if (dialogType < 0.7) {
        // 40%的概率显示随机对话
        setTipText(getRandomDialogue(), true);
      } else {
        // 30%的概率显示默认问候
        setTipText(getDefaultTip(), false);
      }
    }
    
    lastClickTime.value = now;
    showTipsWithTimeout(5000); // 显示5秒
  }
};

// 监听路由变化，每次页面切换时显示提示5秒
watch(() => route.path, (newPath, oldPath) => {
  if (isLoggedIn.value && newPath !== oldPath && newPath !== '/login') {
    // 根据路由生成特定的页面提示
    let pageTip = '您正在浏览新页面~';
    
    if (newPath.includes('/products') && !newPath.includes('/products/')) {
      pageTip = '这里是商品列表，看看有什么喜欢的商品吧！';
    } else if (newPath.includes('/products/')) {
      pageTip = '这是商品详情页面，仔细看看吧，这个商品不错哦！';
    } else if (newPath.includes('/profile')) {
      pageTip = '这是您的个人信息页面，在这里可以管理您的账户~';
    }
    
    // 设置页面特定提示
    setTipText(pageTip, true);
    showTipsWithTimeout(5000); // 显示5秒
  }
});

// 随机间隔显示对话
const scheduleRandomDialogue = () => {
  if (!isLoggedIn.value || showTips.value) return;
  
  // 随机11-20分钟显示一次
  const randomInterval = 11 * 60 * 1000 + Math.random() * 9 * 60 * 1000;
  
  setTimeout(() => {
    if (isLoggedIn.value && !showTips.value && Math.random() < 0.7) { // 70%概率显示
      setTipText(getRandomDialogue(), true);
      showTipsWithTimeout(5000);
    }
    scheduleRandomDialogue(); // 继续安排下一次随机对话
  }, randomInterval);
};

onMounted(async () => {
  // 检查登录状态
  checkLoginStatus();
  
  // 监听登录事件
  window.addEventListener('storage', (e) => {
    if (e.key === 'token' || e.key === 'username') {
      checkLoginStatus();
    }
  });
  
  // 订阅自定义登录事件
  window.addEventListener('user-logged-in', () => {
    checkLoginStatus();
    if (isLoggedIn.value) {
      setTipText(getTimeBasedGreeting(), true);
      showTipsWithTimeout(5000); // 登录后显示5秒
      // 登录后开始安排随机对话
      scheduleRandomDialogue();
    }
  });
  
  // 订阅页面切换事件
  window.addEventListener('page-changed', (event) => {
    if (isLoggedIn.value) {
      // 根据路由生成特定的页面提示
      let pageTip = '您正在浏览';
      const path = event.detail.newPath;
      
      if (path.includes('/products') && !path.includes('/products/')) {
        pageTip = '这里是商品列表，挑选一些喜欢的商品吧~';
      } else if (path.includes('/products/')) {
        pageTip = '这是商品详情页面，仔细了解商品信息很重要哦！';
      } else if (path.includes('/profile')) {
        pageTip = '这是您的个人信息页面，保持个人信息的安全很重要~';
      }
      
      // 设置页面特定提示
      setTipText(pageTip, true);
      showTipsWithTimeout(5000); // 显示5秒
    }
  });
  
  await live2d.initializeLive2D({
    // live2d 所在区域的背景颜色
    BackgroundRGBA: [0.0, 0.0, 0.0, 0.0],

    // live2d 的 model3.json 文件的相对 根目录 的路径
    ResourcesPath: './rabbit/psd_b.model3.json',

    // live2d 的大小 - 减小尺寸以避免重叠
    CanvasSize: {
      height: 350,
      width: 280
    },

    // 展示工具箱（可以控制 live2d 的展出隐藏，使用特定表情）
    ShowToolBox: true,

    // 是否使用 indexDB 进行缓存优化，这样下一次载入就不会再发起网络请求了
    LoadFromCache: true,
    
    // 指定要使用的Canvas元素ID
    CanvasID: 'live2d-canvas'
  });

  console.log('Live2D 加载完成');
  
  // 初始登录状态检查完成后，如果已登录则显示提示
  if (isLoggedIn.value) {
    // 延迟显示提示，确保Live2D已加载完成
    setTimeout(() => {
      setTipText(getTimeBasedGreeting(), true);
      showTipsWithTimeout(5000); // 显示5秒
      // 开始安排随机对话
      scheduleRandomDialogue();
    }, 1000);
  }
});

onBeforeUnmount(() => {
  // 组件卸载前清除定时器
  if (tipsTimer.value) {
    clearTimeout(tipsTimer.value);
  }
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

/* 提示气泡样式 - 放置在左上方 */
.tips-bubble {
  position: absolute;
  top: -65px; /* 向上移动 */
  left: 100px; /* 适当向右移动，但不超出视口 */
  max-width: 180px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  font-size: 12px;
  color: #333;
  line-height: 1.5;
  z-index: 99;
  border: 1px solid rgba(200, 200, 200, 0.3);
}

.tips-arrow {
  position: absolute;
  bottom: -8px;
  left: 20px; /* 箭头位置 */
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-top: 8px solid rgba(255, 255, 255, 0.95);
}

/* 提示气泡动画 */
.slide-fade-enter-active {
  transition: all 0.5s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s ease-in;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px); /* 修改为垂直方向动画 */
  opacity: 0;
}
</style>
