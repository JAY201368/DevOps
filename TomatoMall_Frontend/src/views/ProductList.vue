<template>
  <div class="product-list">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2 class="page-title" v-if="!isBannerProducts">
              商品列表
              <el-tag type="info" effect="plain" class="product-count">
                共 {{ totalItems }} 个商品
              </el-tag>
            </h2>
            <h2 class="page-title" v-else>
              图书专题
            </h2>
          </div>
          <div class="header-right">
            <el-button
              v-if="isAdmin"
              type="primary"
              @click="handleAdd"
              class="action-button"
            >
              <el-icon><Plus /></el-icon> 添加商品
            </el-button>
            <el-button
              v-if="loadError"
              type="warning"
              @click="fetchProducts(true)"
              class="action-button"
            >
              <el-icon><Refresh /></el-icon> 重新加载
            </el-button>
            <el-button
              v-else
              type="info"
              @click="fetchProducts(true)"
              class="action-button"
            >
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 轮播图专题介绍 -->
      <div v-if="isBannerProducts && currentBanner" class="banner-detail-container">
        <div class="banner-detail-content">
          <div class="banner-detail-icon">
            <el-icon class="banner-icon"><Collection /></el-icon>
          </div>
          <div class="banner-detail-text">
            <h3 class="banner-detail-title">{{ currentBanner.title }}</h3>
            <p class="banner-detail-description">{{ currentBanner.description }}</p>
            <div class="banner-detail-tags">
              <el-tag size="small" effect="dark" type="success" class="detail-tag">精选推荐</el-tag>
              <el-tag size="small" effect="dark" type="info" class="detail-tag">共 {{ totalItems }} 本图书</el-tag>
            </div>
          </div>
        </div>
        <div class="banner-detail-divider">
          <span class="divider-text">精选图书</span>
        </div>
      </div>

      <el-alert
        v-if="loadError"
        type="error"
        :title="loadErrorMessage"
        :description="
          isOnline ? '服务器可能暂时无法访问，请稍后再试' : '请检查您的网络连接'
        "
        show-icon
        :closable="false"
        class="error-alert"
      />

      <div v-if="loading && products.length === 0" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>

      <!-- 卡片视图 -->
      <div class="card-view-container" :class="{ 'banner-products': isBannerProducts }">
        <el-row :gutter="20">
          <el-col
            v-for="product in products"
            :key="product.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            :xl="4"
            class="card-col"
          >
            <el-card class="product-card" shadow="hover" :class="{ 'banner-product-card': isBannerProducts }">
              <div class="product-card-header">
                <h3 class="product-card-title">{{ product.title }}</h3>
              </div>

              <!-- 添加商品封面图片 -->
              <div class="product-card-image" v-if="product.cover">
                <img
                  :src="product.cover"
                  :alt="product.title"
                  class="product-image"
                />
              </div>
              <div class="product-card-image placeholder" v-else>
                <el-icon class="no-image-icon"><Picture /></el-icon>
                <span class="no-image-text">暂无图片</span>
              </div>

              <div class="product-card-content">
                <div class="product-card-rating">
                  <el-rate
                    :model-value="Number(product.rate) / 2"
                    disabled
                    text-color="#ff9900"
                    :allow-half="true"
                    class="card-rate"
                  />
                  <span class="rate-value"
                    >{{ Number(product.rate).toFixed(1) }}分</span
                  >
                </div>

                <div class="product-card-description">
                  {{ product.description || "暂无描述" }}
                </div>
              </div>

              <!-- 操作按钮 - 移到卡片外部并使用绝对定位 -->
              <div class="product-card-actions">
                <el-button
                  type="primary"
                  link
                  @click="handleView(product)"
                  class="card-action-button"
                >
                  <el-icon><View /></el-icon> 查看
                </el-button>
                <el-button
                  v-if="isAdmin"
                  type="success"
                  link
                  @click="handleEdit(product)"
                  class="card-action-button"
                >
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>
                <el-button
                  v-if="isAdmin"
                  type="warning"
                  link
                  @click="handleStock(product)"
                  class="card-action-button"
                >
                  <el-icon><Box /></el-icon> 库存
                </el-button>
                <el-button
                  v-if="isAdmin"
                  type="danger"
                  link
                  @click="handleDelete(product)"
                  class="card-action-button"
                >
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && products.length === 0"
        description="暂无商品数据"
        :image-size="200"
        class="empty-state"
      />

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
          background
        />
      </div>
    </el-card>

    <!-- 商品表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加商品' : '编辑商品'"
      width="70%"
      class="product-dialog"
    >
      <div class="form-header">
        <div class="form-icon">{{ dialogType === "add" ? "📦" : "✏️" }}</div>
        <div class="form-title">
          {{ dialogType === "add" ? "新增商品信息" : "编辑商品信息" }}
        </div>
      </div>

      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="rules"
        label-width="120px"
        class="product-form"
      >
        <div class="form-section basic-info">
          <div class="section-header">
            <div class="section-title">基本信息</div>
            <div class="section-line"></div>
          </div>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品名称" prop="title">
                <el-input
                  v-model="productForm.title"
                  placeholder="请输入商品名称"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input-number
                  v-model="productForm.price"
                  :precision="2"
                  :step="0.1"
                  :min="0"
                  :max="999999.99"
                  style="width: 100%"
                  placeholder="请输入商品价格"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="评分" prop="rate">
            <div class="rate-edit-container">
              <el-rate
                v-model="productForm.rate"
                :max="5"
                :allow-half="true"
                :colors="['#ffd21e', '#ffd21e', '#ffd21e']"
              />
              <div class="rate-value-display">
                {{ (productForm.rate * 2).toFixed(1) }} 分
              </div>
              <div class="rate-hint">（每半颗星代表1分，满分10分）</div>
            </div>
          </el-form-item>
        </div>

        <div class="form-section description-info">
          <div class="section-header">
            <div class="section-title">描述信息</div>
            <div class="section-line"></div>
          </div>

          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="productForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入商品描述"
            />
          </el-form-item>

          <el-form-item label="详细说明" prop="detail">
            <el-input
              v-model="productForm.detail"
              type="textarea"
              :rows="5"
              placeholder="请输入商品详细说明"
            />
          </el-form-item>
        </div>

        <div class="form-section cover-info">
          <div class="section-header">
            <div class="section-title">封面图片</div>
            <div class="section-line"></div>
          </div>

          <el-form-item label="封面URL" prop="cover">
            <el-input
              v-model="productForm.cover"
              placeholder="请输入商品封面图片URL"
            >
              <template #append>
                <el-button @click="previewCover">
                  <el-icon><Picture /></el-icon> 预览
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <div v-if="productForm.cover" class="cover-preview">
            <img :src="productForm.cover" alt="商品封面预览" />
          </div>
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ dialogType === "add" ? "添加商品" : "保存修改" }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 库存调整对话框 -->
    <el-dialog
      v-model="stockDialogVisible"
      title="调整库存"
      width="40%"
      class="product-dialog"
    >
      <el-form
        ref="stockFormRef"
        :model="stockForm"
        :rules="stockRules"
        label-width="120px"
      >
        <el-form-item label="商品名称">
          <span class="product-name">{{ currentProduct?.title }}</span>
        </el-form-item>
        <el-form-item label="当前价格">
          <span class="product-price">¥{{ currentProduct?.price }}</span>
        </el-form-item>
        <el-form-item label="库存数量" prop="amount">
          <el-input-number
            v-model="stockForm.amount"
            :min="0"
            :max="999999"
            style="width: 200px"
          />
          <div class="stock-hint">设置为0表示商品售罄</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockDialogVisible = false">取消</el-button>
          <el-button
            type="success"
            @click="handleStockSubmit"
            :loading="stockSubmitting"
          >
            保存库存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Refresh,
  Picture,
  View,
  Edit,
  Box,
  Delete,
  Plus,
  Collection,
} from "@element-plus/icons-vue";
import {
  getAllProducts,
  createProduct,
  updateProduct,
  updateProductBasicInfo,
  deleteProduct,
  updateStockpile,
  getProductDetails,
} from "../api/product";
import { getUserInfo } from "../api/user";
import { checkBackendHealth, checkProductsAPI } from "../api/health";
import { getBannerById } from '../api/banner';
import { useUserStore } from '../store/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const loading = ref(false);
const products = ref([]);
const allProducts = ref([]);
const dialogVisible = ref(false);
const stockDialogVisible = ref(false);
const dialogType = ref("add");
const currentProduct = ref(null);
const productFormRef = ref(null);
const stockFormRef = ref(null);

