import request from '../utils/request'

export function fetchListings(params) {
  return request.get('/market/listings', { params })
}

export function fetchCategories() {
  return request.get('/market/categories')
}
