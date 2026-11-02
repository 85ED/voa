import { api } from '../../shared/api/client'

export type StatusCotacao = 'RASCUNHO' | 'ENVIADA' | 'ACEITA' | 'RECUSADA' | 'EXPIRADA'

export type CotacaoSummary = {
  id: string
  solicitacaoId: string
  versao: number
  validade: string
  status: StatusCotacao
  totalBruto: number
  moeda: string
  criadoEm: string
}

export type CotacaoDetail = CotacaoSummary & {
  observacoes?: string
  itens: Array<{
    id: string
    tipo: string
    descricao: string
    valorUnitario: number
    quantidade: number
  }>
  alternativas: Array<{
    id: string
    rotulo: string
    resumo?: string
    valorTotal: number
  }>
}

export type Solicitacao = {
  id: string
  titulo: string
  destino?: string
  status: string
  clienteId: string
  canal?: 'INTERNO' | 'PUBLICO' | 'PARCEIRO_LINK'
  parceiroSlugSnapshot?: string | null
  parceiroId?: string | null
  origem?: string | null
}

export async function fetchSolicitacoes() {
  const { data } = await api.get<{ content: Solicitacao[] }>('/api/v1/solicitacoes', {
    params: { size: 50, sort: 'criadoEm,desc' },
  })
  return data.content
}

export async function fetchCotacoes(solicitacaoId: string) {
  const { data } = await api.get<CotacaoSummary[]>(
    `/api/v1/solicitacoes/${solicitacaoId}/cotacoes`,
  )
  return data
}

export async function fetchCotacao(id: string) {
  const { data } = await api.get<CotacaoDetail>(`/api/v1/cotacoes/${id}`)
  return data
}

export async function createCotacao(solicitacaoId: string, validade: string) {
  const { data } = await api.post<CotacaoDetail>(
    `/api/v1/solicitacoes/${solicitacaoId}/cotacoes`,
    { validade, copiarUltimaVersao: true },
  )
  return data
}

export async function addAlternativa(
  cotacaoId: string,
  payload: { rotulo: string; resumo: string; valorTotal: number; origem?: string; destino?: string },
) {
  const { data } = await api.post<CotacaoDetail>(`/api/v1/cotacoes/${cotacaoId}/alternativas`, payload)
  return data
}

export async function enviarCotacao(cotacaoId: string) {
  const { data } = await api.post<CotacaoDetail>(`/api/v1/cotacoes/${cotacaoId}/enviar`)
  return data
}

export async function aceitarCotacao(cotacaoId: string) {
  const { data } = await api.post<CotacaoDetail>(`/api/v1/cotacoes/${cotacaoId}/aceitar`)
  return data
}

export async function converterVenda(cotacaoId: string) {
  const { data } = await api.post(`/api/v1/cotacoes/${cotacaoId}/converter-venda`, null, {
    headers: { 'Idempotency-Key': crypto.randomUUID() },
  })
  return data
}
