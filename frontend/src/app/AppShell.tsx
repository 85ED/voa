import { NavLink, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import styles from './AppShell.module.css'
import { CotacoesPage } from '../features/cotacoes/CotacoesPage'
import { DashboardPage } from '../features/dashboard/DashboardPage'
import { fetchNotificacoesNaoLidasCount } from '../features/notificacoes/api'
import { NotificacoesPage } from '../features/notificacoes/NotificacoesPage'
import { OperacoesPage } from '../features/operacoes/OperacoesPage'
import { ParceirosPage } from '../features/parceiros/ParceirosPage'
import { PromocoesAdminPage } from '../features/promocoes/PromocoesAdminPage'
import { SolicitacoesPage } from '../features/solicitacoes/SolicitacoesPage'
import { IconBell } from '../shared/ui/IconBell'

const navItems = [
  { to: '/dashboard', label: 'Painel', prefix: '/dashboard' },
  { to: '/solicitacoes', label: 'Solicitações', prefix: '/solicitacoes' },
  { to: '/cotacoes', label: 'Cotações', prefix: '/cotacoes' },
  { to: '/operacoes', label: 'Operações', prefix: '/operacoes' },
  { to: '/parceiros', label: 'Parceiros', prefix: '/parceiros' },
  { to: '/promocoes', label: 'Promoções', prefix: '/promocoes' },
] as const

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
    if (location.pathname.startsWith('/notificacoes')) return <NotificacoesPage />
    if (location.pathname.startsWith('/solicitacoes')) return <SolicitacoesPage />
    if (location.pathname.startsWith('/dashboard')) return <DashboardPage />
    return <DashboardPage />
  })()

  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <NavLink to="/dashboard" className={styles.brand}>
            <span className={styles.brandMark}>VOA</span>
            <span className={styles.brandSub}>JL Turismo</span>
          </NavLink>
          <nav className={styles.nav}>
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  [styles.navLink, isActive || location.pathname.startsWith(item.prefix) ? styles.navActive : '']
                    .filter(Boolean)
                    .join(' ')
                }
              >
                {item.label}
              </NavLink>
            ))}
            <NavLink
              to="/notificacoes"
              className={({ isActive }) =>
                [styles.notifLink, isActive || location.pathname.startsWith('/notificacoes') ? styles.notifActive : '']
                  .filter(Boolean)
                  .join(' ')
              }
              aria-label={
                notifQuery.data && notifQuery.data > 0
                  ? `Notificações, ${notifQuery.data} não lidas`
                  : 'Notificações'
              }
            >
              <IconBell className={styles.notifIcon} />
              {notifQuery.data && notifQuery.data > 0 ? (
                <span className={styles.notifCount}>{notifQuery.data}</span>
              ) : null}
            </NavLink>
          </nav>
        </div>
      </header>
      <main className={styles.main}>
        <div className={styles.mainInner}>{page}</div>
      </main>
    </div>
  )
}
