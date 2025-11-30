<template>
  <div class="product-detail">
    <el-card v-loading="loading" class="main-card">
      <template #header>
        <div class="card-header">
          <span class="page-title">
            <span class="title-icon">📚</span>
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
              <template v-if="comments.length > 0">
              <el-rate
                  :model-value="averageRating"
                disabled
                text-color="#ff9900"
                :allow-half="true"
              />
                <span class="product-score">{{ averageRating.toFixed(1) }}分</span>
                <span class="comment-count">({{ comments.length }}条评价)</span>
              </template>
              <template v-else>
                <span class="no-rating">暂无评分</span>
              </template>
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
                {{ product.stockpile?.amount ?? 0 }}
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
                :min="product.stockpile?.amount > 0 ? 1 : 0"
                :max="Math.max(product.stockpile?.amount ?? 0, 0)"
                size="large"
                :disabled="product.stockpile?.amount <= 0"
              />
            </div>
            <div class="action-buttons">
              <el-button
                type="primary"
                size="large"
                :disabled="product.stockpile?.amount <= 0 || purchaseQuantity <= 0"
                @click="addToCart"
                class="cart-button"
              >
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
              <el-button 
                v-if="!isAdmin"
                :type="isInWishList ? 'danger' : 'default'"
                size="large"
                @click="handleWishList"
                :loading="wishlistLoading"
                class="wishlist-button"
              >
                <el-icon><Star /></el-icon>
                {{ isInWishList ? '已加入愿望单' : '加入愿望单' }}
              </el-button>
              <el-button size="large" @click="goToCart" class="view-cart-button"> 查看购物车 </el-button>
            </div>
          </div>

          <el-divider content-position="left">
            <el-icon class="divider-icon"><InfoFilled /></el-icon> 商品描述
          </el-divider>

          <div class="product-description">
            <h3 class="section-title">
              <el-icon><InfoFilled /></el-icon> 商品描述
            </h3>
            <div class="description-content">
              {{ product.description || "暂无描述" }}
            </div>
          </div>

          <!-- 添加标签展示 -->
          <div v-if="product.tags" class="product-tags">
            <h3 class="section-title">
              <el-icon><Collection /></el-icon> 图书类别
            </h3>
            <div class="tags-container">
              <el-tag
                v-for="tag in tagsArray"
                :key="tag"
                type="info"
                effect="light"
                class="book-tag"
                @click="handleTagClick(tag)"
              >
                {{ tag }}
              </el-tag>
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
            <el-icon class="divider-icon"><Reading /></el-icon> 外部书评
          </el-divider>

          <div class="external-reviews">
            <el-button 
              type="success" 
              plain
              @click="openExternalReview"
              class="review-button"
            >
              <el-icon><Link /></el-icon>
              查看豆瓣书评
            </el-button>
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
                @click="handleCommentClick"
                class="comment-button"
              >
                <el-icon><Edit /></el-icon> 写评价
              </el-button>
            </div>

            <div class="comments-list" v-if="comments.length > 0">
              <el-card v-for="comment in comments" :key="comment.id" class="comment-card">
                <div class="comment-header">
                  <div class="comment-user">
                    <el-avatar :size="32" class="user-avatar">{{ (comment.nickname || comment.username || '?').charAt(0) }}</el-avatar>
                    <span class="username">{{ comment.nickname || comment.username || '未知用户' }}</span>
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
                    v-if="comment.userId && currentUserId && comment.userId === currentUserId"
                    type="danger" 
                    size="small" 
                    @click="handleDeleteComment(comment.id)"
                    class="delete-button"
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
            :title="' '"
            width="500px"
            class="comment-dialog"
            :append-to-body="true"
            :close-on-click-modal="false"
            :destroy-on-close="true"
            :show-close="true"
            center
          >
            <div class="comment-dialog-content">
              <div class="comment-dialog-header">
                <div class="comment-icon">💬</div>
                <div class="comment-title">我的书评</div>
                <div class="comment-subtitle">分享您的阅读体验，帮助其他读者做出选择</div>
              </div>
              
              <el-form
                ref="commentFormRef"
                :model="commentForm"
                :rules="commentRules"
                label-width="80px"
                class="comment-form"
              >
                <el-form-item label="评分" prop="rating">
                  <div class="rating-container">
                    <el-rate
                      v-model="commentForm.rating"
                      :max="5"
                      :allow-half="true"
                      :colors="['#ffd21e', '#ffd21e', '#ffd21e']"
                      class="comment-rate"
                      :show-score="false"
                    />
                    <div class="rating-value" v-if="commentForm.rating > 0">
                      <span class="rating-text">{{ getRatingText(commentForm.rating) }}</span>
                    </div>
                  </div>
                  <div class="rate-hint">（请选择您对这本书的评价）</div>
                </el-form-item>
                <el-form-item label="书评" prop="content">
                  <el-input
                    v-model="commentForm.content"
                    type="textarea"
                    :rows="4"
                    placeholder="写下您对这本书的感受、收获或建议..."
                    maxlength="500"
                    show-word-limit
                    resize="none"
                    class="comment-textarea"
                  />
                </el-form-item>
              </el-form>
            </div>
            
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="showCommentDialog = false" plain class="cancel-button">取消</el-button>
                <el-button type="primary" @click="handleSubmitComment" :loading="submittingComment" class="submit-button">
                  <el-icon class="submit-icon"><ChatDotRound /></el-icon>
                  发布书评
                </el-button>
              </div>
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
                disabled
              />
              <div class="rate-value-display">
                {{ Number(productForm.rate).toFixed(1) }} 分
              </div>
              <div class="rate-hint">（评分由用户评论自动计算，不可手动修改）</div>
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
          
          <el-form-item label="书籍类别" prop="tags">
            <el-input
              v-model="productForm.tags"
              placeholder="请输入书籍类别，多个类别用逗号分隔，如：文学,小说,科幻"
            />
            <div class="tags-hint">多个类别用逗号分隔（支持中英文逗号），例如：文学,小说,科幻 或 文学，小说，科幻</div>
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
import { ref, onMounted, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
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
import { clearCache, clearUrlCache } from "../api/request";
import {
  Edit,
  Goods,
  InfoFilled,
  Document,
  List,
  PriceTag,
  Picture,
  ShoppingCart,
  Star,
  Link,
  Reading,
  ChatDotRound,
  Collection,
} from "@element-plus/icons-vue";
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
  tags: "",
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
const currentUsername = ref(null);

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

// 计算标签数组
const tagsArray = computed(() => {
  if (!product.value || !product.value.tags) return [];
  // 先将中文逗号替换为英文逗号，然后按逗号分隔，去除每个标签的前后空格，最后过滤掉空字符串
  return product.value.tags.replace(/，/g, ',').split(',').map(tag => tag.trim()).filter(tag => tag);
});

// 添加标签点击处理函数
const handleTagClick = (tag) => {
  console.log(`点击了标签: ${tag}`);
  ElMessage.info(`您点击了标签: ${tag}`);
  // 这里可以添加按标签筛选相关商品的逻辑
  // 例如：router.push(`/products?tag=${encodeURIComponent(tag)}`);
};

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
          console.log("开始获取库存");
          const stockRes = await getStockpile(product.value.id);
          if (stockRes.code === 200 || stockRes.code === "200") {
            // 确保stockpile是一个对象，包含amount属性
            product.value.stockpile = { amount: stockRes.data.amount };
            console.log("库存信息:", product.value.stockpile.amount);
          }
        } catch (stockError) {
          console.error("获取库存信息失败", stockError);
          // 设置默认库存对象，避免界面错误
          product.value.stockpile = { amount: 0 };
        }
      }
      
      // 根据库存情况调整购买数量
      if (product.value.stockpile?.amount <= 0) {
        purchaseQuantity.value = 0;
      } else if (purchaseQuantity.value <= 0) {
        purchaseQuantity.value = 1;
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
            // 确保stockpile是一个对象，包含amount属性
            product.value.stockpile = { amount: stockRes.data.amount };
            console.log("库存信息:", product.value.stockpile);
          }
        } catch (stockError) {
          console.error("获取库存信息失败", stockError);
          // 设置默认库存对象，避免界面错误
          product.value.stockpile = { amount: 0 };
        }
      }
      
      // 根据库存情况调整购买数量
      if (product.value.stockpile?.amount <= 0) {
        purchaseQuantity.value = 0;
      } else if (purchaseQuantity.value <= 0) {
        purchaseQuantity.value = 1;
      }
    } else {
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
        ? Number(product.value.rate)
        : 0,
    specifications: specifications, // 明确设置规格信息
    tags: product.value.tags || "" // 添加tags字段
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
          description: productForm.value.description,
          cover: productForm.value.cover,
          detail: productForm.value.detail,
          // 确保传递完整的规格信息
          specifications: product.value.specifications || [],
          tags: productForm.value.tags || "" // 添加tags字段
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
          
          // 清除相关缓存：商品详情和库存信息
          localStorage.removeItem(`product_${product.value.id}`);
          clearUrlCache(`/api/products/${product.value.id}`);
          clearUrlCache(`/api/products/stockpile/${product.value.id}`);
          clearUrlCache('/api/products'); // 清除商品列表缓存
          
          // 重新获取商品数据
          await fetchProduct();
        } else if (res.data && res.data.code === "200") {
          ElMessage.success("调整库存成功");
          stockDialogVisible.value = false;
          
          // 清除相关缓存：商品详情和库存信息
          localStorage.removeItem(`product_${product.value.id}`);
          clearUrlCache(`/api/products/${product.value.id}`);
          clearUrlCache(`/api/products/stockpile/${product.value.id}`);
          clearUrlCache('/api/products'); // 清除商品列表缓存
          
          // 重新获取商品数据
          await fetchProduct();
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

// 预览封面图片
const previewCover = () => {
  if (!productForm.value.cover) {
    ElMessage.warning('请先输入封面图片URL');
    return;
  }
  // 封面预览已通过 v-if="productForm.cover" 自动显示
  ElMessage.success('封面预览已显示在下方');
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
    console.log('开始获取评论列表，商品ID:', route.params.id);
    const res = await getComments(route.params.id);
    console.log('获取评论列表响应:', res);
    
    if (res.code === 200 || res.code === "200") {
      // 确保评论数据包含用户名
      comments.value = await Promise.all(res.data.map(async (comment) => {
        try {
          // 获取评论者的用户信息
          const userRes = await getUserInfo(comment.userId); // 使用 userId 而不是 username
          console.log('获取用户信息响应:', userRes);
          if (userRes.code === 200 || userRes.code === "200") {
            return {
              ...comment,
              username: userRes.data.username, // 确保设置用户名
              nickname: userRes.data.nickname || userRes.data.username // 使用昵称，如果没有则使用用户名
            };
          }
          return comment;
        } catch (error) {
          console.error('获取用户信息失败:', error);
          return {
            ...comment,
            username: comment.username || '未知用户' // 确保至少有一个用户名显示
          };
        }
      }));
      console.log('处理后的评论列表:', comments.value);
    } else {
      console.error('获取评论列表失败:', res.msg);
      ElMessage.error(res.msg || '获取评论列表失败');
    }
  } catch (error) {
    console.error('获取评论列表出错:', error);
    ElMessage.error('获取评论列表失败，请刷新页面重试');
  }
};

