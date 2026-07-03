import request from '../utils/request'

export function getQuestionPage(params) {
  return request.get('/question', { params })
}

export function getQuestionById(id) {
  return request.get(`/question/${id}`)
}

export function addQuestion(data) {
  return request.post('/question', data)
}

export function updateQuestion(data) {
  return request.put('/question', data)
}

export function deleteQuestion(id) {
  return request.delete(`/question/${id}`)
}

export function getCategoryList() {
  return request.get('/category')
}

export function addCategory(name) {
  return request.post('/category', null, { params: { name } })
}

export function deleteCategory(id) {
  return request.delete(`/category/${id}`)
}
