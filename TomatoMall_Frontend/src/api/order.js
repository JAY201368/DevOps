import request from '../utils/request'

export function initiatePayment(orderId) {
  return request({
    url: `/api/orders/${orderId}/pay`,
    method: 'post'
  })
}
