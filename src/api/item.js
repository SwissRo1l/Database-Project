import request from '../utils/request'

export function fetchItem(id) {
  return request.get(`/items/${id}`)
}

export function searchItems(query) {
  return request.get('/items/search', { params: { q: query } })
}
