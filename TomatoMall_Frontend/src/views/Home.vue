<template>
  <div class="home">
    <!-- 轮播图 -->
    <div class="banner-section">
      <el-carousel height="400px" :interval="5000" arrow="always" indicator-position="outside">
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="banner-content">
            <div class="banner-info">
              <h2 class="banner-title">{{ banner.title }}</h2>
              <p class="banner-description">{{ banner.description }}</p>
              <el-button type="primary" @click="navigateTo(banner)">查看详情</el-button>
            </div>
            <div class="banner-books">
              <div v-if="banner.books.length === 0" class="no-books-message">
                <el-empty description="加载中..." :image-size="100"></el-empty>
              </div>
              <div v-else class="book-carousel" :class="{ 'few-books': banner.books.length < 4 }">
                <div v-for="(book, bookIndex) in banner.books" :key="bookIndex" class="book-item" @click="navigateToBook(book)">
                  <div class="book-cover">
                    <img :src="book.cover" :alt="book.title" class="book-image" />
                  </div>
                  <div class="book-info">
                    <div class="book-title">{{ book.title }}</div>
                    <div class="book-price">¥{{ book.price }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    
    <!-- 热门推荐组件 -->
    <div class="recommendation-section container">
      <BookRecommendation 
        title="热门图书"
        description="当前最受欢迎的图书"
        type="popular"
        :limit="12"
        @loaded="recommendationLoaded('popular', $event)"
      />
    </div>
    
    <!-- AI助手按钮 -->
    <div class="ai-assistant-container container">
      <el-card class="ai-assistant-card">
        <div class="ai-assistant-content">
          <div class="ai-assistant-info">
            <h3>智能AI助手</h3>
            <p>有任何问题都可以向我咨询，我会为您提供专业的解答</p>
            <el-button type="primary" @click="openAiChat">开始对话</el-button>
          </div>
          <div class="ai-assistant-icon">
            <el-icon :size="40" color="#1890ff"><ChatDotRound /></el-icon>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ChatDotRound } from '@element-plus/icons-vue';
import BookRecommendation from '../components/BookRecommendation.vue';
import { getPopularRecommendations } from '../api/recommendation';
import { getAllBanners } from '../api/banner';

const router = useRouter();

// 轮播图数据
const banners = ref([]);

// AI助手相关方法
const openAiChat = () => {
  if (window._Ai) {
    window._Ai.send("你好，我想找一些好书推荐，有什么热门小说可以推荐吗？");
  } else {
    ElMessage.warning('AI助手加载失败，请刷新页面重试');
  }
};

// 导航到指定链接
const navigateTo = (banner) => {
  console.log('点击了轮播图:', banner);
  
  // 如果banner有ID，则导航到该轮播图的商品列表页
  if (banner.id) {
    console.log(`跳转到轮播图商品列表页: /banner-products/${banner.id}`);
    router.push(`/banner-products/${banner.id}`);
  } else {
    // 如果没有ID（比如使用默认数据时），则使用原来的链接
    console.log(`跳转到默认链接: ${banner.link || '/products'}`);
    router.push(banner.link || '/products');
  }
};

// 导航到指定书籍详情页
const navigateToBook = (book) => {
  console.log('点击了书籍:', book);
  console.log(`跳转到书籍详情页: /products/${book.id}`);
  router.push(`/products/${book.id}`);
};

// 监听推荐组件加载状态
const recommendationLoaded = (type, data) => {
  console.log(`${type}推荐组件加载完成:`, data);
  if (data.success) {
    console.log(`${type}推荐加载成功，共${data.count}本书`);
  } else {
    console.error(`${type}推荐加载失败:`, data.error);
    ElMessage.warning(`${type === 'personalized' ? '个性化' : '热门'}推荐加载失败，请稍后再试`);
  }
};

// 获取轮播图数据
const fetchBanners = async () => {
  try {
    const response = await getAllBanners();
    if (response.code === '200' && response.data) {
      banners.value = response.data;
      console.log('轮播图数据加载成功:', banners.value);
    } else {
      console.error('获取轮播图数据失败:', response.msg);
      // 如果获取失败，使用默认数据
      useFallbackBanners();
    }
  } catch (error) {
    console.error('获取轮播图数据出错:', error);
    // 如果出错，使用默认数据
    useFallbackBanners();
  }
};

// 使用默认轮播图数据
const useFallbackBanners = async () => {
  // 默认轮播图数据
  banners.value = [
    {
      title: '畅销新书',
      description: '发现本月最受欢迎的图书',
      link: '/products',
      books: []
    },
    {
      title: '特价优惠',
      description: '限时折扣，错过等一年',
      link: '/products',
      books: []
    },
    {
      title: '文学经典',
      description: '经典永不过时',
      link: '/products',
      books: []
    }
  ];
  
  // 获取书籍数据
  await fetchBannerBooks();
};

