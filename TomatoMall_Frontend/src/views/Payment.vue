<template>
  <div class="payment-container">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>
    <div v-else>
      <!-- 支付页面头部 -->
      <h2>订单支付</h2>
      <div class="order-info">
        <div class="info-item">
          <span class="label">订单号：</span>
          <span>{{ orderId }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付金额：</span>
          <span class="amount">¥{{ totalAmount }}</span>
        </div>
      </div>

      <!-- 支付表单区域 -->
      <div class="payment-form" v-html="paymentForm"></div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { pay } from '../api/order';

export default {
  name: 'Payment',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const loading = ref(true);
    const orderId = ref('');
    const totalAmount = ref('0.00');
    const paymentForm = ref('');

    const initPayment = async () => {
      try {
        loading.value = true;
        const orderIdFromRoute = route.params.orderId;
        if (!orderIdFromRoute) {
          ElMessage.error('订单信息不完整');
          router.push('/orders');
          return;
        }

        orderId.value = orderIdFromRoute;
        const response = await pay(orderIdFromRoute);
        
        if (response.code === '200') {
          const { paymentForm: form, totalAmount: amount } = response.data;
          paymentForm.value = form;
          totalAmount.value = amount;
        } else {
          ElMessage.error(response.msg || '获取支付信息失败');
          router.push('/orders');
        }
      } catch (error) {
        console.error('支付初始化失败:', error);
        ElMessage.error('支付初始化失败，请稍后重试');
        router.push('/orders');
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      initPayment();
    });

    return {
      loading,
      orderId,
      totalAmount,
      paymentForm
    };
  }
};
</script>

<style scoped>
.payment-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.loading {
  padding: 40px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.order-info {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 30px;
}

.info-item {
  margin-bottom: 10px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  margin-right: 10px;
}

.amount {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.payment-form {
  margin-top: 20px;
}

/* 支付宝表单的一些美化 */
:deep(.payment-form form) {
  margin: 0 auto;
  text-align: center;
}

:deep(.payment-form button) {
  background: #409eff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

:deep(.payment-form button:hover) {
  background: #66b1ff;
}
</style>
