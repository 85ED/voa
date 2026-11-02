import { useQuery } from '@tanstack/react-query'
import { fetchDashboard } from '../operacoes/api'
import styles from './DashboardPage.module.css'

export function DashboardPage() {
  const dashboardQuery = useQuery({ queryKey: ['dashboard'], queryFn: fetchDashboard })
  const dash = dashboardQuery.data

  return (
    <div className={styles.page}>
      <h1>Painel operacional</h1>
      <p className={styles.sub}>Resumo em tempo real via `/api/v1/dashboard/resumo`.</p>
      {dashboardQuery.isLoading ? (
        <p>Carregando KPIs…</p>
      ) : dash ? (
        <div className={styles.grid}>
          <article className={styles.kpi}>
            <span className={styles.label}>Aguardando pagamento</span>
            <strong>{dash.vendasAguardandoPagamento}</strong>
          </article>
          <article className={styles.kpi}>
            <span className={styles.label}>Pagamento parcial</span>
            <strong>{dash.vendasPagamentoParcial}</strong>
          </article>
          <article className={styles.kpi}>
            <span className={styles.label}>Emissão pendente</span>
            <strong>{dash.emissaoPendente}</strong>
          </article>
          <article className={styles.kpi}>
            <span className={styles.label}>Bilhetes pendentes</span>
            <strong>{dash.bilhetesPendentes}</strong>
          </article>
          <article className={styles.kpi}>
            <span className={styles.label}>Aguardando envio</span>
            <strong>{dash.bilhetesAguardandoEnvio}</strong>
          </article>
          <article className={styles.kpi}>
            <span className={styles.label}>Pagamentos abertos</span>
            <strong>{dash.pagamentosAbertos}</strong>
          </article>
          <article className={styles.kpi}>
            <span className={styles.label}>Solicitações ativas</span>
            <strong>{dash.solicitacoesAtivas}</strong>
          </article>
        </div>
      ) : (
        <p>Não foi possível carregar o painel.</p>
      )}
    </div>
  )
}
