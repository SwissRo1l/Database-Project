<<<<<<< HEAD
import request from '../utils/request'

export function fetchProfile(userId) {
  return request.get(`/users/${userId}`)
}

export function updateProfile(userId, data) {
  return request.put(`/users/${userId}`, data)
}
=======
import request from '../utils/request'

export function fetchProfile(userId) {
  return request.get(`/users/${userId}`)
}

export function updateProfile(userId, data) {
  return request.put(`/users/${userId}`, data)
}
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
