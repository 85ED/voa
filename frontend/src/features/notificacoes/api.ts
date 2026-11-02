import { api } from '../../shared/api/client'

export async function fetchNotificacoesNaoLidasCount() {
  const { data } = await api.get<{ count: number }>('/api/v1/notificacoes/nao-lidas/count')
  return data.count
}

export async function marcarNotificacaoLida(id: string) {
  await api.post(`/api/v1/notificacoes/${id}/marcar-lida`)
}

export async function fetchNotificacoes() {
  const { data } = await api.get<{ content: Array<{ id: string; titulo: string; lida: boolean }> }>(
    '/api/v1/notificacoes',
    { params: { size: 10 } },
  )
  return data.content
}
