import request from './request'

export function getApplications(params) {
  return request.get('/recruiter/applications', { params })
}

export function updateStage(id, data) {
  return request.put(`/recruiter/applications/${id}/stage`, data)
}

export function scheduleInterview(data) {
  return request.post('/recruiter/interviews', data)
}

export function getInterviews() {
  return request.get('/recruiter/interviews')
}

export function cancelInterview(id) {
  return request.post(`/recruiter/interviews/${id}/cancel`)
}

export function submitEvaluation(data) {
  return request.post('/recruiter/evaluations', data)
}

export function getEvaluations(applicationId) {
  return request.get(`/recruiter/applications/${applicationId}/evaluations`)
}

export function getTalentPool() {
  return request.get('/recruiter/talent-pool')
}

export function getResumeFile(applicationId) {
  return request.get(`/recruiter/applications/${applicationId}/resume-file`, { responseType: 'blob' })
}

export function getDuplicates() {
  return request.get('/recruiter/duplicates')
}

export function mergeDuplicates(data) {
  return request.post('/recruiter/duplicates/merge', data)
}

export function matchTalentPool(positionId) {
  return request.get('/recruiter/talent-pool/match', { params: { positionId } })
}

export function activateTalent(applicationId, positionId) {
  return request.post('/recruiter/talent-pool/activate', null, { params: { applicationId, positionId } })
}

export function getWorkflow(positionId) {
  return request.get(`/recruiter/positions/${positionId}/workflow`)
}

export function updateWorkflow(positionId, steps) {
  return request.put(`/recruiter/positions/${positionId}/workflow`, { steps })
}

export function createOffer(applicationId) {
  return request.post(`/recruiter/applications/${applicationId}/offer`)
}

export function startBackgroundCheck(applicationId) {
  return request.post(`/recruiter/applications/${applicationId}/background-check`)
}

export function getBackgroundCheck(applicationId) {
  return request.get(`/recruiter/applications/${applicationId}/background-check`)
}

export function getMeetingSummary(applicationId) {
  return request.get(`/recruiter/applications/${applicationId}/meeting-summary`)
}

export function getCollabCode(applicationId) {
  return request.get(`/recruiter/applications/${applicationId}/collab-code`)
}

export function saveCollabCode(applicationId, code) {
  return request.put(`/recruiter/applications/${applicationId}/collab-code`, { code })
}
