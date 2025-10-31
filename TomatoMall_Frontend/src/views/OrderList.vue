<template>
    <div class="order-list">
        <el-card class="main-card">
            <template #header>
                <div class="card-header">
                    <div class="header-left">
                        <h2 class="page-title">
                            <el-icon><List /></el-icon>
                            我的订单
                        </h2>
                        <el-tag type="info" effect="plain" class="order-count">
                            共 {{ filteredOrders.length }} 个订单
                        </el-tag>
                    </div>
                </div>
            </template>

            <!-- 加载状态 -->
            <div v-if="loading" class="loading-container">
                <el-skeleton :rows="3" animated />
                <el-skeleton :rows="3" animated />
            </div>

            <!-- 错误提示 -->
            <el-alert
                v-if="error"
                :title="error"
                type="error"
                show-icon
                :closable="true"
                @close="error = ''"
                class="error-alert"
            />

            <!-- 订单状态筛选 -->
            <div v-if="!loading && !error" class="status-filter">
                <el-radio-group v-model="currentStatus" @change="handleStatusChange" size="large">
                    <el-radio-button label="all">全部订单</el-radio-button>
                    <el-radio-button label="PENDING">待支付</el-radio-button>
                    <el-radio-button label="SUCCESS">已支付</el-radio-button>
                    <el-radio-button label="CANCELLED">已取消</el-radio-button>
                </el-radio-group>
            </div>

            <!-- 订单列表 -->
            <div v-if="!loading && !error" class="order-items">
                <el-card v-for="order in filteredOrders" 
                        :key="order.orderId" 
                        class="order-item"
                        shadow="hover"
                        @click="handleViewDetails(order)">
                    <div class="order-header">
                        <div class="order-info">
                            <span class="order-time">
                                <el-icon><Clock /></el-icon>
                                下单时间：{{ formatDate(order.createTime) }}
                            </span>
                        </div>
                        <el-tag :type="getStatusType(order.status)" 
                               effect="dark" 
                               class="order-status">
                            {{ getStatusText(order.status) }}
                        </el-tag>
                    </div>

                    <div class="order-products">
                        <div v-for="item in order.orderItems" 
                             :key="item.id" 
                             class="product-item">
                            <el-image :src="item.productCover" 
                                     class="product-image"
                                     fit="cover">
                                <template #error>
                                    <div class="image-error">
                                        <el-icon><Picture /></el-icon>
                                    </div>
                                </template>
                            </el-image>
                            <div class="product-info">
                                <div class="product-title">{{ item.productTitle }}</div>
                                <div class="product-meta">
                                    <span class="product-price">
                                        <span class="currency">¥</span>{{ item.price }}
                                    </span>
                                    <span class="product-quantity">× {{ item.quantity }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="order-footer">
                        <div class="order-total">
                            <span class="total-label">订单总额：</span>
                            <span class="total-price">
                                <span class="currency">¥</span>{{ order.totalAmount }}
                            </span>
                        </div>
                        <div class="order-actions">
                            <el-button v-if="order.status === 'PENDING'" 
                                     type="primary" 
                                     size="large"
                                     @click.stop="handlePay(order)">
                                <el-icon><Wallet /></el-icon>
                                立即支付
                            </el-button>
                            <el-button v-if="order.status === 'PENDING'" 
                                     type="danger" 
                                     size="large"
                                     @click.stop="handleCancel(order)">
                                <el-icon><Close /></el-icon>
                                取消订单
                            </el-button>
                            <el-button v-if="order.status === 'SUCCESS'" 
                                     type="success" 
                                     size="large"
                                     @click.stop="handleViewDetails(order)">
                                <el-icon><View /></el-icon>
                                查看详情
                            </el-button>
                        </div>
                    </div>
                </el-card>
            </div>

            <!-- 没有订单时显示 -->
            <el-empty v-if="!loading && !error && filteredOrders.length === 0" 
                     description="暂无订单"
                     :image-size="200">
                <el-button type="primary" @click="$router.push('/products')">
                    去选购商品
                </el-button>
            </el-empty>
        </el-card>

        <!-- 订单详情对话框 -->
        <el-dialog
            v-model="detailsDialogVisible"
            title="订单详情"
            width="600px"
            class="order-details-dialog"
            destroy-on-close
        >
            <div v-if="currentOrder" class="order-details">
                <div class="details-section">
                    <div class="section-header">
                        <el-icon><Document /></el-icon>
                        <span>基本信息</span>
                    </div>
                    <div class="details-content">
                        <div class="detail-row">
                            <span class="label">订单状态：</span>
                            <el-tag :type="getStatusType(currentOrder.status)" effect="dark">
                                {{ getStatusText(currentOrder.status) }}
                            </el-tag>
                        </div>
                        <div class="detail-row">
                            <span class="label">下单时间：</span>
                            <span class="value">{{ formatDate(currentOrder.createTime) }}</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">支付方式：</span>
                            <span class="value">支付宝</span>
                        </div>
                    </div>
                </div>

                <div class="details-section">
                    <div class="section-header">
                        <el-icon><Location /></el-icon>
                        <span>收货信息</span>
                    </div>
                    <div class="details-content">
                        <div class="detail-row">
                            <span class="label">收货人：</span>
                            <span class="value">{{ currentOrder.receiverName }}</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">联系电话：</span>
                            <span class="value">{{ currentOrder.receiverPhone }}</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">收货地址：</span>
                            <span class="value">{{ currentOrder.receiverAddress }}</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">邮政编码：</span>
                            <span class="value">{{ currentOrder.receiverZipcode }}</span>
                        </div>
                    </div>
                </div>

                <div class="details-section">
                    <div class="section-header">
                        <el-icon><ShoppingCart /></el-icon>
                        <span>商品信息</span>
                    </div>
                    <div class="details-content">
                        <div v-for="item in currentOrder.orderItems" 
                             :key="item.id" 
                             class="detail-product-item">
                            <el-image :src="item.productCover" 
                                     class="product-image"
                                     fit="cover">
                                <template #error>
                                    <div class="image-error">
                                        <el-icon><Picture /></el-icon>
                                    </div>
                                </template>
                            </el-image>
                            <div class="product-info">
                                <div class="product-title">{{ item.productTitle }}</div>
                                <div class="product-meta">
                                    <span class="product-price">
                                        <span class="currency">¥</span>{{ item.price }}
                                    </span>
                                    <span class="product-quantity">× {{ item.quantity }}</span>
                                    <span class="product-subtotal">
                                        小计：<span class="currency">¥</span>{{ (item.price * item.quantity).toFixed(2) }}
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="order-total">
                            <span class="total-label">订单总额：</span>
                            <span class="total-price">
                                <span class="currency">¥</span>{{ currentOrder.totalAmount }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { getOrders, pay, cancelOrder } from '../api/order'
import { 
    List, 
    Document, 
    Clock, 
    Picture, 
    Wallet, 
    Close, 
    View, 
    Location, 
    ShoppingCart 
} from '@element-plus/icons-vue'

const router = useRouter()
const orders = ref([])
const loading = ref(false)
const error = ref('')
const currentStatus = ref('all')
let loadingInstance = null
const detailsDialogVisible = ref(false)
const currentOrder = ref(null)

// 获取订单列表
const fetchOrders = async () => {
    loading.value = true
    error.value = ''
    loadingInstance = ElLoading.service({
        target: '.order-list',
        fullscreen: false,
        text: '加载订单中...'
    })
    
    try {
        const response = await getOrders()
        if (response.code === '200') {
            console.log('订单数据:', response.data); // 添加日志
            orders.value = response.data
        } else if (response.code === '401') {
            error.value = '请先登录'
            ElMessage.warning('请先登录后再查看订单')
            router.push('/login')
        } else {
            error.value = response.msg || '获取订单列表失败'
            ElMessage.error(error.value)
        }
    } catch (err) {
        console.error('获取订单列表失败:', err)
        error.value = '获取订单列表失败，请稍后重试'
        ElMessage.error(error.value)
    } finally {
        loading.value = false
        if (loadingInstance) {
            loadingInstance.close()
        }
    }
}

// 根据状态筛选订单并按时间排序
const filteredOrders = computed(() => {
    let filtered = currentStatus.value === 'all' 
        ? orders.value 
        : orders.value.filter(order => order.status === currentStatus.value);
    
    // 按创建时间降序排序（最新的在前）
    return filtered.sort((a, b) => {
        const dateA = new Date(a.createTime).getTime();
        const dateB = new Date(b.createTime).getTime();
        return dateB - dateA;
    });
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

// 获取状态类型
const getStatusType = (status) => {
    const typeMap = {
        'PENDING': 'warning',
        'SUCCESS': 'success',
        'CANCELLED': 'danger'
    }
    return typeMap[status] || 'info'
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

// 跳转到商品详情
const goToProductDetail = (productId) => {
    router.push(`/products/${productId}`)
}

// 查看订单详情
const handleViewDetails = (order) => {
    currentOrder.value = order
    detailsDialogVisible.value = true
}

// 格式化日期
const formatDate = (dateString) => {
    console.log('格式化日期输入:', dateString, '类型:', typeof dateString); // 添加日志
    
    if (!dateString) {
        console.warn('Date string is empty or null');
        return '未知时间';
    }
    
    try {
        // 尝试解析日期字符串
        let date;
        if (typeof dateString === 'string') {
            // 如果是字符串，尝试直接解析
            date = new Date(dateString);
            console.log('字符串转日期:', date); // 添加日志
        } else if (dateString instanceof Date) {
            // 如果已经是 Date 对象，直接使用
            date = dateString;
            console.log('已经是Date对象:', date); // 添加日志
        } else {
            // 如果是时间戳或其他格式，尝试转换
            date = new Date(dateString);
            console.log('其他格式转日期:', date); // 添加日志
        }

        // 检查日期是否有效
        if (isNaN(date.getTime())) {
            console.error('Invalid date:', dateString, 'Type:', typeof dateString);
            return '无效日期';
        }

        // 格式化日期
        const formattedDate = date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });

        console.log('格式化后的日期:', formattedDate); // 添加日志
        return formattedDate;
    } catch (error) {
        console.error('Date formatting error:', error, 'Input:', dateString);
        return '日期格式错误';
    }
};

onMounted(() => {
    fetchOrders()
})
</script>

<style scoped>
.order-list {
    min-height: 100vh;
    padding: 30px;
    background: linear-gradient(to bottom, #f9f9f9, #f0f2f5);
}

.main-card {
    max-width: 1200px;
    margin: 0 auto;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: linear-gradient(to right, #3a8ee6, #53a8ff);
    margin: -20px -20px 20px -20px;
    border-radius: 12px 12px 0 0;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 15px;
}

.page-title {
    margin: 0;
    color: white;
    font-size: 24px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.order-count {
    font-size: 14px;
    padding: 4px 12px;
    border-radius: 12px;
}

.status-filter {
    margin: 20px 0;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 8px;
    display: flex;
    justify-content: center;
}

.order-items {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.order-item {
    cursor: pointer;
    transition: all 0.3s ease;
    margin-bottom: 20px;
}

.order-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: #f8f9fa;
    border-bottom: 1px solid #ebeef5;
}

.order-info {
    display: flex;
    gap: 20px;
    color: #606266;
}

.order-time {
    display: flex;
    align-items: center;
    gap: 5px;
}

.order-status {
    font-size: 14px;
    padding: 6px 12px;
    border-radius: 4px;
}

.order-products {
    padding: 20px;
}

.product-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border-radius: 8px;
    background: #fff;
    margin-bottom: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.product-item:hover {
    background: #f5f7fa;
    transform: translateX(5px);
}

.product-image {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    margin-right: 20px;
    object-fit: cover;
}

.image-error {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #909399;
    font-size: 24px;
}

.product-info {
    flex: 1;
}

.product-title {
    font-size: 16px;
    color: #303133;
    margin-bottom: 10px;
    font-weight: 500;
}

.product-meta {
    display: flex;
    align-items: center;
    gap: 15px;
    color: #606266;
}

.product-price {
    color: #f56c6c;
    font-size: 18px;
    font-weight: bold;
}

.currency {
    font-size: 14px;
    margin-right: 2px;
}

.product-quantity {
    color: #909399;
}

.order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background: #f8f9fa;
    border-top: 1px solid #ebeef5;
}

.order-total {
    font-size: 16px;
}

.total-label {
    color: #606266;
}

.total-price {
    color: #f56c6c;
    font-size: 24px;
    font-weight: bold;
    margin-left: 8px;
}

.order-actions {
    display: flex;
    gap: 12px;
}

.order-actions .el-button {
    padding: 12px 24px;
}

@media (max-width: 768px) {
    .order-list {
        padding: 15px;
    }

    .order-header {
        flex-direction: column;
        gap: 10px;
        align-items: flex-start;
    }

    .order-info {
        flex-direction: column;
        gap: 10px;
    }

    .product-item {
        flex-direction: column;
        align-items: flex-start;
    }

    .product-image {
        width: 100%;
        height: 200px;
        margin-right: 0;
        margin-bottom: 15px;
    }

    .order-footer {
        flex-direction: column;
        gap: 15px;
    }

    .order-actions {
        width: 100%;
        flex-direction: column;
    }

    .order-actions .el-button {
        width: 100%;
    }
}

.loading-container {
    padding: 20px;
}

.error-alert {
    margin: 20px 0;
}

/* 订单详情对话框样式 */
.order-details-dialog :deep(.el-dialog__header) {
    background: linear-gradient(to right, #409EFF, #67C23A);
    margin: 0;
    padding: 20px;
    border-radius: 8px 8px 0 0;
}

.order-details-dialog :deep(.el-dialog__title) {
    color: white;
    font-size: 20px;
    font-weight: bold;
}

.order-details-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: white;
}

.order-details-dialog :deep(.el-dialog__body) {
    padding: 0;
}

.order-details {
    padding: 20px;
}

.details-section {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #f0f2f5;
    color: #303133;
    font-size: 16px;
    font-weight: bold;
}

.section-header .el-icon {
    margin-right: 8px;
    font-size: 20px;
    color: #409EFF;
}

.details-content {
    padding: 10px 0;
}

.detail-row {
    display: flex;
    margin-bottom: 15px;
    line-height: 1.5;
}

.detail-row .label {
    color: #606266;
    width: 100px;
    flex-shrink: 0;
}

.detail-row .value {
    color: #303133;
    flex: 1;
}

.detail-product-item {
    display: flex;
    align-items: center;
    padding: 15px 0;
    border-bottom: 1px solid #f0f2f5;
}

.detail-product-item:last-child {
    border-bottom: none;
}

.detail-product-item .product-image {
    width: 80px;
    height: 80px;
    border-radius: 4px;
    margin-right: 15px;
}

.detail-product-item .product-info {
    flex: 1;
    min-width: 0;
}

.detail-product-item .product-title {
    font-size: 15px;
    color: #303133;
    margin-bottom: 10px;
    font-weight: 500;
}

.detail-product-item .product-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #909399;
    font-size: 14px;
}

.detail-product-item .product-price {
    color: #f56c6c;
    font-weight: bold;
}

.detail-product-item .product-subtotal {
    color: #f56c6c;
    font-weight: bold;
    margin-left: 20px;
}

.order-total {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 2px dashed #f0f2f5;
    text-align: right;
}

.order-total .total-label {
    font-size: 16px;
    color: #606266;
    margin-right: 10px;
}

.order-total .total-price {
    font-size: 20px;
    color: #f56c6c;
    font-weight: bold;
}

.currency {
    font-size: 0.8em;
    margin-right: 2px;
}
</style>