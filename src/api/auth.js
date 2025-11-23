import request from '../utils/request'

export function login(credentials) {
  return request.post('/auth/login', credentials)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function refreshToken() {
  return request.get('/auth/refresh')
}
