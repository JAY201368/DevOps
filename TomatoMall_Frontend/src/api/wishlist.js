import request from '../utils/request'

// 添加图书到愿望单
export const addToWishList = (bookId) => {
  return request({
    url: '/api/wishlist',
    method: 'post',
    data: { bookId }
  })
}

// 获取用户愿望单列表
export const getWishList = () => {
  return request({
    url: '/api/wishlist',
    method: 'get'
  })
}

// 从愿望单中删除图书
export const removeFromWishList = (bookId) => {
  return request({
    url: `/api/wishlist/${bookId}`,
    method: 'delete'
  })
}

// 检查图书是否在愿望单中
export const checkInWishList = (bookId) => {
  return request({
    url: `/api/wishlist/check/${bookId}`,
    method: 'get'
  })
}
