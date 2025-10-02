import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.token = token
  }
  return config
})

export const login = (username, password) => {
  return api.post('/accounts/login', { username, password })
}

export const register = (userData) => {
  return api.post('/accounts', userData)
}

export const getUserInfo = (username) => {
  return api.get(`/accounts/${username}`)
}

export const updateUserInfo = (userData) => {
  return api.put('/accounts', userData)
} 