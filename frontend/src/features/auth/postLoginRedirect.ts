const AGENCY_ROLES = ['ADMIN', 'COMERCIAL', 'OPERACOES', 'FINANCEIRO', 'AUDITOR'] as const

/** Destino pós-login conforme papéis retornados pelo backend. */
export function resolvePostLoginPath(roles: string[]): string {
  const set = new Set(roles)
  const hasAgencyRole = AGENCY_ROLES.some((role) => set.has(role))
  if (set.has('PARTNER') && !hasAgencyRole) {
    return '/parceiro'
  }
  return '/dashboard'
}
