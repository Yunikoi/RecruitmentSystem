import { createRouter, createWebHistory } from 'vue-router'
import { authState } from '../utils/auth'
import { getHomeRoute } from '../utils/auth'
import PositionList from '../views/PositionList.vue'
import PositionForm from '../views/PositionForm.vue'
import PositionDetail from '../views/PositionDetail.vue'
import ExcelImport from '../views/ExcelImport.vue'
import Statistics from '../views/Statistics.vue'
import PublicList from '../views/PublicList.vue'
import PublicDetail from '../views/PublicDetail.vue'
import Login from '../views/Login.vue'
import CandidateApplications from '../views/candidate/CandidateApplications.vue'
import CandidateApply from '../views/candidate/CandidateApply.vue'
import RecruiterPipeline from '../views/recruiter/RecruiterPipeline.vue'
import InterviewCollab from '../views/recruiter/InterviewCollab.vue'
import ManagementDashboard from '../views/management/ManagementDashboard.vue'

const routes = [
  { path: '/', redirect: '/public' },
  { path: '/login', name: 'Login', component: Login, meta: { guest: true } },
  { path: '/public', name: 'PublicList', component: PublicList, meta: { public: true } },
  { path: '/public/:id', name: 'PublicDetail', component: PublicDetail, meta: { public: true } },
  // C端
  { path: '/candidate/applications', component: CandidateApplications, meta: { role: 'CANDIDATE' } },
  { path: '/candidate/apply/:id', component: CandidateApply, meta: { role: 'CANDIDATE' } },
  // B端
  { path: '/recruiter/pipeline', component: RecruiterPipeline, meta: { role: ['ADMIN', 'INTERVIEWER'] } },
  { path: '/recruiter/collab/:id', component: InterviewCollab, meta: { role: ['ADMIN', 'INTERVIEWER'] } },
  { path: '/recruiter/talent-pool', component: RecruiterPipeline, meta: { role: ['ADMIN', 'INTERVIEWER'] } },
  { path: '/management/dashboard', component: ManagementDashboard, meta: { role: ['EXECUTIVE', 'ADMIN'] } },
  // 原有
  { path: '/positions', component: PositionList, meta: { role: ['ADMIN', 'DEPARTMENT'] } },
  { path: '/positions/create', component: PositionForm, meta: { role: 'DEPARTMENT' } },
  { path: '/positions/:id/edit', component: PositionForm, meta: { role: 'DEPARTMENT' } },
  { path: '/positions/:id', component: PositionDetail, meta: { role: ['ADMIN', 'DEPARTMENT'] } },
  { path: '/import', component: ExcelImport, meta: { role: 'DEPARTMENT' } },
  { path: '/statistics', component: Statistics, meta: { role: ['ADMIN', 'EXECUTIVE'] } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = authState.token
  const user = authState.user

  if (to.meta.public || to.meta.guest) {
    if (to.path === '/login' && token) { next(getHomeRoute(user?.role)); return }
    next(); return
  }

  if (!token) { next({ path: '/login', query: { redirect: to.fullPath } }); return }

  if (to.meta.role) {
    const allowed = Array.isArray(to.meta.role) ? to.meta.role : [to.meta.role]
    if (!allowed.includes(user?.role)) { next(getHomeRoute(user?.role)); return }
  }

  next()
})

export default router
