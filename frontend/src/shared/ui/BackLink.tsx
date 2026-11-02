import { Link } from 'react-router-dom'
import { IconArrowLeft } from './IconArrowLeft'
import styles from './BackLink.module.css'

export function BackLink({ to = '/', label = 'Voltar' }: { to?: string; label?: string }) {
  return (
    <Link to={to} className={styles.back}>
      <IconArrowLeft className={styles.icon} />
      <span>{label}</span>
    </Link>
  )
}
