<template>
  <div class="product-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <el-button
            v-if="isAdmin"
            type="primary"
            @click="handleAdd"
          >
            添加商品
          </el-button>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="products"
        style="width: 100%"
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getAllProducts,
  createProduct,
  updateProduct,
  deleteProduct,
  updateStockpile
} from '../api/product';

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

const isAdmin = ref(false); // TODO: 从用户状态获取

const fetchProducts = async () => {
  loading.value = true;
  try {
    const res = await getAllProducts();
    if (res.data.code === '200') {
      products.value = res.data.data;
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败');
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
      const res = await deleteProduct(row.id);
      if (res.data.code === '200') {
        ElMessage.success('删除成功');
        fetchProducts();
      }
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

const handleView = (row) => {
  router.push(`/products/${row.id}`);
};

const handleStock = (row) => {
  currentProduct.value = row;
  stockForm.value.amount = row.stockpile?.amount || 0;
  stockDialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!productFormRef.value) return;
  
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const api = dialogType.value === 'add' ? createProduct : updateProduct;
        const res = await api(productForm.value);
        if (res.data.code === '200') {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功');
          dialogVisible.value = false;
          fetchProducts();
        }
      } catch (error) {
        ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败');
      }
    }
  });
};

const handleStockSubmit = async () => {
  if (!stockFormRef.value) return;
  
  await stockFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await updateStockpile(currentProduct.value.id, stockForm.value.amount);
        if (res.data.code === '200') {
          ElMessage.success('调整库存成功');
          stockDialogVisible.value = false;
          fetchProducts();
        }
      } catch (error) {
        ElMessage.error('调整库存失败');
      }
    }
  });
};

onMounted(() => {
  fetchProducts();
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
</style> 