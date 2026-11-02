import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? ''

export const publicApi = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

export type PromocaoPublic = {
  id: string
  titulo: string
  descricao?: string
  imagemUrl?: string
  slug: string
  destaque: boolean
  ativo?: boolean
  validoDe?: string
  validoAte?: string
}

export async function fetchPublicPromocoes() {
  const { data } = await publicApi.get<PromocaoPublic[]>('/api/v1/public/promocoes')
  return data
}

export async function submitPublicSolicitacao(payload: {
  nome: string
  email: string
  telefone?: string
  titulo: string
  destino: string
  dataIda?: string
  dataVolta?: string
  observacoes?: string
}) {
  const { data } = await publicApi.post<{ solicitacaoId: string; status: string }>(
    '/api/v1/public/solicitacoes-viagem',
    payload,
  )
  return data
}
