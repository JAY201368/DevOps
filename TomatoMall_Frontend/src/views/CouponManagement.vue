<template>
  <div class="coupon-management-container">
    <h1 class="page-title">促销券管理</h1>

    <!-- 操作按钮区域 -->
    <div class="action-bar">
      <el-button type="primary" @click="showCreateCouponDialog">
        <el-icon><Plus /></el-icon>创建促销券
      </el-button>
      <el-button type="success" @click="showDistributeCouponDialog">
        <el-icon><Share /></el-icon>发放促销券
      </el-button>
    </div>

    <!-- 促销券列表 -->
    <el-table
      v-loading="loading"
      :data="coupons"
      border
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="促销券名称" min-width="150" />
      <el-table-column prop="discountAmount" label="折扣金额" width="100">
        <template #default="scope">
          ¥{{ scope.row.discountAmount }}
        </template>
      </el-table-column>
      <el-table-column prop="minOrderAmount" label="最低订单金额" width="120">
        <template #default="scope">
          ¥{{ scope.row.minOrderAmount }}
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" width="160" />
      <el-table-column prop="endDate" label="结束日期" width="160" />
      <el-table-column prop="remainingQuantity" label="剩余数量" width="100" />
      <el-table-column prop="totalQuantity" label="总数量" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row)">{{ getStatusText(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="showCouponDetail(scope.row)">详情</el-button>
          <el-button size="small" type="primary" @click="showEditCouponDialog(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="confirmDeleteCoupon(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑促销券对话框 -->
    <el-dialog
      v-model="couponDialogVisible"
      :title="isEditing ? '编辑促销券' : '创建促销券'"
      width="600px"
    >
      <el-form
        ref="couponFormRef"
        :model="couponForm"
        :rules="couponRules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="促销券名称" prop="name">
          <el-input v-model="couponForm.name" placeholder="请输入促销券名称" />
        </el-form-item>
        <el-form-item label="折扣金额" prop="discountAmount">
          <el-input-number
            v-model="couponForm.discountAmount"
            :precision="2"
            :step="1"
            :min="0"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="最低订单金额" prop="minOrderAmount">
          <el-input-number
            v-model="couponForm.minOrderAmount"
            :precision="2"
            :step="10"
            :min="0"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="couponForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="总数量" prop="totalQuantity">
          <el-input-number
            v-model="couponForm.totalQuantity"
            :min="1"
            :step="10"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="couponForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入促销券描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="couponForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="有效"
            inactive-text="无效"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="couponDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCouponForm" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发放促销券对话框 -->
    <el-dialog
      v-model="distributeDialogVisible"
      title="发放促销券"
      width="600px"
    >
      <el-form
        ref="distributeFormRef"
        :model="distributeForm"
        :rules="distributeRules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="选择促销券" prop="couponId">
          <el-select
            v-model="distributeForm.couponId"
            placeholder="请选择要发放的促销券"
            style="width: 100%"
          >
            <el-option
              v-for="coupon in validCoupons"
              :key="coupon.id"
              :label="coupon.name"
              :value="coupon.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>{{ coupon.name }}</span>
                <span style="color: #8492a6; font-size: 13px">
                  剩余: {{ coupon.remainingQuantity }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="发放方式" prop="distributeType">
          <el-radio-group v-model="distributeForm.distributeType">
            <el-radio :label="'all'">所有用户</el-radio>
            <el-radio :label="'selected'">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="distributeForm.distributeType === 'selected'"
          label="选择用户"
          prop="userIds"
        >
          <el-select
            v-model="distributeForm.userIds"
            multiple
            placeholder="请选择用户"
            style="width: 100%"
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            >
              {{ user.username }} ({{ user.name }})
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="distributeForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="distributeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDistributeForm" :loading="distributing">
            确定发放
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 促销券详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="促销券详情"
      width="700px"
    >
      <div v-if="selectedCoupon" class="coupon-detail">
        <div class="detail-item">
          <span class="label">促销券ID:</span>
          <span class="value">{{ selectedCoupon.id }}</span>
        </div>
        <div class="detail-item">
          <span class="label">促销券名称:</span>
          <span class="value">{{ selectedCoupon.name }}</span>
        </div>
        <div class="detail-item">
          <span class="label">折扣金额:</span>
          <span class="value">¥{{ selectedCoupon.discountAmount }}</span>
        </div>
        <div class="detail-item">
          <span class="label">最低订单金额:</span>
          <span class="value">¥{{ selectedCoupon.minOrderAmount }}</span>
        </div>
        <div class="detail-item">
          <span class="label">开始日期:</span>
          <span class="value">{{ selectedCoupon.startDate }}</span>
        </div>
        <div class="detail-item">
          <span class="label">结束日期:</span>
          <span class="value">{{ selectedCoupon.endDate }}</span>
        </div>
        <div class="detail-item">
          <span class="label">总数量:</span>
          <span class="value">{{ selectedCoupon.totalQuantity }}</span>
        </div>
        <div class="detail-item">
          <span class="label">剩余数量:</span>
          <span class="value">{{ selectedCoupon.remainingQuantity }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态:</span>
          <span class="value">
            <el-tag :type="getStatusType(selectedCoupon)">{{ getStatusText(selectedCoupon) }}</el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="label">创建时间:</span>
          <span class="value">{{ selectedCoupon.createdAt }}</span>
        </div>
        <div class="detail-item">
          <span class="label">更新时间:</span>
          <span class="value">{{ selectedCoupon.updatedAt }}</span>
        </div>
        <div class="detail-item full-width">
          <span class="label">描述:</span>
          <span class="value">{{ selectedCoupon.description || '无' }}</span>
        </div>
      </div>
      
      <h3 class="distribution-title">发放记录</h3>
      <el-table
        v-loading="loadingLogs"
        :data="distributionLogs"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        max-height="300"
      >
        <el-table-column prop="adminUsername" label="管理员" width="120" />
        <el-table-column prop="distributionTime" label="发放时间" width="160" />
        <el-table-column prop="distributionCount" label="发放数量" width="100" />
        <el-table-column prop="distributionCondition" label="发放条件" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Share } from '@element-plus/icons-vue';
import {
  getAllCoupons,
  createCoupon,
  updateCoupon,
  deleteCoupon,
  distributeCoupons,
  getCouponDistributionLogs
} from '../api/coupon';
import { getAllUsers } from '../api/user';
import { clearUrlCache } from '../api/request';

// 数据定义
const loading = ref(false);
const coupons = ref([]);
const validCoupons = computed(() => {
  return coupons.value.filter(coupon => 
    coupon.status === 1 && 
    coupon.remainingQuantity > 0 && 
    !coupon.isExpired && 
    coupon.isStarted
  );
});
const users = ref([]);
const loadingLogs = ref(false);
const distributionLogs = ref([]);

// 对话框控制
const couponDialogVisible = ref(false);
const distributeDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const isEditing = ref(false);
const submitting = ref(false);
const distributing = ref(false);

// 选中的促销券
const selectedCoupon = ref(null);

// 表单引用
const couponFormRef = ref(null);
const distributeFormRef = ref(null);

// 促销券表单数据
const couponForm = reactive({
  id: null,
  name: '',
  discountAmount: 10,
  minOrderAmount: 0,
  dateRange: [],
  totalQuantity: 100,
  description: '',
  status: 1
});

// 促销券表单校验规则
const couponRules = {
  name: [
    { required: true, message: '请输入促销券名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  discountAmount: [
    { required: true, message: '请输入折扣金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '折扣金额必须大于等于0', trigger: 'blur' }
  ],
  minOrderAmount: [
    { required: true, message: '请输入最低订单金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '最低订单金额必须大于等于0', trigger: 'blur' }
  ],
  dateRange: [
    { required: true, message: '请选择有效期', trigger: 'change' }
  ],
  totalQuantity: [
    { required: true, message: '请输入总数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '总数量必须大于0', trigger: 'blur' }
  ]
};

// 发放表单数据
const distributeForm = reactive({
  couponId: null,
  distributeType: 'all',
  userIds: [],
  distributionCondition: '',
  remark: ''
});

// 发放表单校验规则
const distributeRules = {
  couponId: [
    { required: true, message: '请选择促销券', trigger: 'change' }
  ],
  userIds: [
    { 
      required: true, 
      message: '请选择至少一个用户', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (distributeForm.distributeType === 'selected' && (!value || value.length === 0)) {
          callback(new Error('请选择至少一个用户'));
        } else {
          callback();
        }
      }
    }
  ]
};

// 生命周期钩子
onMounted(() => {
  // 清除促销券相关的缓存
  clearUrlCache('/api/coupons');
  fetchCoupons();
  fetchUsers();
});

// 获取所有促销券
const fetchCoupons = async () => {
  loading.value = true;
  try {
    // 清除促销券相关的缓存
    clearUrlCache('/api/coupons');
    
    const res = await getAllCoupons();
    if (res.code === '200') {
      coupons.value = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取促销券列表失败');
    }
  } catch (error) {
    console.error('获取促销券列表出错:', error);
    ElMessage.error('获取促销券列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取所有用户
const fetchUsers = async () => {
  try {
    console.log('开始获取用户列表');
    const res = await getAllUsers();
    console.log('获取用户列表结果:', res);
    
    if (res.code === '200') {
      // 只筛选普通用户
      users.value = res.data.filter(user => user.role === 'user') || [];
      console.log('筛选后的用户列表:', users.value);
    } else {
      console.error('获取用户列表失败:', res.msg);
      ElMessage.error(res.msg || '获取用户列表失败');
    }
  } catch (error) {
    console.error('获取用户列表出错:', error);
    ElMessage.error('获取用户列表失败');
  }
};

// 获取促销券发放记录
const fetchDistributionLogs = async (couponId) => {
  loadingLogs.value = true;
  try {
    const res = await getCouponDistributionLogs(couponId);
    if (res.code === '200') {
      distributionLogs.value = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取发放记录失败');
    }
  } catch (error) {
    console.error('获取发放记录出错:', error);
    ElMessage.error('获取发放记录失败');
  } finally {
    loadingLogs.value = false;
  }
};

// 状态类型
const getStatusType = (coupon) => {
  if (coupon.status !== 1) return 'info';
  if (coupon.isExpired) return 'danger';
  if (!coupon.isStarted) return 'warning';
  if (coupon.remainingQuantity <= 0) return 'info';
  return 'success';
};

// 状态文本
const getStatusText = (coupon) => {
  if (coupon.status !== 1) return '已禁用';
  if (coupon.isExpired) return '已过期';
  if (!coupon.isStarted) return '未开始';
  if (coupon.remainingQuantity <= 0) return '已领完';
  return '有效';
};

// 显示创建促销券对话框
const showCreateCouponDialog = () => {
  isEditing.value = false;
  resetCouponForm();
  couponDialogVisible.value = true;
};

// 显示编辑促销券对话框
const showEditCouponDialog = (row) => {
  isEditing.value = true;
  resetCouponForm();
  
  couponForm.id = row.id;
  couponForm.name = row.name;
  couponForm.discountAmount = parseFloat(row.discountAmount);
  couponForm.minOrderAmount = parseFloat(row.minOrderAmount);
  couponForm.dateRange = [row.startDate, row.endDate];
  couponForm.totalQuantity = row.totalQuantity;
  couponForm.description = row.description || '';
  couponForm.status = row.status;
  
  couponDialogVisible.value = true;
};

// 显示促销券详情
const showCouponDetail = (row) => {
  selectedCoupon.value = row;
  detailDialogVisible.value = true;
  fetchDistributionLogs(row.id);
};

// 确认删除促销券
const confirmDeleteCoupon = (row) => {
  ElMessageBox.confirm(
    '确定要删除该促销券吗？此操作将直接删除该促销券及其所有相关记录，无法恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      handleDeleteCoupon(row.id);
    })
    .catch(() => {
      // 用户取消删除
    });
};

// 处理删除促销券
const handleDeleteCoupon = async (couponId) => {
  try {
    const res = await deleteCoupon(couponId);
    if (res.code === '200') {
      ElMessage.success('删除成功');
      
      // 清除促销券相关的缓存
      clearUrlCache('/api/coupons');
      
      // 重新加载数据
      await fetchCoupons();
    } else {
      ElMessage.error(res.msg || '删除失败');
    }
  } catch (error) {
    console.error('删除促销券出错:', error);
    ElMessage.error('删除失败');
  }
};

// 重置促销券表单
const resetCouponForm = () => {
  couponForm.id = null;
  couponForm.name = '';
  couponForm.discountAmount = 10;
  couponForm.minOrderAmount = 0;
  couponForm.dateRange = [];
  couponForm.totalQuantity = 100;
  couponForm.description = '';
  couponForm.status = 1;
  
  if (couponFormRef.value) {
    couponFormRef.value.resetFields();
  }
};

// 提交促销券表单
const submitCouponForm = async () => {
  if (!couponFormRef.value) return;
  
  await couponFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    
    try {
      // 准备提交数据
      const submitData = {
        name: couponForm.name,
        discountAmount: couponForm.discountAmount,
        minOrderAmount: couponForm.minOrderAmount,
        startDate: couponForm.dateRange[0],
        endDate: couponForm.dateRange[1],
        totalQuantity: couponForm.totalQuantity,
        description: couponForm.description,
        status: couponForm.status
      };
      
      let res;
      if (isEditing.value) {
        // 编辑模式
        submitData.id = couponForm.id;
        res = await updateCoupon(couponForm.id, submitData);
      } else {
        // 创建模式
        res = await createCoupon(submitData);
      }
      
      if (res.code === '200') {
        ElMessage.success(isEditing.value ? '更新成功' : '创建成功');
        couponDialogVisible.value = false;
        
        // 清除促销券相关的缓存
        clearUrlCache('/api/coupons');
        
        // 重新加载数据
        await fetchCoupons();
      } else {
        ElMessage.error(res.msg || (isEditing.value ? '更新失败' : '创建失败'));
      }
    } catch (error) {
      console.error(isEditing.value ? '更新促销券出错:' : '创建促销券出错:', error);
      ElMessage.error(isEditing.value ? '更新失败' : '创建失败');
    } finally {
      submitting.value = false;
    }
  });
};

// 显示发放促销券对话框
const showDistributeCouponDialog = () => {
  resetDistributeForm();
  distributeDialogVisible.value = true;
};

// 重置发放表单
const resetDistributeForm = () => {
  distributeForm.couponId = null;
  distributeForm.distributeType = 'all';
  distributeForm.userIds = [];
  distributeForm.distributionCondition = '';
  distributeForm.remark = '';
  
  if (distributeFormRef.value) {
    distributeFormRef.value.resetFields();
  }
};

// 提交发放表单
const submitDistributeForm = async () => {
  if (!distributeFormRef.value) return;
  
  await distributeFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    distributing.value = true;
    
    try {
      // 准备提交数据
      const submitData = {
        couponId: distributeForm.couponId,
        remark: distributeForm.remark
      };
      
      // 根据发放方式设置用户ID
      if (distributeForm.distributeType === 'selected') {
        submitData.userIds = distributeForm.userIds;
        submitData.distributionCondition = '指定用户发放';
      } else {
        // 全部用户
        submitData.distributionCondition = '所有用户发放';
      }
      
      const res = await distributeCoupons(submitData);
      
      if (res.code === '200') {
        ElMessage.success('促销券发放成功');
        distributeDialogVisible.value = false;
        
        // 清除促销券相关的缓存
        clearUrlCache('/api/coupons');
        
        // 重新加载数据
        await fetchCoupons();
      } else {
        ElMessage.error(res.msg || '促销券发放失败');
      }
    } catch (error) {
      console.error('发放促销券出错:', error);
      ElMessage.error('促销券发放失败');
    } finally {
      distributing.value = false;
    }
  });
};
</script>

<style scoped>
.coupon-management-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.coupon-detail {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.detail-item {
  width: 50%;
  margin-bottom: 12px;
  display: flex;
}

.detail-item.full-width {
  width: 100%;
}

.detail-item .label {
  width: 120px;
  color: #606266;
  text-align: right;
  padding-right: 12px;
  font-weight: 500;
}

.detail-item .value {
  flex: 1;
  color: #303133;
}

.distribution-title {
  margin: 20px 0 10px;
  font-size: 16px;
  color: #303133;
}
</style>

