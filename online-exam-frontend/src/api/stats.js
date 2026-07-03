import request from '../utils/request'

export function getStats(params) {
  return request.get('/stats', { params })
}
