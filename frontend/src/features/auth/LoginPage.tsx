import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { useAuth } from './useAuth'
import styles from './LoginPage.module.css'

const schema = z.object({
  email: z.string().email('E-mail inválido'),
  password: z.string().min(8, 'Senha deve ter ao menos 8 caracteres'),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const { loginMutation } = useAuth()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  return (
    <div className={styles.page}>
      <form
        className={styles.card}
        onSubmit={handleSubmit((values) => loginMutation.mutateAsync(values))}
      >
        <h1>VOA</h1>
        <p className={styles.subtitle}>Gestão operacional JL Turismo</p>
        <label>
          E-mail
          <input type="email" autoComplete="username" {...register('email')} />
          {errors.email && <span className={styles.error}>{errors.email.message}</span>}
        </label>
        <label>
          Senha
          <input type="password" autoComplete="current-password" {...register('password')} />
          {errors.password && <span className={styles.error}>{errors.password.message}</span>}
        </label>
        {loginMutation.isError && (
          <p className={styles.error}>Não foi possível entrar. Verifique suas credenciais.</p>
        )}
        <button type="submit" disabled={isSubmitting || loginMutation.isPending}>
          Entrar
        </button>
      </form>
    </div>
  )
}
