// 环境变量配置
const env = {
  // API基础URL
  VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  
  // API超时时间（毫秒）
  VITE_API_TIMEOUT: import.meta.env.VITE_API_TIMEOUT || 10000,
  
  // 缓存持续时间（毫秒）
  VITE_CACHE_DURATION: import.meta.env.VITE_CACHE_DURATION || 30000,
  
  // 上传文件大小限制（字节）
  VITE_MAX_UPLOAD_SIZE: import.meta.env.VITE_MAX_UPLOAD_SIZE || 5 * 1024 * 1024, // 默认5MB
  
  // 是否启用请求缓存
  VITE_ENABLE_REQUEST_CACHE: import.meta.env.VITE_ENABLE_REQUEST_CACHE !== 'false',
  
  // 是否启用跨域请求携带cookie
  VITE_WITH_CREDENTIALS: import.meta.env.VITE_WITH_CREDENTIALS === 'true',

  // AI聊天配置
  VITE_AI_MODEL: import.meta.env.VITE_AI_MODEL || 'Qwen/Qwen3-14B',
  VITE_AI_API_KEY: import.meta.env.VITE_AI_API_KEY || 'sk-szrfqqlzjbkbysppmurhkqjufcxuswzgoewuocxdmxlqjjfq',
  VITE_AI_AVATAR_IMG: import.meta.env.VITE_AI_AVATAR_IMG || '',

  // Live2D配置
  VITE_LIVE2D_ENABLED: import.meta.env.VITE_LIVE2D_ENABLED !== 'false',
  VITE_LIVE2D_MODEL_PATH: import.meta.env.VITE_LIVE2D_MODEL_PATH || './rabbit/psd_b.model3.json',
  VITE_LIVE2D_CANVAS_WIDTH: import.meta.env.VITE_LIVE2D_CANVAS_WIDTH || 280,
  VITE_LIVE2D_CANVAS_HEIGHT: import.meta.env.VITE_LIVE2D_CANVAS_HEIGHT || 350,
  VITE_LIVE2D_SHOW_TOOLBOX: import.meta.env.VITE_LIVE2D_SHOW_TOOLBOX !== 'false',
  VITE_LIVE2D_USE_CACHE: import.meta.env.VITE_LIVE2D_USE_CACHE !== 'false',
}

export default env 