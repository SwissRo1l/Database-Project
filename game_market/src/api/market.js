<<<<<<< HEAD
import request from '../utils/request'

export function fetchListings(params) {
  return request.get('/market/listings', { params })
}

export function fetchCategories() {
  return request.get('/market/categories')
}
=======
import request from '../utils/request'

export function fetchListings(params) {
  return request.get('/market/listings', { params })
}

export function fetchCategories() {
  return request.get('/market/categories')
}
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
