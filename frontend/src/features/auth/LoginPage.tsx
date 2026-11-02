import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button'
import { TextField } from '../../shared/ui/TextField'
import { useAuth } from './useAuth'
import styles from './LoginPage.module.css'

const schema = z.object({
  email: z.string().email('E-mail inválido'),
  password: z.string().min(8, 'Senha deve ter ao menos 8 caracteres'),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const { loginMutation } = useAuth()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  return (
    <div className={styles.page}>
      <aside className={styles.aside}>
        <p className={styles.asideKicker}>JL Turismo</p>
        <h2 className={styles.asideTitle}>Operação, cotações e emissões em um só lugar.</h2>
        <p className={styles.asideText}>
          Acesse com seu usuário da agência para acompanhar solicitações, vendas e bilhetes.
        </p>
      </aside>
      <form
        className={styles.card}
        onSubmit={handleSubmit(async (values) => {
          const result = await loginMutation.mutateAsync(values)
          if (result.user.roles.includes('PARTNER')) {
            navigate('/parceiro')
          } else {
            navigate('/dashboard')
          }
        })}
      >
        <div className={styles.cardHead}>
          <h1 className={styles.title}>Entrar</h1>
          <p className={styles.subtitle}>Gestão operacional VOA</p>
        </div>
        <TextField
          label="E-mail"
          type="email"
          autoComplete="username"
          error={errors.email?.message}
          {...register('email')}
        />
        <TextField
          label="Senha"
          type="password"
          autoComplete="current-password"
          error={errors.password?.message}
          {...register('password')}
        />
        {loginMutation.isError && (
          <p className={styles.formError}>Não foi possível entrar. Verifique suas credenciais.</p>
        )}
        <Button type="submit" disabled={isSubmitting || loginMutation.isPending} className={styles.submit}>
          Entrar
        </Button>
      </form>
    </div>
  )
}
