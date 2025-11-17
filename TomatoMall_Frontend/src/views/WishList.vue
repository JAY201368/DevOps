<template>
  <div class="wishlist-container">
    <el-card class="wishlist-card">
      <template #header>
        <div class="card-header">
          <span class="page-title">
            <el-icon><Star /></el-icon> 我的愿望单
          </span>
          <span class="book-count">共 {{ wishList.length }} 本图书</span>
          <el-button type="primary" size="small" @click="refreshWishList">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="wishList.length === 0" class="empty-wishlist">
        <el-empty description="您的愿望单还是空的">
          <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
        </el-empty>
      </div>
      
      <div v-else class="wishlist-grid">
        <el-card v-for="item in wishList" :key="item.bookId" class="book-card">
          <template #header>
            <div class="book-header">
              <h3 class="book-title">{{ item.book?.title || '未知书名' }}</h3>
              <el-button
                type="danger"
                circle
                size="small"
                :loading="isRemoving(item.bookId)"
                @click="handleRemoveFromWishList(item.bookId)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div class="book-content">
            <div class="book-image">
              <el-image
                v-if="item.book?.cover"
                :src="item.book.cover"
                fit="contain"
                @click="viewBookDetail(item.bookId)"
              />
              <el-empty v-else description="暂无图片" />
            </div>
            
            <div class="book-info">
              <div class="book-price">
                <span class="price-label">价格：</span>
                <span class="price-value">¥{{ item.book?.price || '暂无' }}</span>
              </div>
                <div class="book-actions">
                <el-button type="primary" @click="handleAddToCart(item.bookId)">
                  <el-icon><ShoppingCart /></el-icon> 加入购物车
                </el-button>
                <el-button @click="viewBookDetail(item.bookId)">查看详情</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, Delete, ShoppingCart, Refresh } from '@element-plus/icons-vue'
import { getWishList, removeFromWishList as removeBook } from '../api/wishlist'
import { addToCart as addToCartApi } from '../api/cart'
import { getProductById } from '../api/product'
import { useWishListStore } from '../store/wishlist'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const wishList = ref([])
const removingIds = ref(new Set())
const isRemoving = (bookId) => {
  if (!(removingIds.value instanceof Set)) {
    console.warn('removingIds不是Set对象，重新初始化');
    removingIds.value = new Set();
    return false;
  }
  return removingIds.value.has(bookId);
}
const addToRemoving = (bookId) => {
  if (!(removingIds.value instanceof Set)) {
    console.warn('removingIds不是Set对象，重新初始化');
    removingIds.value = new Set();
  }
  removingIds.value.add(bookId);
}
const removeFromRemoving = (bookId) => {
  if (!(removingIds.value instanceof Set)) {
    console.warn('removingIds不是Set对象，重新初始化');
    removingIds.value = new Set();
    return;
  }
  removingIds.value.delete(bookId);
}
const addingToCartIds = ref(new Set())
const wishlistStore = useWishListStore()
const fetchingData = ref(false)

// 获取愿望单列表
const fetchWishList = async () => {
  if (fetchingData.value) {
    console.log('已有请求在进行中，跳过此次刷新')
    return
  }
  
  fetchingData.value = true
  loading.value = true;
  try {
    console.log('开始获取愿望单数据...');
    await wishlistStore.fetchWishListCount(true);
    
    if (wishlistStore.wishlistItems.length > 0) {
      console.log('使用store中的愿望单数据:', wishlistStore.wishlistItems);
      wishList.value = await Promise.all(
        wishlistStore.wishlistItems.map(async (bookId) => {
          try {
            const bookResponse = await getProductById(bookId);
            return {
              bookId,
              book: bookResponse.code === '200' ? bookResponse.data : null
            };
          } catch (error) {
            console.error('获取图书信息失败:', error);
            return { bookId };
          }
        })
      );
      console.log('愿望单数据处理完成，共', wishList.value.length, '项');
    } else {
      const response = await getWishList();
      if (response.code === '200') {
        console.log('获取愿望单数据成功:', response.data);
        wishList.value = await Promise.all(
          response.data.map(async (item) => {
            try {
              const bookResponse = await getProductById(item.bookId);
              return {
                ...item,
                book: bookResponse.code === '200' ? bookResponse.data : null
              };
            } catch (error) {
              console.error('获取图书信息失败:', error);
              return item;
            }
          })
        );
        console.log('愿望单数据处理完成，共', wishList.value.length, '项');
      } else {
        ElMessage.error(response.msg || '获取愿望单失败');
      }
    }
  } catch (error) {
    console.error('获取愿望单失败:', error);
    ElMessage.error('获取愿望单失败，请稍后重试');
  } finally {
    loading.value = false;
    setTimeout(() => {
      fetchingData.value = false
    }, 1000)
  }
};