const productForm = ref({
  title: "",
  price: 0,
  rate: 0,
  description: "",
  cover: "",
  detail: "",
  specifications: [],
});

const stockForm = ref({
  amount: 0,
});

const rules = {
  title: [{ required: true, message: "请输入商品名称", trigger: "blur" }],
  price: [
    { required: true, message: "请输入商品价格", trigger: "blur" },
    {
      type: "number",
      min: 0,
      max: 999999.99,
      message: "价格必须在0-999999.99之间",
      trigger: "blur",
    },
  ],
  rate: [
    { required: true, message: "请选择商品评分", trigger: "change" },
    {
      type: "number",
      min: 0,
      max: 5,
      message: "评分必须在0-5之间",
      trigger: "blur",
    },
  ],
  description: [{ required: true, message: "请输入商品描述", trigger: "blur" }],
  cover: [{ required: true, message: "请输入商品封面URL", trigger: "blur" }],
};

const stockRules = {
  amount: [
    { required: true, message: "请输入库存数量", trigger: "blur" },
    {
      type: "number",
      min: 0,
      max: 999999,
      message: "库存数量必须在0-999999之间",
      trigger: "blur",
    },
  ],
};

// 从用户状态获取管理员状态
const isAdmin = computed(() => userStore.isAdmin);

