import styles from './ui.module.css'

export function StatCard({ label, value, hint }: { label: string; value: number | string; hint?: string }) {
  return (
    <article className={styles.statCard}>
      <span className={styles.statLabel}>{label}</span>
      <strong className={styles.statValue}>{value}</strong>
      {hint ? <span className={styles.statHint}>{hint}</span> : null}
    </article>
  )
}
