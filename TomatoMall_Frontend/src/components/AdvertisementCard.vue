<template>
  <div class="advertisement-card" @click="goToProduct">
    <img :src="advertisement.imgUrl" alt="广告图片" class="ad-image" @error="handleImageError" />
    <div v-if="imageError" class="image-error">
      <i class="el-icon-picture"></i>
      <span>图片加载失败</span>
    </div>
    <div class="ad-content">
      <h3>{{ advertisement.title }}</h3>
      <p>{{ advertisement.content }}</p>
      <div class="product-link">关联产品ID: {{ advertisement.productId }}</div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

export default {
  name: 'AdvertisementCard',
  props: {
    advertisement: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const router = useRouter();
    const imageError = ref(false);

    const goToProduct = () => {
      console.log('跳转到商品页面:', props.advertisement.productId);
      router.push(`/products/${props.advertisement.productId}`);
    };
    
    const handleImageError = () => {
      console.log('广告图片加载失败');
      imageError.value = true;
    };

    return {
      goToProduct,
      imageError,
      handleImageError
    };
  }
}
</script>

<style scoped>
.advertisement-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  background-color: #fff;
}

.advertisement-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.ad-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.image-error {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.image-error i {
  font-size: 48px;
  margin-bottom: 12px;
}

.ad-content {
  padding: 15px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.ad-content h3 {
  margin-top: 0;
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
}

.ad-content p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  margin: 0 0 12px 0;
  flex-grow: 1;
}

.product-link {
  font-size: 12px;
  color: #909399;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}
</style> 