// 获取路由参数
const bannerId = computed(() => route.params.bannerId);
const isBannerProducts = computed(() => route.name === 'BannerProducts');

// 当前轮播图信息
const currentBanner = ref(null);

// 添加错误状态变量
const loadError = ref(false);
const loadErrorMessage = ref("");

// 在 onMounted 之前添加网络状态检测
const isOnline = ref(navigator.onLine);

// 添加诊断功能
const diagnoseConnectionIssue = async () => {
  if (!isOnline.value) {
    return "网络连接已断开，请检查您的网络设置";
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

  return "未知错误，请稍后再试";
};

// 添加产品缓存
const useProductCache = () => {
  const CACHE_KEY = "product_cache";
  const CACHE_EXPIRY = 5 * 60 * 1000; // 5分钟缓存

  const saveToCache = (data) => {
    const cache = {
      timestamp: Date.now(),
      data: data,
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
    clearCache,
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

// 获取所有商品
const fetchProducts = async (forceRefresh = false) => {
  loading.value = true;
  loadError.value = false;
  loadErrorMessage.value = "";
  
  try {
    // 如果是轮播图商品页面，则获取轮播图信息
    if (isBannerProducts.value && bannerId.value) {
      console.log(`正在获取轮播图(ID:${bannerId.value})的商品列表`);
      await fetchBannerProducts();
    } else {
      console.log('正在获取所有商品列表');
      // 正常获取所有商品
      const res = await getAllProducts(forceRefresh);
      if (res.code === '200') {
        allProducts.value = res.data || [];
        totalItems.value = allProducts.value.length;
        
        // 更新分页数据
        updateDisplayedProducts();
        
        console.log(`成功加载${allProducts.value.length}个商品`);
      } else {
        handleLoadError(res.msg || '获取商品数据失败');
      }
    }
  } catch (error) {
    console.error('获取商品列表失败:', error);
    handleLoadError('获取商品数据失败');
  } finally {
    loading.value = false;
  }
};

// 获取轮播图商品
const fetchBannerProducts = async () => {
  try {
    const res = await getBannerById(bannerId.value);
    if (res.code === '200' && res.data) {
      currentBanner.value = res.data;
      
      // 确保只显示轮播图中的书籍
      if (res.data.books && Array.isArray(res.data.books)) {
        products.value = res.data.books;
        allProducts.value = res.data.books; // 更新allProducts以便分页正常工作
        totalItems.value = products.value.length;
        
        // 重置分页到第一页
        currentPage.value = 1;
        
        // 更新页面标题
        document.title = `${currentBanner.value.title} - 商品列表`;
        
        console.log(`成功加载轮播图(ID:${bannerId.value})的${products.value.length}本图书`);
      } else {
        console.error('轮播图中没有图书数据:', res.data);
        products.value = [];
        allProducts.value = [];
        totalItems.value = 0;
        handleLoadError('该轮播图中没有图书');
      }
    } else {
      handleLoadError(res.msg || '获取轮播图商品失败');
    }
  } catch (error) {
    console.error('获取轮播图商品失败:', error);
    handleLoadError('获取轮播图商品失败');
  }
};

const handleAdd = () => {
  dialogType.value = "add";
  productForm.value = {
    title: "",
    price: 0,
    rate: 0,
    description: "",
    cover: "",
    detail: "",
    specifications: [],
  };
  dialogVisible.value = true;
};

const handleEdit = async (row) => {
  try {
    console.log("准备编辑商品:", row.id);

    // 在编辑前先获取完整的商品数据，确保包含specifications
    const productResponse = await getProductDetails(row.id);
    console.log("获取到的完整商品数据:", productResponse);

    // 获取完整的商品数据，包括specifications
    let fullProductData;
    if (productResponse.code === 200 || productResponse.code === "200") {
      fullProductData = productResponse.data;
    } else if (productResponse.data && productResponse.data.code === "200") {
      fullProductData = productResponse.data.data;
    }

    if (!fullProductData) {
      throw new Error("获取商品详情失败");
    }

    // 保存完整的商品数据，以便在提交时使用
    currentProduct.value = fullProductData;
    console.log("保存的完整商品数据:", currentProduct.value);

    // 设置表单数据
    productForm.value = {
      id: row.id,
      title: row.title,
      price: row.price,
      rate: row.rate / 2, // 转换为5分制
      description: row.description,
      cover: row.cover,
      detail: row.detail || "",
    };

    dialogType.value = "edit";
    dialogVisible.value = true;
  } catch (error) {
    console.error("获取商品详情失败:", error);
    ElMessage.error(
      "获取商品详情失败: " + (error.message || JSON.stringify(error))
    );
  }
};

const handleDelete = (row) => {
  ElMessageBox.confirm("确定要删除该商品吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      console.log("开始删除商品:", row.id);

      // 1. 清除缓存，防止从缓存中读取
      clearCache();
      console.log("已清除商品缓存");

      // 2. 删除商品
      const res = await deleteProduct(row.id);
      console.log("删除商品响应:", res);

      // 3. 根据响应处理结果
      if (
        res.code === 200 ||
        res.code === "200" ||
        (res.data && res.data.code === "200")
      ) {
        ElMessage.success("删除成功");

        // 4. 直接从本地数组中移除该商品
        const index = allProducts.value.findIndex((p) => p.id === row.id);
        if (index !== -1) {
          console.log("从本地数组中移除商品，索引:", index);
          allProducts.value.splice(index, 1);
          totalItems.value = allProducts.value.length;
          updateDisplayedProducts();
        }

        // 5. 再次清除缓存并强制从服务器重新获取数据
        console.log("再次强制刷新商品列表");
        setTimeout(async () => {
          await fetchProducts(true);
        }, 300);
      } else {
        ElMessage.error(res.msg || "删除失败");
      }
    } catch (error) {
      console.error("删除商品失败:", error);
      ElMessage.error("删除失败: " + (error.message || JSON.stringify(error)));

      // 如果删除失败，仍然强制刷新以确保数据同步
      setTimeout(async () => {
        await fetchProducts(true);
      }, 500);
    }
  });
};

const handleView = (row) => {
  router.push(`/products/${row.id}`);
};

const handleStock = (row) => {
  currentProduct.value = row;
  // 处理不同的库存数据结构
  if (row.stockpile && typeof row.stockpile === "object") {
    stockForm.value.amount = row.stockpile.amount || 0;
  } else {
    stockForm.value.amount = row.stockpile || 0;
  }
  stockDialogVisible.value = true;
};

const submitting = ref(false);
const stockSubmitting = ref(false);

const handleSubmit = async () => {
  if (!productFormRef.value) return;

  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 检查网络连接
        if (!navigator.onLine) {
          throw new Error("网络连接已断开，请检查网络设置");
        }

        let res;

        if (dialogType.value === "add") {
          // 添加新商品
          const submitData = {
            ...productForm.value,
            rate: Number(productForm.value.rate) * 2, // 半颗星代表1分，转换为10分制
          };

          console.log("提交新商品数据:", submitData);
          res = await createProduct(submitData);
        } else {
          // 编辑商品 - 使用删除后重新创建的方式
          const currentId = productForm.value.id;
          console.log("当前编辑商品ID:", currentId);
          console.log("当前保存的完整商品数据:", currentProduct.value);

          if (!currentProduct.value || !currentProduct.value.specifications) {
            console.warn("警告: 未找到原始商品的规格信息!");
          }

          // 准备更新商品数据
          const updateData = {
            id: currentId,
            title: productForm.value.title,
            price: productForm.value.price,
            rate: Number(productForm.value.rate) * 2, // 半颗星代表1分，转换为10分制
            description: productForm.value.description,
            cover: productForm.value.cover,
            detail: productForm.value.detail,
            // 非常重要：保留原有的规格信息
            specifications:
              currentProduct.value && currentProduct.value.specifications
                ? currentProduct.value.specifications
                : [],
          };

          console.log("准备更新商品:", JSON.stringify(updateData));

          // 直接更新现有商品
          res = await updateProduct(updateData);
          console.log("更新商品响应:", res);
        }

        console.log("提交商品响应:", res);

        // 处理不同的响应结构
        if (res.code === 200 || res.code === "200") {
          ElMessage.success(
            dialogType.value === "add" ? "添加成功" : "更新成功"
          );
          dialogVisible.value = false;

          // 清除缓存并强制刷新数据
          clearCache();
          console.log("清除缓存，强制刷新数据");
          await fetchProducts(true);

          // 只有在添加模式下才跳转到新商品详情页
          if (dialogType.value === "add" && res.data?.id) {
            console.log("跳转到新商品详情页:", res.data.id);
            router.push(`/products/${res.data.id}`);
          }
        } else if (res.data && res.data.code === "200") {
          ElMessage.success(
            dialogType.value === "add" ? "添加成功" : "更新成功"
          );
          dialogVisible.value = false;

          // 清除缓存并强制刷新数据
          clearCache();
          console.log("清除缓存，强制刷新数据");
          await fetchProducts(true);

          // 只有在添加模式下才跳转到新商品详情页
          if (dialogType.value === "add" && res.data.data?.id) {
            console.log("跳转到新商品详情页:", res.data.data.id);
            router.push(`/products/${res.data.data.id}`);
          }
        } else {
          ElMessage.error(
            res.msg || (dialogType.value === "add" ? "添加失败" : "更新失败")
          );
        }
      } catch (error) {
        console.error("提交商品失败:", error);

        let errorMessage =
          error.message ||
          (dialogType.value === "add" ? "添加失败" : "更新失败");
        if (error.response && error.response.data) {
          errorMessage = error.response.data.msg || errorMessage;
        }

        ElMessage.error(errorMessage);
      } finally {
        submitting.value = false;
      }
    }
  });
};

