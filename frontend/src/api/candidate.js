import request from './request'

export function applyJob(data) {
  return request.post('/candidate/apply', data)
}

export function applyWithResume(positionId, file, channel) {
  const form = new FormData()
  form.append('positionId', positionId)
  form.append('file', file)
  if (channel) form.append('channel', channel)
  return request.post('/candidate/apply/upload', form)
}

export function parseResume(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/candidate/resume/parse', form)
}

export function analyzeResume(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/candidate/resume/analyze', form, { timeout: 120000 })
}

export function analyzeResumeText(text) {
  return request.post('/candidate/resume/analyze-text', { text }, { timeout: 120000 })
}

export function getMyApplications() {
  return request.get('/candidate/applications')
}

export function getMyInterviews() {
  return request.get('/candidate/interviews')
}

export function acceptInterview(id, note) {
  return request.post(`/candidate/interviews/${id}/accept`, note ? { note } : {})
}

export function declineInterview(id, note) {
  return request.post(`/candidate/interviews/${id}/decline`, { note })
}

export function rescheduleInterview(id, data) {
  return request.post(`/candidate/interviews/${id}/reschedule`, data)
}

export function getMyApplication(id) {
  return request.get(`/candidate/applications/${id}`)
}

export function getAiInterview(id) {
  return request.get(`/candidate/applications/${id}/ai-interview`)
}

export function startAiInterview(id) {
  return request.post(`/candidate/applications/${id}/ai-interview/start`)
}

export function replyAiInterview(id, data) {
  return request.post(`/candidate/applications/${id}/ai-interview/reply`, data)
}

export function askAi(positionId, question) {
  return request.post(`/candidate/positions/${positionId}/ask`, { question })
}

export function getMockInterviewStatus(id) {
  return request.get(`/candidate/applications/${id}/mock-interview`)
}

export function startMockInterview(id) {
  return request.post(`/candidate/applications/${id}/mock-interview/start`)
}

export function submitMockInterview(id, data) {
  return request.post(`/candidate/applications/${id}/mock-interview/submit`, data)
}

export function getCalendarSlots(id) {
  return request.get(`/candidate/applications/${id}/calendar-slots`)
}

export function bookCalendarSlot(id, slotId) {
  return request.post(`/candidate/applications/${id}/calendar-slots/${slotId}/book`)
}

export function signOffer(id) {
  return request.post(`/candidate/applications/${id}/offer/sign`)
}
