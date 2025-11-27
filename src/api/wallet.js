import request from '../utils/request'

export function recharge(playerId, payload) {
  // payload: { amount: number }
  return request.post(`/wallet/${playerId}/recharge`, payload)
}

export default {
  recharge
}
