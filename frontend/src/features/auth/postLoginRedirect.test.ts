import { describe, expect, it } from 'vitest'
import { resolvePostLoginPath } from './postLoginRedirect'

describe('resolvePostLoginPath', () => {
  it('envia parceiro exclusivo ao portal', () => {
    expect(resolvePostLoginPath(['PARTNER'])).toBe('/parceiro')
  })

  it('envia usuário da agência ao painel', () => {
    expect(resolvePostLoginPath(['ADMIN'])).toBe('/dashboard')
    expect(resolvePostLoginPath(['COMERCIAL'])).toBe('/dashboard')
  })

  it('prioriza painel quando há papéis mistos', () => {
    expect(resolvePostLoginPath(['PARTNER', 'COMERCIAL'])).toBe('/dashboard')
  })
})
