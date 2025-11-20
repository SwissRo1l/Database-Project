import request from '../utils/request'

export function createOrder(payload) {
  return request.post('/trade/orders', payload)
}

export function fetchOrders(params) {
  return request.get('/trade/orders', { params })
}
