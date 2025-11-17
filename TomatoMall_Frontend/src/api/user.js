import request from './request'

export function login(username, password) {
  return request({
    url: '/api/accounts/login',
    method: 'post',
    data: {
      username,
      password
    }
  })
}

export function register(data) {
  return request({
    url: '/api/accounts',
    method: 'post',
    data
  })
}

export function getUserInfo(username) {
  return request({
    url: `/api/accounts/${username}`,
    method: 'get'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/api/accounts',
    method: 'put',
    data
  })
}

export function getAllUsers() {
  return request({
    url: '/api/accounts/all',
    method: 'get'
  })
} 