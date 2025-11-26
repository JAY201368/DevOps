<template>
  <div class="banner-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">轮播图管理</h2>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 添加轮播图
          </el-button>
        </div>
      </template>

      <!-- 轮播图列表 -->
      <el-table 
        v-loading="loading" 
        :data="banners" 
        border 
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="description" label="描述" min-width="300" />
        <el-table-column label="书籍数量" width="120" align="center">
          <template #default="scope">
            {{ scope.row.books ? scope.row.books.length : 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="success" size="small" @click="handleEditBooks(scope.row)">
              <el-icon><Collection /></el-icon> 管理书籍
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="!loading && banners.length === 0" description="暂无轮播图数据" />
    </el-card>

    <!-- 轮播图表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加轮播图' : '编辑轮播图'"
      width="50%"
    >
      <div class="form-info-box">
        <el-alert
          title="提示"
          type="info"
          description="链接将自动设置为商品列表页，显示顺序将自动设置。"
          show-icon
          :closable="false"
        />
      </div>
      <el-form
        ref="bannerFormRef"
        :model="bannerForm"
        :rules="rules"
        label-width="100px"
        class="banner-form"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="bannerForm.title" placeholder="请输入标题" />
          <div class="form-item-tip">轮播图显示的主标题</div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="bannerForm.description" placeholder="请输入描述" />
          <div class="form-item-tip">轮播图显示的副标题或描述文字</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 书籍管理对话框 -->
    <el-dialog
      v-model="booksDialogVisible"
      title="管理轮播图书籍"
      width="70%"
    >
      <div v-if="currentBanner" class="banner-info">
        <h3>{{ currentBanner.title }}</h3>
        <p>{{ currentBanner.description }}</p>
      </div>

      <div class="books-selection">
        <div class="selected-books">
          <h4>已选择的书籍 ({{ selectedBooks.length }})</h4>
          <el-empty v-if="selectedBooks.length === 0" description="暂未选择书籍" :image-size="100" />
          <div v-else class="books-grid">
            <el-card 
              v-for="book in selectedBooks" 
              :key="book.id" 
              class="book-card"
              shadow="hover"
            >
              <div class="book-cover">
                <el-image :src="book.cover" fit="contain" class="cover-image" />
              </div>
              <div class="book-info">
                <h5 class="book-title">{{ book.title }}</h5>
                <div class="book-price">¥{{ book.price }}</div>
              </div>
              <el-button 
                type="danger" 
                size="small" 
                circle 
                class="remove-button"
                @click="removeBook(book)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </el-card>
          </div>
        </div>

        <div class="books-search">
          <h4>添加书籍</h4>
          <div class="search-bar">
            <el-input
              v-model="searchQuery"
              placeholder="搜索书籍..."
              clearable
              @input="searchBooks"
            >
              <template #append>
                <el-button :icon="Search" @click="searchBooks" />
              </template>
            </el-input>
          </div>

          <div class="search-results">
            <el-empty v-if="searchResults.length === 0" description="暂无搜索结果" :image-size="100" />
            <div v-else class="books-grid">
              <el-card 
                v-for="book in searchResults" 
                :key="book.id" 
                class="book-card"
                shadow="hover"
                :class="{ 'is-selected': isBookSelected(book) }"
              >
                <div class="book-cover">
                  <el-image :src="book.cover" fit="contain" class="cover-image" />
                </div>
                <div class="book-info">
                  <h5 class="book-title">{{ book.title }}</h5>
                  <div class="book-price">¥{{ book.price }}</div>
                </div>
                <el-button 
                  :type="isBookSelected(book) ? 'danger' : 'success'" 
                  size="small" 
                  circle 
                  class="add-button"
                  @click="toggleBook(book)"
                >
                  <el-icon>
                    <component :is="isBookSelected(book) ? 'Close' : 'Plus'" />
                  </el-icon>
                </el-button>
              </el-card>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="booksDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveBooks">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Collection, Close, Search } from '@element-plus/icons-vue';
import { getAllBanners, createBanner, updateBanner, deleteBanner, updateBannerBooks } from '../api/banner';
import { getAllProducts } from '../api/product';

// 轮播图列表
const banners = ref([]);
const loading = ref(false);

// 轮播图表单
const dialogVisible = ref(false);
const dialogType = ref('add'); // 'add' 或 'edit'
const bannerFormRef = ref(null);
const bannerForm = reactive({
  id: null,
  title: '',
  description: '',
  link: '/products',
  displayOrder: 1
});

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
};

// 书籍管理
const booksDialogVisible = ref(false);
const currentBanner = ref(null);
const selectedBooks = ref([]);
const searchQuery = ref('');
const searchResults = ref([]);
const allBooks = ref([]);

// 获取所有轮播图
const fetchBanners = async () => {
  loading.value = true;
  try {
    const res = await getAllBanners();
    if (res.code === '200') {
      banners.value = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取轮播图失败');
    }
  } catch (error) {
    console.error('获取轮播图失败:', error);
    ElMessage.error('获取轮播图失败');
  } finally {
    loading.value = false;
  }
};

