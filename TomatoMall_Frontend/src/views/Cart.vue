<template>
  <div class="cart-container">
    <h2>我的购物车</h2>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车为空">
        <el-button type="primary" @click="$router.push('/products')">去选购商品</el-button>
      </el-empty>
    </div>
    
    <div v-else class="cart-content">
      <div class="cart-header">
        <div class="col product-info">商品信息</div>
        <div class="col price">单价</div>
        <div class="col quantity">数量</div>
        <div class="col subtotal">小计</div>
        <div class="col operations">操作</div>
      </div>
      
      <el-card v-for="item in cartItems" :key="item.cartItemId" class="cart-item">
        <div class="cart-item-content">
          <div class="col product-info">
            <el-image 
              :src="item.cover" 
              fit="cover"
              class="product-image"
              @click="goToProductDetail(item.productId)">
              <template #error>
                <div class="image-error">
                  <el-icon><picture-filled /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="product-details">
              <div class="product-title" @click="goToProductDetail(item.productId)">{{ item.title }}</div>
              <div class="product-description">{{ item.description }}</div>
            </div>
          </div>
          
          <div class="col price">
            <span class="currency">¥</span>{{ formatPrice(item.price) }}
          </div>
          
          <div class="col quantity">
            <el-input-number 
              v-model="item.displayQuantity" 
              :min="1" 
              :max="item.maxStock || 999"
              :disabled="item.updatingQuantity"
              @change="(val) => handleQuantityChange(item, val)" />
            <div class="stock-info">
              <span v-if="item.maxStock" class="stock-hint">
                库存: {{ item.maxStock }}
              </span>
              <el-tag v-if="item.updatingQuantity" size="small">更新中...</el-tag>
            </div>
          </div>
          
          <div class="col subtotal">
            <span class="currency">¥</span>{{ formatPrice(calculateSubtotal(item)) }}
          </div>
          
          <div class="col operations">
            <el-button 
              type="danger" 
              size="small"
              :disabled="item.updatingQuantity"
              @click="removeItem(item.cartItemId)">
              删除
            </el-button>
          </div>
        </div>
      </el-card>
      
      <div class="cart-footer">
        <div class="cart-summary">
          <div class="summary-item">
            <span>商品总数:</span>
            <span>{{ totalItems }}</span>
          </div>
          <div class="summary-item total-amount">
            <span>合计:</span>
            <span class="price"><span class="currency">¥</span>{{ formatPrice(totalAmount) }}</span>
          </div>
        </div>
        
        <div class="checkout-actions">
          <el-button @click="$router.push('/products')">继续购物</el-button>
          <el-button type="primary" @click="checkout" :disabled="cartItems.length === 0">
            结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getCartItems, removeFromCart, updateCartItemQuantity } from '../api/cart';
import { getProductById, getStockpile } from '../api/product';
import { PictureFilled } from '@element-plus/icons-vue';

