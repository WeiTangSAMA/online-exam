import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useUserStore } from '../store/user'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：携带 token
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  error => {
    const status = error.response && error.response.status
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error('无权限访问')
    } else {
      ElMessage.error(error.response && error.response.data && error.response.data.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
