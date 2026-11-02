import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import styles from './LandingPage.module.css'
import { fetchPublicPromocoes } from './publicApi'

export function LandingPage() {
  const promocoesQuery = useQuery({ queryKey: ['public-promocoes'], queryFn: fetchPublicPromocoes })

  return (
    <div className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.kicker}>JL Turismo</p>
          <h1>VOA — sua viagem começa aqui</h1>
          <p className={styles.lead}>
            Solicite uma cotação personalizada ou conheça as promoções ativas da agência.
          </p>
          <div className={styles.actions}>
            <Link className={styles.buttonPrimary} to="/solicitar-viagem">
              Solicitar viagem
            </Link>
            <Link className={styles.buttonGhost} to="/login">
              Área da agência
            </Link>
          </div>
        </div>
      </header>

      <section className={styles.section}>
        <h2>Promoções</h2>
        {promocoesQuery.isLoading ? (
          <p>Carregando promoções…</p>
        ) : promocoesQuery.data && promocoesQuery.data.length > 0 ? (
          <ul className={styles.promoGrid}>
            {promocoesQuery.data.map((p) => (
              <li key={p.id} className={styles.promoCard}>
                <h3>{p.titulo}</h3>
                {p.descricao && <p>{p.descricao}</p>}
                <Link className={styles.promoCta} to="/solicitar-viagem">
                  Pedir cotação desta viagem
                </Link>
              </li>
            ))}
          </ul>
        ) : (
          <p className={styles.muted}>Nenhuma promoção publicada no momento.</p>
        )}
      </section>
    </div>
  )
}
