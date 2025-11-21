<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 品牌标题 -->
      <div class="brand-section">
        <h1 class="brand-title">📚 番茄书城</h1>
        <p class="brand-subtitle">TomatoMall - 发现好书，享受阅读</p>
      </div>

      <!-- 注册表单 -->
      <div class="register-card">
        <div class="card-header">
          <h2 class="register-title">注册</h2>
          <p class="register-subtitle">创建您的新账号</p>
        </div>
        
        <el-form
          :model="registerForm"
          :rules="rules"
          ref="registerFormRef"
          class="register-form"
        >
          <el-form-item prop="username">
            <div class="input-wrapper">
              <label class="input-label">用户名</label>
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <label class="input-label">密码</label>
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                class="register-input"
                show-password
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>

          <el-form-item prop="name">
            <div class="input-wrapper">
              <label class="input-label">姓名</label>
              <el-input
                v-model="registerForm.name"
                placeholder="请输入真实姓名"
                size="large"
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Avatar /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>

          <el-form-item prop="role">
            <div class="input-wrapper">
              <label class="input-label">角色</label>
              <el-select
                v-model="registerForm.role"
                placeholder="请选择角色"
                size="large"
                class="register-select"
              >
                <el-option label="管理员" value="admin" />
                <el-option label="普通用户" value="user" />
              </el-select>
            </div>
          </el-form-item>

          <el-form-item prop="telephone">
            <div class="input-wrapper">
              <label class="input-label">手机号</label>
              <el-input
                v-model="registerForm.telephone"
                placeholder="请输入手机号"
                size="large"
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Phone /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>

          <el-form-item prop="email">
            <div class="input-wrapper">
              <label class="input-label">邮箱</label>
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                size="large"
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Message /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>

          <el-form-item prop="location">
            <div class="input-wrapper">
              <label class="input-label">所在地</label>
              <el-input
                v-model="registerForm.location"
                placeholder="请输入所在地"
                size="large"
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Location /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              @click="handleRegister"
              :loading="loading"
              class="register-button"
            >
              <span v-if="!loading">注册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <div class="login-link">
              已有账号？<router-link to="/login">立即登录</router-link>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Avatar, Phone, Message, Location } from "@element-plus/icons-vue";
import { register } from "../api/user";

const router = useRouter();
const registerFormRef = ref(null);
const loading = ref(false);

const registerForm = reactive({
  username: "",
  password: "",
  name: "",
  role: "",
  telephone: "",
  email: "",
  location: "",
});

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback();
  } else if (!/^1(3[0-9]|4[579]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[189])\d{8}$/.test(value)) {
    callback(new Error("手机号不合法, 请输入正确的手机号"));
  } else {
    callback();
  }
};

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback();
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error("请输入正确的邮箱地址"));
  } else {
    callback();
  }
};

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
  telephone: [{ validator: validatePhone, trigger: "blur" }],
  email: [{ validator: validateEmail, trigger: "blur" }],
  location: [{ required: false }],
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        console.log("开始注册，提交数据:", JSON.stringify(registerForm));
        const res = await register(registerForm);
        console.log("服务器响应:", res);
        
        if (res.code === 200 || res.code === "200") {
          ElMessage.success("注册成功");
          router.push("/login");
        } else {
          const errorMsg = res.msg || "注册失败";
          console.error("注册失败，错误信息:", errorMsg);
          ElMessage.error(errorMsg);
        }
      } catch (error) {
        console.error("注册请求异常:", error);
        ElMessage.error(error.message || "注册失败，请稍后重试");
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 主容器 */
.register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 520px;
}

/* 品牌区域 */
.brand-section {
  text-align: center;
  margin-bottom: 40px;
}

.brand-title {
  font-size: 36px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.brand-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
}

/* 注册卡片 */
.register-card {
  width: 100%;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 30px 15px;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 0 5px;
}

.register-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.register-subtitle {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0;
}

.register-form {
  width: 100%;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.input-wrapper {
  margin-bottom: 16px;
  width: 100%;
}

.input-label {
  display: block;
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 8px;
  padding: 0;
}

.register-input {
  width: 100%;
}

.register-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid #e1e8ed;
  padding: 12px 15px;
  transition: all 0.3s ease;
  background: #f8f9fa;
  min-height: 48px;
  width: 100%;
  box-sizing: border-box;
}

.register-input :deep(.el-input__inner) {
  font-size: 15px;
  height: auto;
  width: 100%;
}

.register-input :deep(.el-input__wrapper):hover {
  border-color: #667eea;
  background: white;
}

.register-input :deep(.el-input__wrapper.is-focus) {
  background: white;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.register-select {
  width: 100%;
}

.register-select :deep(.el-select__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid #e1e8ed;
  padding: 12px 15px;
  transition: all 0.3s ease;
  background: #f8f9fa;
  min-height: 48px;
  width: 100%;
  box-sizing: border-box;
}

.register-select :deep(.el-select__wrapper):hover {
  border-color: #667eea;
  background: white;
}

.register-select :deep(.el-select__wrapper.is-focused) {
  background: white;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.input-icon {
  font-size: 16px;
  color: #95a5a6;
  margin-right: 8px;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  margin-top: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s ease;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

.login-link {
  font-size: 14px;
  color: #7f8c8d;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-link a:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    max-width: 100%;
  }

  .brand-title {
    font-size: 30px;
  }

  .register-card {
    padding: 25px 10px;
  }
}

@media (max-width: 480px) {
  .register-page {
    padding: 10px;
  }

  .brand-section {
    margin-bottom: 30px;
  }

  .register-card {
    padding: 20px 8px;
  }
}
</style>
