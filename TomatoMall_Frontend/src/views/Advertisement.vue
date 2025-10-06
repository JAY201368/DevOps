<template>
  <div class="advertisement-page">
    <div class="page-header">
      <div class="header-content">
        <h1>广告管理中心</h1>
      </div>
      <button @click="showAddForm = true" class="btn-add">
        <span class="btn-add-content">
          <i class="el-icon-plus"></i>
          <span>新增广告</span>
        </span>
      </button>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载广告数据...</p>
    </div>

    <div v-else-if="error" class="error-message">
      <i class="el-icon-warning"></i>
      <p>{{ error }}</p>
      <button @click="fetchAdvertisements" class="btn-retry">重新加载</button>
    </div>

    <div v-else-if="advertisements.length === 0" class="empty-state">
      <div class="empty-icon">📢</div>
      <h3>暂无广告数据</h3>
      <p>您还没有创建任何广告，立即创建一个吧！</p>
      <button @click="showAddForm = true" class="btn-add">创建新广告</button>
    </div>

    <div v-else class="advertisement-grid">
      <div v-for="ad in advertisements" :key="ad.id" class="ad-grid-item">
        <div class="ad-card">
          <div class="ad-image-container">
            <img :src="ad.imgUrl" alt="广告图片" class="ad-image" @error="handleAdImageError($event, ad.id)" />
            <div v-if="adImageErrors[ad.id]" class="image-error-overlay">
              <i class="el-icon-picture-outline"></i>
              <span>图片加载失败</span>
            </div>
            <div class="ad-badge">广告</div>
          </div>
          <div class="ad-content">
            <h3 class="ad-title">{{ ad.title }}</h3>
            <p class="ad-desc">{{ ad.content }}</p>
            <div class="ad-meta">
              <span class="product-link">关联商品ID: {{ ad.productId }}</span>
            </div>
          </div>
          <div class="ad-actions">
            <button @click.stop="editAdvertisement(ad)" class="btn-edit">
              <i class="el-icon-edit"></i> 编辑
            </button>
            <button @click.stop="confirmDelete(ad)" class="btn-delete">
              <i class="el-icon-delete"></i> 删除
            </button>
            <button @click.stop="previewAd(ad)" class="btn-preview">
              <i class="el-icon-view"></i> 预览
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 成功消息提示 -->
    <div v-if="successMessage" class="success-message">
      <i class="el-icon-success"></i>
      <p>{{ successMessage }}</p>
    </div>

    <!-- 新增/编辑广告表单 -->
    <div v-if="showAddForm || showEditForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal-content">
        <button @click="closeForm" class="modal-close">&times;</button>
        <advertisement-form
          :advertisement="currentAd"
          :is-edit="showEditForm"
          @success="handleSuccess"
          @error="handleError"
          @cancel="closeForm"
        />
      </div>
    </div>

    <!-- 广告预览框 -->
    <div v-if="showPreview" class="modal-overlay" @click.self="closePreview">
      <div class="modal-content preview-content">
        <button @click="closePreview" class="modal-close">&times;</button>
        <div class="preview-container">
          <h2>广告预览</h2>
          <div class="preview-ad">
            <img :src="previewData.imgUrl" alt="广告预览" @error="handlePreviewImageError" 
                 class="preview-image" v-if="!previewImageError" />
            <div v-else class="preview-image-error">
              <i class="el-icon-picture-outline"></i>
              <span>图片加载失败，请检查URL</span>
            </div>
            <div class="preview-info">
              <h3>{{ previewData.title }}</h3>
              <p>{{ previewData.content }}</p>
              <div class="preview-meta">
                <span>关联商品ID: {{ previewData.productId }}</span>
                <button @click="goToProduct(previewData.productId)" class="btn-view-product">
                  查看关联商品
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除确认框 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="showDeleteConfirm = false">
      <div class="modal-content delete-confirm">
        <div class="delete-header">
          <i class="el-icon-warning-outline"></i>
          <h3>确认删除</h3>
        </div>
        <p>确定要删除广告 "{{ currentAd.title }}" 吗？</p>
        <p class="delete-warning">此操作不可撤销</p>
        <div class="confirm-actions">
          <button @click="showDeleteConfirm = false" class="btn-cancel">取消</button>
          <button @click="deleteAd" class="btn-confirm-delete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import AdvertisementCard from '../components/AdvertisementCard.vue';
import AdvertisementForm from '../components/AdvertisementForm.vue';
import { getAdvertisements, deleteAdvertisement } from '../api/advertisement';

