<template>
  <div class="cart-page">
    <div class="cart-container">
      <div class="cart-title-container">
        <h2 class="cart-title">我的购物车</h2>
        <div class="cart-subtitle" v-if="!loading && cartItems.length > 0">
          共 <span class="highlight">{{ totalItems }}</span> 件商品
        </div>
      </div>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="cartItems.length === 0" class="empty-cart">
        <el-empty description="购物车还是空的，去挑选心仪的商品吧~">
          <el-button
            type="primary"
            size="large"
            class="action-button"
            @click="$router.push('/products')"
          >
            去选购商品
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
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

        <transition-group name="cart-item" tag="div" class="cart-items">
          <el-card
            v-for="item in cartItems"
            :key="item.cartItemId"
            class="cart-item"
          >
            <div class="cart-item-content">
              <div class="col product-info">
                <el-image
                  :src="item.cover"
                  fit="cover"
                  class="product-image"
                  @click="goToProductDetail(item.productId)"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><picture-filled /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="product-details">
                  <div
                    class="product-title"
                    @click="goToProductDetail(item.productId)"
                  >
                    {{ item.title }}
                  </div>
                  <div class="product-description">
                    {{ item.description || "暂无描述" }}
                  </div>
                </div>
              </div>

              <div class="col price">
                <span class="price-label">单价：</span>
                <span class="price-value"
                  ><span class="currency">¥</span
                  >{{ formatPrice(item.price) }}</span
                >
              </div>

              <div class="col quantity">
                <span class="quantity-label">数量：</span>
                <div class="quantity-control">
                  <el-input-number
                    v-model="item.displayQuantity"
                    :min="1"
                    :max="item.maxStock || 999"
                    :disabled="item.updatingQuantity"
                    size="small"
                    controls-position="right"
                    @change="(val) => handleQuantityChange(item, val)"
                  />
                  <div class="stock-info">
                    <el-tag
                      v-if="item.maxStock <= 20"
                      type="danger"
                      size="small"
                      effect="light"
                    >
                      库存紧张: {{ item.maxStock }}
                    </el-tag>
                    <el-tag
                      v-else-if="item.maxStock > 0"
                      type="success"
                      size="small"
                      effect="light"
                    >
                      库存: {{ item.maxStock }}
                    </el-tag>
                    <el-tag
                      v-if="item.updatingQuantity"
                      type="info"
                      size="small"
                      >更新中...</el-tag
                    >
                  </div>
                </div>
              </div>

              <div class="col subtotal">
                <span class="subtotal-label">小计：</span>
                <span class="subtotal-value"
                  ><span class="currency">¥</span
                  >{{ formatPrice(calculateSubtotal(item)) }}</span
                >
              </div>

              <div class="col operations">
                <el-popconfirm
                  title="确定要从购物车删除此商品吗?"
                  confirm-button-text="删除"
                  cancel-button-text="取消"
                  @confirm="removeItem(item.cartItemId)"
                >
                  <template #reference>
                    <el-button
                      type="danger"
                      size="small"
                      :disabled="item.updatingQuantity"
                      class="delete-button"
                      icon="Delete"
                    >
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </el-card>
        </transition-group>

        <el-card class="cart-footer">
          <div class="cart-summary">
            <div class="summary-item">
              <span class="summary-label">商品总数:</span>
              <span class="summary-value">{{ totalItems }} 件</span>
            </div>
            <div class="summary-item total-amount">
              <span class="summary-label">合计金额:</span>
              <span class="summary-value price-total"
                ><span class="currency">¥</span
                >{{ formatPrice(totalAmount) }}</span
              >
            </div>
          </div>

          <div class="checkout-actions">
            <el-button
              class="continue-shopping"
              @click="$router.push('/products')"
            >
              <el-icon><ArrowLeft /></el-icon> 继续购物
            </el-button>
            <el-button
              type="primary"
              size="large"
              class="checkout-button"
              @click="checkout"
              :disabled="cartItems.length === 0"
            >
              <el-icon><ShoppingCart /></el-icon> 去结算
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 结算弹窗（简化版） -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="确认订单"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="shippingForm"
        ref="shippingFormRef"
        :rules="shippingRules"
        label-width="80px"
        style="margin-bottom: 10px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="shippingForm.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="shippingForm.phone" autocomplete="off" />
        </el-form-item>
        <el-form-item label="邮编" prop="zipcode">
          <el-input v-model="shippingForm.zipcode" autocomplete="off" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="shippingForm.address" autocomplete="off" />
        </el-form-item>
      </el-form>
      <div class="order-summary">
        <div class="order-row">
          <span class="label">用户名：</span>
          <span class="value">{{ username }}</span>
        </div>
        <div class="order-row">
          <span class="label">订单内容：</span>
          <ul class="order-items">
            <li v-for="item in cartItems" :key="item.cartItemId">
              {{ item.title }} × {{ item.quantity }}
            </li>
          </ul>
        </div>
        <div class="order-row">
          <span class="label">支付总金额：</span>
          <span class="value price">¥{{ formatPrice(totalAmount) }}</span>
        </div>
        <div class="order-row">
          <span class="label">支付方式：</span>
          <span class="value">支付宝</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="checkoutLoading"
          @click="handleCheckoutSubmit"
          >提交订单</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getCartItems,
  removeFromCart,
  updateCartItemQuantity,
  checkoutCart,
} from "../api/cart";
import { getProductById, getStockpile } from "../api/product";
import {
  PictureFilled,
  ArrowRight,
  ArrowLeft,
  ShoppingCart,
  Delete,
} from "@element-plus/icons-vue";
import { pay } from "../api/order";

