<template>
  <div class="user-coupons-container">
    <h1 class="page-title">我的促销券</h1>

    <!-- 促销券列表 -->
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="未使用" name="unused">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="unusedCoupons.length === 0" class="empty-container">
          <el-empty description="您还没有未使用的促销券" />
        </div>
        <div v-else class="coupons-list">
          <div 
            v-for="coupon in unusedCoupons" 
            :key="coupon.id" 
            class="coupon-card"
          >
            <div class="coupon-left">
              <div class="coupon-amount">¥{{ coupon.coupon.discountAmount }}</div>
              <div class="coupon-condition" v-if="coupon.coupon.minOrderAmount > 0">
                满{{ coupon.coupon.minOrderAmount }}元可用
              </div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.coupon.name }}</div>
              <div class="coupon-desc" v-if="coupon.coupon.description">
                {{ coupon.coupon.description }}
              </div>
              <div class="coupon-time">
                {{ formatDate(coupon.coupon.startDate) }} 至 {{ formatDate(coupon.coupon.endDate) }}
              </div>
              <div class="coupon-actions">
                <el-button type="primary" size="small" @click="goToProducts">
                  去使用
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="已使用" name="used">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="usedCoupons.length === 0" class="empty-container">
          <el-empty description="您还没有已使用的促销券" />
        </div>
        <div v-else class="coupons-list">
          <div 
            v-for="coupon in usedCoupons" 
            :key="coupon.id" 
            class="coupon-card used"
          >
            <div class="coupon-left">
              <div class="coupon-amount">¥{{ coupon.coupon.discountAmount }}</div>
              <div class="coupon-condition" v-if="coupon.coupon.minOrderAmount > 0">
                满{{ coupon.coupon.minOrderAmount }}元可用
              </div>
              <div class="coupon-status">已使用</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.coupon.name }}</div>
              <div class="coupon-desc" v-if="coupon.coupon.description">
                {{ coupon.coupon.description }}
              </div>
              <div class="coupon-time">
                使用时间: {{ formatDate(coupon.usedTime) }}
              </div>
              <div class="coupon-order" v-if="coupon.orderId">
                <el-button type="text" size="small" @click="viewOrder(coupon.orderId)">
                  查看订单
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="已过期" name="expired">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="expiredCoupons.length === 0" class="empty-container">
          <el-empty description="您还没有已过期的促销券" />
        </div>
        <div v-else class="coupons-list">
          <div 
            v-for="coupon in expiredCoupons" 
            :key="coupon.id" 
            class="coupon-card expired"
          >
            <div class="coupon-left">
              <div class="coupon-amount">¥{{ coupon.coupon.discountAmount }}</div>
              <div class="coupon-condition" v-if="coupon.coupon.minOrderAmount > 0">
                满{{ coupon.coupon.minOrderAmount }}元可用
              </div>
              <div class="coupon-status">已过期</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.coupon.name }}</div>
              <div class="coupon-desc" v-if="coupon.coupon.description">
                {{ coupon.coupon.description }}
              </div>
              <div class="coupon-time">
                {{ formatDate(coupon.coupon.startDate) }} 至 {{ formatDate(coupon.coupon.endDate) }}
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { getUserCoupons } from '../api/coupon';
import { getUserInfo } from '../api/user';
import { clearUrlCache } from '../api/request';

const router = useRouter();
const loading = ref(false);
const activeTab = ref('unused');
const userCoupons = ref([]);
const userId = ref(null);

// 计算属性：未使用的促销券
const unusedCoupons = computed(() => {
  return userCoupons.value.filter(coupon => 
    coupon.isUsed === 0 && 
    !isExpired(coupon.coupon.endDate)
  );
});

// 计算属性：已使用的促销券
const usedCoupons = computed(() => {
  return userCoupons.value.filter(coupon => 
    coupon.isUsed === 1
  );
});

// 计算属性：已过期的促销券
const expiredCoupons = computed(() => {
  return userCoupons.value.filter(coupon => 
    coupon.isUsed === 0 && 
    isExpired(coupon.coupon.endDate)
  );
});

// 判断是否已过期
const isExpired = (endDate) => {
  const now = new Date();
  const end = new Date(endDate);
  return now > end;
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 跳转到商品列表
const goToProducts = () => {
  router.push('/products');
};

// 查看订单
const viewOrder = (orderId) => {
  router.push(`/orders?orderId=${orderId}`);
};

// 获取用户促销券
const fetchUserCoupons = async () => {
  loading.value = true;
  try {
    // 清除促销券相关的缓存
    clearUrlCache('/api/coupons');
    
    // 尝试从localStorage获取用户ID
    userId.value = localStorage.getItem('userId');
    
    // 如果localStorage中没有userId，尝试从token获取用户信息
    if (!userId.value) {
      const token = localStorage.getItem('token');
      if (!token) {
        ElMessage.error('请先登录');
        router.push('/login');
        return;
      }
      
      // 通过token获取用户信息
      const username = localStorage.getItem('username');
      if (username) {
        const userRes = await getUserInfo(username);
        if (userRes.code === '200' && userRes.data) {
          userId.value = userRes.data.id;
          // 保存到localStorage以便下次使用
          localStorage.setItem('userId', userId.value);
        } else {
          ElMessage.error('未找到用户信息');
          return;
        }
      } else {
        ElMessage.error('未找到用户信息');
        return;
      }
    }
    
    const res = await getUserCoupons(userId.value);
    if (res.code === '200') {
      userCoupons.value = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取促销券失败');
    }
  } catch (error) {
    console.error('获取用户促销券出错:', error);
    ElMessage.error('获取促销券失败');
  } finally {
    loading.value = false;
  }
};

// 生命周期钩子
onMounted(() => {
  // 清除促销券相关的缓存
  clearUrlCache('/api/coupons');
  fetchUserCoupons();
});
</script>

<style scoped>
.user-coupons-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 0;
}

.coupons-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.coupon-card {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
}

.coupon-card::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 120px;
  width: 1px;
  background: repeating-linear-gradient(to bottom, transparent, transparent 5px, #e0e0e0 5px, #e0e0e0 10px);
  z-index: 1;
}

.coupon-card::after {
  content: '';
  position: absolute;
  left: 120px;
  top: -6px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #f5f7fa;
  z-index: 2;
}

.coupon-card::before {
  content: '';
  position: absolute;
  left: 120px;
  bottom: -6px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #f5f7fa;
  z-index: 2;
}

.coupon-left {
  width: 120px;
  padding: 16px;
  background-color: #409eff;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}

.coupon-card.used .coupon-left {
  background-color: #909399;
}

.coupon-card.expired .coupon-left {
  background-color: #c0c4cc;
}

.coupon-amount {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.coupon-condition {
  font-size: 12px;
  text-align: center;
}

.coupon-status {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: rgba(0, 0, 0, 0.2);
}

.coupon-right {
  flex: 1;
  padding: 16px;
  background-color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.coupon-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
}

.coupon-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.coupon-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.coupon-actions {
  display: flex;
  justify-content: flex-end;
}

.coupon-order {
  display: flex;
  justify-content: flex-end;
}
</style> 