export default {
  name: 'Advertisement',
  components: {
    AdvertisementCard,
    AdvertisementForm
  },
  setup() {
    const router = useRouter();
    const advertisements = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const successMessage = ref('');
    const showAddForm = ref(false);
    const showEditForm = ref(false);
    const showDeleteConfirm = ref(false);
    const showPreview = ref(false);
    const currentAd = ref(null);
    const previewData = ref({});
    const previewImageError = ref(false);
    const adImageErrors = reactive({});

    const fetchAdvertisements = async () => {
      loading.value = true;
      error.value = null;
      successMessage.value = '';
      
      try {
        console.log('开始获取广告列表');
        const response = await getAdvertisements();
        console.log('获取广告响应:', response);
        
        if (response.code === '200') {
          advertisements.value = response.data;
          console.log('成功获取广告数据:', advertisements.value.length);
        } else {
          error.value = response.msg || '获取广告列表失败';
          console.error('获取广告失败:', response.msg);
        }
      } catch (err) {
        error.value = '获取广告列表失败，请稍后重试';
        console.error('获取广告异常:', err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(fetchAdvertisements);

    const handleAdImageError = (event, adId) => {
      console.log('广告图片加载失败:', adId);
      adImageErrors[adId] = true;
    };

    const handlePreviewImageError = () => {
      console.log('预览图片加载失败');
      previewImageError.value = true;
    };

    const editAdvertisement = (ad) => {
      console.log('编辑广告:', ad);
      currentAd.value = { ...ad }; // 使用解构复制，避免引用问题
      showEditForm.value = true;
    };

    const confirmDelete = (ad) => {
      console.log('确认删除广告:', ad);
      currentAd.value = ad;
      showDeleteConfirm.value = true;
    };

    const deleteAd = async () => {
      try {
        console.log('删除广告:', currentAd.value.id);
        const response = await deleteAdvertisement(currentAd.value.id);
        console.log('删除广告响应:', response);
        
        if (response.code === '200') {
          // 显示成功消息
          successMessage.value = `广告"${currentAd.value.title}"已成功删除`;
          
          // 关闭删除确认框
          showDeleteConfirm.value = false;
          
          // 刷新广告列表
          await fetchAdvertisements();
          
          // 3秒后自动清除成功消息
          setTimeout(() => {
            successMessage.value = '';
          }, 3000);
        } else {
          error.value = response.msg || '删除广告失败';
          console.error('删除广告失败:', response.msg);
        }
      } catch (err) {
        error.value = '删除广告失败，请稍后重试';
        console.error('删除广告异常:', err);
      }
    };

    const previewAd = (ad) => {
      console.log('预览广告:', ad);
      previewData.value = { ...ad };
      previewImageError.value = false;
      showPreview.value = true;
    };

    const closePreview = () => {
      showPreview.value = false;
    };

    const goToProduct = (productId) => {
      console.log('跳转到商品页面:', productId);
      router.push(`/products/${productId}`);
    };

    const handleSuccess = async (data) => {
      console.log('操作成功:', data);
      // 根据操作类型显示不同的成功消息
      if (showEditForm.value) {
        successMessage.value = `广告"${currentAd.value.title}"已成功更新`;
      } else {
        successMessage.value = `已成功创建新广告"${data.title}"`;
      }
      
      // 关闭表单
      closeForm();
      
      // 刷新广告列表
      await fetchAdvertisements();
      
      // 3秒后自动清除成功消息
      setTimeout(() => {
        successMessage.value = '';
      }, 3000);
    };

    const handleError = (msg) => {
      console.error('操作失败:', msg);
      error.value = msg;
    };

    const closeForm = () => {
      console.log('关闭表单');
      showAddForm.value = false;
      showEditForm.value = false;
      currentAd.value = null;
    };

    return {
      advertisements,
      loading,
      error,
      successMessage,
      showAddForm,
      showEditForm,
      showDeleteConfirm,
      showPreview,
      currentAd,
      previewData,
      previewImageError,
      adImageErrors,
      fetchAdvertisements,
      editAdvertisement,
      confirmDelete,
      deleteAd,
      previewAd,
      closePreview,
      goToProduct,
      handleSuccess,
      handleError,
      closeForm,
      handleAdImageError,
      handlePreviewImageError
    };
  }
};
</script>

<style scoped>
.advertisement-page {
  padding: 30px;
  max-width: 1280px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 25px;
  border-bottom: 1px solid #eaeaea;
}

.header-content {
  display: flex;
  flex-direction: column;
}

.page-header h1 {
  margin: 0;
  font-size: 32px;
  color: #333;
  font-weight: 600;
}

.btn-add {
  background-color: #4caf50;
  color: white;
  border: none;
  width: 90px;
  height: 50px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(76, 175, 80, 0.3);
  padding: 0;
  position: relative;
  overflow: hidden;
}

.btn-add::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 100%);
  z-index: 1;
}

.btn-add-content {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  gap: 5px;
  position: relative;
  z-index: 2;
}

.btn-add-content i {
  font-size: 16px;
}

.btn-add:hover {
  background-color: #43a047;
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.5);
}

