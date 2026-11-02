import { api } from '../../shared/api/client'

export type PromocaoAdmin = {
  id: string
  titulo: string
  descricao?: string
  ativo: boolean
  destaque: boolean
}

export async function fetchPromocoesAdmin() {
  const { data } = await api.get<{ content: PromocaoAdmin[] }>('/api/v1/promocoes', {
    params: { size: 30 },
  })
  return data.content
}

export async function createPromocao(body: { titulo: string; descricao?: string; destaque?: boolean }) {
  const { data } = await api.post<PromocaoAdmin>('/api/v1/promocoes', { ...body, ativo: true })
  return data
}
