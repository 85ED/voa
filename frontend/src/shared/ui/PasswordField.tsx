import { useState, type InputHTMLAttributes } from 'react'
import styles from './ui.module.css'

type Props = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & {
  label: string
  hint?: string
  error?: string
}

export function PasswordField({ label, hint, error, id, ...rest }: Props) {
  const [visible, setVisible] = useState(false)
  const inputId = id ?? rest.name

  return (
    <div className={styles.field}>
      <span className={styles.label} id={`${inputId}-label`}>
        {label}
      </span>
      <div className={styles.passwordWrap}>
        <input
          id={inputId}
          className={styles.input}
          type={visible ? 'text' : 'password'}
          aria-labelledby={`${inputId}-label`}
          aria-invalid={error ? true : undefined}
          {...rest}
        />
        <button
          type="button"
          className={styles.passwordToggle}
          onClick={() => setVisible((v) => !v)}
          aria-pressed={visible}
          aria-label={visible ? 'Ocultar senha' : 'Mostrar senha'}
        >
          {visible ? 'Ocultar' : 'Mostrar'}
        </button>
      </div>
      {error ? <span className={styles.error}>{error}</span> : hint ? <span className={styles.hint}>{hint}</span> : null}
    </div>
  )
}
