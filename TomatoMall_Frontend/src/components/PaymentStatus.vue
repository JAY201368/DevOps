<template>
  <div class="payment-status">
    <el-result
      :icon="icon"
      :title="title"
      :sub-title="subTitle">
      <template #extra>
        <el-button type="primary" @click="$router.push('/orders')">查看订单</el-button>
        <el-button @click="$router.push('/products')">继续购物</el-button>
      </template>
    </el-result>
  </div>
</template>

<script>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

export default {
  name: 'PaymentStatus',
  setup() {
    const route = useRoute()
    
    const status = computed(() => route.query.status || 'pending')
    
    const icon = computed(() => {
      switch(status.value) {
        case 'success':
          return 'success'
        case 'error':
          return 'error'
        default:
          return 'info'
      }
    })
    
    const title = computed(() => {
      switch(status.value) {
        case 'success':
          return '支付成功'
        case 'error':
          return '支付失败'
        default:
          return '支付处理中'
      }
    })
    
    const subTitle = computed(() => {
      switch(status.value) {
        case 'success':
          return '您的订单已支付成功，我们将尽快为您发货'
        case 'error':
          return '支付过程中出现错误，请稍后重试或联系客服'
        default:
          return '正在处理您的支付请求，请稍候...'
      }
    })
    
    return {
      icon,
      title,
      subTitle
    }
  }
}
</script>

<style scoped>
.payment-status {
  padding: 40px;
  text-align: center;
}
</style>
