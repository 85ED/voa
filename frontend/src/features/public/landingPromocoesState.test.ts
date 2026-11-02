import { describe, expect, it } from 'vitest'
import { resolvePromocoesUiState } from './landingPromocoesState'

describe('resolvePromocoesUiState', () => {
  it('prioriza carregamento', () => {
    expect(resolvePromocoesUiState({ isLoading: true, isError: false, count: 0 })).toBe('loading')
  })

  it('expõe erro da API', () => {
    expect(resolvePromocoesUiState({ isLoading: false, isError: true, count: 0 })).toBe('error')
  })

  it('lista vazia elegível', () => {
    expect(resolvePromocoesUiState({ isLoading: false, isError: false, count: 0 })).toBe('empty')
  })

  it('lista com itens', () => {
    expect(resolvePromocoesUiState({ isLoading: false, isError: false, count: 3 })).toBe('ready')
  })
})
