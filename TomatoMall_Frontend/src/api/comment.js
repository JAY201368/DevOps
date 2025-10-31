import request from './request';

// 获取商品评论列表
export function getComments(productId, page = 1, size = 10) {
  return request({
    url: '/api/comments/list',
    method: 'get',
    params: {
      productId,
      page,
      size
    }
  });
}

// 添加评论
export function addComment(userId, productId, content, rating) {
  return request({
    url: '/api/comments/add',
    method: 'post',
    data: {
      userId,
      productId,
      content,
      rating
    }
  });
}

// 删除评论
export function deleteComment(commentId, userId) {
  return request({
    url: `/api/comments/delete/${commentId}`,
    method: 'delete',
    params: {
      userId
    }
  });
}

// 检查用户购买状态
export function checkPurchaseStatus(userId, productId) {
  return request({
    url: '/api/comments/check-purchase',
    method: 'get',
    params: {
      userId,
      productId
    }
  });
} 