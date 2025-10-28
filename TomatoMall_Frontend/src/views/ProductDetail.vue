<template>
  <div class="product-detail">
    <el-card v-loading="loading" class="main-card">
      <template #header>
        <div class="card-header">
          <span class="page-title">
            <span class="title-icon">📦</span>
            商品详情
          </span>
          <div v-if="isAdmin">
            <el-button type="primary" @click="handleEdit">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="success" @click="handleStock">
              <el-icon><Goods /></el-icon> 调整库存
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="product" class="product-content">
        <div class="product-cover-container">
          <el-image
            v-if="product.cover"
            :src="product.cover"
            fit="contain"
            class="product-image"
          />
          <el-empty
            v-else
            description="暂无图片"
            class="product-image-placeholder"
          />
          <div class="image-decorator"></div>
        </div>

        <div class="product-info">
          <div class="product-title">
            <h2>{{ product.title }}</h2>
            <div class="product-rating">
              <el-rate
                :model-value="Number(product.rate) / 2"
                disabled
                text-color="#ff9900"
                :allow-half="true"
              />
              <span class="product-score"
                >{{ Number(product.rate).toFixed(1) }}分</span
              >
            </div>
          </div>

          <el-divider content-position="left">
            <el-icon class="divider-icon"><PriceTag /></el-icon> 价格信息
          </el-divider>

          <div class="product-meta">
            <div class="product-price-container">
              <div class="product-price">
                <span class="price-label">价格：</span>
                <span class="price-value">¥{{ product.price }}</span>
              </div>
              <el-tag
                v-if="Number(product.price) < 15"
                type="danger"
                effect="dark"
                class="price-tag"
                >特惠</el-tag
              >
            </div>

            <div class="product-stock">
              <span class="stock-label">库存：</span>
              <span
                class="stock-value"
                :class="{ 'low-stock': product.stockpile?.amount < 20 }"
              >
                {{ product.stockpile?.amount || 1 }}
                <el-tag
                  v-if="product.stockpile?.amount < 20"
                  type="danger"
                  size="small"
                  effect="dark"
                  >库存紧张</el-tag
                >
                <el-tag
                  v-else-if="product.stockpile?.amount > 50"
                  type="success"
                  size="small"
                  effect="dark"
                  >库存充足</el-tag
                >
                <el-tag v-else type="warning" size="small" effect="dark"
                  >库存适中</el-tag
                >
              </span>
            </div>
          </div>

          <!-- 添加购买数量和加入购物车按钮 -->
          <div class="product-actions">
            <div class="quantity-selector">
              <span class="quantity-label">数量：</span>
              <el-input-number
                v-model="purchaseQuantity"
                :min="1"
                :max="product.stockpile?.amount || 1"
                size="large"
              />
            </div>            <div class="action-buttons">              <el-button
                type="primary"
                size="large"
                :disabled="product.stockpile?.amount <= 0"
                @click="addToCart"
              >
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>              <el-button 
                v-if="!isAdmin"
                :type="isInWishList ? 'danger' : 'default'"
                size="large"
                @click="handleWishList"
                :loading="wishlistLoading"
              >
                <el-icon><Star /></el-icon>
                {{ isInWishList ? '已加入愿望单' : '加入愿望单' }}
              </el-button>
              <el-button size="large" @click="goToCart"> 查看购物车 </el-button>
            </div>
          </div>

          <el-divider content-position="left">
            <el-icon class="divider-icon"><InfoFilled /></el-icon> 商品介绍
          </el-divider>

          <div class="product-description">
            <h3 class="section-title">
              <el-icon><InfoFilled /></el-icon> 商品描述
            </h3>
            <div class="description-content">
              {{ product.description || "暂无描述" }}
            </div>
          </div>

          <el-divider content-position="left">
            <el-icon class="divider-icon"><Document /></el-icon> 详细信息
          </el-divider>

          <div class="product-detail-info">
            <h3 class="section-title">
              <el-icon><Document /></el-icon> 详细说明
            </h3>
            <div class="detail-content">
              {{ product.detail || "暂无详细说明" }}
            </div>
          </div>

          <el-divider content-position="left">
            <el-icon class="divider-icon"><List /></el-icon> 规格参数
          </el-divider>

          <div
            class="product-specifications"
            v-if="product.specifications && product.specifications.length > 0"
          >
            <h3 class="section-title">
              <el-icon><List /></el-icon> 规格信息
            </h3>
            <el-descriptions :column="2" border class="spec-table">
              <el-descriptions-item
                v-for="spec in product.specifications"
                :key="spec.id"
                :label="spec.item"
                :span="1"
                class="spec-item"
              >
                <el-tag
                  :type="getSpecTagType(spec.item)"
                  effect="plain"
                  class="spec-tag"
                >
                  {{ spec.value }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else class="no-specs">
            <el-empty description="暂无规格信息" :image-size="80" />
          </div>

          <el-divider content-position="left">
            <el-icon class="divider-icon"><ChatDotRound /></el-icon> 商品评价
          </el-divider>

          <div class="product-comments">
            <div class="comments-header">
              <div class="comments-summary">
                <div class="average-rating">
                  <span class="rating-label">商品评分：</span>
                  <el-rate
                    :model-value="averageRating"
                    disabled
                    text-color="#ff9900"
                    :allow-half="true"
                  />
                  <span class="rating-value">{{ averageRating.toFixed(1) }}分</span>
                  <span class="comment-count">({{ comments.length }}条评价)</span>
                </div>
              </div>
              <el-button 
                type="primary" 
                @click="showCommentDialog = true"
                :disabled="!canComment"
              >
                <el-icon><Edit /></el-icon> 写评价
              </el-button>
            </div>

            <div class="comments-list" v-if="comments.length > 0">
              <el-card v-for="comment in comments" :key="comment.id" class="comment-card">
                <div class="comment-header">
                  <div class="comment-user">
                    <el-avatar :size="32">{{ comment.username?.charAt(0) }}</el-avatar>
                    <span class="username">{{ comment.username }}</span>
                  </div>
                  <div class="comment-rating">
                    <el-rate
                      :model-value="comment.rating"
                      disabled
                      text-color="#ff9900"
                      :allow-half="true"
                    />
                    <span class="rating-value">{{ comment.rating.toFixed(1) }}分</span>
                  </div>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-footer">
                  <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  <el-button 
                    v-if="comment.userId === currentUserId"
                    type="danger" 
                    size="small" 
                    @click="handleDeleteComment(comment.id)"
                  >
                    删除
                  </el-button>
                </div>
              </el-card>
            </div>
            <el-empty v-else description="暂无评价" />
          </div>

          <!-- 评论对话框 -->
          <el-dialog
            v-model="showCommentDialog"
            title="写评价"
            width="500px"
            class="comment-dialog"
          >
            <el-form
              ref="commentFormRef"
              :model="commentForm"
              :rules="commentRules"
              label-width="80px"
            >
              <el-form-item label="评分" prop="rating">
                <el-rate
                  v-model="commentForm.rating"
                  :max="5"
                  :allow-half="true"
                  :colors="['#ffd21e', '#ffd21e', '#ffd21e']"
                />
                <div class="rate-hint">（每半颗星代表1分，满分5分）</div>
              </el-form-item>
              <el-form-item label="评价内容" prop="content">
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入您的评价内容（最多500字）"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="showCommentDialog = false">取消</el-button>
                <el-button type="primary" @click="handleSubmitComment" :loading="submittingComment">
                  提交评价
                </el-button>
              </span>
            </template>
          </el-dialog>
        </div>
      </div>

      <div v-else-if="!loading" class="no-product">
        <el-empty description="商品信息不存在" />
      </div>
    </el-card>

    <!-- 编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑商品"
      width="70%"
      class="product-dialog"
    >
      <div class="form-header">
        <div class="form-icon">✏️</div>
        <div class="form-title">编辑商品信息</div>
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

        <div class="form-section image-section">
          <div class="section-header">
            <div class="section-title">图片信息</div>
            <div class="section-line"></div>
          </div>

          <el-form-item label="封面图片" prop="cover">
            <ImageUploader
              v-model="productForm.cover"
              @upload-success="handleImageUploadSuccess"
              @upload-error="handleImageUploadError"
            />
          </el-form-item>
        </div>

        <div class="form-section detail-section">
          <div class="section-header">
            <div class="section-title">详细描述</div>
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
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            保存修改
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
          <span class="product-name">{{ product?.title }}</span>
        </el-form-item>
        <el-form-item label="当前价格">
          <span class="product-price">¥{{ product?.price }}</span>
        </el-form-item>
        <el-form-item label="库存数量" prop="amount">
          <el-input-number
            v-model="stockForm.amount"
            :min="0"
            :precision="0"
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
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useWishListStore } from '../store/wishlist';
import {
  getProductById,
  updateProduct,
  updateProductBasicInfo,
  updateProductBasicOnly,
  updateStockpile,
  getStockpile,
  createProduct,
  deleteProduct,
} from "../api/product";
import { addToCart as addProductToCart, getCartItems } from "../api/cart";
import { getUserInfo } from "../api/user";
import { addToWishList, removeFromWishList, checkInWishList } from "../api/wishlist";
import {
  Edit,
  Goods,
  InfoFilled,
  Document,
  List,
  PriceTag,
  Picture,
  ShoppingCart,
} from "@element-plus/icons-vue";
import ImageUploader from "../components/ImageUploader.vue";
import { getComments, addComment, deleteComment, checkPurchaseStatus } from "../api/comment";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const product = ref(null);
const dialogVisible = ref(false);
const stockDialogVisible = ref(false);
const productFormRef = ref(null);
const stockFormRef = ref(null);
const purchaseQuantity = ref(1);
const isInWishList = ref(false);
const wishlistLoading = ref(false);
const wishlistStore = useWishListStore();

