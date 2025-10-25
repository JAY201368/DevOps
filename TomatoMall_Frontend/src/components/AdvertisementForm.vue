<template>
  <div class="ad-form-container">
    <h2>{{ isEdit ? '编辑广告' : '创建新广告' }}</h2>
    
    <div v-if="error" class="form-error">
      <i class="el-icon-warning-outline"></i>
      <span>{{ error }}</span>
    </div>

    <form @submit.prevent="submitForm" class="ad-form">
      <div class="form-group">
        <label for="adTitle">广告标题 <span class="required">*</span></label>
        <input 
          id="adTitle" 
          v-model="form.title" 
          type="text" 
          placeholder="输入引人注目的广告标题" 
          :class="{'input-error': validationErrors.title}"
          @input="clearValidationError('title')"
        />
        <span v-if="validationErrors.title" class="validation-error">{{ validationErrors.title }}</span>
      </div>

      <div class="form-group">
        <label for="adContent">广告内容 <span class="required">*</span></label>
        <textarea 
          id="adContent" 
          v-model="form.content" 
          placeholder="详细描述您的广告内容" 
          rows="4"
          :class="{'input-error': validationErrors.content}"
          @input="clearValidationError('content')"
        ></textarea>
        <span v-if="validationErrors.content" class="validation-error">{{ validationErrors.content }}</span>
      </div>

      <div class="form-group">
        <label for="adImgUrl">广告图片URL <span class="required">*</span></label>
        <div class="image-input-container">
          <input 
            id="adImgUrl" 
            v-model="form.imgUrl" 
            type="text" 
            placeholder="输入广告图片URL" 
            :class="{'input-error': validationErrors.imgUrl}"
            @input="handleImageUrlChange"
          />
          <button type="button" @click="previewImage" class="btn-preview-image" :disabled="!form.imgUrl">
            <i class="el-icon-view"></i>
          </button>
        </div>
        <span v-if="validationErrors.imgUrl" class="validation-error">{{ validationErrors.imgUrl }}</span>
        
        <div v-if="showImagePreview" class="image-preview">
          <img 
            v-if="!imagePreviewError" 
            :src="form.imgUrl" 
            alt="广告图片预览" 
            @error="handleImagePreviewError" 
          />
          <div v-else class="image-preview-error">
            <i class="el-icon-picture-outline"></i>
            <span>图片加载失败，请检查URL</span>
          </div>
        </div>
      </div>

      <div class="form-group">
        <label for="adProductId">关联商品ID <span class="required">*</span></label>
        <div class="product-input-container">
          <input 
            id="adProductId" 
            v-model="form.productId" 
            type="text" 
            placeholder="输入关联商品ID" 
            :class="{'input-error': validationErrors.productId}"
            @input="clearValidationError('productId')"
          />
          <button 
            type="button" 
            @click="validateProductId" 
            class="btn-validate-product"
            :disabled="!form.productId"
          >
            <i class="el-icon-check"></i>
          </button>
        </div>
        <span v-if="validationErrors.productId" class="validation-error">{{ validationErrors.productId }}</span>
        <span v-if="productValidated" class="validation-success">
          <i class="el-icon-success"></i> 商品ID验证成功
        </span>
      </div>

      <div class="form-actions">
        <button type="button" @click="cancel" class="btn-cancel">取消</button>
        <button type="submit" class="btn-submit" :disabled="submitting">
          <span v-if="!submitting">{{ isEdit ? '保存修改' : '创建广告' }}</span>
          <span v-else class="submitting">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
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
    const showImagePreview = ref(false);
    const imagePreviewError = ref(false);

    // 如果是编辑模式，填充表单数据
    onMounted(() => {
      if (props.isEdit && props.advertisement) {
        console.log('编辑模式，填充表单数据:', props.advertisement);
        form.title = props.advertisement.title || '';
        form.content = props.advertisement.content || '';
        form.imgUrl = props.advertisement.imgUrl || '';
        form.productId = props.advertisement.productId || '';
      }
    });

    // 图片URL变化时清除预览和验证
    const handleImageUrlChange = () => {
      clearValidationError('imgUrl');
      showImagePreview.value = false;
      imagePreviewError.value = false;
    };

    // 预览图片
    const previewImage = () => {
      if (!form.imgUrl) {
        validationErrors.imgUrl = '请输入图片URL';
        return;
      }

      imagePreviewError.value = false;
      showImagePreview.value = true;
    };

    // 处理图片预览加载错误
    const handleImagePreviewError = () => {
      console.log('图片预览加载失败');
      imagePreviewError.value = true;
    };

    // 验证商品ID
    const validateProductId = async () => {
      if (!form.productId) {
        validationErrors.productId = '请输入商品ID';
        return;
      }

      try {
        console.log('验证商品ID:', form.productId);
        const response = await getProductById(form.productId);
        console.log('验证商品ID响应:', response);
        
        if (response.code === '200' && response.data) {
          productValidated.value = true;
          clearValidationError('productId');
        } else {
          // 处理商品不存在的情况
          if (response.code === '400' && response.msg.includes('商品不存在')) {
            validationErrors.productId = '商品不存在，请检查商品ID';
          } else {
            validationErrors.productId = response.msg || '商品ID无效，请检查';
          }
          productValidated.value = false;
        }
      } catch (err) {
        console.error('验证商品ID错误:', err);
        validationErrors.productId = '验证商品ID失败，请稍后重试';
        productValidated.value = false;
      }
    };

    // 清除特定字段的验证错误
    const clearValidationError = (field) => {
      if (validationErrors[field]) {
        delete validationErrors[field];
      }
      if (field === 'productId') {
        productValidated.value = false;
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
      showImagePreview,
      imagePreviewError,
      submitForm,
      cancel,
      validateProductId,
      clearValidationError,
      previewImage,
      handleImageUrlChange,
      handleImagePreviewError
    };
  }
};
</script>

