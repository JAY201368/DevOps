<template>
  <div class="product-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>商品详情</span>
          <div v-if="isAdmin">
            <el-button
              type="primary"
              @click="handleEdit"
            >
              编辑
            </el-button>
            <el-button
              type="primary"
              @click="handleStock"
            >
              调整库存
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="product" class="product-content">
        <div class="product-info">
          <div class="product-title">
            <h2>{{ product.title }}</h2>
            <el-rate
              v-model="product.rate"
              disabled
              show-score
              text-color="#ff9900"
            />
          </div>
          
          <div class="product-price">
            <span class="price-label">价格：</span>
            <span class="price-value">¥{{ product.price }}</span>
          </div>

          <div class="product-stock">
            <span class="stock-label">库存：</span>
            <span class="stock-value">{{ product.stockpile?.amount || 0 }}</span>
          </div>

          <div class="product-description">
            <h3>商品描述</h3>
            <p>{{ product.description }}</p>
          </div>

          <div class="product-detail">
            <h3>详细说明</h3>
            <p>{{ product.detail }}</p>
          </div>

          <div class="product-specifications">
            <h3>规格信息</h3>
            <el-descriptions
              :column="2"
              border
            >
              <el-descriptions-item
                v-for="spec in product.specifications"
                :key="spec.id"
                :label="spec.item"
                :span="1"
              >
                {{ spec.value }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div class="product-cover">
          <el-image
            v-if="product.cover"
            :src="product.cover"
            fit="contain"
            style="width: 300px; height: 300px"
          />
          <el-empty
            v-else
            description="暂无图片"
          />
        </div>
      </div>
    </el-card>

    <!-- 编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑商品"
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
          <span>{{ product?.title }}</span>
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
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  getProductById,
  updateProduct,
  updateStockpile
} from '../api/product';

const route = useRoute();
const loading = ref(false);
const product = ref(null);
const dialogVisible = ref(false);
const stockDialogVisible = ref(false);
const productFormRef = ref(null);
const stockFormRef = ref(null);

const productForm = ref({
  id: '',
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

const isAdmin = ref(false); // TODO: 从用户状态获取

const fetchProduct = async () => {
  loading.value = true;
  try {
    const res = await getProductById(route.params.id);
    if (res.data.code === '200') {
      product.value = res.data.data;
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败');
  } finally {
    loading.value = false;
  }
};

const handleEdit = () => {
  productForm.value = { ...product.value };
  dialogVisible.value = true;
};

const handleStock = () => {
  stockForm.value.amount = product.value.stockpile?.amount || 0;
  stockDialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!productFormRef.value) return;
  
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await updateProduct(productForm.value);
        if (res.data.code === '200') {
          ElMessage.success('更新成功');
          dialogVisible.value = false;
          fetchProduct();
        }
      } catch (error) {
        ElMessage.error('更新失败');
      }
    }
  });
};

const handleStockSubmit = async () => {
  if (!stockFormRef.value) return;
  
  await stockFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await updateStockpile(product.value.id, stockForm.value.amount);
        if (res.data.code === '200') {
          ElMessage.success('调整库存成功');
          stockDialogVisible.value = false;
          fetchProduct();
        }
      } catch (error) {
        ElMessage.error('调整库存失败');
      }
    }
  });
};

onMounted(() => {
  fetchProduct();
});
</script>

<style scoped>
.product-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-content {
  display: flex;
  gap: 40px;
}

.product-info {
  flex: 1;
}

.product-title {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.product-title h2 {
  margin: 0;
}

.product-price,
.product-stock {
  margin-bottom: 20px;
  font-size: 16px;
}

.price-value {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.product-description,
.product-detail {
  margin-bottom: 20px;
}

.product-description h3,
.product-detail h3,
.product-specifications h3 {
  margin-bottom: 10px;
}

.product-cover {
  width: 300px;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 