import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { fetchSolicitacoes, type Solicitacao } from '../cotacoes/api'
import { ButtonLink } from '../../shared/ui/Button'
import { EmptyState } from '../../shared/ui/EmptyState'
import { PageHeader } from '../../shared/ui/PageHeader'
import styles from './SolicitacoesPage.module.css'

function statusLabel(status: string) {
  const map: Record<string, string> = {
    RASCUNHO: 'Rascunho',
    EM_COTACAO: 'Em cotação',
    CANCELADA: 'Cancelada',
    CONCLUIDA: 'Concluída',
  }
  return map[status] ?? status
}

export function SolicitacoesPage() {
  const query = useQuery({ queryKey: ['solicitacoes'], queryFn: fetchSolicitacoes })

  return (
    <div className={styles.page}>
      <PageHeader
        title="Solicitações"
        subtitle="Pedidos de viagem em andamento. Abra cotações a partir de uma solicitação em cotação."
        actions={
          <ButtonLink to="/cotacoes" variant="secondary" size="sm">
            Ir para cotações
          </ButtonLink>
        }
      />
      {query.isLoading ? (
        <p className={styles.muted}>Carregando solicitações…</p>
      ) : query.data && query.data.length > 0 ? (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Título</th>
                <th>Destino</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {query.data.map((s: Solicitacao) => (
                <tr key={s.id}>
                  <td>{s.titulo}</td>
                  <td>{s.destino ?? '—'}</td>
                  <td>
                    <span className={styles.status}>{statusLabel(s.status)}</span>
                  </td>
                  <td className={styles.actions}>
                    {s.status === 'EM_COTACAO' ? (
                      <Link to="/cotacoes">Cotar</Link>
                    ) : (
                      <span className={styles.muted}>—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState title="Nenhuma solicitação cadastrada">
          <p className={styles.muted}>Use o formulário público ou cadastre via API para iniciar o fluxo comercial.</p>
        </EmptyState>
      )}
    </div>
  )
}
