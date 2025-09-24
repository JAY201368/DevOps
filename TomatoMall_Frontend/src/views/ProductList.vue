<template>
  <div class="product-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <div>
            <el-button
              v-if="isAdmin"
              type="primary"
              @click="handleAdd"
            >
              添加商品
            </el-button>
            <el-button
              v-if="loadError"
              type="warning"
              @click="fetchProducts(true)"
            >
              重新加载
            </el-button>
            <el-button v-else type="info" @click="fetchProducts(true)">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-alert
        v-if="loadError"
        type="error"
        :title="loadErrorMessage"
        :description="isOnline ? '服务器可能暂时无法访问，请稍后再试' : '请检查您的网络连接'"
        show-icon
        :closable="false"
        style="margin-bottom: 15px;"
      />
      
      <div v-if="loading && products.length === 0">
        <el-skeleton :rows="6" animated />
      </div>
      
      <el-table
        v-loading="loading && products.length > 0"
        :data="products"
        style="width: 100%"
        v-show="!loading || products.length > 0"
        :default-sort="{ prop: 'id', order: 'ascending' }"
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />
        <el-table-column
          prop="title"
          label="商品名称"
          width="200"
        />
        <el-table-column
          prop="price"
          label="价格"
          width="120"
        >
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column
          prop="rate"
          label="评分"
          width="120"
        >
          <template #default="scope">
            <el-rate
              v-model="scope.row.rate"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="描述"
          show-overflow-tooltip
        />
        <el-table-column
          label="操作"
          width="200"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              type="primary"
              link
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              v-if="isAdmin"
              type="primary"
              link
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="isAdmin"
              type="primary"
              link
              @click="handleStock(scope.row)"
            >
              库存
            </el-button>
            <el-button
              v-if="isAdmin"
              type="danger"
              link
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 商品表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加商品' : '编辑商品'"
      width="50%"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="商品名称"
          prop="title"
        >
          <el-input v-model="productForm.title" />
        </el-form-item>
        <el-form-item
          label="价格"
          prop="price"
        >
          <el-input-number
            v-model="productForm.price"
            :precision="2"
            :step="0.1"
            :min="0"
          />
        </el-form-item>
        <el-form-item
          label="评分"
          prop="rate"
        >
          <el-rate
            v-model="productForm.rate"
            :max="10"
            :texts="['1分', '2分', '3分', '4分', '5分', '6分', '7分', '8分', '9分', '10分']"
            show-text
          />
        </el-form-item>
        <el-form-item
          label="描述"
          prop="description"
        >
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item
          label="封面"
          prop="cover"
        >
          <el-input v-model="productForm.cover" />
        </el-form-item>
        <el-form-item
          label="详细说明"
          prop="detail"
        >
          <el-input
            v-model="productForm.detail"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 库存调整对话框 -->
    <el-dialog
      v-model="stockDialogVisible"
      title="调整库存"
      width="30%"
    >
      <el-form
        ref="stockFormRef"
        :model="stockForm"
        :rules="stockRules"
        label-width="100px"
      >
        <el-form-item
          label="商品名称"
        >
          <span>{{ currentProduct?.title }}</span>
        </el-form-item>
        <el-form-item
          label="库存数量"
          prop="amount"
        >
          <el-input-number
            v-model="stockForm.amount"
            :min="0"
            :precision="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleStockSubmit"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import {
  getAllProducts,
  createProduct,
  updateProduct,
  deleteProduct,
  updateStockpile
} from '../api/product';
import { getUserInfo } from '../api/user';
import { checkBackendHealth, checkProductsAPI } from '../api/health';

const router = useRouter();
const loading = ref(false);
const products = ref([]);
const dialogVisible = ref(false);
const stockDialogVisible = ref(false);
const dialogType = ref('add');
const currentProduct = ref(null);
const productFormRef = ref(null);
const stockFormRef = ref(null);

const productForm = ref({
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  detail: ''
});

const stockForm = ref({
  amount: 0
});

const rules = {
  title: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  rate: [{ required: true, message: '请选择商品评分', trigger: 'change' }]
};

const stockRules = {
  amount: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
};

// 从用户状态获取管理员状态
const isAdmin = ref(false);

// 获取用户信息并设置管理员状态
const fetchUserInfo = async () => {
  // 首先从localStorage中获取角色信息
  const userRole = localStorage.getItem('userRole');
  if (userRole) {
    isAdmin.value = userRole === 'admin';
  }
  
  // 然后尝试从API获取最新的用户信息
  try {
    const username = localStorage.getItem('username');
    if (username) {
      const res = await getUserInfo(username);
      if (res && res.data) {
        isAdmin.value = res.data.role === 'admin';
        // 更新localStorage中的角色信息
        localStorage.setItem('userRole', res.data.role);
      }
    }
  } catch (error) {
    console.error('获取用户信息失败', error);
  }
};

