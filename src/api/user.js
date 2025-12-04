import request from '../utils/request'

export function fetchProfile(userId) {
  return request.get(`/users/${userId}`)
}

export function updateProfile(userId, data) {
  return request.put(`/users/${userId}`, data)
}

export function validatePassword(userId, currentPassword) {
  return request.post(`/users/${userId}/validate-password`, { currentPassword })
}

export function fetchInventory(userId) {
  return request.get(`/users/${userId}/inventory`)
}
