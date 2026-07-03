import request from '../utils/request'

export function getMyScores(params) {
  return request.get('/score/mine', { params })
}

export function getRanking(params) {
  return request.get('/score/ranking', { params })
}

export function getAllScores(params) {
  return request.get('/score/all', { params })
}
