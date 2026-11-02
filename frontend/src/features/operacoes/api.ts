import { api } from '../../shared/api/client'

export type DashboardResumo = {
  vendasAguardandoPagamento: number
  vendasPagamentoParcial: number
  emissaoPendente: number
  bilhetesPendentes: number
  bilhetesAguardandoEnvio: number
  pagamentosAbertos: number
  solicitacoesAtivas: number
}

export type VendaSummary = {
  id: string
  codigo: string
  status: string
  total: number
  moeda: string
  clienteId: string
}

export type PagamentoDetail = {
  id: string
  vendaId: string
  forma: string
  valorTotal: number
  status: string
  parcelas: Array<{
    id: string
    numero: number
    valor: number
    comprovanteRef?: string
    confirmadoEm?: string
  }>
}

export type BilheteView = {
  id: string
  vendaId: string
  status: string
  localizador?: string
  arquivoPath?: string
}

export async function fetchDashboard() {
  const { data } = await api.get<DashboardResumo>('/api/v1/dashboard/resumo')
  return data
}

export async function fetchVendas(status?: string) {
  const { data } = await api.get<{ content: VendaSummary[] }>('/api/v1/vendas', {
    params: { size: 30, sort: 'criadoEm,desc', status },
  })
  return data.content
}

export async function registrarPagamento(
  vendaId: string,
  payload: { forma: string; parcelas: Array<{ vencimento: string; valor: number }> },
) {
  const { data } = await api.post<PagamentoDetail>(`/api/v1/vendas/${vendaId}/pagamentos`, payload)
  return data
}

export async function confirmarParcela(parcelaId: string) {
  const { data } = await api.post<PagamentoDetail>(`/api/v1/parcelas/${parcelaId}/confirmar`)
  return data
}

export async function confirmarQuitacao(pagamentoId: string) {
  const { data } = await api.post<PagamentoDetail>(`/api/v1/pagamentos/${pagamentoId}/confirmar-quitacao`)
  return data
}

export async function criarBilhete(vendaId: string, localizador: string) {
  const { data } = await api.post<BilheteView>(`/api/v1/vendas/${vendaId}/bilhetes`, {
    tipo: 'AEREO',
    localizador,
  })
  return data
}

export async function atualizarBilhete(id: string, body: Record<string, unknown>) {
  const { data } = await api.patch<BilheteView>(`/api/v1/bilhetes/${id}`, body)
  return data
}

export async function fetchPagamentos(vendaId: string) {
  const { data } = await api.get<PagamentoDetail[]>(`/api/v1/vendas/${vendaId}/pagamentos`)
  return data
}

export async function fetchBilhetes(vendaId: string) {
  const { data } = await api.get<BilheteView[]>(`/api/v1/vendas/${vendaId}/bilhetes`)
  return data
}
