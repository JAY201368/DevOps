<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 品牌标题 -->
      <div class="brand-section">
        <h1 class="brand-title">📚 番茄书城</h1>
        <p class="brand-subtitle">TomatoMall - 发现好书，享受阅读</p>
      </div>

      <!-- 登录表单 -->
      <div class="login-card">
        <div class="card-header">
          <h2 class="login-title">登录</h2>
          <p class="login-subtitle">请输入您的账号信息</p>
        </div>
        
        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          class="login-form"
        >
          <el-form-item prop="username">
            <div class="input-wrapper">
              <label class="input-label">用户名</label>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                class="login-input"
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
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                class="login-input"
                show-password
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              @click="handleLogin"
              :loading="loading"
              class="login-button"
            >
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <div class="register-link">
              还没有账号？<router-link to="/register">立即注册</router-link>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock } from "@element-plus/icons-vue";
import { login, getUserInfo } from "../api/user";

const router = useRouter();
const loginFormRef = ref(null);
const loading = ref(false);
const appHeaderRef = inject("appHeaderRef");

const loginForm = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

// 组件挂载时将logined设置为false
onMounted(() => {
  if (appHeaderRef && appHeaderRef.value) {
    appHeaderRef.value.setLogined(false);
  }
});

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const res = await login(loginForm.username, loginForm.password);

        // 检查响应中是否有错误信息
        if (res.code !== "200" && res.code !== 200) {
          ElMessage.error("账号或密码错误");
          loading.value = false;
          return;
        }

        // 保存token和用户名
        localStorage.setItem("token", res.data.token || res.data);
        localStorage.setItem("username", loginForm.username);
        
        // 设置登录状态
        sessionStorage.setItem("logined", "true");

        // 获取用户信息，保存角色
        try {
          const userInfo = await getUserInfo(loginForm.username);
          if (userInfo && userInfo.data) {
            localStorage.setItem("userRole", userInfo.data.role);
            localStorage.setItem("userId", userInfo.data.id);
          }
        } catch (error) {
          console.error("获取用户信息失败", error);
        }

        ElMessage.success("登录成功");

        // 设置登录状态为true
        if (appHeaderRef && appHeaderRef.value) {
          appHeaderRef.value.setLogined(true);
        }

        // 触发自定义登录事件，通知Live2D组件用户已登录
        window.dispatchEvent(new CustomEvent("user-logged-in"));

        // 清除API缓存，确保获取最新数据
        import('../api/request').then(module => {
          module.clearCache();
        });

        // 确保在设置登录状态后再跳转
        await router.push("/home");
      } catch (error) {
        // 显示详细的错误信息
        console.error("登录失败:", error);
        ElMessage.error("账号或密码错误");
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 主容器 */
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 480px;
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

/* 登录卡片 */
.login-card {
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

.login-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.login-subtitle {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0;
}

.login-form {
  width: 100%;
}

.login-form :deep(.el-form-item) {
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

.login-input {
  width: 100%;
}

.login-input :deep(.el-input__wrapper) {
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

.login-input :deep(.el-input__inner) {
  font-size: 15px;
  height: auto;
  width: 100%;
}

.login-input :deep(.el-input__wrapper):hover {
  border-color: #667eea;
  background: white;
}

.login-input :deep(.el-input__wrapper.is-focus) {
  background: white;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.input-icon {
  font-size: 16px;
  color: #95a5a6;
  margin-right: 8px;
}

.login-button {
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

.login-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

.register-link {
  font-size: 14px;
  color: #7f8c8d;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    max-width: 100%;
  }

  .brand-title {
    font-size: 30px;
  }

  .login-card {
    padding: 25px 10px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 10px;
  }

  .brand-section {
    margin-bottom: 30px;
  }

  .login-card {
    padding: 20px 8px;
  }
}
</style>
