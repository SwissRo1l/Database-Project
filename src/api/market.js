import request from '../utils/request'

export function fetchListings(params) {
  return request.get('/market/listings', { params })
}

export function fetchCategories() {
  return request.get('/market/categories')
}

export function executeTrade(data) {
  return request.post('/market/trade', data)
}

export function fetchTradeHistory(itemId) {
  return request.get('/market/history', { params: { itemId } })
}

export function fetchDailyHistory(itemId) {
  return request.get('/market/history/daily', { params: { itemId } })
}

export function fetchHourlyHistory(itemId, hours) {
  const params = { itemId }
  if (hours) params.hours = hours
  return request.get('/market/history/hourly', { params })
}
