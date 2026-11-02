import { Link } from 'react-router-dom'
import styles from './AppShell.module.css'

export function AppShell() {
  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <strong>VOA</strong>
        <nav>
          <Link to="/solicitacoes">Solicitações</Link>
        </nav>
      </header>
      <main className={styles.main}>
        <p>Painel autenticado — módulos comerciais em evolução (Etapa 3).</p>
      </main>
    </div>
  )
}
