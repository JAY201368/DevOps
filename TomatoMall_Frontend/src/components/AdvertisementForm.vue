<template>
  <div class="ad-form-container">
    <!-- 美化的表单头部 -->
    <div class="form-header">
      <div class="form-icon">{{ isEdit ? '✏️' : '📢' }}</div>
      <div class="header-content">
        <h2 class="form-title">{{ isEdit ? '编辑广告' : '创建新广告' }}</h2>
        <p class="form-subtitle">{{ isEdit ? '修改广告信息，让您的推广更有效' : '制作精彩广告，吸引更多用户关注' }}</p>
      </div>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="form-error">
      <div class="error-icon">⚠️</div>
      <div class="error-content">
        <div class="error-title">操作失败</div>
        <div class="error-message">{{ error }}</div>
      </div>
    </div>

    <form @submit.prevent="submitForm" class="ad-form">
      <!-- 基本信息区块 -->
      <div class="form-section">
        <div class="section-header">
          <div class="section-icon">📝</div>
          <div class="section-title">基本信息</div>
          <div class="section-line"></div>
        </div>
        
        <div class="form-group">
          <label for="adTitle" class="modern-label">
            <span class="label-icon">🏷️</span>
            <span class="label-text">广告标题</span>
            <span class="required">*</span>
          </label>
          <div class="input-wrapper">
            <input 
              id="adTitle" 
              v-model="form.title" 
              type="text" 
              placeholder="输入引人注目的广告标题" 
              :class="{'input-error': validationErrors.title}"
              @input="clearValidationError('title')"
              class="modern-input"
            />
          </div>
          <span v-if="validationErrors.title" class="validation-error">
            <i class="error-icon">❌</i> {{ validationErrors.title }}
          </span>
        </div>

        <div class="form-group">
          <label for="adContent" class="modern-label">
            <span class="label-icon">📄</span>
            <span class="label-text">广告内容</span>
            <span class="required">*</span>
          </label>
          <div class="input-wrapper">
            <textarea 
              id="adContent" 
              v-model="form.content" 
              placeholder="详细描述您的广告内容，让用户了解产品特色" 
              rows="4"
              :class="{'input-error': validationErrors.content}"
              @input="clearValidationError('content')"
              class="modern-textarea"
            ></textarea>
          </div>
          <span v-if="validationErrors.content" class="validation-error">
            <i class="error-icon">❌</i> {{ validationErrors.content }}
          </span>
        </div>
      </div>

      <!-- 图片信息区块 -->
      <div class="form-section">
        <div class="section-header">
          <div class="section-icon">🖼️</div>
          <div class="section-title">图片信息</div>
          <div class="section-line"></div>
        </div>
        
        <div class="form-group">
          <label for="adImgUrl" class="modern-label">
            <span class="label-icon">🔗</span>
            <span class="label-text">广告图片URL</span>
            <span class="required">*</span>
          </label>
          <div class="input-wrapper">
            <input 
              id="adImgUrl" 
              v-model="form.imgUrl" 
              type="text" 
              placeholder="输入广告图片URL（支持 JPG、PNG、GIF 格式）" 
              :class="{'input-error': validationErrors.imgUrl}"
              @input="handleImageUrlChange"
              class="modern-input"
            />
          </div>
          <span v-if="validationErrors.imgUrl" class="validation-error">
            <i class="error-icon">❌</i> {{ validationErrors.imgUrl }}
          </span>
          
          <!-- 图片预览区域 -->
          <div v-if="form.imgUrl && form.imgUrl.trim()" class="image-preview-container">
            <div class="preview-header">
              <span class="preview-title">📸 图片预览</span>
            </div>
            <div class="image-preview">
              <img 
                v-if="!imagePreviewError" 
                :src="form.imgUrl" 
                alt="广告图片预览" 
                @error="handleImagePreviewError" 
              />
              <div v-else class="image-preview-error">
                <div class="error-icon-large">🖼️</div>
                <div class="error-text">图片加载失败</div>
                <div class="error-hint">请检查URL是否正确</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 关联商品区块 -->
      <div class="form-section">
        <div class="section-header">
          <div class="section-icon">🛍️</div>
          <div class="section-title">关联商品</div>
          <div class="section-line"></div>
        </div>
        
        <div class="form-group">
          <label for="adProductId" class="modern-label">
            <span class="label-icon">🆔</span>
            <span class="label-text">商品ID</span>
            <span class="required">*</span>
          </label>
          <div class="input-wrapper">
            <input 
              id="adProductId" 
              v-model="form.productId" 
              type="text" 
              placeholder="输入关联商品ID，系统将自动验证" 
              :class="{'input-error': validationErrors.productId}"
              @input="handleProductIdChange"
              class="modern-input"
            />
          </div>
          <span v-if="validationErrors.productId" class="validation-error">
            <i class="error-icon">❌</i> {{ validationErrors.productId }}
          </span>
          <span v-if="productValidated && productData" class="validation-success">
            <i class="success-icon">✅</i> 
            <span class="success-text">商品验证成功: {{ productData.title }}</span>
          </span>
          
          <!-- 商品信息预览 -->
          <div v-if="productData" class="product-preview-container">
            <div class="preview-header">
              <span class="preview-title">🛍️ 关联商品信息</span>
            </div>
            <div class="product-info-card">
              <div class="product-basic-info">
                <div class="info-item">
                  <span class="info-label">📖 商品名称:</span>
                  <span class="info-value">{{ productData.title }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">💰 商品价格:</span>
                  <span class="info-value price">¥{{ productData.price }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">📝 商品描述:</span>
                  <span class="info-value">{{ productData.description }}</span>
                </div>
              </div>
              
              <div v-if="productData.cover" class="product-image-section">
                <div class="image-label">📷 商品封面:</div>
                <div class="product-image-wrapper">
                  <img 
                    v-if="!productImageError" 
                    :src="productData.cover" 
                    :alt="productData.title" 
                    @error="handleProductImageError" 
                    class="product-image"
                  />
                  <div v-else class="product-image-error">
                    <div class="error-icon-large">🖼️</div>
                    <div class="error-text">商品图片加载失败</div>
                  </div>
                </div>
              </div>
              <div v-else class="no-product-image">
                <div class="no-image-icon">📷</div>
                <div class="no-image-text">该商品暂无封面图片</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <button type="button" @click="cancel" class="btn-cancel">
          <span class="btn-icon">❌</span>
          <span>取消</span>
        </button>
        <button type="submit" class="btn-submit" :disabled="submitting">
          <span v-if="!submitting" class="btn-content">
            <span class="btn-icon">{{ isEdit ? '💾' : '✨' }}</span>
            <span>{{ isEdit ? '保存修改' : '创建广告' }}</span>
          </span>
          <span v-else class="submitting">
            <span class="loading-icon">⏳</span>
            <span class="loading-text">处理中...</span>
          </span>
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue';
import { createAdvertisement, updateAdvertisement } from '../api/advertisement';
import { getProductById } from '../api/product';

export default {
  name: 'AdvertisementForm',
  props: {
    advertisement: {
      type: Object,
      default: null
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  emits: ['success', 'error', 'cancel'],
  setup(props, { emit }) {
    // 表单数据
    const form = reactive({
      title: '',
      content: '',
      imgUrl: '',
      productId: ''
    });

    // 表单状态
    const error = ref('');
    const submitting = ref(false);
    const validationErrors = reactive({});
    const productValidated = ref(false);
    const imagePreviewError = ref(false);
    const productData = ref(null);
    const productImageError = ref(false);

    // 如果是编辑模式，填充表单数据
    onMounted(() => {
      if (props.isEdit && props.advertisement) {
        console.log('编辑模式，填充表单数据:', props.advertisement);
        form.title = props.advertisement.title || '';
        form.content = props.advertisement.content || '';
        form.imgUrl = props.advertisement.imgUrl || '';
        form.productId = props.advertisement.productId || '';
        
        // 如果有商品ID，验证商品
        if (form.productId) {
          validateProductId();
        }
      }
    });

    // 图片URL变化时处理预览
    const handleImageUrlChange = () => {
      clearValidationError('imgUrl');
      imagePreviewError.value = false;
    };

    // 处理图片预览加载错误
    const handleImagePreviewError = () => {
      console.log('图片预览加载失败');
      imagePreviewError.value = true;
    };

    // 处理商品图片加载错误
    const handleProductImageError = () => {
      console.log('商品图片加载失败');
      productImageError.value = true;
    };

    // 商品ID变化时处理
    const handleProductIdChange = () => {
      clearValidationError('productId');
      productValidated.value = false;
      productData.value = null;
      productImageError.value = false;
      
      // 如果输入了商品ID，自动验证
      if (form.productId && form.productId.trim()) {
        // 使用节流，避免频繁请求
        clearTimeout(handleProductIdChange.timeoutId);
        handleProductIdChange.timeoutId = setTimeout(() => {
          validateProductId();
        }, 500);
      }
    };

    // 验证商品ID
    const validateProductId = async () => {
      if (!form.productId || !form.productId.trim()) {
        productValidated.value = false;
        productData.value = null;
        return;
      }

      try {
        const response = await getProductById(form.productId.trim());
        
        if (response.code === '200' && response.data) {
          productValidated.value = true;
          productImageError.value = false;
          
          // 先设置商品数据
          productData.value = {
            id: response.data.id,
            title: response.data.title,
            price: response.data.price,
            rate: response.data.rate,
            description: response.data.description,
            cover: response.data.cover,
            detail: response.data.detail
          };
          
          // 然后清除验证错误（但不清空商品数据）
          if (validationErrors.productId) {
            delete validationErrors.productId;
          }
        } else {
          // 处理商品不存在的情况
          productValidated.value = false;
          productData.value = null;
          if (response.code === '400' && response.msg && response.msg.includes('商品不存在')) {
            validationErrors.productId = '商品不存在，请检查商品ID';
          } else {
            validationErrors.productId = response.msg || '商品ID无效，请检查';
          }
        }
      } catch (err) {
        console.error('验证商品ID错误:', err);
        productValidated.value = false;
        productData.value = null;
        validationErrors.productId = '验证商品ID失败，请稍后重试';
      }
    };

    // 清除特定字段的验证错误
    const clearValidationError = (field) => {
      if (validationErrors[field]) {
        delete validationErrors[field];
      }
      if (field === 'productId') {
        productValidated.value = false;
        productData.value = null;
      }
    };

    // 验证表单
    const validateForm = () => {
      let isValid = true;
      
      // 清除之前的验证错误
      Object.keys(validationErrors).forEach(key => delete validationErrors[key]);
      
      // 验证标题
      if (!form.title || form.title.trim() === '') {
        validationErrors.title = '广告标题不能为空';
        isValid = false;
      } else if (form.title.length > 50) {
        validationErrors.title = '广告标题不能超过50个字符';
        isValid = false;
      }
      
      // 验证内容
      if (!form.content || form.content.trim() === '') {
        validationErrors.content = '广告内容不能为空';
        isValid = false;
      } else if (form.content.length > 200) {
        validationErrors.content = '广告内容不能超过200个字符';
        isValid = false;
      }
      
      // 验证图片URL
      if (!form.imgUrl || form.imgUrl.trim() === '') {
        validationErrors.imgUrl = '广告图片URL不能为空';
        isValid = false;
      } else if (!isValidUrl(form.imgUrl)) {
        validationErrors.imgUrl = '请输入有效的URL';
        isValid = false;
      }
      
      // 验证商品ID
      if (!form.productId || form.productId.trim() === '') {
        validationErrors.productId = '关联商品ID不能为空';
        isValid = false;
      }
      
      return isValid;
    };

    // 检查URL格式是否有效
    const isValidUrl = (url) => {
      try {
        new URL(url);
        return true;
      } catch (err) {
        return false;
      }
    };

    // 提交表单
    const submitForm = async () => {
      // 验证表单
      if (!validateForm()) {
        // 验证失败，滚动到第一个错误字段
        const firstErrorField = document.querySelector('.input-error');
        if (firstErrorField) {
          firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
          firstErrorField.focus();
        }
        return;
      }
      
      // 设置提交状态
      submitting.value = true;
      error.value = '';
      
      try {
        // 准备表单数据
        const formData = {
          title: form.title.trim(),
          content: form.content.trim(),
          imgUrl: form.imgUrl.trim(),
          productId: form.productId.trim()
        };
        
        console.log('提交广告表单:', formData);
        
        let response;
        if (props.isEdit) {
          // 编辑模式
          formData.id = props.advertisement.id;
          response = await updateAdvertisement(formData);
        } else {
          // 创建模式
          response = await createAdvertisement(formData);
        }
        
        console.log('表单提交响应:', response);
        
        if (response.code === '200') {
          // 成功响应，触发成功事件
          emit('success', formData);
        } else {
          // 处理特定的错误情况
          if (response.code === '400' && response.msg && response.msg.includes('商品不存在')) {
            validationErrors.productId = '商品不存在，请检查商品ID';
            error.value = '关联的商品不存在，请检查商品ID';
            
            // 滚动到商品ID输入框
            const productIdField = document.getElementById('adProductId');
            if (productIdField) {
              productIdField.scrollIntoView({ behavior: 'smooth', block: 'center' });
              productIdField.focus();
            }
          } else {
            // 其他错误情况
            error.value = response.msg || '操作失败，请稍后重试';
          }
          emit('error', error.value);
        }
      } catch (err) {
        // 异常处理
        console.error('表单提交异常:', err);
        error.value = '操作失败，请稍后重试';
        emit('error', error.value);
      } finally {
        // 恢复提交状态
        submitting.value = false;
      }
    };

    // 取消操作
    const cancel = () => {
      emit('cancel');
    };

    return {
      form,
      error,
      submitting,
      validationErrors,
      productValidated,
      imagePreviewError,
      productData,
      productImageError,
      submitForm,
      cancel,
      clearValidationError,
      handleImageUrlChange,
      handleImagePreviewError,
      handleProductImageError,
      handleProductIdChange
    };
  }
};
</script>

<style scoped>
/* 主容器样式 */
.ad-form-container {
  max-width: 900px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 0;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
}

.ad-form-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  z-index: 0;
}

/* 表单头部 */
.form-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 40px 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
}

.form-icon {
  font-size: 48px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.form-title {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #2c3e50, #3498db);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 16px;
  color: #64748b;
  margin: 0;
  font-weight: 400;
  line-height: 1.5;
}

/* 表单内容区域 */
.ad-form {
  background: white;
  padding: 40px 30px;
  position: relative;
  z-index: 1;
}

/* 错误提示 */
.form-error {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  border: 1px solid #fca5a5;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 30px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.error-icon {
  font-size: 24px;
  margin-top: 2px;
}

.error-content {
  flex: 1;
}

.error-title {
  font-size: 16px;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 4px;
}

.error-message {
  font-size: 14px;
  color: #991b1b;
  line-height: 1.4;
}

/* 表单区块 */
.form-section {
  margin-bottom: 40px;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-radius: 16px;
  padding: 30px;
  border: 1px solid #e2e8f0;
  position: relative;
  transition: all 0.3s ease;
}

.form-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  gap: 12px;
}

.section-icon {
  font-size: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.section-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(to right, #e2e8f0, transparent);
  border-radius: 2px;
}

/* 表单组 */
.form-group {
  margin-bottom: 25px;
}

.modern-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.label-icon {
  font-size: 18px;
}

.label-text {
  flex: 1;
}

.required {
  color: #ef4444;
  font-weight: 700;
  font-size: 18px;
}

/* 输入框包装器 */
.input-wrapper {
  position: relative;
}

/* 现代化输入框 */
.modern-input,
.modern-textarea {
  width: 100%;
  padding: 16px 20px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 16px;
  font-family: inherit;
  transition: all 0.3s ease;
  background: white;
  color: #1f2937;
  box-sizing: border-box;
}

.modern-input:focus,
.modern-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.modern-input::placeholder,
.modern-textarea::placeholder {
  color: #9ca3af;
  opacity: 1;
}

.modern-textarea {
  resize: vertical;
  min-height: 120px;
  line-height: 1.6;
}

.input-error {
  border-color: #ef4444 !important;
  background-color: #fef2f2;
  animation: shake 0.3s ease-in-out;
}

/* 验证消息 */
.validation-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #dc2626;
  font-size: 14px;
  margin-top: 8px;
  font-weight: 500;
}

.validation-success {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #059669;
  font-size: 14px;
  margin-top: 8px;
  font-weight: 500;
}

.success-icon {
  font-size: 16px;
}

.success-text {
  font-weight: 600;
}

/* 预览容器 */
.image-preview-container,
.product-preview-container {
  margin-top: 20px;
  border: 2px dashed #d1d5db;
  border-radius: 16px;
  overflow: hidden;
  background: white;
  transition: all 0.3s ease;
}

.image-preview-container:hover,
.product-preview-container:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.preview-header {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  padding: 12px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

/* 图片预览 */
.image-preview {
  padding: 20px;
  text-align: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.image-preview img:hover {
  transform: scale(1.02);
}

.image-preview-error {
  padding: 40px;
  text-align: center;
  color: #6b7280;
}

.error-icon-large {
  font-size: 48px;
  opacity: 0.5;
  margin-bottom: 12px;
}

.error-text {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.error-hint {
  font-size: 14px;
  opacity: 0.8;
}

/* 商品信息卡片 */
.product-info-card {
  padding: 25px;
}

.product-basic-info {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  min-width: 120px;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.info-value {
  flex: 1;
  font-size: 14px;
  color: #1f2937;
  font-weight: 600;
}

.info-value.price {
  color: #dc2626;
  font-size: 16px;
  font-weight: 700;
}

/* 商品图片区域 */
.product-image-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.image-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.product-image-wrapper {
  display: flex;
  justify-content: center;
}

.product-image {
  max-width: 200px;
  max-height: 150px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.product-image:hover {
  transform: scale(1.05);
}

.product-image-error {
  padding: 30px;
  text-align: center;
  color: #6b7280;
}

.no-product-image {
  padding: 30px;
  text-align: center;
  color: #9ca3af;
  background: #f9fafb;
  border-radius: 8px;
  margin-top: 20px;
}

.no-image-icon {
  font-size: 32px;
  opacity: 0.5;
  margin-bottom: 8px;
}

.no-image-text {
  font-size: 14px;
  font-style: italic;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #f1f5f9;
}

.btn-cancel,
.btn-submit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  min-width: 140px;
  justify-content: center;
}

.btn-cancel {
  background: #f8fafc;
  color: #64748b;
  border: 2px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-submit {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.btn-submit::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.btn-submit:hover::before {
  left: 100%;
}

.btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-submit:disabled::before {
  display: none;
}

.btn-icon {
  font-size: 18px;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 加载状态 */
.submitting {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-icon {
  font-size: 18px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ad-form-container {
    margin: 10px;
    border-radius: 16px;
  }
  
  .form-header {
    padding: 30px 20px;
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .form-title {
    font-size: 28px;
  }
  
  .ad-form {
    padding: 30px 20px;
  }
  
  .form-section {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column-reverse;
  }
  
  .btn-cancel,
  .btn-submit {
    width: 100%;
  }
  
  .modern-input,
  .modern-textarea {
    padding: 14px 16px;
  }
}

/* 动画效果 */
.form-section {
  animation: fadeInUp 0.6s ease-out;
}

.form-section:nth-child(2) {
  animation-delay: 0.1s;
}

.form-section:nth-child(3) {
  animation-delay: 0.2s;
}

.form-section:nth-child(4) {
  animation-delay: 0.3s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a67d8, #6b46c1);
}
</style> 