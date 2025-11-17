import request from './request'

/**
 * 获取所有轮播图
 * @returns {Promise}
 */
export const getAllBanners = () => {
  return request({
    url: '/api/banners',
    method: 'get'
  })
}

/**
 * 获取指定ID的轮播图
 * @param {number} id - 轮播图ID
 * @returns {Promise}
 */
export const getBannerById = (id) => {
  return request({
    url: `/api/banners/${id}`,
    method: 'get'
  })
}

/**
 * 创建轮播图（仅管理员）
 * @param {Object} banner - 轮播图数据
 * @returns {Promise}
 */
export const createBanner = (banner) => {
  return request({
    url: '/api/banners',
    method: 'post',
    data: banner
  })
}

/**
 * 更新轮播图（仅管理员）
 * @param {number} id - 轮播图ID
 * @param {Object} banner - 轮播图数据
 * @returns {Promise}
 */
export const updateBanner = (id, banner) => {
  return request({
    url: `/api/banners/${id}`,
    method: 'put',
    data: banner
  })
}

/**
 * 删除轮播图（仅管理员）
 * @param {number} id - 轮播图ID
 * @returns {Promise}
 */
export const deleteBanner = (id) => {
  return request({
    url: `/api/banners/${id}`,
    method: 'delete'
  })
}

/**
 * 更新轮播图的书籍（仅管理员）
 * @param {number} id - 轮播图ID
 * @param {Array<number>} productIds - 书籍ID列表
 * @returns {Promise}
 */
export const updateBannerBooks = (id, productIds) => {
  return request({
    url: `/api/banners/${id}/books`,
    method: 'put',
    data: { productIds }
  })
} 