const handleStockSubmit = async () => {
  if (!stockFormRef.value) return;

  await stockFormRef.value.validate(async (valid) => {
    if (valid) {
      stockSubmitting.value = true;
      try {
        console.log("更新库存:", {
          productId: currentProduct.value.id,
          amount: stockForm.value.amount,
        });

        const res = await updateStockpile(
          currentProduct.value.id,
          stockForm.value.amount
        );
        console.log("更新库存响应:", res);

        if (res.code === 200 || res.code === "200") {
          ElMessage.success("调整库存成功");
          stockDialogVisible.value = false;
          clearCache();
          fetchProducts(true);
        } else if (res.data && res.data.code === "200") {
          ElMessage.success("调整库存成功");
          stockDialogVisible.value = false;
          clearCache();
          fetchProducts(true);
        } else {
          ElMessage.error(res.msg || "调整库存失败");
        }
      } catch (error) {
        console.error("调整库存失败:", error);
        ElMessage.error("调整库存失败");
      } finally {
        stockSubmitting.value = false;
      }
    }
  });
};

// 监听网络状态变化
const handleNetworkChange = () => {
  isOnline.value = navigator.onLine;
  if (!isOnline.value) {
    ElMessage.warning("网络连接已断开，请检查网络设置");
  } else {
    ElMessage.success("网络已连接，正在重新加载数据");
    fetchProducts();
  }
};

