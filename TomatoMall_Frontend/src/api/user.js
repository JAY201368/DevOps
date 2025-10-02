import request from './request'

export const login = (username, password) => {
  return request.post('/accounts/login', { username, password })
}

export const register = (userData) => {
  return request.post('/accounts', userData)
}

export const getUserInfo = (username) => {
  return request.get(`/accounts/${username}`)
}

export const updateUserInfo = (userData) => {
  return request.put('/accounts', userData)
} 