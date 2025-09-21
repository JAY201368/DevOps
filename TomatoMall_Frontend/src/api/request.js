import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.token = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    console.log(res)
    if (res.code !== 200 && res.code !== '200') {
      let errorMessage = res.msg || '请求失败'
      if (res.code === 401 || res.code === '401') {
        localStorage.removeItem('token')
      }
      
      ElMessage.error(errorMessage)
      return Promise.reject(new Error(errorMessage))
    }
    return res  // 返回完整的响应数据
  },
  error => {
    const errorMessage = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(errorMessage)
    return Promise.reject(new Error(errorMessage))
  }
)

export default request