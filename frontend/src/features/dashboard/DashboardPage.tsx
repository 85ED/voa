import { useQuery } from '@tanstack/react-query'
import { fetchDashboard } from '../operacoes/api'
import { ButtonLink } from '../../shared/ui/Button'
import { EmptyState } from '../../shared/ui/EmptyState'
import { PageHeader } from '../../shared/ui/PageHeader'
import { StatCard } from '../../shared/ui/StatCard'
import styles from './DashboardPage.module.css'

export function DashboardPage() {
  const dashboardQuery = useQuery({ queryKey: ['dashboard'], queryFn: fetchDashboard })
  const dash = dashboardQuery.data

  return (
    <div className={styles.page}>
      <PageHeader
        title="Painel operacional"
        subtitle="Indicadores do fluxo comercial e financeiro da agência. Atualize vendas e emissões em Operações."
        actions={
          <ButtonLink to="/operacoes" variant="secondary" size="sm">
            Ver operações
          </ButtonLink>
        }
      />
      {dashboardQuery.isLoading ? (
        <p className={styles.muted}>Carregando indicadores…</p>
      ) : dash ? (
        <>
          <div className={styles.grid}>
            <StatCard label="Aguardando pagamento" value={dash.vendasAguardandoPagamento} hint="Vendas confirmadas" />
            <StatCard label="Pagamento parcial" value={dash.vendasPagamentoParcial} />
            <StatCard label="Emissão pendente" value={dash.emissaoPendente} hint="Aguardando ou em emissão" />
            <StatCard label="Bilhetes pendentes" value={dash.bilhetesPendentes} />
            <StatCard label="Aguardando envio" value={dash.bilhetesAguardandoEnvio} hint="Emitidos ao cliente" />
            <StatCard label="Pagamentos abertos" value={dash.pagamentosAbertos} />
            <StatCard label="Solicitações ativas" value={dash.solicitacoesAtivas} hint="Rascunho ou em cotação" />
          </div>
          <section className={styles.hintBox}>
            <strong>Próximos passos</strong>
            <p className={styles.hintText}>
              Revise solicitações, monte cotações e acompanhe emissões em operações.
            </p>
            <div className={styles.hintLinks}>
              <ButtonLink to="/solicitacoes" variant="ghost" size="sm">
                Solicitações
              </ButtonLink>
              <ButtonLink to="/cotacoes" variant="ghost" size="sm">
                Cotações
              </ButtonLink>
              <ButtonLink to="/operacoes" variant="ghost" size="sm">
                Operações
              </ButtonLink>
            </div>
          </section>
        </>
      ) : (
        <EmptyState title="Não foi possível carregar o painel">
          <p className={styles.muted}>Verifique se a API está no ar e tente novamente.</p>
        </EmptyState>
      )}
    </div>
  )
}
