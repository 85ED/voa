import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { PageHeader } from '../../shared/ui/PageHeader'
import styles from './PromocoesAdminPage.module.css'
import { createPromocao, fetchPromocoesAdmin } from './api'

export function PromocoesAdminPage() {
  const queryClient = useQueryClient()
  const [titulo, setTitulo] = useState('')
  const [descricao, setDescricao] = useState('')
  const promocoesQuery = useQuery({ queryKey: ['promocoes-admin'], queryFn: fetchPromocoesAdmin })

  const createMutation = useMutation({
    mutationFn: () => createPromocao({ titulo, descricao, destaque: true }),
    onSuccess: () => {
      setTitulo('')
      setDescricao('')
      queryClient.invalidateQueries({ queryKey: ['promocoes-admin'] })
    },
  })

  return (
    <div className={styles.page}>
      <PageHeader
        title="Promoções da agência"
        subtitle="Publicação na landing pública. O modelo atual usa título e descrição (sem campo de preço separado — valores de referência podem ir no texto). Itens inativos não aparecem no site."
      />
      <section className={styles.card}>
        <label>
          Título
          <input value={titulo} onChange={(e) => setTitulo(e.target.value)} />
        </label>
        <label>
          Descrição
          <textarea rows={3} value={descricao} onChange={(e) => setDescricao(e.target.value)} />
        </label>
        <button type="button" disabled={!titulo || createMutation.isPending} onClick={() => createMutation.mutate()}>
          Publicar promoção
        </button>
      </section>
      <section className={styles.card}>
        <h2>Publicadas</h2>
        <ul className={styles.list}>
          {promocoesQuery.data?.map((p) => (
            <li key={p.id}>
              {p.titulo}{' '}
              <span className={styles.tag}>{p.ativo ? 'Ativa no site' : 'Inativa'}</span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
