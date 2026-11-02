import styles from './LoginFlowSteps.module.css'

const STEPS = [
  { title: 'Solicitação', detail: 'Pedidos de viagem e leads' },
  { title: 'Cotação', detail: 'Propostas e alternativas' },
  { title: 'Venda', detail: 'Pagamentos e confirmações' },
  { title: 'Emissão', detail: 'Bilhetes e entrega ao cliente' },
] as const

export function LoginAside() {
  return (
    <div className={styles.wrap}>
      <p className={styles.kicker}>JL Turismo</p>
      <h2 className={styles.title}>VOA centraliza a operação da sua agência.</h2>
      <p className={styles.lead}>
        Entre para acompanhar as atividades disponíveis no seu perfil — da solicitação de cotação ao
        acompanhamento de vendas e emissões.
      </p>
      <ol className={styles.flow} aria-label="Fluxo operacional VOA">
        {STEPS.map((step, index) => (
          <li key={step.title} className={styles.step}>
            <span className={styles.marker} aria-hidden>
              {index + 1}
            </span>
            <div>
              <strong>{step.title}</strong>
              <span>{step.detail}</span>
            </div>
          </li>
        ))}
      </ol>
    </div>
  )
}
