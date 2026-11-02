import { api } from '../../shared/api/client'

export type Parceiro = {
  id: string
  nome: string
  slug: string
  nomeComercial?: string
  subtitulo?: string
  email?: string
  telefone?: string
  percentualReferencia?: number
  ativo: boolean
  publicLink: string
}

export async function fetchParceiros() {
  const { data } = await api.get<{ content: Parceiro[] }>('/api/v1/parceiros', {
    params: { size: 50, sort: 'nome' },
  })
  return data.content
}

export async function createParceiro(body: {
  nome: string
  slug?: string
  nomeComercial?: string
  subtitulo?: string
}) {
  const { data } = await api.post<Parceiro>('/api/v1/parceiros', { ...body, ativo: true })
  return data
}

export async function updateParceiroSlug(id: string, slug: string) {
  const { data } = await api.patch<Parceiro>(`/api/v1/parceiros/${id}/slug`, { slug })
  return data
}

export async function criarAcessoParceiro(id: string, email: string, nome: string) {
  const { data } = await api.post<{ usuarioId: string; email: string; senhaTemporaria: string }>(
    `/api/v1/parceiros/${id}/acesso`,
    { email, nome },
  )
  return data
}

export async function fetchSolicitacoesPorParceiro(parceiroId: string) {
  const { data } = await api.get<{ content: Array<{ id: string; titulo: string; status: string }> }>(
    '/api/v1/solicitacoes',
    { params: { parceiroId, size: 20 } },
  )
  return data.content
}
