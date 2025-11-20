<<<<<<< HEAD
import request from '../utils/request'

export function login(credentials) {
  return request.post('/auth/login', credentials)
}

export function logout() {
  return request.post('/auth/logout')
}

export function refreshToken() {
  return request.get('/auth/refresh')
}
=======
import request from '../utils/request'

export function login(credentials) {
  return request.post('/auth/login', credentials)
}

export function logout() {
  return request.post('/auth/logout')
}

export function refreshToken() {
  return request.get('/auth/refresh')
}
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
