import axios from 'axios';

// 创建一个简单的axios实例，不添加复杂的配置
const instance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
});

/**
 * 检查后端服务是否可访问
 * @returns {Promise<{status: boolean, message: string}>}
 */
export const checkBackendHealth = async () => {
  try {
    // 尝试访问后端API的根路径
    await instance.get('/', { validateStatus: () => true });
    return {
      status: true,
      message: '后端服务正常'
    };
  } catch (error) {
    console.error('后端健康检查失败:', error);
    
    let message = '无法连接到后端服务';
    if (error.code === 'ECONNABORTED') {
      message = '连接超时，服务器响应时间过长';
    } else if (error.response) {
      // 有响应但状态码不是200，API服务可能仍在运行
      message = `后端服务返回错误状态 (${error.response.status})`;
    } else if (error.request) {
      // 请求已发送但无响应，可能端口错误或服务未启动
      message = '后端服务未响应，请检查服务是否启动';
    }
    
    return {
      status: false,
      message
    };
  }
};

/**
 * 检查能否访问商品API
 * @returns {Promise<{status: boolean, message: string}>}
 */
export const checkProductsAPI = async () => {
  try {
    // 添加validateStatus选项，接受任何状态码作为成功响应
    await instance.get('/api/products', { validateStatus: () => true });
    return {
      status: true,
      message: '商品API正常'
    };
  } catch (error) {
    console.error('商品API检查失败:', error);
    return {
      status: false,
      message: '商品API不可访问'
    };
  }
}; 