// 获取所有书籍
const fetchAllBooks = async () => {
  try {
    const res = await getAllProducts();
    if (res.code === '200') {
      allBooks.value = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取书籍失败');
    }
  } catch (error) {
    console.error('获取书籍失败:', error);
    ElMessage.error('获取书籍失败');
  }
};

// 添加轮播图
const handleAdd = () => {
  dialogType.value = 'add';
  Object.assign(bannerForm, {
    id: null,
    title: '',
    description: '',
    link: '/products',
    displayOrder: banners.value.length + 1
  });
  dialogVisible.value = true;
};

// 编辑轮播图
const handleEdit = (row) => {
  dialogType.value = 'edit';
  Object.assign(bannerForm, {
    id: row.id,
    title: row.title,
    description: row.description,
    link: row.link || '/products',
    displayOrder: row.displayOrder || 1
  });
  dialogVisible.value = true;
};

// 删除轮播图
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteBanner(row.id);
      if (res.code === '200') {
        ElMessage.success('删除成功');
        fetchBanners();
      } else {
        ElMessage.error(res.msg || '删除失败');
      }
    } catch (error) {
      console.error('删除轮播图失败:', error);
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

// 提交表单
const submitForm = async () => {
  if (!bannerFormRef.value) return;

  await bannerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 确保提交的表单数据包含link和displayOrder
        const formData = {
          ...bannerForm,
          link: bannerForm.link || '/products', // 默认链接
          displayOrder: bannerForm.displayOrder || banners.value.length + 1 // 默认顺序
        };

        let res;
        if (dialogType.value === 'add') {
          res = await createBanner(formData);
        } else {
          res = await updateBanner(formData.id, formData);
        }

        if (res.code === '200') {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功');
          dialogVisible.value = false;
          fetchBanners();
        } else {
          ElMessage.error(res.msg || (dialogType.value === 'add' ? '添加失败' : '更新失败'));
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加轮播图失败:' : '更新轮播图失败:', error);
        ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败');
      }
    }
  });
};

// 管理书籍
const handleEditBooks = (row) => {
  currentBanner.value = row;
  selectedBooks.value = row.books || [];
  booksDialogVisible.value = true;
  searchBooks();
};

// 搜索书籍
const searchBooks = () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = allBooks.value;
    return;
  }

  const query = searchQuery.value.toLowerCase();
  searchResults.value = allBooks.value.filter(book => 
    book.title.toLowerCase().includes(query) ||
    (book.description && book.description.toLowerCase().includes(query))
  );
};

// 检查书籍是否已选择
const isBookSelected = (book) => {
  return selectedBooks.value.some(selected => selected.id === book.id);
};

// 切换书籍选择状态
const toggleBook = (book) => {
  if (isBookSelected(book)) {
    removeBook(book);
  } else {
    selectedBooks.value.push(book);
  }
};

// 移除已选择的书籍
const removeBook = (book) => {
  selectedBooks.value = selectedBooks.value.filter(selected => selected.id !== book.id);
};

// 保存书籍
const saveBooks = async () => {
  if (!currentBanner.value) return;

  try {
    const productIds = selectedBooks.value.map(book => book.id);
    const res = await updateBannerBooks(currentBanner.value.id, productIds);

    if (res.code === '200') {
      ElMessage.success('保存成功');
      booksDialogVisible.value = false;
      fetchBanners();
    } else {
      ElMessage.error(res.msg || '保存失败');
    }
  } catch (error) {
    console.error('保存书籍失败:', error);
    ElMessage.error('保存失败');
  }
};

onMounted(() => {
  fetchBanners();
  fetchAllBooks();
});
</script>

<style scoped>
.banner-management {
  padding: 20px;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 20px;
}

.form-info-box {
  margin-bottom: 20px;
}

.banner-form {
  margin-top: 20px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.banner-info {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.banner-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.banner-info p {
  margin: 0;
  color: #606266;
}

.books-selection {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.selected-books,
.books-search {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.selected-books h4,
.books-search h4 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.book-card {
  position: relative;
  height: 100%;
}

.book-cover {
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  overflow: hidden;
}

.cover-image {
  height: 100%;
  width: 100%;
  object-fit: contain;
}

.book-info {
  padding: 10px;
}

.book-title {
  margin: 0 0 10px 0;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-price {
  color: #f56c6c;
  font-weight: bold;
}

.remove-button,
.add-button {
  position: absolute;
  top: 5px;
  right: 5px;
  opacity: 0.8;
}

.book-card:hover .remove-button,
.book-card:hover .add-button {
  opacity: 1;
}

.is-selected {
  border: 2px solid #67c23a;
}

.search-bar {
  margin-bottom: 15px;
}

.search-results {
  max-height: 400px;
  overflow-y: auto;
}
</style> 