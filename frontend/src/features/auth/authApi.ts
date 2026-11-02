import { api } from '../../shared/api/client'

export async function requestPasswordReset(email: string) {
  const { data } = await api.post<{ message: string; devResetUrl?: string }>(
    '/api/v1/auth/forgot-password',
    { email },
  )
  return data
}

export async function resetPassword(token: string, newPassword: string) {
  const { data } = await api.post<{ message: string }>('/api/v1/auth/reset-password', {
    token,
    newPassword,
  })
  return data
}
