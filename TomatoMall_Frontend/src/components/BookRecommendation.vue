<template>
  <div class="book-recommendation">
    <div class="recommendation-header">
      <h2 class="section-title">
        <el-icon><StarFilled /></el-icon>
        {{ title }}
      </h2>
      <div class="recommendation-description">{{ description }}</div>
    </div>

    <div v-if="loading" class="recommendation-loading">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="books.length === 0" class="recommendation-empty">
      <el-empty description="暂无推荐书籍" />
    </div>

    <div v-else class="recommendation-grid">
      <el-card 
        v-for="book in books" 
        :key="book.id" 
        class="book-card"
        shadow="hover"
        @click="viewBookDetail(book.id)"
      >
        <div class="book-cover">
          <el-image 
            :src="book.cover" 
            fit="contain"
            :lazy="true"
            class="cover-image"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        
        <div class="book-info">
          <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
          
          <div class="book-rating">
            <template v-if="book.rate !== null && book.rate !== undefined">
              <el-rate 
                v-model="book.rate" 
                disabled 
                :max="5"
                :allow-half="true"
                :colors="['#FF9900', '#FF9900', '#FF9900']"
              />
              <span class="rating-value">{{ Number(book.rate).toFixed(1) }}分</span>
            </template>
            <template v-else>
              <span class="no-rating">暂无评分</span>
            </template>
          </div>
          
          <div class="book-price">¥{{ book.price }}</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineEmits } from 'vue';
import { useRouter } from 'vue-router';
import { StarFilled, Picture } from '@element-plus/icons-vue';
import { getPersonalizedRecommendations, getPopularRecommendations } from '../api/recommendation';

const props = defineProps({
  title: {
    type: String,
    default: '为您推荐'
  },
  description: {
    type: String,
    default: '根据您的阅读偏好推荐'
  },
  type: {
    type: String,
    default: 'personalized', // 'personalized' 或 'popular'
    validator: (value) => ['personalized', 'popular'].includes(value)
  },
  limit: {
    type: Number,
    default: 9
  }
});

// 定义事件
const emit = defineEmits(['loaded']);

const router = useRouter();
const books = ref([]);
const loading = ref(true);

// 获取推荐书籍
const fetchRecommendations = async () => {
  loading.value = true;
  try {
    console.log(`正在获取${props.type}类型的推荐书籍...`);
    let response;
    
    if (props.type === 'personalized') {
      response = await getPersonalizedRecommendations(props.limit);
    } else {
      response = await getPopularRecommendations(props.limit);
    }
    
    console.log(`获取${props.type}推荐响应:`, response);
    
    if (response.code === '200' && response.data) {
      books.value = response.data.map(book => ({
        ...book,
        rate: book.rate !== null && book.rate !== undefined ? Number(book.rate) : null // 数据库已经是5分制，直接使用
      }));
      console.log(`成功获取${books.value.length}本推荐书籍`);
      // 发射加载成功事件
      emit('loaded', { success: true, count: books.value.length });
    } else {
      console.error(`获取${props.type}推荐失败:`, response.msg || '未知错误');
      // 发射加载失败事件
      emit('loaded', { success: false, error: response.msg || '未知错误' });
    }
  } catch (error) {
    console.error(`获取${props.type}推荐书籍失败:`, error);
    // 发射加载失败事件
    emit('loaded', { success: false, error: error.message || '网络错误' });
  } finally {
    loading.value = false;
  }
};

// 查看书籍详情
const viewBookDetail = (bookId) => {
  router.push(`/products/${bookId}`);
};

onMounted(() => {
  fetchRecommendations();
});
</script>

<style scoped>
.book-recommendation {
  margin: 20px 0;
}

.recommendation-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.recommendation-description {
  color: #909399;
  font-size: 14px;
}

.recommendation-loading,
.recommendation-empty {
  padding: 20px 0;
}

.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.book-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.book-cover {
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}

.cover-image {
  height: 100%;
  width: 100%;
  object-fit: contain;
}

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  font-size: 48px;
  color: #c0c4cc;
}

.book-info {
  padding: 10px 0;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.book-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-rating {
  margin: 10px 0;
  display: flex;
  align-items: center;
}

.rating-value {
  margin-left: 8px;
  color: #FF9900;
  font-weight: 500;
  font-size: 14px;
}

.no-rating {
  color: #909399;
  font-size: 14px;
  font-style: italic;
}

.book-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

@media (max-width: 992px) {
  .recommendation-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .recommendation-grid {
    grid-template-columns: 1fr;
  }
}
</style> 