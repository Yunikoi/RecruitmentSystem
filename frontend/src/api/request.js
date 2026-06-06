import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '../utils/auth'
import router from '../router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/webapi',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // FormData 必须由浏览器自动设置 Content-Type（含 boundary），手动设置会导致上传失败
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
    config.timeout = 120000
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络错误'
    if (status === 401) {
      clearAuth()
      if (router.currentRoute.value.path !== '/login') {
        ElMessage.warning('登录已过期，请重新登录')
        router.push('/login')
      }
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
