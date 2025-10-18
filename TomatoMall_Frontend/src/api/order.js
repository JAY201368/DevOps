import request from '../utils/request';

export function getOrders() {
  return request({
    url: '/api/orders',
    method: 'get'
  })
}

export function pay(orderId) {
  return request({
    url: `/api/orders/${orderId}/pay`,
    method: 'post'
  })
}

export function cancelOrder(orderId) {
  return request({
    url: `/api/orders/${orderId}/cancel`,
    method: 'post'
  })
}
