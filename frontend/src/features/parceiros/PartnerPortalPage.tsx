import { useQuery } from '@tanstack/react-query'
import { api } from '../../shared/api/client'
import styles from './PartnerPortalPage.module.css'

async function fetchMinhasSolicitacoes() {
  const { data } = await api.get<{ content: Array<{ id: string; titulo: string; status: string; destino?: string }> }>(
    '/api/v1/solicitacoes',
    { params: { size: 30 } },
  )
  return data.content
}

export function PartnerPortalPage() {
  const query = useQuery({ queryKey: ['partner-solicitacoes'], queryFn: fetchMinhasSolicitacoes })

  return (
    <div className={styles.page}>
      <h1>Minhas solicitações</h1>
      <p className={styles.sub}>Área restrita do parceiro — apenas pedidos vinculados ao seu link.</p>
      {query.isLoading ? (
        <p>Carregando…</p>
      ) : (
        <ul>
          {query.data?.map((s) => (
            <li key={s.id}>
              {s.titulo} — {s.destino ?? '—'} — {s.status}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
