import request from './request';

/**
 * 添加商品到购物车
 * @param {Object} data - 包含productId和quantity
 * @returns {Promise} - 返回添加的商品信息
 */
export function addToCart(data) {
  return request({
    url: '/cart',
    method: 'post',
    data
  });
}

/**
 * 从购物车中删除商品
 * @param {number} cartItemId - 购物车商品ID
 * @returns {Promise} - 返回操作结果
 */
export function removeFromCart(cartItemId) {
  return request({
    url: `/cart/${cartItemId}`,
    method: 'delete'
  });
}

/**
 * 修改购物车商品数量
 * @param {number} cartItemId - 购物车商品ID
 * @param {number} quantity - 新数量
 * @returns {Promise} - 返回操作结果
 */
export function updateCartItemQuantity(cartItemId, quantity) {
  return request({
    url: `/cart/${cartItemId}`,
    method: 'patch',
    data: { quantity }
  });
}

/**
 * 获取购物车商品列表
 * @returns {Promise} - 返回购物车商品列表
 */
export function getCartItems() {
  return request({
    url: '/cart',
    method: 'get',
    headers: {
      'Cache-Control': 'no-cache' // 禁用缓存，确保获取最新数据
    }
  });
} 