const productForm = ref({
  id: "",
  title: "",
  price: 0,
  rate: 0,
  description: "",
  cover: "",
  detail: "",
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

const isAdmin = ref(false);

const submitting = ref(false);
const stockSubmitting = ref(false);

// 评论相关
const comments = ref([]);
const showCommentDialog = ref(false);
const commentFormRef = ref(null);
const submittingComment = ref(false);
const canComment = ref(false);
const currentUserId = ref(null);

const commentForm = ref({
  rating: 0,
  content: ''
});

const commentRules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' },
    { type: 'number', min: 0.5, max: 5, message: '评分必须在0.5-5分之间', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 1, max: 500, message: '评价内容长度必须在1-500字之间', trigger: 'blur' }
  ]
};

// 计算平均评分
const averageRating = computed(() => {
  if (comments.value.length === 0) return 0;
  const sum = comments.value.reduce((acc, comment) => acc + comment.rating, 0);
  return sum / comments.value.length;
});

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取用户信息并设置管理员状态
const fetchUserInfo = async () => {
  // 首先从localStorage中获取角色信息
  const userRole = localStorage.getItem("userRole");
  if (userRole) {
    isAdmin.value = userRole === "admin";
  }

  // 然后尝试从API获取最新的用户信息
  try {
    const username = localStorage.getItem("username");
    if (username) {
      const res = await getUserInfo(username);
      if (res && res.data) {
        isAdmin.value = res.data.role === "admin";
        // 更新localStorage中的角色信息
        localStorage.setItem("userRole", res.data.role);
      }
    }
  } catch (error) {
    console.error("获取用户信息失败", error);
  }
};

