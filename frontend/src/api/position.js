import request from './request'

export function getPositions(params) {
  return request.get('/positions', { params })
}

export function getPublishedPositions(params) {
  return request.get('/positions/published', { params })
}

export function getPublicPosition(id) {
  return request.get(`/public/positions/${id}`)
}

export function getPosition(id) {
  return request.get(`/positions/${id}`)
}

export function createPosition(data) {
  return request.post('/positions', data)
}

export function updatePosition(id, data) {
  return request.put(`/positions/${id}`, data)
}

export function deletePosition(id) {
  return request.delete(`/positions/${id}`)
}

export function submitPosition(id) {
  return request.post(`/positions/${id}/submit`)
}

export function approvePosition(id, data) {
  return request.post(`/positions/${id}/approve`, data)
}

export function rejectPosition(id, data) {
  return request.post(`/positions/${id}/reject`, data)
}

export function closePosition(id) {
  return request.post(`/positions/${id}/close`)
}

export function getStatistics() {
  return request.get('/positions/statistics')
}

export function downloadTemplate() {
  return request.get('/positions/template', { responseType: 'blob' })
}

export function importPositions(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/positions/import', formData)
}

export function generateJd(brief) {
  return request.post('/positions/copilot', { brief })
}

export function remindApproval(id) {
  return request.post(`/positions/${id}/remind`)
}

export function shareCandidate(id, data) {
  return request.post(`/positions/${id}/share`, data)
}
