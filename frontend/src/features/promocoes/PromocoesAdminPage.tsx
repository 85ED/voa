import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
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
      <h1>Promoções (admin)</h1>
      <p className={styles.hint}>Itens ativos aparecem na landing pública.</p>
      <section className={styles.card}>
        <label>
          Título
          <input value={titulo} onChange={(e) => setTitulo(e.target.value)} />
        </label>
        <label>
          Descrição
          <textarea rows={2} value={descricao} onChange={(e) => setDescricao(e.target.value)} />
        </label>
        <button type="button" disabled={!titulo || createMutation.isPending} onClick={() => createMutation.mutate()}>
          Publicar promoção
        </button>
      </section>
      <ul>
        {promocoesQuery.data?.map((p) => (
          <li key={p.id}>
            {p.titulo} {p.ativo ? '✓' : '(inativa)'}
          </li>
        ))}
      </ul>
    </div>
  )
}