// 添加预加载函数
const preloadProductAPI = () => {
  // 利用Service Worker或资源预获取
  const link = document.createElement("link");
  link.rel = "prefetch";
  link.href = "http://localhost:8080/api/products";
  document.head.appendChild(link);

  // 预热API连接
  setTimeout(() => {
    fetch("http://localhost:8080/api/products", {
      method: "HEAD",
      mode: "no-cors",
    }).catch(() => {
      // 忽略错误，这只是预热连接
    });
  }, 1000);
};

// 添加预览封面方法
const previewCover = () => {
  if (productForm.value.cover) {
    window.open(productForm.value.cover, "_blank");
  } else {
    ElMessage.warning("请先输入封面URL");
  }
};

// 监听路由参数变化
watch(() => route.params.bannerId, (newVal) => {
  if (newVal) {
    fetchProducts(true);
  }
});

// 监听路由名称变化
watch(() => route.name, (newName, oldName) => {
  console.log(`路由名称从 ${oldName} 变为 ${newName}`);
  
  // 当从BannerProducts切换到ProductList时，强制刷新数据
  if (oldName === 'BannerProducts' && newName === 'ProductList') {
    console.log('从轮播图详情页切换到商品列表页，强制刷新数据');
    // 清除当前轮播图信息
    currentBanner.value = null;
    fetchProducts(true);
  }
});

