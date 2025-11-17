import request from './request'

// 获取所有促销券（管理员）
export function getAllCoupons() {
  return request({
    url: '/api/coupons',
    method: 'get'
  })
}

// 获取有效的促销券（用户）
export function getValidCoupons() {
  return request({
    url: '/api/coupons/valid',
    method: 'get'
  })
}

// 获取促销券详情
export function getCouponById(couponId) {
  return request({
    url: `/api/coupons/${couponId}`,
    method: 'get'
  })
}

// 创建促销券（管理员）
export function createCoupon(data) {
  return request({
    url: '/api/coupons',
    method: 'post',
    data
  })
}

// 更新促销券（管理员）
export function updateCoupon(couponId, data) {
  return request({
    url: `/api/coupons/${couponId}`,
    method: 'put',
    data
  })
}

// 删除促销券（管理员）
export function deleteCoupon(couponId) {
  return request({
    url: `/api/coupons/${couponId}`,
    method: 'delete'
  })
}

// 发放促销券（管理员）
export function distributeCoupons(data) {
  return request({
    url: '/api/coupons/distribute',
    method: 'post',
    data
  })
}

// 获取用户的促销券
export function getUserCoupons(userId) {
  return request({
    url: `/api/coupons/user/${userId}`,
    method: 'get'
  })
}

// 获取用户未使用的促销券
export function getUserUnusedCoupons(userId) {
  return request({
    url: `/api/coupons/user/${userId}/unused`,
    method: 'get'
  })
}

// 获取促销券发放记录（管理员）
export function getCouponDistributionLogs(couponId) {
  return request({
    url: `/api/coupons/${couponId}/distribution-logs`,
    method: 'get'
  })
}

// 获取管理员的促销券发放记录
export function getAdminCouponDistributionLogs(adminId) {
  return request({
    url: `/api/coupons/admin/${adminId}/distribution-logs`,
    method: 'get'
  })
} 