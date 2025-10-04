import request from '../utils/request'

export function pay(orderId) {
  return request({
    url: `/api/orders/${orderId}/pay`,
    method: 'post'
  })
}