// 检查用户是否可以评论
const checkCanComment = async () => {
  try {
    const username = localStorage.getItem('username');
    if (!username) {
      canComment.value = false;
      currentUserId.value = null;
      return;
    }

    // 获取用户信息
    const userRes = await getUserInfo(username);
    console.log('获取当前用户信息:', userRes);
    if (userRes.code === 200 || userRes.code === "200") {
      currentUserId.value = userRes.data.id;
      currentUsername.value = userRes.data.username;
      console.log('设置当前用户ID:', currentUserId.value);
      
      // 检查用户是否购买过该商品
      try {
        const purchaseRes = await checkPurchaseStatus(currentUserId.value, route.params.id);
        console.log('检查购买状态结果:', purchaseRes);
        if (purchaseRes.code === 200 || purchaseRes.code === "200") {
          canComment.value = purchaseRes.data === true;
          if (!canComment.value) {
            console.log('用户未购买该商品，不能评论');
          }
        } else {
          canComment.value = false;
        }
      } catch (purchaseError) {
        console.error('检查购买状态失败:', purchaseError);
        canComment.value = false;
      }
    }
  } catch (error) {
    console.error('检查评论权限失败:', error);
    canComment.value = false;
    currentUserId.value = null;
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
          // 重新获取评论列表
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
      // 重新获取评论列表和商品信息以更新评分
      await Promise.all([
        fetchComments(),
        fetchProduct()
      ]);
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
    const token = localStorage.getItem('token');
    if (!token) {
      isInWishList.value = false;
      return;
    }

    // 先尝试从store中获取状态
    if (wishlistStore.wishlistItems.length > 0) {
      isInWishList.value = wishlistStore.isInWishList(product.value.id);
      return;
    }
    
    // 如果store中没有数据，则请求API
    const res = await checkInWishList(product.value.id);
    if (res.code === '200') {
      isInWishList.value = res.data === true;
    } else {
      isInWishList.value = false;
    }
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
    const token = localStorage.getItem('token');
    if (!token) {
      ElMessage.warning('请先登录');
      router.push('/login');
      return;
    }

    if (isInWishList.value) {
      // 从愿望单中移除
      const res = await removeFromWishList(product.value.id);
      if (res.code === '200') {
        isInWishList.value = false;
        ElMessage.success('已从愿望单中移除');
        // 更新愿望单状态 - 只保留这一个更新机制，移除其他冗余的通知方式
        wishlistStore.removeFromWishList(product.value.id);
        
        // 强制刷新store中的数据，确保状态一致
        setTimeout(() => {
          wishlistStore.fetchWishListCount(true);
        }, 300);
      }
    } else {
      // 添加到愿望单
      const res = await addToWishList(product.value.id);
      if (res.code === '200') {
        isInWishList.value = true;
        ElMessage.success('已添加到愿望单');
        // 更新愿望单状态 - 只保留这一个更新机制，移除其他冗余的通知方式
        wishlistStore.addToWishList(product.value.id);
        
        // 强制刷新store中的数据，确保状态一致
        setTimeout(() => {
          wishlistStore.fetchWishListCount(true);
        }, 300);
      }
    }
  } catch (error) {
    console.error('操作愿望单失败:', error);
    ElMessage.error('操作失败，请稍后重试');
  } finally {
    wishlistLoading.value = false;
  }
};

