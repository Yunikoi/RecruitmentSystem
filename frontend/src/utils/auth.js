import { reactive, readonly } from 'vue'

const TOKEN_KEY = 'recruitment_token'
const USER_KEY = 'recruitment_user'

function loadUser() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

const state = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: loadUser()
})

export const authState = readonly(state)

export function getToken() {
  return state.token
}

export function getUser() {
  return state.user
}

export function setAuth(token, user) {
  state.token = token
  state.user = user
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuth() {
  state.token = ''
  state.user = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function isAdmin() {
  return state.user?.role === 'ADMIN'
}

export function isDepartment() {
  return state.user?.role === 'DEPARTMENT'
}

export function isCandidate() {
  return state.user?.role === 'CANDIDATE'
}

export function isInterviewer() {
  return state.user?.role === 'INTERVIEWER'
}

export function isExecutive() {
  return state.user?.role === 'EXECUTIVE'
}

export function getRoleLabel(role) {
  const map = {
    ADMIN: '招聘HR',
    DEPARTMENT: '部门账号',
    CANDIDATE: '求职者',
    INTERVIEWER: '面试官',
    EXECUTIVE: '管理层'
  }
  return map[role] || role
}

export function getHomeRoute(role) {
  const map = {
    ADMIN: '/recruiter/pipeline',
    DEPARTMENT: '/positions',
    CANDIDATE: '/candidate/applications',
    INTERVIEWER: '/recruiter/pipeline',
    EXECUTIVE: '/management/dashboard'
  }
  return map[role] || '/public'
}

export function isLoggedIn() {
  return !!state.token
}