// 添加获取规格标签类型的函数
const getSpecTagType = (item) => {
  const types = {
    作者: "success",
    副标题: "info",
    ISBN: "danger",
    页数: "warning",
    装帧: "primary",
    出版社: "success",
    出版日期: "info",
  };

  return types[item] || "info";
};

const fetchProduct = async () => {
  loading.value = true;
  try {
    console.log("开始获取商品详情，ID:", route.params.id);
    
    // 确保不重复检查愿望单状态
    const isFirstLoad = !product.value;

    // 清除可能存在的缓存
    const cacheKey = `product_${route.params.id}`;
    localStorage.removeItem(cacheKey);

    const res = await getProductById(route.params.id);
    console.log("商品详情响应:", res);

    if (res.code === 200 || res.code === "200") {
      // 确保评分是数字
      product.value = {
        ...res.data,
        rate:
          res.data.rate !== null && res.data.rate !== undefined
            ? Number(res.data.rate)
            : 0,
      };

      console.log("设置商品数据:", JSON.stringify(product.value));

      // 如果没有库存信息，查询库存API
      if (!product.value.stockpile) {
        try {
          const stockRes = await getStockpile(product.value.id);
          if (stockRes.code === 200 || stockRes.code === "200") {
            product.value.stockpile = stockRes.data;
          }
        } catch (stockError) {
          console.error("获取库存信息失败", stockError);
        }
      }
    } else if (res.data && res.data.code === "200") {
      // 确保评分是数字
      product.value = {
        ...res.data.data,
        rate:
          res.data.data.rate !== null && res.data.data.rate !== undefined
            ? Number(res.data.data.rate)
            : 0,
      };

      console.log("设置商品数据:", JSON.stringify(product.value));

      // 如果没有库存信息，查询库存API
      if (!product.value.stockpile) {
        try {
          const stockRes = await getStockpile(product.value.id);
          if (stockRes.code === 200 || stockRes.code === "200") {
            product.value.stockpile = stockRes.data;
          } else if (stockRes.data && stockRes.data.code === "200") {
            product.value.stockpile = stockRes.data.data;
          }
        } catch (stockError) {
          console.error("获取库存信息失败", stockError);
        }
      }    } else {
      ElMessage.error(res.msg || "获取商品详情失败");
    }

    // 在商品加载完成后检查愿望单状态
    if (isFirstLoad) {
      await checkWishListStatus();
    }
  } catch (error) {
    console.error("获取商品详情失败:", error);
    ElMessage.error("获取商品详情失败");
  } finally {
    loading.value = false;
  }
};