// 在 script setup 部分添加处理函数
const handleCommentClick = async () => {
  const username = localStorage.getItem('username');
  if (!username) {
    ElMessage.warning('请先登录后再评价');
    router.push('/login');
    return;
  }

  try {
    const userRes = await getUserInfo(username);
    if (userRes.code === 200 || userRes.code === "200") {
      currentUserId.value = userRes.data.id;
      
      // 检查用户是否购买过该商品
      const purchaseRes = await checkPurchaseStatus(currentUserId.value, route.params.id);
      if (purchaseRes.code === 200 || purchaseRes.code === "200" && purchaseRes.data === true) {
        // 已购买，可以评论
        showCommentDialog.value = true;
      } else {
        // 未购买，提示用户
        ElMessage.warning('您需要购买此商品后才能评价');
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败');
  }
};

// 添加路由监听，确保每次进入页面都刷新评论
watch(
  () => route.params.id,
  async (newId) => {
    if (newId) {
      console.log('商品ID变化，重新获取评论列表');
      await fetchComments();
    }
  }
);

// 打开外部书评链接
const openExternalReview = () => {
  if (!product.value) return;
  
  try {
    // 构建豆瓣搜索链接
    const searchQuery = encodeURIComponent(product.value.title);
    const doubanUrl = `https://search.douban.com/book/subject_search?search_text=${searchQuery}`;
    
    // 在新窗口打开链接
    window.open(doubanUrl, '_blank');
  } catch (error) {
    console.error('打开外部书评链接失败:', error);
    ElMessage.error('打开外部书评链接失败，请稍后重试');
  }
};

// 根据评分获取评价文本
const getRatingText = (rating) => {
  if (rating >= 4.5) return "超赞读物";
  if (rating >= 4) return "很棒";
  if (rating >= 3) return "不错";
  if (rating >= 2) return "一般";
  if (rating >= 1) return "失望";
  return "不推荐";
};

const calculateSubtotal = (item) => {
  return item.price * item.quantity;
};

onMounted(async () => {
  await fetchUserInfo(); // 获取用户角色信息
  await checkCanComment(); // 获取当前用户ID和评论权限
  await fetchProduct();
  await fetchComments();
});
</script>

<style scoped>
.product-detail {
  padding: 30px 0;
}

.main-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 12px !important;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1) !important;
  overflow: hidden;
  border: none !important;
  background-color: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color);
  display: flex;
  align-items: center;
}

