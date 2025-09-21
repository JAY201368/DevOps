import request from './request';

export function getAllProducts() {
  return request({
    url: '/products',
    method: 'get'
  });
}

export function getProductById(id) {
  return request({
    url: `/products/${id}`,
    method: 'get'
  });
}

export function createProduct(data) {
  return request({
    url: '/products',
    method: 'post',
    data
  });
}

export function updateProduct(data) {
  return request({
    url: '/products',
    method: 'put',
    data
  });
}

export function deleteProduct(id) {
  return request({
    url: `/products/${id}`,
    method: 'delete'
  });
}

export function updateStockpile(productId, amount) {
  return request({
    url: `/products/stockpile/${productId}`,
    method: 'patch',
    params: { amount }
  });
}

export function getStockpile(productId) {
  return request({
    url: `/products/stockpile/${productId}`,
    method: 'get'
  });
} 