const handleEdit = () => {
  // 确保规格信息被正确复制
  const specifications = product.value.specifications
    ? [...product.value.specifications]
    : [];

  productForm.value = {
    ...product.value,
    rate:
      product.value.rate !== null && product.value.rate !== undefined
        ? Number(product.value.rate) / 2
        : 0,
    specifications: specifications, // 明确设置规格信息
  };

  console.log("编辑表单数据:", JSON.stringify(productForm.value));
  dialogVisible.value = true;
};

const handleStock = () => {
  if (!isAdmin.value) {
    ElMessage.warning("只有管理员可以调整库存");
    return;
  }
  stockForm.value.amount = product.value.stockpile?.amount || 1;
  stockDialogVisible.value = true;
};

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

        // 获取当前商品ID
        const currentId = route.params.id;
        console.log("当前商品ID:", currentId);
        console.log("当前商品数据:", JSON.stringify(product.value));

        // 准备更新商品数据
        const updateData = {
          id: currentId,
          title: productForm.value.title,
          price: productForm.value.price,
          rate: Number(productForm.value.rate) * 2, // 半颗星代表1分，转换为10分制
          description: productForm.value.description,
          cover: productForm.value.cover,
          detail: productForm.value.detail,
          // 确保传递完整的规格信息
          specifications: product.value.specifications || [],
        };

        console.log("准备更新商品:", JSON.stringify(updateData));

        // 直接更新现有商品
        const updateRes = await updateProduct(updateData);
        console.log("更新商品响应:", updateRes);

        if (
          updateRes.code === 200 ||
          updateRes.code === "200" ||
          (updateRes.data && updateRes.data.code === "200")
        ) {
          // 清除缓存
          localStorage.removeItem(`product_${currentId}`);

          // 重新获取商品数据
          console.log("重新获取商品数据:", currentId);
          await fetchProduct();

          // 显示成功消息
          ElMessage.success("更新成功");

          // 关闭编辑对话框
          dialogVisible.value = false;
        } else {
          throw new Error(updateRes.msg || "更新失败");
        }
      } catch (error) {
        console.error("提交商品表单失败:", error);
        ElMessage.error(error.message || "更新失败");
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
          productId: product.value.id,
          amount: stockForm.value.amount,
        });

        const res = await updateStockpile(
          product.value.id,
          stockForm.value.amount
        );
        console.log("更新库存响应:", res);

        if (res.code === 200 || res.code === "200") {
          ElMessage.success("调整库存成功");
          stockDialogVisible.value = false;
          fetchProduct();
        } else if (res.data && res.data.code === "200") {
          ElMessage.success("调整库存成功");
          stockDialogVisible.value = false;
          fetchProduct();
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

const handleImageUploadSuccess = (url) => {
  console.log("图片上传成功：", url);
  productForm.value.cover = url;
};

const handleImageUploadError = (error) => {
  console.error("图片上传失败：", error);
  ElMessage.error("图片上传失败：" + (error.message || "未知错误"));
};

// 添加购物车
const addToCart = async () => {
  if (!product.value) return;

  try {
    const response = await addProductToCart({
      productId: product.value.id,
      quantity: purchaseQuantity.value,
    });

    if (response.code === "200") {
      ElMessage.success({
        message: "成功加入购物车",
        duration: 300
      });
      
      // 更新购物车数量
      try {
        const cartResponse = await getCartItems();
        if (cartResponse.code === '200' && cartResponse.data && cartResponse.data.items) {
          // 触发全局事件，通知 AppHeader 更新购物车数量
          window.dispatchEvent(new CustomEvent('cart-updated', {
            detail: { count: cartResponse.data.items.length }
          }));
        }
      } catch (error) {
        console.error('更新购物车数量失败:', error);
      }
    } else {
      ElMessage.error(response.msg || "加入购物车失败");
    }
  } catch (error) {
    console.error("加入购物车出错:", error);
    ElMessage.error("加入购物车失败，请稍后重试");
  }
};

// 前往购物车页面
const goToCart = () => {
  router.push("/cart");
};

// 获取评论列表
const fetchComments = async () => {
  try {
    const res = await getComments(route.params.id);
    if (res.code === 200 || res.code === "200") {
      comments.value = res.data;
    }
  } catch (error) {
    console.error('获取评论列表失败:', error);
    ElMessage.error('获取评论列表失败');
  }
};

// 检查用户是否可以评论
const checkCanComment = async () => {
  try {
    const username = localStorage.getItem('username');
    if (!username) {
      canComment.value = false;
      return;
    }

    // 获取用户ID
    const userRes = await getUserInfo(username);
    if (userRes.code === 200 || userRes.code === "200") {
      currentUserId.value = userRes.data.id;
      // 检查购买状态
      const purchaseRes = await checkPurchaseStatus(userRes.data.id, route.params.id);
      if (purchaseRes.code === 200 || purchaseRes.code === "200") {
        canComment.value = purchaseRes.data;
      }
    }
  } catch (error) {
    console.error('检查评论权限失败:', error);
    canComment.value = false;
  }
};

// 提交评论
const handleSubmitComment = async () => {
  if (!commentFormRef.value) return;

  await commentFormRef.value.validate(async (valid) => {
    if (valid) {
      submittingComment.value = true;
      try {
        const res = await addComment(
          currentUserId.value,
          route.params.id,
          commentForm.value.content,
          commentForm.value.rating
        );

        if (res.code === 200 || res.code === "200") {
          ElMessage.success('评价成功');
          showCommentDialog.value = false;
          commentForm.value = { rating: 0, content: '' };
          await fetchComments();
        } else {
          throw new Error(res.msg || '评价失败');
        }
      } catch (error) {
        console.error('提交评价失败:', error);
        ElMessage.error(error.message || '提交评价失败');
      } finally {
        submittingComment.value = false;
      }
    }
  });
};

// 删除评论
const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const res = await deleteComment(commentId, currentUserId.value);
    if (res.code === 200 || res.code === "200") {
      ElMessage.success('删除成功');
      await fetchComments();
    } else {
      throw new Error(res.msg || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error);
      ElMessage.error(error.message || '删除评价失败');
    }
  }
};

