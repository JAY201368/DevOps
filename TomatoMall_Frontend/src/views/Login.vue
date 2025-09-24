<template>
  <div class="login-container">
    <div class="live2d-container">
      <Live2D />
    </div>
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/user'
import Live2D from '../components/Live2D.vue'

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
        ElMessage.success('登录成功')
        // 保存token和用户名
        localStorage.setItem('token', res.data.token || res.data)
        localStorage.setItem('username', loginForm.username)
        
        // 设置登录状态为true
        if (appHeaderRef && appHeaderRef.value) {
          appHeaderRef.value.setLogined(true)
        }
        // 确保在设置登录状态后再跳转
        await router.push('/products')
      } catch (error) {
        // 错误已经在拦截器中处理
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
  position: relative;
}

.live2d-container {
  position: absolute;
  bottom: 0;
  right: 20px;
  z-index: 1;
  width: 400px;
  height: 500px;
}

.login-card {
  width: 400px;
  z-index: 2;
}

.register-link {
  text-align: center;
  margin-top: 20px;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
}
</style>