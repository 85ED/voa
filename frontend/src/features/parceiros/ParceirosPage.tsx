import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { PageHeader } from '../../shared/ui/PageHeader'
import styles from './ParceirosPage.module.css'
import {
  createParceiro,
  criarAcessoParceiro,
  fetchParceiros,
  fetchSolicitacoesPorParceiro,
  updateParceiroSlug,
} from './api'

export function ParceirosPage() {
  const queryClient = useQueryClient()
  const [nome, setNome] = useState('')
  const [slug, setSlug] = useState('')
  const [selectedId, setSelectedId] = useState('')
  const [acessoEmail, setAcessoEmail] = useState('')
  const [acessoNome, setAcessoNome] = useState('')
  const [tempPassword, setTempPassword] = useState<string | null>(null)
  const [novoSlug, setNovoSlug] = useState('')
  const [linkCopiado, setLinkCopiado] = useState(false)

  const parceirosQuery = useQuery({ queryKey: ['parceiros'], queryFn: fetchParceiros })
  const solicitacoesQuery = useQuery({
    queryKey: ['parceiro-solicitacoes', selectedId],
    queryFn: () => fetchSolicitacoesPorParceiro(selectedId),
    enabled: Boolean(selectedId),
  })

  const createMutation = useMutation({
    mutationFn: () => createParceiro({ nome, slug: slug || undefined }),
    onSuccess: () => {
      setNome('')
      setSlug('')
      queryClient.invalidateQueries({ queryKey: ['parceiros'] })
    },
  })

  const slugMutation = useMutation({
    mutationFn: () => updateParceiroSlug(selectedId, novoSlug),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['parceiros'] }),
  })

  const acessoMutation = useMutation({
    mutationFn: () => criarAcessoParceiro(selectedId, acessoEmail, acessoNome),
    onSuccess: (data) => {
      setTempPassword(data.senhaTemporaria)
      setAcessoEmail('')
      setAcessoNome('')
    },
  })

  const selected = parceirosQuery.data?.find((p) => p.id === selectedId)

  return (
    <div className={styles.page}>
      <PageHeader
        title="Parceiros comerciais"
        subtitle="Consultores ou empresas parceiras (ex.: Thalita) recebem um link público. Pedidos feitos por esse link ficam vinculados ao parceiro. Pedidos do site ou da equipe JL aparecem em Solicitações com outra origem."
      />
      <section className={styles.card}>
        <h2>Cadastrar parceiro</h2>
        <p className={styles.hint}>Uso da equipe JL Turismo — não confundir com login da plataforma VOA.</p>
        <label>
          Nome
          <input value={nome} onChange={(e) => setNome(e.target.value)} />
        </label>
        <label>
          Slug público
          <input value={slug} onChange={(e) => setSlug(e.target.value)} placeholder="edlene-viagens" />
        </label>
        <button type="button" disabled={!nome || createMutation.isPending} onClick={() => createMutation.mutate()}>
          Salvar
        </button>
      </section>
      <section className={styles.card}>
        <h2>Cadastrados</h2>
        <ul>
          {parceirosQuery.data?.map((p) => (
            <li key={p.id}>
              <button type="button" className={styles.linkButton} onClick={() => setSelectedId(p.id)}>
                {p.nome} ({p.slug})
              </button>
            </li>
          ))}
        </ul>
      </section>
      {selected && (
        <section className={styles.card}>
          <h2>{selected.nome}</h2>
          <p>
            Link público:{' '}
            <a href={selected.publicLink} target="_blank" rel="noreferrer">
              {selected.publicLink}
            </a>
          </p>
          <button
            type="button"
            className={styles.buttonSecondary}
            onClick={async () => {
              await navigator.clipboard.writeText(selected.publicLink)
              setLinkCopiado(true)
              window.setTimeout(() => setLinkCopiado(false), 2500)
            }}
          >
            {linkCopiado ? 'Link copiado' : 'Copiar link'}
          </button>
          <label>
            Alterar slug do link
            <input value={novoSlug} onChange={(e) => setNovoSlug(e.target.value)} />
          </label>
          <button type="button" disabled={!novoSlug} onClick={() => slugMutation.mutate()}>
            Atualizar link
          </button>
          <h3>Acesso ao portal do parceiro</h3>
          <label>
            E-mail
            <input value={acessoEmail} onChange={(e) => setAcessoEmail(e.target.value)} />
          </label>
          <label>
            Nome
            <input value={acessoNome} onChange={(e) => setAcessoNome(e.target.value)} />
          </label>
          <button
            type="button"
            disabled={!acessoEmail || !acessoNome}
            onClick={() => acessoMutation.mutate()}
          >
            Criar acesso
          </button>
          {tempPassword && <p className={styles.hint}>Senha temporária: {tempPassword}</p>}
          <h3>Solicitações deste parceiro</h3>
          <ul>
            {solicitacoesQuery.data?.map((s) => (
              <li key={s.id}>
                {s.titulo} — {s.status}
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}