// 监听isBannerProducts的变化
watch(isBannerProducts, (newValue, oldValue) => {
  console.log(`isBannerProducts从 ${oldValue} 变为 ${newValue}`);
  
  // 当从轮播图详情页切换到普通商品列表页时，强制刷新数据
  if (oldValue === true && newValue === false) {
    console.log('从轮播图模式切换到普通商品列表模式，强制刷新数据');
    // 清除当前轮播图信息
    currentBanner.value = null;
    fetchProducts(true);
  }
});

// 处理加载错误
const handleLoadError = (message) => {
  loadError.value = true;
  loadErrorMessage.value = message || "加载失败";
  ElMessage.error(loadErrorMessage.value);
};

onMounted(() => {
  console.log('ProductList组件已挂载');
  console.log('当前路由名称:', route.name);
  console.log('当前路由参数:', route.params);
  console.log('是否为轮播图商品页面:', isBannerProducts.value);
  
  // 获取商品列表
  fetchProducts(true);
  
  // 获取用户信息
  userStore.fetchUserInfo();
  
  // 添加网络状态监听器
  window.addEventListener("online", handleNetworkChange);
  window.addEventListener("offline", handleNetworkChange);

  // 预加载API
  preloadProductAPI();
});

// 在组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener("online", handleNetworkChange);
  window.removeEventListener("offline", handleNetworkChange);
});
</script>

