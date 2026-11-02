import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button'
import { PasswordField } from '../../shared/ui/PasswordField'
import { TextField } from '../../shared/ui/TextField'
import { LoginAside } from './LoginAside'
import { LoginLayout } from './LoginLayout'
import layout from './LoginLayout.module.css'
import { resolvePostLoginPath } from './postLoginRedirect'
import { useAuth } from './useAuth'

const schema = z.object({
  email: z.string().email('E-mail inválido'),
  password: z.string().min(8, 'Senha deve ter ao menos 8 caracteres'),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const passwordResetDone = (location.state as { passwordReset?: boolean } | null)?.passwordReset
  const { loginMutation } = useAuth()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const busy = isSubmitting || loginMutation.isPending

  return (
    <LoginLayout aside={<LoginAside />}>
      <form
        className={layout.formCard}
        noValidate
        onSubmit={handleSubmit(async (values) => {
          const result = await loginMutation.mutateAsync(values)
          navigate(resolvePostLoginPath(result.user.roles))
        })}
      >
        <div className={layout.formHead}>
          <h1 className={layout.formTitle}>Entrar</h1>
          <p className={layout.formSubtitle}>Use o e-mail cadastrado para acessar o VOA.</p>
        </div>
        <TextField
          label="E-mail"
          type="email"
          autoComplete="username"
          autoFocus
          error={errors.email?.message}
          {...register('email')}
        />
        <PasswordField
          label="Senha"
          autoComplete="current-password"
          error={errors.password?.message}
          {...register('password')}
        />
        {passwordResetDone ? (
          <p className={layout.formSuccess} role="status">
            Senha atualizada. Entre com a nova senha.
          </p>
        ) : null}
        {loginMutation.isError && (
          <p className={layout.formError} role="alert">
            Não foi possível entrar. Verifique e-mail e senha ou redefina sua senha.
          </p>
        )}
        <Button type="submit" disabled={busy} className={layout.submit} aria-busy={busy}>
          {busy ? 'Entrando…' : 'Entrar'}
        </Button>
        <div className={layout.formFooter}>
          <Link to="/login/esqueci-senha">Esqueci minha senha</Link>
          <Link to="/">Voltar ao site público</Link>
        </div>
      </form>
    </LoginLayout>
  )
}
