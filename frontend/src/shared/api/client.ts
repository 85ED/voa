import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? ''

export const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

function readCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`))
  return match ? decodeURIComponent(match[1]) : null
}

api.interceptors.request.use((config) => {
  const csrf = readCookie('XSRF-TOKEN')
  if (csrf && config.method && !['get', 'head', 'options'].includes(config.method)) {
    config.headers['X-XSRF-TOKEN'] = csrf
  }
  return config
})

export type User = {
  id: string
  organizacaoId: string
  email: string
  nome: string
  roles: string[]
}

export async function login(email: string, password: string) {
  const { data } = await api.post<{ user: User; expiresIn: number }>('/api/v1/auth/login', {
    email,
    password,
  })
  return data
}

export async function fetchMe() {
  const { data } = await api.get<User>('/api/v1/auth/me')
  return data
}

export async function fetchHealth() {
  const { data } = await api.get<{ status: string }>('/actuator/health')
  return data
}
