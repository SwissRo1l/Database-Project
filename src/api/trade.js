import request from '../utils/request'

export function createOrder(payload) {
  // Ensure userId is included so backend can attribute the order correctly
  const userId = Number(localStorage.getItem('userId'))
  const body = Object.assign({}, payload)
  if (!body.userId && userId) body.userId = userId
  return request.post('/trade/orders', body)
}

export function fetchOrders(params) {
  return request.get('/trade/orders', { params })
}

export function fetchPendingOrders(userId) {
  return request.get('/trade/pending', { params: { userId } })
}

export function cancelOrder(orderId) {
  const userId = Number(localStorage.getItem('userId'))
  return request.post('/trade/cancel', { orderId, userId })
}
