<<<<<<< HEAD
import request from '../utils/request'

export function createOrder(payload) {
  return request.post('/trade/orders', payload)
}

export function fetchOrders(params) {
  return request.get('/trade/orders', { params })
}
=======
import request from '../utils/request'

export function createOrder(payload) {
  return request.post('/trade/orders', payload)
}

export function fetchOrders(params) {
  return request.get('/trade/orders', { params })
}
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
