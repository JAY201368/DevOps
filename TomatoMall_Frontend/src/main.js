import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './style.css'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 配置了 Element Plus
app.use(ElementPlus, {
  size: 'default',
  zIndex: 3000,
  message: {
    duration: 500 // 设置消息提示的默认显示时长为 500 毫秒
  }
})

app.use(createPinia())
app.use(router)

console.log("mode:" + import.meta.env.MODE);

app.mount('#app')
