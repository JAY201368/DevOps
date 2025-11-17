import request from './request';
import axios from 'axios';
import env from '../config/env';

const BASE_URL = `${env.VITE_API_BASE_URL}/api/products`;

export const getAllProducts = async () => {
  try {
    const response = await axios.get(BASE_URL);
    return response.data;
  } catch (error) {
    console.error('获取商品列表失败', error);
    return { code: '500', msg: '获取商品列表失败' };
  }
};

export function getProductById(id) {
  return request({
    url: `/api/products/${id}`,
    method: 'get'
  });
}

export function createProduct(data) {
  return request({
    url: '/api/products',
    method: 'post',
    data
  });
}

export function updateProduct(data) {
  return request({
    url: '/api/products',
    method: 'put',
    data
  });
}

export function updateProductBasicInfo(data) {
  return request({
    url: '/api/products/basic',
    method: 'post',
    data
  });
}

export function updateProductBasicOnly(data) {
  return request({
    url: '/api/products/basic-only',
    method: 'post',
    data
  });
}

export function deleteProduct(id) {
  return request({
    url: `/api/products/${id}`,
    method: 'delete'
  });
}

export function updateStockpile(productId, amount) {
  return request({
    url: `/api/products/stockpile/${productId}`,
    method: 'patch',
    data: { amount }
  });
}

export function getStockpile(productId) {
  return request({
    url: `/api/products/stockpile/${productId}`,
    method: 'get'
  });
}

export function getProductDetails(id) {
  return request({
    url: `/api/products/${id}`,
    method: 'get',
    headers: {
      'Cache-Control': 'no-cache',
      'Pragma': 'no-cache'
    }
  });
} 