<template>
  <div class="ads-recommend-page">
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载推荐内容...</p>
    </div>

    <div v-else-if="error" class="error-message">
      <i class="el-icon-warning"></i>
      <p>{{ error }}</p>
      <button @click="fetchAdvertisements" class="btn-retry">重新加载</button>
    </div>

    <div v-else-if="advertisements.length === 0" class="empty-state">
      <div class="empty-icon">📢</div>
      <h3>暂无推荐内容</h3>
      <p>敬请期待更多精彩推荐！</p>
    </div>

    <div v-else class="ads-grid">
      <div v-for="ad in advertisements" :key="ad.id" class="ad-card-wrapper" @click="goToProduct(ad.productId)">
        <div class="ad-card">
          <div class="ad-image-container">
            <img :src="ad.imgUrl" alt="广告图片" class="ad-image" @error="handleAdImageError($event, ad.id)" />
            <div v-if="adImageErrors[ad.id]" class="image-error-overlay">
              <i class="el-icon-picture-outline"></i>
              <span>图片加载失败</span>
            </div>
          </div>
          <div class="ad-title">{{ ad.title }}</div>
          
          <!-- 悬停时显示的详细内容 -->
          <div class="ad-hover-content">
            <p>{{ ad.content }}</p>
            <div class="ad-cta">点击查看商品详情</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getAdvertisements } from '../api/advertisement';

export default {
  name: 'AdsRecommend',
  setup() {
    const router = useRouter();
    const advertisements = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const adImageErrors = reactive({});

    const fetchAdvertisements = async () => {
      loading.value = true;
      error.value = null;
      
      try {
        console.log('开始获取广告推荐');
        const response = await getAdvertisements();
        console.log('获取广告响应:', response);
        
        if (response.code === '200') {
          advertisements.value = response.data;
          console.log('成功获取广告数据:', advertisements.value.length);
        } else {
          error.value = response.msg || '获取推荐内容失败';
          console.error('获取广告失败:', response.msg);
        }
      } catch (err) {
        error.value = '获取推荐内容失败，请稍后重试';
        console.error('获取广告异常:', err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(fetchAdvertisements);

    const handleAdImageError = (event, adId) => {
      console.log('广告图片加载失败:', adId);
      adImageErrors[adId] = true;
    };

    const goToProduct = (productId) => {
      console.log('跳转到商品页面:', productId);
      router.push(`/products/${productId}`);
    };

    return {
      advertisements,
      loading,
      error,
      adImageErrors,
      fetchAdvertisements,
      handleAdImageError,
      goToProduct
    };
  }
};
</script>

<style scoped>
.ads-recommend-page {
  padding: 30px;
  max-width: 1280px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  background: #f9f9f9;
  border-radius: 8px;
  margin: 20px 0;
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #2196F3;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message, .empty-state {
  text-align: center;
  padding: 60px 30px;
  background: #f9f9f9;
  border-radius: 12px;
  margin: 20px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.error-message i {
  font-size: 36px;
  color: #f44336;
  margin-bottom: 16px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  margin-bottom: 12px;
  color: #333;
}

.empty-state p {
  color: #666;
  margin-bottom: 24px;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.btn-retry {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 16px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn-retry:hover {
  background-color: #1e88e5;
}

.ads-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
}

.ad-card-wrapper {
  position: relative;
  cursor: pointer;
}

.ad-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.ad-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}

.ad-image-container {
  position: relative;
  height: 250px;
  overflow: hidden;
}

.ad-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
  display: block;
}

.ad-card:hover .ad-image {
  transform: scale(1.05);
}

.ad-title {
  padding: 16px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
}

.image-error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(245, 247, 250, 0.9);
  color: #909399;
}

.image-error-overlay i {
  font-size: 40px;
  margin-bottom: 10px;
}

/* 悬停内容样式 */
.ad-hover-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  display: flex;
  flex-direction: column;
  padding: 24px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  border-radius: 16px;
  justify-content: center;
}

.ad-card:hover .ad-hover-content {
  opacity: 1;
}

.ad-hover-content p {
  font-size: 18px;
  line-height: 1.6;
  flex-grow: 1;
  overflow: hidden;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
}

.ad-cta {
  margin-top: auto;
  font-weight: 500;
  font-size: 16px;
  color: #4caf50;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
}

@media (max-width: 768px) {
  .ads-recommend-page {
    padding: 20px;
  }
  
  .ads-grid {
    grid-template-columns: 1fr;
  }
}
</style> 