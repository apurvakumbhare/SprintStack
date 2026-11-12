import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('sprintstack.token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use((response) => response, (error) => {
  const path = error.config?.url || ''
  const isCredentialSubmission = ['/auth/login', '/auth/register', '/auth/password'].some(route => path.includes(route))
  const isOptionalProfilePhoto = ['/auth/photo'].some(route => path.includes(route))
  if (error.response?.status === 401 && !isCredentialSubmission && !isOptionalProfilePhoto && sessionStorage.getItem('sprintstack.token')) {
    sessionStorage.removeItem('sprintstack.token')
    sessionStorage.removeItem('sprintstack.email')
    window.dispatchEvent(new Event('sprintstack:session-expired'))
  }
  return Promise.reject(error)
})

export default api