export default {
  name: "Cart",
  components: {
    PictureFilled,
    ArrowRight,
    ArrowLeft,
    ShoppingCart,
    Delete,
  },
  setup() {
    const router = useRouter();
    const cartItems = ref([]);
    const loading = ref(true);
    const username = ref("");

    const totalItems = computed(() => {
      return cartItems.value.reduce((total, item) => total + item.quantity, 0);
    });

    const totalAmount = computed(() => {
      return cartItems.value.reduce((total, item) => {
        return total + item.price * item.quantity;
      }, 0);
    });

    // 加载商品库存信息
    const loadProductStock = async (item) => {
      try {
        // 优先尝试获取库存信息
        const stockResponse = await getStockpile(item.productId);
        if (stockResponse.code === "200" && stockResponse.data) {
          item.maxStock = stockResponse.data.amount || 999;
          return;
        }

        // 如果直接获取库存失败，尝试获取商品详情
        const productResponse = await getProductById(item.productId);
        if (productResponse.code === "200" && productResponse.data) {
          item.maxStock = productResponse.data.stockpile?.amount || 999;
        }
      } catch (e) {
        console.error("获取商品库存失败:", e);
        // 设置一个默认值，防止无法添加
        item.maxStock = 999;
      }
    };

    const fetchCartItems = async () => {
      try {
        loading.value = true;
        const response = await getCartItems();
        if (response.code === "200") {
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
                ElMessage.warning(
                  `商品"${item.title}"的数量已自动调整为最大库存: ${item.maxStock}`
                );
              } catch (e) {
                console.error("自动调整数量失败:", e);
              } finally {
                // 取消更新状态
                item.updatingQuantity = false;
              }
            }
          }

          cartItems.value = items;
        } else {
          ElMessage.error(response.msg || "获取购物车失败");
        }
      } catch (error) {
        console.error("获取购物车出错:", error);
        ElMessage.error("获取购物车失败，请稍后重试");
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
        const response = await updateCartItemQuantity(
          item.cartItemId,
          newQuantity
        );

        if (response.code === "200") {
          ElMessage.success("更新数量成功");
          // 更新实际数量
          item.quantity = newQuantity;
        } else {
          // 如果更新失败，恢复显示数量
          ElMessage.error(response.msg || "更新数量失败");
          item.displayQuantity = item.quantity;
        }
      } catch (error) {
        console.error("更新数量出错:", error);
        ElMessage.error("更新数量失败，请稍后重试");
        // 恢复显示数量
        item.displayQuantity = item.quantity;
      } finally {
        // 取消更新状态
        item.updatingQuantity = false;
      }
    };

    const removeItem = async (cartItemId) => {
      try {
        const response = await removeFromCart(cartItemId);
        if (response.code === "200") {
          ElMessage.success("删除成功");
          // 直接从本地移除，提高响应速度
          cartItems.value = cartItems.value.filter(
            (item) => item.cartItemId !== cartItemId
          );
        } else {
          ElMessage.error(response.msg || "删除失败");
        }
      } catch (error) {
        console.error("删除商品出错:", error);
        ElMessage.error("删除失败，请稍后重试");
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

    const checkoutDialogVisible = ref(false);
    const checkoutLoading = ref(false);
    const checkoutForm = ref({
      payment_method: "支付宝",
    });

    const shippingForm = ref({
      name: "",
      phone: "",
      zipcode: "",
      address: "",
    });
    const shippingFormRef = ref(null);

    // 添加表单验证规则
    const shippingRules = {
      name: [
        { required: true, message: "请输入收货人姓名", trigger: "blur" },
        {
          min: 2,
          max: 20,
          message: "姓名长度在2-20个字符之间",
          trigger: "blur",
        },
      ],
      phone: [
        { required: true, message: "请输入手机号", trigger: "blur" },
        {
          pattern: /^1[3-9]\d{9}$/,
          message: "请输入正确的手机号格式",
          trigger: "blur",
        },
      ],
      zipcode: [
        { pattern: /^\d{6}$/, message: "邮编必须是6位数字", trigger: "blur" },
      ],
      address: [
        { required: true, message: "请输入详细地址", trigger: "blur" },
        {
          min: 5,
          max: 200,
          message: "地址长度在5-200个字符之间",
          trigger: "blur",
        },
      ],
    };

    const checkout = () => {
      if (cartItems.value.length === 0) {
        ElMessage.warning("购物车为空");
        return;
      }
      checkoutDialogVisible.value = true;
    };

    const handleCheckoutSubmit = async () => {
      // 校验收货人信息
      try {
        await shippingFormRef.value.validate();
      } catch (error) {
        return;
      }

      checkoutLoading.value = true;
      try {
        const payload = {
          cartItemIds: cartItems.value.map(item => item.cartItemId.toString()),
          shipping_address: shippingForm.value,
          payment_method: "Alipay"  // 修改为与后端匹配的值
        };

        console.log('Checkout payload:', payload);

        const response = await checkoutCart(payload);
        console.log('Checkout response:', response.code);
        if (response.code === "200") {
          ElMessage.success("订单提交成功");
          checkoutDialogVisible.value = false;
        } else {
          throw new Error(response.msg || "订单提交失败");
        }
      } catch (e) {
        console.error("提交订单或发起支付时出错:", e);
        ElMessage.error(e.message || "操作失败，请稍后重试");
      } finally {
        checkoutLoading.value = false;
      }
    };

    onMounted(() => {
      // 你可以根据实际项目获取用户名
      username.value = localStorage.getItem("username") || "未登录用户";
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
      checkout,
      checkoutDialogVisible,
      checkoutForm,
      checkoutLoading,
      handleCheckoutSubmit,
      username,
      shippingForm,
      shippingFormRef,
      shippingRules,
    };
  },
};
</script>

<style scoped>
/* 页面背景 */
.cart-page {
  min-height: 100vh;
  padding: 30px 0;
  background: linear-gradient(to bottom, #f9f9f9, #f0f2f5);
}

.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 标题样式 */
.cart-title-container {
  margin-bottom: 30px;
  text-align: center;
  position: relative;
}

.cart-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  position: relative;
  display: inline-block;
}

.cart-title::after {
  content: "";
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(to right, #409eff, #53a8ff);
  border-radius: 3px;
}

.cart-subtitle {
  font-size: 16px;
  color: #606266;
  margin-top: 15px;
}

.highlight {
  color: #409eff;
  font-weight: bold;
}

/* 加载状态 */
.loading-container {
  padding: 30px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 空购物车 */
.empty-cart {
  margin: 60px 0;
  text-align: center;
}

.empty-cart :deep(.el-empty__description) {
  font-size: 16px;
  color: #909399;
}

.action-button {
  margin-top: 20px;
  padding: 12px 24px;
  font-size: 16px;
  transition: all 0.3s;
}

/* 购物车内容 */
.cart-content {
  margin-top: 20px;
}

.cart-header {
  display: flex;
  background: linear-gradient(to right, #ecf5ff, #f5f7fa);
  padding: 15px 20px;
  border-radius: 8px 8px 0 0;
  font-weight: 600;
  color: #606266;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.03);
}

/* 购物车项目 */
.cart-items {
  margin-bottom: 30px;
}

.cart-item {
  margin-bottom: 15px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: none;
}

.cart-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.cart-item-content {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

/* 列布局 */
.col {
  display: flex;
  align-items: center;
  padding: 10px;
}

.product-info {
  flex: 3;
  display: flex;
  align-items: center;
}

.price,
.quantity,
.subtotal,
.operations {
  flex: 1;
  justify-content: center;
}

.price,
.subtotal {
  flex-direction: column;
  align-items: flex-start;
}

.quantity {
  flex-direction: column;
  align-items: center;
}

/* 标签和数值样式 */
.price-label,
.quantity-label,
.subtotal-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
  display: none;
}

.price-value,
.subtotal-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.price-value {
  color: #ff9800;
}

.subtotal-value {
  color: #f56c6c;
}

/* 数量控制 */
.quantity-control {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stock-info {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

/* 商品图片 */
.product-image {
  width: 100px;
  height: 120px;
  object-fit: cover;
  margin-right: 20px;
  cursor: pointer;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.product-image:hover {
  transform: scale(1.05);
}

.image-error {
  width: 100px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 30px;
  border-radius: 8px;
}

/* 商品详情 */
.product-details {
  display: flex;
  flex-direction: column;
  width: calc(100% - 120px);
}

.product-title {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 8px;
  cursor: pointer;
  color: #303133;
  transition: color 0.3s;
}

.product-title:hover {
  color: #409eff;
  text-decoration: underline;
}

.product-description {
  font-size: 14px;
  color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

/* 删除按钮 */
.delete-button {
  transition: all 0.3s;
}

.delete-button:hover {
  background-color: #f56c6c;
  color: white;
  transform: scale(1.05);
}

/* 购物车底部 */
.cart-footer {
  margin-top: 20px;
  padding: 20px;
  border-radius: 8px;
  background: linear-gradient(to right, #fff, #f9fafc);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-summary {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-label {
  font-size: 16px;
  color: #606266;
}

.summary-value {
  font-size: 16px;
  color: #303133;
}

.total-amount {
  margin-top: 5px;
}

.total-amount .summary-label {
  font-size: 18px;
  font-weight: 600;
}

.price-total {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.currency {
  font-size: 0.7em;
  margin-right: 3px;
  position: relative;
  top: -2px;
  font-weight: normal;
}

.checkout-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.continue-shopping {
  font-size: 14px;
}

.checkout-button {
  padding: 12px 25px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(to right, #409eff, #53a8ff);
  border: none;
  transition: all 0.3s;
}

.checkout-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
}

/* 动画效果 */
.cart-item-enter-active,
.cart-item-leave-active {
  transition: all 0.5s ease;
}

.cart-item-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.cart-item-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .cart-item-content {
    flex-wrap: wrap;
  }

  .product-info {
    flex: 100%;
    margin-bottom: 15px;
  }

  .price,
  .quantity,
  .subtotal,
  .operations {
    flex: 1 1 48%;
  }

  .price-label,
  .quantity-label,
  .subtotal-label {
    display: block;
  }

  .cart-footer {
    flex-direction: column;
    gap: 20px;
  }

  .cart-summary,
  .checkout-actions {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .cart-container {
    padding: 15px;
  }

  .cart-header {
    display: none;
  }

  .cart-item-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .col {
    width: 100%;
    justify-content: flex-start;
    padding: 5px 0;
  }

  .price-label,
  .quantity-label,
  .subtotal-label {
    display: inline-block;
    width: 70px;
  }

  .product-image {
    width: 80px;
    height: 100px;
  }

  .product-details {
    width: calc(100% - 100px);
  }

  .checkout-actions {
    flex-direction: column;
    width: 100%;
  }

  .continue-shopping,
  .checkout-button {
    width: 100%;
  }
}

.order-summary {
  padding: 10px 0;
}
.order-row {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
}
.order-row .label {
  width: 90px;
  color: #666;
  font-weight: 500;
}
.order-row .value {
  flex: 1;
}
.order-row .price {
  color: #e4393c;
  font-weight: bold;
}
.order-items {
  margin: 0;
  padding-left: 18px;
  list-style: disc;
}
</style>
