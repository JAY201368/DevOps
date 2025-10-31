import request from './request';

export function getOrders() {
  return request({
    url: '/api/orders',
    method: 'get',
    headers: {
      'Cache-Control': 'no-cache',
      'Pragma': 'no-cache'
    }
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