// 刷新愿望单
const refreshWishList = () => {
  fetchWishList()
}

// 从愿望单中移除
const handleRemoveFromWishList = async (bookId) => {
  if (isRemoving(bookId)) {
    console.log('该商品正在删除中，跳过操作:', bookId);
    return;
  }
  
  console.log('尝试从愿望单移除商品:', bookId);
  addToRemoving(bookId);
  
  try {
    if (!bookId) {
      console.error('无效的商品ID');
      ElMessage.error('移除失败：无效的商品ID');
      return;
    }
    
    console.log('发送移除愿望单请求:', bookId);
    const response = await removeBook(bookId);
    console.log('移除愿望单响应:', response);
    
    if (response && response.code === '200') {
      ElMessage.success('已从愿望单移除');
      wishList.value = wishList.value.filter(item => item.bookId !== bookId);
      
      wishlistStore.removeFromWishList(bookId);
      
      setTimeout(() => {
        wishlistStore.fetchWishListCount(true);
      }, 300);
    } else {
      console.error('移除失败，响应:', response);
      ElMessage.error(response?.msg || '移除失败');
    }
  } catch (error) {
    console.error('移除失败:', error);
    ElMessage.error('移除失败，请稍后重试');
  } finally {
    removeFromRemoving(bookId);
  }
}

// 加入购物车
const handleAddToCart = async (bookId) => {
  try {
    const response = await addToCartApi({ productId: bookId, quantity: 1 })
    if (response.code === '200') {
      ElMessage.success('已添加到购物车')
    } else {
      ElMessage.error(response.msg || '添加购物车失败')
    }
  } catch (error) {
    console.error('添加购物车失败:', error)
    ElMessage.error('添加购物车失败，请稍后重试')
  }
}

// 查看图书详情
const viewBookDetail = (bookId) => {
  router.push(`/products/${bookId}`)
}

// 监听路由变化
watch(
  () => route.fullPath,
  () => {
    console.log('路由变化，刷新愿望单数据')
    if (route.query && Object.keys(route.query).length > 0) {
    fetchWishList()
  }
  }
)

// 监听愿望单状态变化 - 优化刷新逻辑
watch(() => wishlistStore.lastUpdated, (newTimestamp, oldTimestamp) => {
  console.log('wishlistStore时间戳变化:', newTimestamp)
  if (newTimestamp && oldTimestamp && newTimestamp - oldTimestamp > 1000) {
    console.log('愿望单状态变化，刷新列表')
    fetchWishList()
  }
})

onMounted(() => {
  console.log('WishList组件挂载')
  
  // 确保removingIds是Set对象
  if (!(removingIds.value instanceof Set)) {
    console.log('重新初始化removingIds为Set对象');
    removingIds.value = new Set();
  }
  
  fetchWishList()
})

onUnmounted(() => {
  // 清理工作
  removingIds.value = new Set();
})
</script>

<style scoped>
.wishlist-container {
  padding: 20px;
  background: linear-gradient(to bottom, #f0f2f5, #eaedf1);
  min-height: calc(100vh - 120px);
}

.wishlist-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  background-color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.book-count {
  font-size: 14px;
  color: #909399;
  margin-right: auto;
  margin-left: 20px;
}

.loading-container {
  padding: 20px;
}

.empty-wishlist {
  padding: 40px 0;
  text-align: center;
}

.wishlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px;
}

.book-card {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.book-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 10px;
}

.book-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.book-image {
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.book-image .el-image {
  height: 100%;
  width: 100%;
  object-fit: contain;
}

.book-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.book-price {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.price-label {
  font-size: 14px;
  color: #909399;
}

.price-value {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.book-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .wishlist-grid {
    grid-template-columns: 1fr;
  }
  
  .book-actions {
    flex-direction: column;
  }
}</style>
