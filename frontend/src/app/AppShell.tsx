import { Link, useLocation } from 'react-router-dom'
import styles from './AppShell.module.css'
import { CotacoesPage } from '../features/cotacoes/CotacoesPage'

export function AppShell() {
  const location = useLocation()
  const isCotacoes = location.pathname.startsWith('/cotacoes')

  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <strong>VOA</strong>
        <nav>
          <Link to="/solicitacoes">Solicitações</Link>
          <Link to="/cotacoes">Cotações</Link>
        </nav>
      </header>
      <main className={styles.main}>
        {isCotacoes ? (
          <CotacoesPage />
        ) : (
          <p>Painel autenticado — solicitações e clientes via API (telas em evolução).</p>
        )}
      </main>
    </div>
  )
}