// 添加错误状态变量
const loadError = ref(false);
const loadErrorMessage = ref('');

// 在 onMounted 之前添加网络状态检测
const isOnline = ref(navigator.onLine);

// 添加诊断功能
const diagnoseConnectionIssue = async () => {
  if (!isOnline.value) {
    return '网络连接已断开，请检查您的网络设置';
  }
  
  // 检查后端健康状态
  const healthStatus = await checkBackendHealth();
  if (!healthStatus.status) {
    return healthStatus.message;
  }
  
  // 检查商品API
  const productAPIStatus = await checkProductsAPI();
  if (!productAPIStatus.status) {
    return productAPIStatus.message;
  }
  
  return '未知错误，请稍后再试';
};

// 添加产品缓存
const useProductCache = () => {
  const CACHE_KEY = 'product_cache';
  const CACHE_EXPIRY = 5 * 60 * 1000; // 5分钟缓存
  
  const saveToCache = (data) => {
    const cache = {
      timestamp: Date.now(),
      data: data
    };
    localStorage.setItem(CACHE_KEY, JSON.stringify(cache));
  };
  
  const getFromCache = () => {
    const cacheStr = localStorage.getItem(CACHE_KEY);
    if (!cacheStr) return null;
    
    try {
      const cache = JSON.parse(cacheStr);
      // 检查缓存是否过期
      if (Date.now() - cache.timestamp > CACHE_EXPIRY) {
        localStorage.removeItem(CACHE_KEY);
        return null;
      }
      return cache.data;
    } catch (e) {
      localStorage.removeItem(CACHE_KEY);
      return null;
    }
  };
  
  const clearCache = () => {
    localStorage.removeItem(CACHE_KEY);
  };
  
  return {
    saveToCache,
    getFromCache,
    clearCache
  };
};

const { saveToCache, getFromCache, clearCache } = useProductCache();

// 添加分页相关的状态
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);

// 分页数据的计算属性
const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return products.value.slice(start, end);
});

// 存储所有商品的引用
const allProducts = ref([]);

// 分页处理函数
const handleSizeChange = (size) => {
  pageSize.value = size;
  updateDisplayedProducts();
};

const handleCurrentChange = (page) => {
  currentPage.value = page;
  updateDisplayedProducts();
};

const updateDisplayedProducts = () => {
  // 前端分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  products.value = allProducts.value.slice(start, end);
};

// 修改fetchProducts函数以支持分页
const fetchProducts = async (forceRefresh = false) => {
  loading.value = true;
  loadError.value = false;
  loadErrorMessage.value = '';

  // 如果不强制刷新，先尝试从缓存获取
  if (!forceRefresh) {
    const cachedProducts = getFromCache();
    if (cachedProducts) {
      console.log('从缓存获取商品列表');
      allProducts.value = cachedProducts;
      totalItems.value = cachedProducts.length;
      updateDisplayedProducts();
      loading.value = false;
      return;
    }
  }
  
  try {
    if (!navigator.onLine) {
      throw new Error('网络连接已断开');
    }
    
    console.log('开始获取商品列表');
    const res = await getAllProducts();
    console.log('获取商品列表响应:', res);
    
    // 根据响应结构处理数据
    if (res.code === 200 || res.code === '200') {
      allProducts.value = res.data;
      totalItems.value = res.data.length;
      updateDisplayedProducts();
      // 保存到缓存
      saveToCache(res.data);
      ElMessage.success('商品列表加载成功');
    } else if (res.data && res.data.code === '200') {
      allProducts.value = res.data.data;
      totalItems.value = res.data.data.length;
      updateDisplayedProducts();
      // 保存到缓存
      saveToCache(res.data.data);
      ElMessage.success('商品列表加载成功');
    } else {
      loadError.value = true;
      loadErrorMessage.value = res.msg || '获取商品列表失败';
      ElMessage.error(loadErrorMessage.value);
    }
  } catch (error) {
    console.error('获取商品列表失败:', error);
    loadError.value = true;
    
    // 使用诊断功能获取更具体的错误信息
    loadErrorMessage.value = await diagnoseConnectionIssue();
    ElMessage.error(loadErrorMessage.value);
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  dialogType.value = 'add';
  productForm.value = {
    title: '',
    price: 0,
    rate: 0,
    description: '',
    cover: '',
    detail: ''
  };
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  dialogType.value = 'edit';
  productForm.value = { ...row };
  dialogVisible.value = true;
};

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该商品吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      console.log('删除商品:', row.id);
      const res = await deleteProduct(row.id);
      console.log('删除商品响应:', res);
      
      if (res.code === 200 || res.code === '200') {
        ElMessage.success('删除成功');
        clearCache();
        fetchProducts(true);
      } else if (res.data && res.data.code === '200') {
        ElMessage.success('删除成功');
        clearCache();
        fetchProducts(true);
      } else {
        ElMessage.error(res.msg || '删除失败');
      }
    } catch (error) {
      console.error('删除商品失败:', error);
      ElMessage.error('删除失败');
    }
  });
};

