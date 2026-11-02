import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button'
import { TextField } from '../../shared/ui/TextField'
import { requestPasswordReset } from './authApi'
import { LoginAside } from './LoginAside'
import { LoginLayout } from './LoginLayout'
import layout from './LoginLayout.module.css'

const schema = z.object({
  email: z.string().email('E-mail inválido'),
})

type FormValues = z.infer<typeof schema>

export function ForgotPasswordPage() {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const mutation = useMutation({
    mutationFn: (values: FormValues) => requestPasswordReset(values.email),
  })

  const busy = isSubmitting || mutation.isPending

  return (
    <LoginLayout aside={<LoginAside />}>
      <form
        className={layout.formCard}
        noValidate
        onSubmit={handleSubmit(async (values) => {
          await mutation.mutateAsync(values)
        })}
      >
        <div className={layout.formHead}>
          <h1 className={layout.formTitle}>Recuperar senha</h1>
          <p className={layout.formSubtitle}>
            Informe seu e-mail. Se houver cadastro, enviaremos instruções para criar uma nova senha.
          </p>
        </div>
        <TextField
          label="E-mail"
          type="email"
          autoComplete="username"
          autoFocus
          error={errors.email?.message}
          {...register('email')}
        />
        {mutation.isError && (
          <p className={layout.formError} role="alert">
            Não foi possível processar o pedido. Tente novamente em instantes.
          </p>
        )}
        {mutation.isSuccess && (
          <p className={layout.formSuccess} role="status">
            {mutation.data.message}
          </p>
        )}
        {mutation.data?.devResetUrl ? (
          <div className={layout.devLink}>
            Ambiente local: use o link abaixo para redefinir a senha (e-mail ainda não configurado).
            <br />
            <a href={mutation.data.devResetUrl}>Abrir redefinição de senha</a>
          </div>
        ) : null}
        <Button type="submit" disabled={busy} className={layout.submit} aria-busy={busy}>
          {busy ? 'Enviando…' : 'Enviar instruções'}
        </Button>
        <div className={layout.formFooter}>
          <Link to="/login">Voltar ao login</Link>
          <Link to="/">Voltar ao site público</Link>
        </div>
      </form>
    </LoginLayout>
  )
}