.btn-add:active {
  transform: translateY(-2px);
  box-shadow: 0 3px 10px rgba(76, 175, 80, 0.4);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  background: #f9f9f9;
  border-radius: 8px;
  margin: 20px 0;
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #2196F3;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message, .empty-state {
  text-align: center;
  padding: 60px 30px;
  background: #f9f9f9;
  border-radius: 12px;
  margin: 20px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.error-message i {
  font-size: 36px;
  color: #f44336;
  margin-bottom: 16px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  margin-bottom: 12px;
  color: #333;
}

.empty-state p {
  color: #666;
  margin-bottom: 24px;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.btn-retry {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 16px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn-retry:hover {
  background-color: #1e88e5;
}

.advertisement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 30px;
}

.ad-grid-item {
  display: flex;
  flex-direction: column;
}

.ad-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ad-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}

.ad-image-container {
  position: relative;
  height: 250px;
  overflow: hidden;
}

.ad-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
  display: block;
}

.ad-card:hover .ad-image {
  transform: scale(1.05);
}

.image-error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(245, 247, 250, 0.9);
  color: #909399;
}

.image-error-overlay i {
  font-size: 40px;
  margin-bottom: 10px;
}

.ad-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.ad-content {
  padding: 24px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.ad-title {
  margin-top: 0;
  font-size: 22px;
  color: #333;
  margin-bottom: 14px;
  font-weight: 600;
}

.ad-desc {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 18px;
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.ad-meta {
  font-size: 14px;
  color: #888;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
}

.product-link {
  display: block;
}

.ad-actions {
  display: flex;
  padding: 0 24px 24px;
  gap: 12px;
}

.btn-edit, .btn-delete, .btn-preview {
  flex: 1;
  padding: 12px 0;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.btn-edit {
  background-color: #2196F3;
  color: white;
}

.btn-edit:hover {
  background-color: #1e88e5;
}

.btn-delete {
  background-color: #f44336;
  color: white;
}

.btn-delete:hover {
  background-color: #e53935;
}

.btn-preview {
  background-color: #9e9e9e;
  color: white;
}

.btn-preview:hover {
  background-color: #757575;
}

.success-message {
  position: fixed;
  top: 20px;
  right: 20px;
  background-color: #4caf50;
  color: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1100;
  animation: fadeIn 0.3s, fadeOut 0.5s 2.5s;
  display: flex;
  align-items: center;
  gap: 12px;
}

.success-message i {
  font-size: 22px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeOut {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(-20px); }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(6px);
  animation: fadeIn 0.3s;
}

.modal-content {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  position: relative;
  max-width: 90%;
  max-height: 90%;
  overflow-y: auto;
  animation: slideIn 0.3s;
}

@keyframes slideIn {
  from { transform: translateY(30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #888;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  z-index: 10;
}

.modal-close:hover {
  background: #f5f5f5;
  color: #333;
}

.preview-content {
  width: 800px;
  max-width: 90%;
}

.preview-container h2 {
  margin-top: 0;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
  font-size: 28px;
}

.preview-ad {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-image {
  width: 100%;
  border-radius: 12px;
  max-height: 400px;
  object-fit: cover;
}

.preview-image-error {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #909399;
}

.preview-image-error i {
  font-size: 48px;
  margin-bottom: 12px;
}

.preview-info h3 {
  margin-top: 0;
  font-size: 24px;
  color: #333;
  margin-bottom: 16px;
}

.preview-info p {
  color: #666;
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.preview-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #eee;
  font-size: 14px;
  color: #888;
}

.btn-view-product {
  background-color: #ff9800;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-view-product:hover {
  background-color: #f57c00;
}

.delete-confirm {
  text-align: center;
  min-width: 400px;
}

.delete-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.delete-header i {
  font-size: 48px;
  color: #ff9800;
  margin-bottom: 10px;
}

.delete-header h3 {
  margin: 0;
  font-size: 22px;
  color: #333;
}

.delete-warning {
  color: #f44336;
  font-size: 14px;
  margin-top: 10px;
}

.confirm-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 15px;
}

.btn-cancel, .btn-confirm-delete {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #333;
}

.btn-cancel:hover {
  background-color: #e0e0e0;
}

.btn-confirm-delete {
  background-color: #f44336;
  color: white;
}

.btn-confirm-delete:hover {
  background-color: #e53935;
}

@media (max-width: 768px) {
  .advertisement-page {
    padding: 20px;
  }
  
  .advertisement-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .btn-add {
    width: 100%;
    justify-content: center;
  }
  
  .modal-content {
    padding: 30px 20px;
  }
}
</style> 