const handleView = (row) => {
  router.push(`/products/${row.id}`);
};

const handleStock = (row) => {
  currentProduct.value = row;
  // 处理不同的库存数据结构
  if (row.stockpile && typeof row.stockpile === 'object') {
    stockForm.value.amount = row.stockpile.amount || 0;
  } else {
    stockForm.value.amount = row.stockpile || 0;
  }
  stockDialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!productFormRef.value) return;
  
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 检查网络连接
        if (!navigator.onLine) {
          throw new Error('网络连接已断开，请检查网络设置');
        }
        
        // 检查后端健康状态
        const healthStatus = await checkBackendHealth();
        if (!healthStatus.status) {
          throw new Error(healthStatus.message);
        }
        
        const api = dialogType.value === 'add' ? createProduct : updateProduct;
        console.log('提交商品数据:', productForm.value);
        
        const res = await api(productForm.value);
        console.log('提交商品响应:', res);
        
        // 处理不同的响应结构
        if (res.code === 200 || res.code === '200') {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功');
          dialogVisible.value = false;
          clearCache();
          fetchProducts(true);
        } else if (res.data && res.data.code === '200') {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功');
          dialogVisible.value = false;
          clearCache();
          fetchProducts(true);
        } else {
          ElMessage.error(res.msg || (dialogType.value === 'add' ? '添加失败' : '更新失败'));
        }
      } catch (error) {
        console.error('提交商品失败:', error);
        
        let errorMessage = error.message || (dialogType.value === 'add' ? '添加失败' : '更新失败');
        if (error.response && error.response.data) {
          errorMessage = error.response.data.msg || errorMessage;
        }
        
        ElMessage.error(errorMessage);
      }
    }
  });
};

const handleStockSubmit = async () => {
  if (!stockFormRef.value) return;
  
  await stockFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        console.log('更新库存:', {
          productId: currentProduct.value.id, 
          amount: stockForm.value.amount
        });
        
        const res = await updateStockpile(currentProduct.value.id, stockForm.value.amount);
        console.log('更新库存响应:', res);
        
        if (res.code === 200 || res.code === '200') {
          ElMessage.success('调整库存成功');
          stockDialogVisible.value = false;
          clearCache();
          fetchProducts(true);
        } else if (res.data && res.data.code === '200') {
          ElMessage.success('调整库存成功');
          stockDialogVisible.value = false;
          clearCache();
          fetchProducts(true);
        } else {
          ElMessage.error(res.msg || '调整库存失败');
        }
      } catch (error) {
        console.error('调整库存失败:', error);
        ElMessage.error('调整库存失败');
      }
    }
  });
};

// 监听网络状态变化
const handleNetworkChange = () => {
  isOnline.value = navigator.onLine;
  if (!isOnline.value) {
    ElMessage.warning('网络连接已断开，请检查网络设置');
  } else {
    ElMessage.success('网络已连接，正在重新加载数据');
    fetchProducts();
  }
};

// 添加预加载函数
const preloadProductAPI = () => {
  // 利用Service Worker或资源预获取
  const link = document.createElement('link');
  link.rel = 'prefetch';
  link.href = 'http://localhost:8080/api/products';
  document.head.appendChild(link);
  
  // 预热API连接
  setTimeout(() => {
    fetch('http://localhost:8080/api/products', { 
      method: 'HEAD',
      mode: 'no-cors'
    }).catch(() => {
      // 忽略错误，这只是预热连接
    });
  }, 1000);
};

onMounted(() => {
  fetchProducts();
  fetchUserInfo();
  
  // 添加网络状态监听器
  window.addEventListener('online', handleNetworkChange);
  window.addEventListener('offline', handleNetworkChange);
  
  // 预加载API
  preloadProductAPI();
});

// 在组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('online', handleNetworkChange);
  window.removeEventListener('offline', handleNetworkChange);
});
</script>

<style scoped>
.product-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 