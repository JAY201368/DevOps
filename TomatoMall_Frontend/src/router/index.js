import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import UserProfile from '../views/UserProfile.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import WishList from '../views/WishList.vue'
import Advertisement from '../views/Advertisement.vue'
import OrderList from '../views/OrderList.vue'
import CouponManagement from '../views/CouponManagement.vue'
import Home from '../views/Home.vue'
import BannerManagement from '../views/BannerManagement.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'  // 直接重定向到登录页面
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true }
  },
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList,
    meta: { requiresAuth: true }
  },
  {
    path: '/banner-products/:bannerId',
    name: 'BannerProducts',
    component: ProductList,
    meta: { requiresAuth: true }
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: ProductDetail,
    meta: { requiresAuth: true }
  },  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: { requiresAuth: true }
  },  {
    path: '/wishlist',
    name: 'WishList',
    component: WishList,
    meta: { 
      requiresAuth: true,
      requiresUser: true // 只允许普通用户访问
    }
  },
  {
    path: '/advertisements',
    name: 'Advertisement',
    component: Advertisement,
    meta: { 
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/ads-recommend',
    name: 'AdsRecommend',
    component: () => import('../views/AdsRecommend.vue'),
    meta: { 
      requiresAuth: true,
      requiresUser: true
    }
  },
  {
    path: '/payment/:orderId',
    name: 'Payment',
    component: () => import('../views/Payment.vue')
  },
  {
    path: '/payment/status',
    name: 'PaymentStatus',
    component: () => import('../views/PaymentStatus.vue')
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: OrderList,
    meta: { requiresAuth: true }
  },
  {
    path: '/coupons',
    name: 'CouponManagement',
    component: CouponManagement,
    meta: { 
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/user-coupons',
    name: 'UserCoupons',
    component: () => import('../views/UserCoupons.vue'),
    meta: { 
      requiresAuth: true,
      requiresUser: true
    }
  },
  {
    path: '/banners',
    name: 'BannerManagement',
    component: BannerManagement,
    meta: { 
      requiresAuth: true,
      requiresAdmin: true // 只允许管理员访问
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 如果已登录且尝试访问登录页，重定向到首页
  if (to.path === '/login' && token) {
    next('/home')
    return
  }
  
  // 检查角色权限
  if (to.meta.requiresAdmin && userRole !== 'admin') {
    next('/products') // 如果不是管理员，重定向到商品列表
    return
  }
  
  if (to.meta.requiresUser && userRole === 'admin') {
    next('/products') // 如果是管理员访问用户专属页面，重定向到商品列表
    return
  }
  
  next()
})

export default router