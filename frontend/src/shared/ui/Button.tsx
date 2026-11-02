import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { Link } from 'react-router-dom'
import styles from './ui.module.css'

type Variant = 'primary' | 'secondary' | 'ghost'
type Size = 'md' | 'sm'

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: Variant
  size?: Size
  children: ReactNode
}

function classes(variant: Variant, size: Size, className?: string) {
  return [styles.btn, styles[variant], size === 'sm' ? styles.sm : '', className]
    .filter(Boolean)
    .join(' ')
}

export function Button({ variant = 'primary', size = 'md', className, type = 'button', ...rest }: Props) {
  return <button type={type} className={classes(variant, size, className)} {...rest} />
}

export function ButtonLink({
  to,
  variant = 'primary',
  size = 'md',
  className,
  children,
}: {
  to: string
  variant?: Variant
  size?: Size
  className?: string
  children: ReactNode
}) {
  return (
    <Link to={to} className={classes(variant, size, className)}>
      {children}
    </Link>
  )
}
