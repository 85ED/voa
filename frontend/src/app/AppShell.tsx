import { Link, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import styles from './AppShell.module.css'
import { CotacoesPage } from '../features/cotacoes/CotacoesPage'
import { DashboardPage } from '../features/dashboard/DashboardPage'
import { fetchNotificacoesNaoLidasCount } from '../features/notificacoes/api'
import { OperacoesPage } from '../features/operacoes/OperacoesPage'
import { ParceirosPage } from '../features/parceiros/ParceirosPage'
import { PromocoesAdminPage } from '../features/promocoes/PromocoesAdminPage'

export function AppShell() {
  const location = useLocation()
  const notifQuery = useQuery({
    queryKey: ['notificacoes-count'],
    queryFn: fetchNotificacoesNaoLidasCount,
    refetchInterval: 60_000,
  })

  const page = (() => {
    if (location.pathname.startsWith('/cotacoes')) return <CotacoesPage />
    if (location.pathname.startsWith('/operacoes')) return <OperacoesPage />
    if (location.pathname.startsWith('/parceiros')) return <ParceirosPage />
    if (location.pathname.startsWith('/promocoes')) return <PromocoesAdminPage />
    if (location.pathname.startsWith('/dashboard')) return <DashboardPage />
    return <DashboardPage />
  })()

  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <strong>VOA</strong>
        <nav>
          <Link to="/dashboard">Painel</Link>
          <Link to="/solicitacoes">Solicitações</Link>
          <Link to="/cotacoes">Cotações</Link>
          <Link to="/operacoes">Operações</Link>
          <Link to="/parceiros">Parceiros</Link>
          <Link to="/promocoes">Promoções</Link>
          <span className={styles.badge} title="Notificações não lidas">
            {notifQuery.data ? `🔔 ${notifQuery.data}` : '🔔'}
          </span>
        </nav>
      </header>
      <main className={styles.main}>{page}</main>
    </div>
  )
}
