import request from '../utils/request'

export function getPaperPage(params) {
  return request.get('/paper', { params })
}

export function getPublishedPapers(params) {
  return request.get('/paper/published', { params })
}

export function getPaperById(id) {
  return request.get(`/paper/${id}`)
}

export function savePaper(data) {
  return request.post('/paper', data)
}

export function updatePaper(data) {
  return request.put('/paper', data)
}

export function bindQuestions(id, questionIds) {
  return request.post(`/paper/${id}/questions`, questionIds)
}

export function publishPaper(id) {
  return request.put(`/paper/${id}/publish`)
}

export function deletePaper(id) {
  return request.delete(`/paper/${id}`)
}
