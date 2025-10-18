<template>
    <div class="order-list">
        <h2>我的订单</h2>

        <!-- 订单状态筛选 -->
        <div class="status-filter">
            <el-radio-group v-model="currentStatus" @change="handleStatusChange">
                <el-radio-button label="all">全部订单</el-radio-button>
                <el-radio-button label="PENDING">待支付</el-radio-button>
                <el-radio-button label="SUCCESS">已支付</el-radio-button>
                <el-radio-button label="CANCELLED">已取消</el-radio-button>
            </el-radio-group>
        </div>

        <!-- 订单列表 -->
        <div class="order-items">
            <el-card v-for="order in filteredOrders" :key="order.orderId" class="order-item">
                <div class="order-header">
                    <span class="order-id">订单号：{{ order.orderId }}</span>
                    <span class="order-status" :class="order.status.toLowerCase()">
                        {{ getStatusText(order.status) }}
                    </span>
                </div>

                <div class="order-products">
                    <div v-for="item in order.orderItems" :key="item.id" class="product-item">
                        <el-image :src="item.productCover" class="product-image"></el-image>
                        <div class="product-info">
                            <div class="product-title">{{ item.productTitle }}</div>
                            <div class="product-price">
                                ¥{{ item.price }} × {{ item.quantity }}
                            </div>
                        </div>
                    </div>
                </div>

                <div class="order-footer">
                    <div class="order-total">
                        总计：<span class="price">¥{{ order.totalAmount }}</span>
                    </div>
                    <div class="order-actions">
                        <el-button v-if="order.status === 'PENDING'" type="primary" @click="handlePay(order)">
                            立即支付
                        </el-button>
                        <el-button v-if="order.status === 'PENDING'" type="danger" @click="handleCancel(order)">
                            取消订单
                        </el-button>
                    </div>
                </div>
            </el-card>
        </div>

        <!-- 没有订单时显示 -->
        <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无订单"></el-empty>
    </div>
</template>

<script>
import { ref, computed } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { getOrders, pay, cancelOrder } from '../api/order'

export default {
    name: 'OrderList',
    setup() {
        const orders = ref([])
        const loading = ref(false)
        const currentStatus = ref('all')
        let loadingInstance = null

        // 获取订单列表
        const fetchOrders = async () => {
            loadingInstance = ElLoading.service({
                target: '.order-list',
                fullscreen: false
            })
            loading.value = true
            try {
                const response = await getOrders()
                if (response.code === '200') {
                    orders.value = response.data
                } else {
                    ElMessage.error(response.msg || '获取订单列表失败')
                }
            } catch (error) {
                ElMessage.error('获取订单列表失败')
                console.error('获取订单列表失败:', error)
            } finally {
                loading.value = false
                loadingInstance.close()
            }
        }

        // 根据状态筛选订单
        const filteredOrders = computed(() => {
            if (currentStatus.value === 'all') {
                return orders.value
            }
            return orders.value.filter(order => order.status === currentStatus.value)
        })

        // 处理状态改变
        const handleStatusChange = (status) => {
            currentStatus.value = status
        }

        // 获取状态文本
        const getStatusText = (status) => {
            const statusMap = {
                'PENDING': '待支付',
                'SUCCESS': '已支付',
                'CANCELLED': '已取消'
            }
            return statusMap[status] || status
        }

        // 处理支付
        const handlePay = async (order) => {
            try {
                console.log("订单数据:", order); // 调试: 先打印整个订单对象

                // 直接使用订单ID，不进行额外转换
                const response = await pay(order.orderId);

                if (response.code === '200' && response.data) {
                    const div = document.createElement('div');
                    div.innerHTML = response.data.paymentForm;
                    document.body.appendChild(div);
                    const form = div.getElementsByTagName('form')[0];
                    if (form) {
                        form.submit();
                    } else {
                        throw new Error('支付表单生成失败');
                    }
                } else {
                    throw new Error(response.msg || '发起支付失败');
                }
            } catch (error) {
                ElMessage.error(error.message || '发起支付失败');
                console.error('发起支付失败:', error);
            }
        }

        // 处理取消订单
        const handleCancel = async (order) => {
            try {
                const response = await cancelOrder(order.orderId)
                if (response.code === '200') {
                    ElMessage.success('订单已取消')
                    fetchOrders() // 刷新订单列表
                } else {
                    ElMessage.error(response.msg || '取消订单失败')
                }
            } catch (error) {
                ElMessage.error('取消订单失败')
                console.error('取消订单失败:', error)
            }
        }

        // 初始加载
        fetchOrders()

        return {
            orders,
            loading,
            currentStatus,
            filteredOrders,
            handleStatusChange,
            getStatusText,
            handlePay,
            handleCancel
        }
    }
}
</script>

<style scoped>
.order-list {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.status-filter {
    margin: 20px 0;
}

.order-item {
    margin-bottom: 20px;
}

.order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
}

.order-status {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 14px;
}

.order-status.pending {
    color: #e6a23c;
    background-color: #fdf6ec;
}

.order-status.success {
    color: #67c23a;
    background-color: #f0f9eb;
}

.order-status.cancelled {
    color: #f56c6c;
    background-color: #fef0f0;
}

.order-products {
    padding: 15px 0;
}

.product-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.product-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    margin-right: 15px;
}

.product-info {
    flex: 1;
}

.product-title {
    font-size: 14px;
    margin-bottom: 5px;
}

.product-price {
    color: #999;
    font-size: 13px;
}

.order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 15px;
    border-top: 1px solid #eee;
}

.order-total .price {
    color: #f56c6c;
    font-size: 18px;
    font-weight: bold;
}

.order-actions {
    display: flex;
    gap: 10px;
}

.loading {
    text-align: center;
    padding: 20px;
}
</style>