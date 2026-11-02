import type { ReactNode } from 'react'
import styles from './LoginLayout.module.css'

export function LoginLayout({ aside, children }: { aside: ReactNode; children: ReactNode }) {
  return (
    <div className={styles.page}>
      <aside className={styles.aside}>{aside}</aside>
      <div className={styles.formColumn}>{children}</div>
    </div>
  )
}
