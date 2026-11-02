import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '../../shared/ui/PageHeader'
import { fetchNotificacoes, marcarNotificacaoLida } from './api'
import styles from './NotificacoesPage.module.css'

export function NotificacoesPage() {
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['notificacoes'], queryFn: fetchNotificacoes })

  const marcarMutation = useMutation({
    mutationFn: marcarNotificacaoLida,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificacoes'] })
      queryClient.invalidateQueries({ queryKey: ['notificacoes-count'] })
    },
  })

  return (
    <div className={styles.page}>
      <PageHeader
        title="Notificações"
        subtitle="Avisos internos da operação (novas solicitações, mudanças de status e eventos da agência)."
      />
      {query.isLoading ? (
        <p className={styles.muted}>Carregando…</p>
      ) : query.data && query.data.length > 0 ? (
        <ul className={styles.list}>
          {query.data.map((n) => (
            <li key={n.id} className={n.lida ? styles.lida : styles.nova}>
              <span>{n.titulo}</span>
              {!n.lida ? (
                <button type="button" onClick={() => marcarMutation.mutate(n.id)}>
                  Marcar como lida
                </button>
              ) : (
                <span className={styles.muted}>Lida</span>
              )}
            </li>
          ))}
        </ul>
      ) : (
        <p className={styles.muted}>Nenhuma notificação no momento.</p>
      )}
    </div>
  )
}
