import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getWishList } from '../api/wishlist'

export const useWishListStore = defineStore('wishlist', () => {
  const wishlistCount = ref(0)
  const loading = ref(false)

  const fetchWishListCount = async () => {
    try {
      loading.value = true
      const response = await getWishList()
      if (response.code === '200' && response.data) {
        wishlistCount.value = response.data.length
      }
    } catch (error) {
      console.error('获取愿望单数量失败:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    wishlistCount,
    loading,
    fetchWishListCount
  }
})
