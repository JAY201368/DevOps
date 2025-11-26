import request from './request'

/**
 * 获取个性化推荐书籍
 * @param {number} limit - 推荐数量，默认9
 * @returns {Promise}
 */
export const getPersonalizedRecommendations = (limit = 9) => {
  return request({
    url: '/api/recommendations/personalized',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取热门推荐书籍
 * @param {number} limit - 推荐数量，默认9
 * @returns {Promise}
 */
export const getPopularRecommendations = (limit = 9) => {
  return request({
    url: '/api/recommendations/popular',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取基于愿望单的推荐书籍
 * @param {number} limit - 推荐数量，默认9
 * @returns {Promise}
 */
export const getWishListBasedRecommendations = (limit = 9) => {
  return request({
    url: '/api/recommendations/wishlist',
    method: 'get',
    params: { limit }
  })
} 