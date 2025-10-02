<template>
  <div class="login-page">
    <div class="login-container">
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <h2 class="login-title">欢迎登录</h2>
          </div>
        </template>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              size="large"
              class="login-input">
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码"
              size="large"
              class="login-input">
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin" 
              :loading="loading" 
              class="login-button">
              登录
            </el-button>
          </el-form-item>
          <div class="register-link">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login, getUserInfo } from '../api/user'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const appHeaderRef = inject('appHeaderRef')

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 组件挂载时将logined设置为false
onMounted(() => {
  if (appHeaderRef && appHeaderRef.value) {
    appHeaderRef.value.setLogined(false)
  }
})

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm.username, loginForm.password)
        
        // 检查响应中是否有错误信息
        if (res.code !== '200' && res.code !== 200) {
          ElMessage.error(res.msg || '登录失败，请检查用户名和密码');
          return;
        }
        
        // 保存token和用户名
        localStorage.setItem('token', res.data.token || res.data)
        localStorage.setItem('username', loginForm.username)
        
        // 获取用户信息，保存角色
        try {
          const userInfo = await getUserInfo(loginForm.username)
          if (userInfo && userInfo.data) {
            localStorage.setItem('userRole', userInfo.data.role)
          }
        } catch (error) {
          console.error('获取用户信息失败', error)
        }
        
        ElMessage.success('登录成功')
        
        // 设置登录状态为true
        if (appHeaderRef && appHeaderRef.value) {
          appHeaderRef.value.setLogined(true)
        }
        
        // 触发自定义登录事件，通知Live2D组件用户已登录
        window.dispatchEvent(new CustomEvent('user-logged-in'))
        
        // 确保在设置登录状态后再跳转
        await router.push('/products')
      } catch (error) {
        // 显示详细的错误信息
        console.error('登录失败:', error)
        const errorMsg = error.response?.data?.msg || error.message || '登录失败，请稍后再试'
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f9f9f9, #f0f2f5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
  position: relative;
  width: 100%;
  padding: 20px;
}

.login-card {
  width: 400px;
  z-index: 2;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.1);
  border: none;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  background: linear-gradient(to right, #3a8ee6, #53a8ff);
  color: white;
  text-align: center;
  padding: 20px;
  margin: -20px -20px 20px -20px;
}

.login-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.login-title::after {
  content: "";
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 3px;
}

.login-form {
  padding: 20px 30px;
}

.login-input {
  margin-bottom: 15px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 5px 15px;
  transition: all 0.3s;
}

.login-input :deep(.el-input__wrapper):hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff;
}

.input-icon {
  font-size: 18px;
  color: #909399;
  margin-right: 8px;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  border-radius: 8px;
  margin-top: 15px;
  background: linear-gradient(to right, #409eff, #53a8ff);
  border: none;
  transition: all 0.3s;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
}

.register-link {
  text-align: center;
  margin-top: 25px;
  font-size: 14px;
  color: #606266;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s;
}

.register-link a:hover {
  color: #66b1ff;
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    width: 100%;
  }
  
  .login-form {
    padding: 20px 15px;
  }
}
</style>