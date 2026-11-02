import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ButtonLink } from '../../shared/ui/Button'
import { EmptyState } from '../../shared/ui/EmptyState'
import { PublicShell } from '../../shared/ui/PublicShell'
import styles from './LandingPage.module.css'
import { fetchPublicPromocoes } from './publicApi'

function formatPeriodo(validoDe?: string, validoAte?: string) {
  if (!validoDe && !validoAte) return null
  const fmt = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })
  const de = validoDe ? fmt.format(new Date(`${validoDe}T12:00:00`)) : '—'
  const ate = validoAte ? fmt.format(new Date(`${validoAte}T12:00:00`)) : '—'
  return `Período demonstrativo: ${de} a ${ate}`
}

export function LandingPage() {
  const promocoesQuery = useQuery({ queryKey: ['public-promocoes'], queryFn: fetchPublicPromocoes })

  return (
    <PublicShell>
      <div className={styles.page}>
        <section className={styles.hero}>
          <div className={styles.heroInner}>
            <p className={styles.kicker}>JL Turismo</p>
            <h1>VOA — sua viagem começa aqui</h1>
            <p className={styles.lead}>
              Solicite uma cotação personalizada ou aproveite promoções selecionadas pela nossa equipe comercial.
            </p>
            <div className={styles.actions}>
              <ButtonLink to="/solicitar-viagem">Solicitar cotação</ButtonLink>
              <ButtonLink to="/login" variant="secondary">
                Área da agência
              </ButtonLink>
            </div>
          </div>
        </section>

        <section className={styles.section}>
          <div className={styles.sectionHead}>
            <h2>Promoções</h2>
            <p className={styles.sectionSub}>
              Ofertas publicadas pela agência. Valores de referência para demonstração; confirmação após cotação.
            </p>
          </div>
          {promocoesQuery.isLoading ? (
            <p className={styles.muted}>Carregando promoções…</p>
          ) : promocoesQuery.data && promocoesQuery.data.length > 0 ? (
            <ul className={styles.promoGrid}>
              {promocoesQuery.data.map((p) => {
                const periodo = formatPeriodo(p.validoDe, p.validoAte)
                return (
                <li key={p.id} className={styles.promoCard}>
                  <div className={styles.promoHead}>
                    <h3>{p.titulo}</h3>
                    <span className={styles.demoBadge}>Demonstração</span>
                  </div>
                  {periodo ? <p className={styles.promoMeta}>{periodo}</p> : null}
                  {p.descricao && <p className={styles.promoDesc}>{p.descricao}</p>}
                  <Link className={styles.promoCta} to="/solicitar-viagem">
                    Solicitar cotação desta oferta
                  </Link>
                </li>
              )})}
            </ul>
          ) : (
            <EmptyState title="Nenhuma promoção publicada no momento">
              <p className={styles.muted}>Volte em breve ou solicite uma cotação sob medida.</p>
            </EmptyState>
          )}
        </section>
      </div>
    </PublicShell>
  )
}
