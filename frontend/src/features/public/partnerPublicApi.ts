import { publicApi } from './publicApi'

export type PublicParceiroProfile = {
  nomeExibicao: string
  subtitulo?: string
}

export async function fetchPublicParceiro(slug: string) {
  const { data } = await publicApi.get<PublicParceiroProfile>(`/api/v1/public/parceiros/${slug}`)
  return data
}

export async function submitPartnerSolicitacao(slug: string, payload: Record<string, unknown>) {
  const { data } = await publicApi.post<{ solicitacaoId: string; status: string }>(
    `/api/v1/public/parceiros/${slug}/solicitacoes`,
    payload,
  )
  return data
}
