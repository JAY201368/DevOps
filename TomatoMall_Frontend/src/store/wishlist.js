import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getWishList } from '../api/wishlist'

export const useWishListStore = defineStore('wishlist', () => {
  const wishlistCount = ref(0)
  const loading = ref(false)
  const wishlistItems = ref([]) // 存储愿望单所有商品ID
  const lastUpdated = ref(Date.now()) // 最后更新时间戳
  const fetchingData = ref(false) // 添加标记，防止重复请求

  const fetchWishListCount = async (forceRefresh = false) => {
    try {
      // 如果已经在请求中且不是强制刷新，则跳过
      if (fetchingData.value && !forceRefresh) {
        console.log('愿望单数据请求中，跳过此次刷新')
        return
      }
      
      // 如果不需要强制刷新且数据已存在，则不重复请求
      if (!forceRefresh && wishlistItems.value.length > 0) {
        console.log('使用缓存的愿望单数据:', wishlistItems.value)
        return
      }
      
      fetchingData.value = true
      loading.value = true
      console.log('从API获取愿望单数据...')
      const response = await getWishList()
      if (response.code === '200' && response.data) {
        wishlistCount.value = response.data.length
        // 同时更新愿望单商品列表
        wishlistItems.value = response.data.map(item => Number(item.bookId))
        console.log('愿望单数据已更新:', wishlistItems.value)
        
        // 更新时间戳
        lastUpdated.value = Date.now()
      }
    } catch (error) {
      console.error('获取愿望单数量失败:', error)
    } finally {
      loading.value = false
      // 设置延时，确保不会立即触发下一次请求
      setTimeout(() => {
        fetchingData.value = false
      }, 1000)
    }
  }
  
  // 检查商品是否在愿望单中
  const isInWishList = (bookId) => {
    const numericBookId = Number(bookId)
    return wishlistItems.value.includes(numericBookId)
  }
  
  // 添加商品到愿望单
  const addToWishList = (bookId) => {
    const numericBookId = Number(bookId)
    if (!isInWishList(numericBookId)) {
      wishlistItems.value.push(numericBookId)
      wishlistCount.value++
      lastUpdated.value = Date.now() // 更新时间戳
      console.log('添加到愿望单:', numericBookId, '当前愿望单:', wishlistItems.value)
      
      // 确保数据持久化
      localStorage.setItem('wishlist_last_updated', lastUpdated.value.toString())
      localStorage.setItem('wishlist_items', JSON.stringify(wishlistItems.value))
    }
  }
  
  // 从愿望单移除商品
  const removeFromWishList = (bookId) => {
    const numericBookId = Number(bookId)
    console.log('Store: 从愿望单移除商品:', numericBookId, '当前愿望单:', wishlistItems.value);
    
    // 确保bookId是有效的
    if (isNaN(numericBookId) || numericBookId <= 0) {
      console.error('Store: 无效的商品ID:', bookId);
      return;
    }
    
    const index = wishlistItems.value.indexOf(numericBookId)
    if (index !== -1) {
      wishlistItems.value.splice(index, 1)
      wishlistCount.value--
      lastUpdated.value = Date.now() // 更新时间戳
      console.log('Store: 从愿望单移除成功:', numericBookId, '当前愿望单:', wishlistItems.value)
      
      // 确保数据持久化
      localStorage.setItem('wishlist_last_updated', lastUpdated.value.toString())
      localStorage.setItem('wishlist_items', JSON.stringify(wishlistItems.value))
    } else {
      console.warn('Store: 商品不在愿望单中:', numericBookId);
    }
  }

  // 初始化函数，从localStorage恢复数据
  const initFromLocalStorage = () => {
    try {
      const storedItems = localStorage.getItem('wishlist_items')
      if (storedItems) {
        const parsedItems = JSON.parse(storedItems)
        if (Array.isArray(parsedItems) && parsedItems.length > 0) {
          wishlistItems.value = parsedItems.map(Number)
          wishlistCount.value = wishlistItems.value.length
          console.log('从本地存储恢复愿望单数据:', wishlistItems.value)
        }
      }
    } catch (error) {
      console.error('从本地存储恢复愿望单数据失败:', error)
    }
  }

  // 在store创建时初始化数据
  initFromLocalStorage()

  return {
    wishlistCount,
    wishlistItems,
    loading,
    lastUpdated,
    fetchWishListCount,
    isInWishList,
    addToWishList,
    removeFromWishList
  }
})
