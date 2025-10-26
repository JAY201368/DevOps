import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建请求实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  // 启用HTTP持久连接
  keepAlive: true,
  // 允许跨域请求携带cookie
  withCredentials: false,
  // 添加HTTP请求头以优化性能
  headers: {
    'Cache-Control': 'max-age=60', // 启用缓存
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 启用简单请求缓存
const cache = new Map();
const CACHE_DURATION = 30 * 1000; // 30秒缓存

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.token = token
    }
    
    // 为购物车相关的请求禁用缓存
    if (config.url.includes('/cart')) {
      config.headers['Cache-Control'] = 'no-cache'
      config.headers['Pragma'] = 'no-cache'
      return config
    }
    
    // 只对GET请求使用缓存
    if (config.method.toLowerCase() === 'get') {
      const cacheKey = `${config.url}${JSON.stringify(config.params || {})}`;
      const cachedData = cache.get(cacheKey);
      
      // 检查是否有有效缓存
      if (cachedData && (Date.now() - cachedData.timestamp < CACHE_DURATION)) {
        // 将缓存标记附加到配置中，以便响应拦截器使用
        config._cachedData = cachedData.data;
        console.log('使用缓存数据:', config.url);
      }
    }
    
    console.log('发送请求:', config.method.toUpperCase(), config.url, config.data || config.params);
    return config;
  },
  error => {
    console.error('请求配置错误:', error);
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 检查是否有缓存数据
    if (response.config._cachedData) {
      console.log('返回缓存响应:', response.config.url);
      return response.config._cachedData;
    }
    
    const res = response.data;
    console.log("API响应:", response.config.url, res);
    
    // 缓存GET请求的成功响应，但排除购物车相关的请求
    if (response.config.method.toLowerCase() === 'get' && !response.config.url.includes('/cart')) {
      const cacheKey = `${response.config.url}${JSON.stringify(response.config.params || {})}`;
      cache.set(cacheKey, {
        data: res,
        timestamp: Date.now()
      });
    }
    
    // 直接返回响应数据，在业务代码中处理不同的响应结构
    return res;
  },
  error => {
    // 网络错误或服务器错误
    console.error("API请求错误:", error);
    
    if (error.code === 'ERR_NETWORK') {
      ElMessage.error('网络连接失败，请检查您的网络连接');
      return Promise.reject(error);
    }
    
    // 尝试提取错误信息
    let errorMessage;
    if (error.response) {
      // 服务器返回错误
      const responseData = error.response.data;
      
      console.error("服务器错误详情:", responseData);
      
      // 尝试解析不同格式的错误响应
      if (responseData) {
        if (responseData.msg) {
          errorMessage = responseData.msg;
        } else if (responseData.message) {
          errorMessage = responseData.message;
        } else if (typeof responseData === 'string') {
          errorMessage = responseData;
        } else {
          errorMessage = `服务器错误 (${error.response.status})`;
        }
      } else {
        errorMessage = `服务器错误 (${error.response.status})`;
      }
      
    } else if (error.request) {
      // 请求已发送但没有响应
      errorMessage = "服务器无响应，请检查网络连接";
    } else {
      // 请求设置时出错
      errorMessage = error.message || "请求错误";
    }
    
    // 显示错误消息
    ElMessage.error(errorMessage);
    return Promise.reject(error);
  }
)

export default request