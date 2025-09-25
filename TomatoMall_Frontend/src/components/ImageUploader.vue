<template>
  <div class="image-uploader">
    <div v-if="!imageUrl" class="upload-area" @click="triggerFileInput">
      <el-icon class="upload-icon"><Plus /></el-icon>
      <div class="upload-text">点击上传图片</div>
      <input
        type="file"
        ref="fileInput"
        style="display: none"
        accept="image/*"
        @change="handleFileChange"
      />
    </div>
    <div v-else class="image-preview">
      <img :src="imageUrl" class="preview-image" />
      <div class="image-actions">
        <el-button type="danger" size="small" @click="removeImage">
          删除
        </el-button>
      </div>
    </div>
    <el-progress
      v-if="uploadProgress > 0 && uploadProgress < 100"
      :percentage="uploadProgress"
      :format="format"
    />
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { uploadImage } from "../api/upload.js";

const props = defineProps({
  modelValue: {
    type: String,
    default: "",
  },
});

const emit = defineEmits([
  "update:modelValue",
  "upload-success",
  "upload-error",
]);

const fileInput = ref(null);
const imageUrl = ref(props.modelValue);
const uploadProgress = ref(0);

const format = (percentage) =>
  percentage === 100 ? "上传完成" : `${percentage}%`;

const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件类型
  if (!file.type.startsWith("image/")) {
    ElMessage.error("请选择图片文件");
    return;
  }

  // 验证文件大小（限制为5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过5MB");
    return;
  }

  try {
    uploadProgress.value = 30;
    const response = await uploadImage(file);
    uploadProgress.value = 100;

    imageUrl.value = response.data;
    emit("update:modelValue", response.data);
    emit("upload-success", response.data);

    ElMessage.success("上传成功");
  } catch (error) {
    uploadProgress.value = 0;
    emit("upload-error", error);
    ElMessage.error("上传失败：" + (error.message || "未知错误"));
  }

  // 清除input的value，这样相同文件可以重复上传
  event.target.value = "";
};

const removeImage = () => {
  imageUrl.value = "";
  emit("update:modelValue", "");
};
</script>

<style scoped>
.image-uploader {
  width: 100%;
  max-width: 360px;
}

.upload-area {
  width: 100%;
  height: 180px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: var(--el-color-primary);
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.upload-text {
  color: #8c939d;
  font-size: 14px;
}

.image-preview {
  width: 100%;
  position: relative;
  border-radius: 6px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: none;
  background: rgba(0, 0, 0, 0.45);
  padding: 8px;
  border-radius: 4px;
}

.image-preview:hover .image-actions {
  display: block;
}
</style>