// 检查商品是否在愿望单中
const checkWishListStatus = async () => {
  if (!product.value || !product.value.id) return;
  
  wishlistLoading.value = true;
  try {
    const username = localStorage.getItem('username');
    if (!username) {
      isInWishList.value = false;
      return;
    }

    const res = await checkInWishList(username, product.value.id);
    isInWishList.value = res.code === 200 || res.code === "200";
  } catch (error) {
    console.error('检查愿望单状态失败:', error);
    isInWishList.value = false;
  } finally {
    wishlistLoading.value = false;
  }
};

// 处理愿望单操作
const handleWishList = async () => {
  if (!product.value || !product.value.id) return;
  
  wishlistLoading.value = true;
  try {
    const username = localStorage.getItem('username');
    if (!username) {
      ElMessage.warning('请先登录');
      router.push('/login');
      return;
    }

    if (isInWishList.value) {
      // 从愿望单中移除
      const res = await removeFromWishList(username, product.value.id);
      if (res.code === 200 || res.code === "200") {
        isInWishList.value = false;
        ElMessage.success('已从愿望单中移除');
        // 更新愿望单状态
        wishlistStore.removeFromWishList(product.value.id);
      }
    } else {
      // 添加到愿望单
      const res = await addToWishList(username, product.value.id);
      if (res.code === 200 || res.code === "200") {
        isInWishList.value = true;
        ElMessage.success('已添加到愿望单');
        // 更新愿望单状态
        wishlistStore.addToWishList(product.value.id);
      }
    }
  } catch (error) {
    console.error('操作愿望单失败:', error);
    ElMessage.error('操作失败，请稍后重试');
  } finally {
    wishlistLoading.value = false;
  }
};

