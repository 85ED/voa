import type { Solicitacao } from '../cotacoes/api'

export function solicitacaoOrigemLabel(s: Solicitacao): string {
  if (s.canal === 'PARCEIRO_LINK') {
    return s.parceiroSlugSnapshot ? `Parceiro (${s.parceiroSlugSnapshot})` : 'Parceiro'
  }
  if (s.canal === 'PUBLICO') {
    return 'Site público'
  }
  if (s.canal === 'INTERNO') {
    return 'Equipe JL Turismo'
  }
  return s.origem?.trim() || '—'
}

export function cotacaoStatusLabel(status: string): string {
  const map: Record<string, string> = {
    RASCUNHO: 'Rascunho',
    ENVIADA: 'Enviada ao cliente',
    ACEITA: 'Aceita',
    RECUSADA: 'Recusada',
    EXPIRADA: 'Expirada',
  }
  return map[status] ?? status
}
