import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, removeToken } from '@/utils/auth'
import { getLanguage } from '@/utils/lang'
import i18n from '@/i18n'

const t = (key) => i18n.global.t(key)

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) config.headers['token'] = token
    config.headers['Accept-Language'] = getLanguage()
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 1 && res.code !== 200) {
      ElMessage.error(res.msg || t('common.requestFailed'))
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || t('common.requestFailed')))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      ElMessage.error(t('common.sessionExpired'))
      removeToken()
      router.push('/login')
    } else {
      ElMessage.error(error.message || t('common.networkError'))
    }
    return Promise.reject(error)
  }
)

export default service
