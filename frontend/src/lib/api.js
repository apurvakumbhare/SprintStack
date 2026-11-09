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

export default api
