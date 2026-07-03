import request from '../utils/request'

export function getExamPaper(paperId) {
  return request.get(`/exam/paper/${paperId}`)
}

export function startExam(paperId) {
  return request.post('/exam/start', null, { params: { paperId } })
}

export function submitExam(data) {
  return request.post('/exam/submit', data)
}

export function getExamDetail(recordId) {
  return request.get(`/exam/detail/${recordId}`)
}
