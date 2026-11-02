import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { ButtonLink } from './Button'
import styles from './PublicShell.module.css'

export function PublicShell({ children }: { children: ReactNode }) {
  return (
    <div className={styles.shell}>
      <header className={styles.top}>
        <div className={styles.topInner}>
          <Link to="/" className={styles.brand}>
            <span className={styles.brandMark}>VOA</span>
            <span className={styles.brandSub}>JL Turismo</span>
          </Link>
          <nav className={styles.nav}>
            <ButtonLink to="/solicitar-viagem" variant="secondary" size="sm">
              Solicitar viagem
            </ButtonLink>
            <ButtonLink to="/login" variant="primary" size="sm">
              Área da agência
            </ButtonLink>
          </nav>
        </div>
      </header>
      <main className={styles.main}>{children}</main>
      <footer className={styles.footer}>
        <div className={styles.footerInner}>Plataforma operacional JL Turismo — VOA</div>
      </footer>
    </div>
  )
}