onMounted(() => {
  fetchProduct();
  fetchUserInfo();
});
</script>

<style scoped>
.product-detail {
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

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 24px;
}

.product-content {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  padding: 10px;
}

.product-cover-container {
  width: 350px;
  height: 350px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
}

.image-decorator {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.05), transparent);
  pointer-events: none;
  z-index: 1;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  z-index: 2;
  transition: transform 0.3s ease;
}

.product-cover-container:hover .product-image {
  transform: scale(1.05);
}

.product-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-info {
  flex: 1;
  min-width: 300px;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.product-title {
  margin-bottom: 15px;
  background: linear-gradient(to right, #f8f9fa, white);
  padding: 15px;
  border-radius: 8px;
}

.product-title h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
  position: relative;
  display: inline-block;
}

.product-title h2::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(to right, #ff9900, #ffb344);
  border-radius: 3px;
}

.product-rating {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.product-score {
  margin-left: 10px;
  color: #ff9900;
  font-weight: bold;
}

.divider-icon {
  margin-right: 5px;
  color: #409eff;
}

.product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  padding: 10px 0;
  background-color: #f9fafc;
  border-radius: 8px;
  padding: 15px;
}

.product-price-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.price-tag {
  position: relative;
  top: -2px;
}

.product-price,
.product-stock {
  font-size: 16px;
  display: flex;
  align-items: center;
}

.price-label,
.stock-label {
  color: #606266;
  margin-right: 10px;
  font-weight: 600;
}

.price-value {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.stock-value {
  font-weight: bold;
  color: #67c23a;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.low-stock {
  color: #f56c6c;
}

.product-description,
.product-detail-info,
.product-specifications {
  margin-bottom: 20px;
  background-color: #f9fafc;
  border-radius: 8px;
  padding: 15px;
  transition: all 0.3s ease;
}

.product-description:hover,
.product-detail-info:hover,
.product-specifications:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.section-title {
  margin-bottom: 15px;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 2px solid #ebeef5;
}

.description-content,
.detail-content {
  color: #606266;
  line-height: 1.8;
  padding: 10px 15px;
  background-color: #fff;
  border-radius: 6px;
  min-height: 80px;
  border-left: 4px solid #409eff;
}

.spec-table {
  margin-top: 15px;
  border-radius: 8px;
  overflow: hidden;
}

.spec-item {
  transition: background-color 0.3s ease;
}

.spec-item:hover {
  background-color: #f5f7fa;
}

.spec-tag {
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.rate-edit-container {
  background-color: #fffbe6;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rate-value-display {
  font-size: 18px;
  font-weight: bold;
  color: #ff9900;
  margin-left: 5px;
}

.rate-hint {
  font-size: 13px;
  color: #909399;
  font-style: italic;
}

.product-dialog {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.product-dialog .el-dialog__header) {
  padding: 15px 20px;
  margin-right: 0;
}

:deep(.product-dialog .el-dialog__title) {
  font-weight: bold;
  color: #333;
}

:deep(.product-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #909399;
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

.el-form-item {
  margin-bottom: 25px;
}

.el-input,
.el-input-number,
.el-textarea {
  width: 100%;
}

.el-input .el-input__inner,
.el-textarea .el-textarea__inner {
  border-radius: 8px;
  transition: all 0.3s;
  border: 1px solid #dcdfe6;
}

.el-input .el-input__inner:hover,
.el-textarea .el-textarea__inner:hover {
  border-color: #c0c4cc;
}

.el-input .el-input__inner:focus,
.el-textarea .el-textarea__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
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
  font-weight: bold;
  color: #333;
}

.product-form {
  padding: 10px;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.basic-info {
  background-color: #f0f8ff;
  border-left: 4px solid #1890ff;
}

.image-section {
  background-color: #f6ffed;
  border-left: 4px solid #52c41a;
}

.detail-section {
  background-color: #fff7e6;
  border-left: 4px solid #fa8c16;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-right: 15px;
}

.section-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(
    to right,
    rgba(0, 0, 0, 0.15),
    rgba(0, 0, 0, 0.02)
  );
}

.cover-preview-container {
  display: flex;
  justify-content: center;
  margin-top: 15px;
}

.cover-preview {
  width: 250px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.cover-preview:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.cover-empty {
  width: 250px;
  height: 200px;
  border-radius: 8px;
  border: 2px dashed #dcdfe6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.cover-empty .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
  color: #c0c4cc;
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

.no-product,
.no-specs {
  padding: 40px 0;
  display: flex;
  justify-content: center;
}

@media screen and (max-width: 768px) {
  .product-content {
    flex-direction: column;
  }

  .product-cover-container {
    width: 100%;
    height: 300px;
  }
}

.product-actions {
  margin: 20px 0;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-label {
  font-weight: bold;
  color: #606266;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
  }
}

.product-comments {
  margin-top: 20px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.comments-summary {
  display: flex;
  align-items: center;
  gap: 20px;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-label {
  font-size: 16px;
  color: #606266;
  font-weight: bold;
}

.rating-value {
  color: #ff9900;
  font-weight: bold;
  font-size: 16px;
}

.comment-count {
  color: #909399;
  font-size: 14px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comment-card {
  transition: all 0.3s ease;
}

.comment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: bold;
  color: #303133;
}

.comment-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  margin: 10px 0;
  padding: 10px;
  background-color: #f9fafc;
  border-radius: 4px;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.comment-time {
  color: #909399;
  font-size: 13px;
}

.comment-dialog {
  border-radius: 8px;
}

.rate-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
