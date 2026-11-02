import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button'
import { PasswordField } from '../../shared/ui/PasswordField'
import { resetPassword } from './authApi'
import { LoginAside } from './LoginAside'
import { LoginLayout } from './LoginLayout'
import layout from './LoginLayout.module.css'

const schema = z
  .object({
    newPassword: z.string().min(8, 'Senha deve ter ao menos 8 caracteres'),
    confirmPassword: z.string().min(8, 'Confirme a senha'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'As senhas não coincidem',
    path: ['confirmPassword'],
  })

type FormValues = z.infer<typeof schema>

export function ResetPasswordPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const token = params.get('token') ?? ''

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const mutation = useMutation({
    mutationFn: (values: FormValues) => resetPassword(token, values.newPassword),
  })

  const busy = isSubmitting || mutation.isPending

  if (!token) {
    return (
      <LoginLayout aside={<LoginAside />}>
        <div className={layout.formCard}>
          <div className={layout.formHead}>
            <h1 className={layout.formTitle}>Link inválido</h1>
            <p className={layout.formSubtitle}>Solicite uma nova redefinição de senha.</p>
          </div>
          <Link to="/login/esqueci-senha">Recuperar senha</Link>
        </div>
      </LoginLayout>
    )
  }

  return (
    <LoginLayout aside={<LoginAside />}>
      <form
        className={layout.formCard}
        noValidate
        onSubmit={handleSubmit(async (values) => {
          await mutation.mutateAsync(values)
          navigate('/login', { replace: true, state: { passwordReset: true } })
        })}
      >
        <div className={layout.formHead}>
          <h1 className={layout.formTitle}>Nova senha</h1>
          <p className={layout.formSubtitle}>Escolha uma senha com ao menos 8 caracteres.</p>
        </div>
        <PasswordField
          label="Nova senha"
          autoComplete="new-password"
          autoFocus
          error={errors.newPassword?.message}
          {...register('newPassword')}
        />
        <PasswordField
          label="Confirmar senha"
          autoComplete="new-password"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />
        {mutation.isError && (
          <p className={layout.formError} role="alert">
            Não foi possível atualizar a senha. O link pode ter expirado — solicite outro.
          </p>
        )}
        <Button type="submit" disabled={busy} className={layout.submit} aria-busy={busy}>
          {busy ? 'Salvando…' : 'Salvar nova senha'}
        </Button>
        <div className={layout.formFooter}>
          <Link to="/login">Voltar ao login</Link>
        </div>
      </form>
    </LoginLayout>
  )
}