<style scoped>
.product-list {
  padding: 20px;
  background: linear-gradient(to bottom, #f0f2f5, #eaedf1);
  min-height: 100vh;
}

.main-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  overflow: hidden;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(to right, #3a8ee6, #53a8ff);
  color: white;
  padding: 15px 20px;
  margin: -20px -20px 20px -20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.product-count {
  background-color: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.view-mode-switch {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  padding: 2px;
}

.view-mode-switch :deep(.el-radio-button__inner) {
  background-color: transparent;
  border: none;
  color: white;
}

.view-mode-switch
  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: rgba(255, 255, 255, 0.3);
  box-shadow: none;
}

.action-button {
  background-color: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.action-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.error-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

.loading-container {
  padding: 20px;
}

/* 卡片视图样式 */
.card-view-container {
  margin-bottom: 20px;
}

.card-col {
  margin-bottom: 20px;
}

.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.product-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.product-card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 商品封面图片样式 */
.product-card-image {
  height: 180px;
  width: 100%;
  margin-bottom: 15px;
  border-radius: 6px;
  overflow: hidden;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.placeholder {
  flex-direction: column;
  gap: 10px;
  color: #909399;
}

.no-image-icon {
  font-size: 36px;
}

.no-image-text {
  font-size: 14px;
}

.product-card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-card-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
}

.card-rate {
  margin-right: 5px;
}

.product-card-description {
  color: #606266;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  margin-bottom: 10px;
}

.product-card-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  padding: 15px;
  border-radius: 0 0 8px 8px;
  transform: translateY(100%);
  transition: transform 0.3s ease;
  z-index: 1;
  opacity: 0;
}

.product-card:hover .product-card-actions {
  transform: translateY(0);
  opacity: 1;
}

.card-action-button {
  padding: 5px 10px;
}

.empty-state {
  padding: 40px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.rate-edit-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rate-value-display {
  font-size: 16px;
  font-weight: bold;
  color: #ff9900;
  margin-left: 5px;
}

.rate-hint {
  font-size: 12px;
  color: #909399;
}

.cover-preview {
  margin-top: 10px;
  width: 200px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* Dialog 美化 */
:deep(.product-dialog .el-dialog__header) {
  background: linear-gradient(to right, #3a8ee6, #53a8ff);
  color: white;
  padding: 15px 20px;
  margin-right: 0;
}

:deep(.product-dialog .el-dialog__title) {
  color: white;
  font-weight: bold;
}

:deep(.product-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

:deep(.product-dialog .el-dialog__body) {
  padding: 30px 20px;
}

:deep(.product-dialog .el-form-item__label) {
  font-weight: 600;
}

:deep(.product-dialog .el-input .el-input__inner),
:deep(.product-dialog .el-textarea .el-textarea__inner) {
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.product-dialog .el-input .el-input__inner:focus),
:deep(.product-dialog .el-textarea .el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.product-name {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.product-price {
  font-weight: bold;
  font-size: 16px;
  color: #f56c6c;
}

.stock-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.form-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  background: linear-gradient(to right, #f0f5ff, #ffffff);
  padding: 15px;
  border-radius: 8px;
}

.form-icon {
  font-size: 32px;
  margin-right: 15px;
}

.form-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9fafc;
  border-radius: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-right: 15px;
}

.section-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, #dcdfe6, transparent);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 15px;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .view-mode-switch {
    display: none;
  }
}

.banner-description {
  margin-top: 8px;
  color: #606266;
  font-size: 14px;
}

.banner-info-container {
  margin-top: 8px;
  color: #606266;
  font-size: 14px;
}

.banner-tag-container {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.banner-tag {
  background-color: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.banner-detail-container {
  margin: 0 0 30px 0;
  padding: 30px;
  background-color: #f9fafc;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  border-left: 4px solid #3a8ee6;
}

.banner-detail-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.banner-detail-container::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, rgba(58, 142, 230, 0.1), transparent);
  border-radius: 0 0 0 100%;
  z-index: 0;
}

.banner-detail-content {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.banner-detail-icon {
  font-size: 40px;
  color: #3a8ee6;
  background-color: rgba(58, 142, 230, 0.1);
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.banner-detail-container:hover .banner-detail-icon {
  transform: rotate(10deg);
}

.banner-icon {
  font-size: 32px;
}

.banner-detail-text {
  flex: 1;
}

.banner-detail-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-bottom: 12px;
}

.banner-detail-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 3px;
  background-color: #3a8ee6;
  border-radius: 3px;
}

.banner-detail-description {
  margin: 15px 0;
  color: #606266;
  font-size: 15px;
  line-height: 1.6;
}

.banner-detail-tags {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.detail-tag {
  border: none;
  transition: all 0.3s ease;
}

.detail-tag:hover {
  transform: translateY(-2px);
}

.banner-detail-divider {
  position: relative;
  margin-top: 25px;
  height: 1px;
  background: linear-gradient(to right, #dcdfe6, transparent);
  text-align: center;
}

.divider-text {
  position: relative;
  top: -10px;
  background-color: #f9fafc;
  padding: 0 15px;
  font-size: 14px;
  color: #909399;
  font-weight: 600;
}

/* 轮播图专题商品列表样式 */
.banner-products {
  margin-top: 10px;
  position: relative;
}

.banner-products::before {
  content: '';
  position: absolute;
  top: -15px;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(to right, #3a8ee6, transparent);
  border-radius: 5px;
}

.banner-product-card {
  transition: all 0.3s ease;
  border: none;
  overflow: hidden;
}

.banner-product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.banner-product-card .product-card-image {
  height: 200px;
  overflow: hidden;
  border-radius: 8px;
}

.banner-product-card .product-image {
  transition: transform 0.5s ease;
}

.banner-product-card:hover .product-image {
  transform: scale(1.08);
}

.banner-product-card .product-card-title {
  font-weight: 600;
  color: #303133;
  transition: color 0.3s ease;
}

.banner-product-card:hover .product-card-title {
  color: #3a8ee6;
}

.banner-product-card .product-card-actions {
  opacity: 0;
  transform: translateY(100%);
  transition: all 0.3s ease;
}

.banner-product-card:hover .product-card-actions {
  opacity: 1;
  transform: translateY(0);
}
</style>