export default {
  name: 'Cart',
  components: {
    PictureFilled
  },
  setup() {
    const router = useRouter();
    const cartItems = ref([]);
    const loading = ref(true);
    
    const totalItems = computed(() => {
      return cartItems.value.reduce((total, item) => total + item.quantity, 0);
    });
    
    const totalAmount = computed(() => {
      return cartItems.value.reduce((total, item) => {
        return total + (item.price * item.quantity);
      }, 0);
    });

    // 加载商品库存信息
    const loadProductStock = async (item) => {
      try {
        // 优先尝试获取库存信息
        const stockResponse = await getStockpile(item.productId);
        if (stockResponse.code === '200' && stockResponse.data) {
          item.maxStock = stockResponse.data.amount || 999;
          return;
        }
        
        // 如果直接获取库存失败，尝试获取商品详情
        const productResponse = await getProductById(item.productId);
        if (productResponse.code === '200' && productResponse.data) {
          item.maxStock = productResponse.data.stockpile?.amount || 999;
        }
      } catch (e) {
        console.error('获取商品库存失败:', e);
        // 设置一个默认值，防止无法添加
        item.maxStock = 999;
      }
    };
    
    const fetchCartItems = async () => {
      try {
        loading.value = true;
        const response = await getCartItems();
        if (response.code === '200') {
          const items = response.data.items || [];
          
          // 为每个商品添加显示属性
          for (const item of items) {
            // 添加用于显示的数量字段，与实际数量分开
            item.displayQuantity = item.quantity;
            // 添加更新状态标志
            item.updatingQuantity = false;
            // 加载库存信息
            await loadProductStock(item);
            
            // 自动修正数量超出库存的情况
            if (item.quantity > item.maxStock) {
              // 标记为正在更新
              item.updatingQuantity = true;
              
              try {
                // 调用API更新实际数量
                await updateCartItemQuantity(item.cartItemId, item.maxStock);
                // 更新显示数量和实际数量
                item.quantity = item.maxStock;
                item.displayQuantity = item.maxStock;
                ElMessage.warning(`商品"${item.title}"的数量已自动调整为最大库存: ${item.maxStock}`);
              } catch (e) {
                console.error('自动调整数量失败:', e);
              } finally {
                // 取消更新状态
                item.updatingQuantity = false;
              }
            }
          }
          
          cartItems.value = items;
        } else {
          ElMessage.error(response.msg || '获取购物车失败');
        }
      } catch (error) {
        console.error('获取购物车出错:', error);
        ElMessage.error('获取购物车失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    };
    
    // 处理数量变更
    const handleQuantityChange = async (item, newQuantity) => {
      // 防止在更新中再次更新
      if (item.updatingQuantity) return;
      
      // 检查库存限制
      if (newQuantity > item.maxStock) {
        // 如果超出库存，调整回库存数量
        ElMessage.warning(`商品数量不能超过库存: ${item.maxStock}`);
        item.displayQuantity = item.maxStock;
        return;
      }
      
      // 如果数量没有变化，不做处理
      if (newQuantity === item.quantity) return;
      
      // 标记为正在更新
      item.updatingQuantity = true;
      
      try {
        // 调用API更新数量
        const response = await updateCartItemQuantity(item.cartItemId, newQuantity);
        
        if (response.code === '200') {
          ElMessage.success('更新数量成功');
          // 更新实际数量
          item.quantity = newQuantity;
        } else {
          // 如果更新失败，恢复显示数量
          ElMessage.error(response.msg || '更新数量失败');
          item.displayQuantity = item.quantity;
        }
      } catch (error) {
        console.error('更新数量出错:', error);
        ElMessage.error('更新数量失败，请稍后重试');
        // 恢复显示数量
        item.displayQuantity = item.quantity;
      } finally {
        // 取消更新状态
        item.updatingQuantity = false;
      }
    };
    
    const removeItem = async (cartItemId) => {
      try {
        await ElMessageBox.confirm('确定要从购物车删除此商品吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        const response = await removeFromCart(cartItemId);
        if (response.code === '200') {
          ElMessage.success('删除成功');
          // 直接从本地移除，提高响应速度
          cartItems.value = cartItems.value.filter(item => item.cartItemId !== cartItemId);
        } else {
          ElMessage.error(response.msg || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除商品出错:', error);
          ElMessage.error('删除失败，请稍后重试');
        }
      }
    };
    
    const calculateSubtotal = (item) => {
      return item.price * item.quantity;
    };
    
    const formatPrice = (price) => {
      return parseFloat(price).toFixed(2);
    };
    
    const goToProductDetail = (productId) => {
      router.push(`/products/${productId}`);
    };
    
    const checkout = () => {
      ElMessage.info('订单结算功能将在下一阶段实现');
      // 后续会实现结算功能
    };
    
    onMounted(() => {
      fetchCartItems();
    });
    
    return {
      cartItems,
      loading,
      totalItems,
      totalAmount,
      handleQuantityChange,
      removeItem,
      calculateSubtotal,
      formatPrice,
      goToProductDetail,
      checkout
    };
  }
};
</script>

<style scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.loading {
  padding: 20px;
}

.empty-cart {
  margin: 40px 0;
  text-align: center;
}

.cart-content {
  margin-top: 20px;
}

.cart-header {
  display: flex;
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  font-weight: bold;
  margin-bottom: 15px;
}

.cart-item {
  margin-bottom: 15px;
}

.cart-item-content {
  display: flex;
  align-items: center;
}

.col {
  display: flex;
  align-items: center;
}

.product-info {
  flex: 3;
  display: flex;
  align-items: center;
}

.price, .quantity, .subtotal, .operations {
  flex: 1;
  justify-content: center;
}

.quantity {
  flex-direction: column;
  align-items: center;
}

.stock-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 5px;
}

.stock-hint {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.product-image {
  width: 80px;
  height: 100px;
  object-fit: cover;
  margin-right: 15px;
  cursor: pointer;
}

.image-error {
  width: 80px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

.product-details {
  display: flex;
  flex-direction: column;
}

.product-title {
  font-weight: bold;
  margin-bottom: 5px;
  cursor: pointer;
  color: #333;
}

.product-title:hover {
  color: #409eff;
}

.product-description {
  font-size: 12px;
  color: #999;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  max-width: 200px;
}

.cart-footer {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-summary {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
}

.currency {
  font-size: 0.8em;
  margin-right: 2px;
}

.checkout-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .cart-item-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .col {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .cart-header {
    display: none;
  }
  
  .cart-footer {
    flex-direction: column;
    gap: 20px;
  }
  
  .checkout-actions {
    width: 100%;
    justify-content: center;
  }
}
</style> 