import request from './request'

export function getComplianceSettings() {
  return request.get('/compliance/settings')
}

export function updateComplianceSettings(data) {
  return request.put('/compliance/settings', data)
}

export function getAuditLogs() {
  return request.get('/compliance/audit-logs')
}