.title-icon {
  font-size: 28px;
  margin-right: 12px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-5px);
  }
  100% {
    transform: translateY(0px);
  }
}

.product-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

@media (min-width: 992px) {
  .product-content {
    flex-direction: row;
  }
}

.product-cover-container {
  position: relative;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

@media (min-width: 992px) {
  .product-cover-container {
    width: 40%;
    margin: 0;
  }
}

.product-image {
  width: 100%;
  height: 500px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background-color: white;
  padding: 20px;
  z-index: 1;
  position: relative;
}

.product-image:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.image-decorator {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 10px;
  left: 10px;
  border: 2px solid var(--primary-color);
  border-radius: 8px;
  z-index: 0;
  opacity: 0.5;
}

.product-image-placeholder {
  width: 100%;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.product-info {
  flex: 1;
  padding: 0 10px;
}

.product-title {
  margin-bottom: 20px;
}

.product-title h2 {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 10px;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-score {
  font-size: 18px;
  font-weight: 600;
  color: #ff9900;
}

.comment-count {
  color: #909399;
  font-size: 14px;
}

.no-rating {
  color: #909399;
  font-style: italic;
}

.divider-icon {
  margin-right: 8px;
  color: var(--primary-color);
}

.product-meta {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background: linear-gradient(to right, rgba(52, 152, 219, 0.05), rgba(155, 89, 182, 0.05));
  border-radius: 8px;
}

.product-price-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-price {
  display: flex;
  align-items: center;
}

.price-label {
  font-size: 16px;
  color: #606266;
  margin-right: 8px;
}

.price-value {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
  background: linear-gradient(to right, #e74c3c, #c0392b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.price-tag {
  margin-left: 10px;
  transform: rotate(-5deg);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: rotate(-5deg) scale(1);
  }
  50% {
    transform: rotate(-5deg) scale(1.1);
  }
  100% {
    transform: rotate(-5deg) scale(1);
  }
}

.product-stock {
  display: flex;
  align-items: center;
}

.stock-label {
  font-size: 16px;
  color: #606266;
  margin-right: 8px;
}

.stock-value {
  font-size: 18px;
  font-weight: 600;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 10px;
}

.low-stock {
  color: #f56c6c;
}

.product-actions {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin: 30px 0;
  padding: 20px;
  background: linear-gradient(to right, rgba(52, 152, 219, 0.08), rgba(155, 89, 182, 0.08));
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-label {
  font-size: 16px;
  color: #606266;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.cart-button {
  flex: 1;
  min-width: 150px;
  transition: all 0.3s ease;
}

.cart-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(52, 152, 219, 0.3);
}

.wishlist-button {
  min-width: 150px;
  transition: all 0.3s ease;
}

.wishlist-button:hover {
  transform: translateY(-3px);
}

.view-cart-button {
  min-width: 150px;
  transition: all 0.3s ease;
}

.view-cart-button:hover {
  transform: translateY(-3px);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 15px;
}

.section-title .el-icon {
  color: var(--primary-color);
}

.description-content,
.detail-content {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  padding: 15px;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  border-left: 4px solid var(--primary-color);
  margin-bottom: 20px;
}

.spec-table {
  margin-top: 15px;
}

.spec-item {
  transition: background-color 0.3s ease;
}

.spec-tag {
  font-size: 14px;
}

.external-reviews {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.review-button {
  padding: 12px 25px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.review-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(103, 194, 58, 0.3);
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-label {
  font-size: 16px;
  color: #606266;
}

.rating-value {
  font-size: 18px;
  font-weight: 600;
  color: #ff9900;
  min-width: 40px;
  display: flex;
  align-items: center;
}

.rating-text {
  background: linear-gradient(to right, #ff9900, #ffb700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid rgba(255, 153, 0, 0.2);
  background-color: rgba(255, 153, 0, 0.05);
}

.comment-button {
  transition: all 0.3s ease;
}

.comment-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(52, 152, 219, 0.3);
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
}

.comment-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  color: white;
  font-weight: bold;
}

.username {
  font-weight: 600;
  color: var(--text-color);
}

.comment-content {
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 15px;
  padding: 10px;
  background-color: rgba(245, 247, 250, 0.5);
  border-radius: 8px;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.delete-button {
  transition: all 0.3s ease;
}

.delete-button:hover {
  transform: translateY(-2px);
}

.comment-dialog {
  border-radius: 12px;
  overflow: hidden;
}

/* 修改评论对话框的样式，使其在屏幕中心显示 */
:deep(.comment-dialog .el-dialog) {
  margin: 0 auto !important;
  position: relative;
  top: 50%;
  transform: translateY(-50%);
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.comment-dialog .el-dialog__header) {
  padding: 0;
  background: none;
  color: white;
  border-bottom: none;
}

:deep(.comment-dialog .el-dialog__headerbtn) {
  top: 15px;
  right: 15px;
  z-index: 10;
}

:deep(.comment-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #909399;
}

:deep(.comment-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
  color: #409eff;
}

:deep(.comment-dialog .el-dialog__body) {
  padding: 25px;
}

:deep(.comment-dialog .el-dialog__footer) {
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.comment-dialog .el-form-item__label) {
  font-weight: 600;
  color: #606266;
  font-size: 16px;
  position: relative;
  padding-left: 10px;
}

:deep(.comment-dialog .el-form-item__label)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: linear-gradient(to bottom, #3a8ee6, #53a8ff);
  border-radius: 2px;
}

:deep(.comment-dialog .el-form-item) {
  margin-bottom: 20px;
}

.rate-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

/* 评论表单样式 */
.comment-form {
  padding: 10px;
}

.rating-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.comment-rate {
  transform: scale(1.2);
  transform-origin: left;
}

.rating-value {
  font-size: 18px;
  font-weight: 600;
  color: #ff9900;
  min-width: 40px;
}

:deep(.comment-textarea .el-textarea__inner) {
  border-radius: 8px;
  padding: 12px;
  font-size: 15px;
  transition: all 0.3s ease;
  border: 1px solid #dcdfe6;
}

:deep(.comment-textarea .el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .product-content {
    flex-direction: column;
  }

  .product-cover-container {
    width: 100%;
    max-width: 300px;
  }

  .product-image {
    height: 350px;
  }

  .product-title h2 {
    font-size: 24px;
  }

  .price-value {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .cart-button,
  .wishlist-button,
  .view-cart-button {
    width: 100%;
  }
}

/* 评论对话框动画效果 */
:deep(.comment-dialog.el-dialog-fade-enter-active) {
  animation: dialog-fade-in 0.4s;
}

:deep(.comment-dialog.el-dialog-fade-leave-active) {
  animation: dialog-fade-out 0.4s;
}

@keyframes dialog-fade-in {
  0% {
    transform: translate3d(0, -30px, 0);
    opacity: 0;
  }
  100% {
    transform: translate3d(0, 0, 0);
    opacity: 1;
  }
}

@keyframes dialog-fade-out {
  0% {
    transform: translate3d(0, 0, 0);
    opacity: 1;
  }
  100% {
    transform: translate3d(0, -30px, 0);
    opacity: 0;
  }
}

/* 评论对话框内容样式 */
.comment-dialog-content {
  padding: 20px 10px 0;
}

.comment-dialog-header {
  text-align: center;
  margin-bottom: 20px;
  padding-top: 10px;
}

.comment-icon {
  font-size: 36px;
  margin-bottom: 10px;
  animation: float-icon 3s ease-in-out infinite;
}

@keyframes float-icon {
  0% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-5px) rotate(10deg);
  }
  100% {
    transform: translateY(0px) rotate(0deg);
  }
}

.comment-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.comment-subtitle {
  font-size: 14px;
  color: #909399;
  margin-bottom: 15px;
}

/* 评论表单样式 */
.comment-form {
  padding: 10px;
  background-color: rgba(245, 247, 250, 0.5);
  border-radius: 10px;
}

.rating-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.comment-rate {
  transform: scale(1.2);
  transform-origin: left;
}

.rating-value {
  font-size: 18px;
  font-weight: 600;
  color: #ff9900;
  min-width: 40px;
}

.rating-text {
  background: linear-gradient(to right, #ff9900, #ffb700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid rgba(255, 153, 0, 0.2);
  background-color: rgba(255, 153, 0, 0.05);
}

:deep(.comment-textarea .el-textarea__inner) {
  border-radius: 8px;
  padding: 15px;
  font-size: 15px;
  transition: all 0.3s ease;
  border: 1px solid #dcdfe6;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.comment-textarea .el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.rate-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
  text-align: center;
  font-style: italic;
}

/* 对话框底部按钮样式 */
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.cancel-button {
  min-width: 100px;
  transition: all 0.3s ease;
}

.submit-button {
  min-width: 120px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.submit-icon {
  animation: pulse-icon 1.5s infinite;
}

@keyframes pulse-icon {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
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

.rate-edit-container .rate-hint {
  color: #909399;
  font-size: 12px;
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

/* 表单样式 - 与商品列表保持一致 */
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

/* Dialog 美化 - 与商品列表保持一致 */
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

.submitting-icon {
  margin-right: 5px;
}

.tags-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.product-tags {
  margin-bottom: 20px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.book-tag {
  margin: 5px;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 16px;
  background-color: #f0f9ff;
  color: #409eff;
  border-color: #d9ecff;
  cursor: pointer;
  transition: all 0.3s;
}

.book-tag:hover {
  background-color: #409eff;
  color: #ffffff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}
</style>