// 获取轮播图中显示的书籍（仅在使用默认数据时调用）
const fetchBannerBooks = async () => {
  try {
    // 获取更多的热门书籍用于轮播图，确保有足够的不重复书籍
    const response = await getPopularRecommendations(30);
    console.log('获取到的书籍数据:', response);
    
    if (response.code === '200' && response.data) {
      const books = response.data;
      console.log('获取到书籍总数:', books.length);
      
      if (books.length > 0) {
        // 打乱书籍顺序，以便每个轮播图显示不同的书籍
        const shuffledBooks = [...books].sort(() => Math.random() - 0.5);
        
        // 确保有足够的不重复书籍
        if (shuffledBooks.length >= 12) {
          // 为每个轮播图分配不同的书籍
          banners.value[0].books = shuffledBooks.slice(0, 4);
          banners.value[1].books = shuffledBooks.slice(4, 8);
          banners.value[2].books = shuffledBooks.slice(8, 12);
        } else {
          // 如果书籍不足12本，尽量不重复
          const uniqueBooks = [...new Map(shuffledBooks.map(book => [book.id, book])).values()];
          
          // 计算每个轮播图可以分配的书籍数量
          const booksPerBanner = Math.floor(uniqueBooks.length / 3);
          
          if (booksPerBanner > 0) {
            banners.value[0].books = uniqueBooks.slice(0, booksPerBanner);
            banners.value[1].books = uniqueBooks.slice(booksPerBanner, booksPerBanner * 2);
            banners.value[2].books = uniqueBooks.slice(booksPerBanner * 2);
          } else {
            // 如果实在书籍太少，每个轮播图至少显示一本
            banners.value.forEach((banner, index) => {
              banner.books = [uniqueBooks[index % uniqueBooks.length]];
            });
          }
        }
        
        console.log('轮播图书籍加载成功');
      } else {
        console.error('没有获取到任何书籍数据');
      }
    } else {
      console.error('获取轮播图书籍失败:', response.msg);
    }
  } catch (error) {
    console.error('获取轮播图书籍出错:', error);
  }
};

onMounted(() => {
  console.log('首页组件已加载');
  fetchBanners();
});
</script>

<style scoped>
.home {
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.banner-section {
  margin-bottom: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

/* AI助手相关样式 */
.ai-assistant-container {
  margin: 30px auto;
}

.ai-assistant-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background: linear-gradient(to right, #ffeef2, #fff5f7);
  overflow: hidden;
}

.ai-assistant-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.ai-assistant-info {
  flex: 1;
}

.ai-assistant-info h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #303133;
}

.ai-assistant-info p {
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
}

.ai-assistant-icon {
  margin-left: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background-color: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.banner-content {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 40px;
  background: linear-gradient(to right, #ffeef2, #fff5f7);
  position: relative;
  border-radius: 16px;
  overflow: hidden;
}

.banner-info {
  flex: 0 0 25%;
  padding: 25px;
  border-radius: 20px;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  z-index: 2;
}

.banner-books {
  flex: 0 0 75%;
  padding-left: 30px;
  z-index: 1;
}

.book-carousel {
  display: flex;
  gap: 20px;
  overflow: hidden;
  justify-content: flex-start;
}

.book-item {
  flex: 0 0 calc(25% - 15px);
  max-width: calc(25% - 15px);
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  border: 1px solid #ffebee;
}

.book-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(255, 182, 193, 0.3);
}

.book-cover {
  height: 180px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff9fa;
  overflow: hidden;
  border-bottom-left-radius: 30px;
}

.book-image {
  height: 100%;
  width: 100%;
  object-fit: contain;
}

.book-info {
  padding: 12px;
}

.book-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.banner-title {
  font-size: 28px;
  margin-bottom: 10px;
  color: #303133;
}

.banner-description {
  font-size: 16px;
  margin-bottom: 20px;
  color: #606266;
}

.recommendation-section {
  margin: 40px auto;
}

.no-books-message {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

@media (max-width: 992px) {
  .banner-content {
    flex-direction: column;
    padding: 20px;
  }
  
  .banner-info {
    flex: 0 0 auto;
    width: 100%;
    margin-bottom: 20px;
  }
  
  .banner-books {
    flex: 0 0 auto;
    width: 100%;
    padding-left: 0;
  }
  
  .book-item {
    flex: 0 0 calc(50% - 10px);
  }
  
  .book-carousel {
    gap: 10px;
  }
  
  /* AI助手响应式 */
  .ai-assistant-content {
    flex-direction: column;
    text-align: center;
  }
  
  .ai-assistant-icon {
    margin-left: 0;
    margin-top: 15px;
  }
}

@media (max-width: 768px) {
  .banner-content {
    padding: 15px;
  }
  
  .banner-info {
    padding: 20px;
  }
  
  .banner-title {
    font-size: 24px;
  }
  
  .book-cover {
    height: 150px;
  }
}

@media (max-width: 480px) {
  .book-item {
    flex: 0 0 100%;
  }
  
  .book-carousel {
    flex-wrap: wrap;
  }
}

/* 当书籍数量少于4本时的样式 */
.book-carousel.few-books {
  justify-content: center;
}

.book-carousel.few-books .book-item {
  flex: 0 0 calc(33.33% - 15px);
  max-width: calc(33.33% - 15px);
}

/* 修改轮播图指示器样式 */
:deep(.el-carousel__indicators) {
  transform: translateY(10px);
}

:deep(.el-carousel__indicator) {
  padding: 8px;
}

:deep(.el-carousel__button) {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: rgba(255, 182, 193, 0.5);
  transition: all 0.3s;
}

:deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background-color: rgba(255, 105, 180, 0.8);
  transform: scale(1.2);
}

/* 修改轮播图箭头样式 */
:deep(.el-carousel__arrow) {
  border-radius: 50%;
  background-color: rgba(255, 182, 193, 0.7);
}

:deep(.el-carousel__arrow:hover) {
  background-color: rgba(255, 105, 180, 0.9);
}

/* 修改按钮样式 */
.banner-info .el-button {
  border-radius: 20px;
  padding: 10px 20px;
}
</style>