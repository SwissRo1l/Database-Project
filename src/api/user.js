import request from '../utils/request'

export function fetchProfile(userId) {
  return request.get(`/users/${userId}`)
}

export function updateProfile(userId, data) {
  return request.put(`/users/${userId}`, data)
}
