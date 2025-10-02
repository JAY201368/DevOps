<template>
  <div class="dbtest-container">
    <h1>数据库连接测试</h1>
    
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <h2>数据库连接测试</h2>
        </div>
      </template>
      <el-button type="primary" @click="testConnection" :loading="loadingConnection">测试连接</el-button>
      <div v-if="connectionResult" class="result-box">
        <pre>{{ JSON.stringify(connectionResult, null, 2) }}</pre>
      </div>
    </el-card>
    
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <h2>查询用户测试</h2>
        </div>
      </template>
      <el-button type="primary" @click="testUsers" :loading="loadingUsers">查询用户</el-button>
      <div v-if="usersResult" class="result-box">
        <pre>{{ JSON.stringify(usersResult, null, 2) }}</pre>
      </div>
    </el-card>
    
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <h2>用户注册测试</h2>
        </div>
      </template>
      
      <el-form :model="registerForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="registerForm.name" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="registerForm.role">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="registerForm.telephone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" />
        </el-form-item>
        <el-form-item label="所在地">
          <el-input v-model="registerForm.location" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="testRegister" :loading="loadingRegister">注册测试</el-button>
        </el-form-item>
      </el-form>
      
      <div v-if="registerResult" class="result-box">
        <pre>{{ JSON.stringify(registerResult, null, 2) }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://db4free.net:8080/api'
})

// 连接测试
const loadingConnection = ref(false)
const connectionResult = ref(null)

const testConnection = async () => {
  loadingConnection.value = true
  try {
    const response = await api.get('/dbtest/connection')
    connectionResult.value = response.data
  } catch (error) {
    connectionResult.value = {
      status: 'error',
      message: '请求失败: ' + (error.response?.data?.message || error.message)
    }
  } finally {
    loadingConnection.value = false
  }
}

// 用户查询测试
const loadingUsers = ref(false)
const usersResult = ref(null)

const testUsers = async () => {
  loadingUsers.value = true
  try {
    const response = await api.get('/dbtest/users')
    usersResult.value = response.data
  } catch (error) {
    usersResult.value = {
      status: 'error',
      message: '请求失败: ' + (error.response?.data?.message || error.message)
    }
  } finally {
    loadingUsers.value = false
  }
}

// 注册测试
const loadingRegister = ref(false)
const registerResult = ref(null)
const registerForm = reactive({
  username: 'testuser' + Math.floor(Math.random() * 1000),
  password: 'testpassword',
  name: '测试用户',
  role: 'user',
  telephone: '13800138000',
  email: 'test@example.com',
  location: '北京'
})

const testRegister = async () => {
  loadingRegister.value = true
  try {
    const response = await api.post('/dbtest/register', registerForm)
    registerResult.value = response.data
  } catch (error) {
    registerResult.value = {
      status: 'error',
      message: '请求失败: ' + (error.response?.data?.message || error.message)
    }
  } finally {
    loadingRegister.value = false
  }
}
</script>

<style scoped>
.dbtest-container {
  padding: 20px;
}

.test-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-box {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: auto;
  max-height: 300px;
}
</style> 