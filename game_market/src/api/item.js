<<<<<<< HEAD
import request from '../utils/request'

export function fetchItem(id) {
  return request.get(`/items/${id}`)
}

export function searchItems(query) {
  return request.get('/items/search', { params: { q: query } })
}
=======
import request from '../utils/request'

export function fetchItem(id) {
  return request.get(`/items/${id}`)
}

export function searchItems(query) {
  return request.get('/items/search', { params: { q: query } })
}
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
