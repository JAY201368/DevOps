import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // 计算属性：判断是否是管理员
  const isAdmin = computed(() => {
    // 首先检查store中的用户信息
    if (userInfo.value && userInfo.value.role) {
      return userInfo.value.role === 'admin'
    }
    
    // 如果store中没有，则从localStorage中获取
    const userRole = localStorage.getItem('userRole')
    return userRole === 'admin'
  })

  // 获取用户信息
  const fetchUserInfo = async (username) => {
    if (!username) {
      username = localStorage.getItem('username')
      if (!username) {
        error.value = '未找到用户名'
        return null
      }
    }

    loading.value = true
    error.value = null
    
    try {
      const res = await getUserInfo(username)
      if (res && res.data) {
        userInfo.value = res.data
        // 更新localStorage中的角色信息
        localStorage.setItem('userRole', res.data.role)
        localStorage.setItem('userId', res.data.id)
        return res.data
      } else {
        error.value = res.msg || '获取用户信息失败'
        return null
      }
    } catch (err) {
      console.error('获取用户信息失败:', err)
      error.value = '获取用户信息失败'
      return null
    } finally {
      loading.value = false
    }
  }

  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = null
  }

  return {
    userInfo,
    loading,
    error,
    isAdmin,
    fetchUserInfo,
    clearUserInfo
  }
}) 