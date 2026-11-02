export type PromocoesUiState = 'loading' | 'error' | 'empty' | 'ready'

export function resolvePromocoesUiState(input: {
  isLoading: boolean
  isError: boolean
  count: number
}): PromocoesUiState {
  if (input.isLoading) return 'loading'
  if (input.isError) return 'error'
  if (input.count <= 0) return 'empty'
  return 'ready'
}
