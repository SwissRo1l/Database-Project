import request from '../utils/request'

export function fetchNotifications(userId) {
  return request.get('/notifications', { params: { userId } })
}

export function markAsRead(id) {
  return request.post('/notifications/read', { id })
}

export function markAllAsRead(userId) {
  return request.post('/notifications/read-all', { userId })
}