<style scoped>
.ad-form-container {
  max-width: 800px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background-color: #ffffff;
  padding: 20px;
  border-radius: 16px;
}

.ad-form-container h2 {
  color: #333;
  font-size: 28px;
  margin-bottom: 30px;
  text-align: center;
}

.form-error {
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-error i {
  font-size: 22px;
}

.ad-form {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

label {
  font-weight: 500;
  color: #333;
  font-size: 16px;
}

.required {
  color: #f56c6c;
  margin-left: 4px;
}

input, textarea {
  padding: 14px 18px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s;
  outline: none;
  background-color: #ffffff;
  color: #333333;
}

input:focus, textarea:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
}

textarea {
  resize: vertical;
  min-height: 120px;
}

.input-error {
  border-color: #f56c6c;
}

.validation-error {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 5px;
}

.validation-success {
  color: #67c23a;
  font-size: 14px;
  margin-top: 5px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.btn-cancel, .btn-submit {
  padding: 14px 28px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 16px;
  border: none;
}

.btn-cancel {
  background-color: #f5f7fa;
  color: #606266;
}

.btn-cancel:hover {
  background-color: #e9ecef;
}

.btn-submit {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 140px;
}

.btn-submit:hover {
  background-color: #66b1ff;
}

.btn-submit:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.image-input-container,
.product-input-container {
  display: flex;
  gap: 10px;
}

.image-input-container input,
.product-input-container input {
  flex: 1;
}

.btn-preview-image,
.btn-validate-product {
  padding: 14px 18px;
  border-radius: 8px;
  background-color: #ecf5ff;
  color: #409eff;
  border: 1px solid #d9ecff;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.btn-preview-image i,
.btn-validate-product i {
  font-size: 18px;
}

.btn-preview-image:hover,
.btn-validate-product:hover {
  background-color: #d9ecff;
  color: #409eff;
}

.btn-preview-image:disabled,
.btn-validate-product:disabled {
  background-color: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
  border-color: #e4e7ed;
}

.image-preview {
  margin-top: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.image-preview img {
  max-width: 100%;
  max-height: 300px;
  display: block;
}

.image-preview-error {
  padding: 40px;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.image-preview-error i {
  font-size: 48px;
  margin-bottom: 12px;
}

.submitting {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.dot {
  width: 8px;
  height: 8px;
  background-color: white;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

@media (max-width: 768px) {
  .ad-form-container {
    padding: 0 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn-cancel, .btn-submit {
    width: 100%;
  }
}
</style> 