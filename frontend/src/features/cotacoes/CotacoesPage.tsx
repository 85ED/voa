import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import styles from './CotacoesPage.module.css'
import {
  aceitarCotacao,
  addAlternativa,
  converterVenda,
  createCotacao,
  enviarCotacao,
  fetchCotacao,
  fetchCotacoes,
  fetchSolicitacoes,
} from './api'

function defaultValidade() {
  const d = new Date()
  d.setDate(d.getDate() + 7)
  return d.toISOString().slice(0, 10)
}

export function CotacoesPage() {
  const queryClient = useQueryClient()
  const [solicitacaoId, setSolicitacaoId] = useState<string>('')
  const [cotacaoId, setCotacaoId] = useState<string>('')
  const [validade, setValidade] = useState(defaultValidade())
  const [altRotulo, setAltRotulo] = useState('Opção A')
  const [altValor, setAltValor] = useState('1500')
  const [message, setMessage] = useState<string | null>(null)

  const solicitacoesQuery = useQuery({
    queryKey: ['solicitacoes'],
    queryFn: fetchSolicitacoes,
  })

  const cotacoesQuery = useQuery({
    queryKey: ['cotacoes', solicitacaoId],
    queryFn: () => fetchCotacoes(solicitacaoId),
    enabled: Boolean(solicitacaoId),
  })

  const cotacaoQuery = useQuery({
    queryKey: ['cotacao', cotacaoId],
    queryFn: () => fetchCotacao(cotacaoId),
    enabled: Boolean(cotacaoId),
  })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['cotacoes', solicitacaoId] })
    if (cotacaoId) {
      queryClient.invalidateQueries({ queryKey: ['cotacao', cotacaoId] })
    }
  }

  const createMutation = useMutation({
    mutationFn: () => createCotacao(solicitacaoId, validade),
    onSuccess: (c) => {
      setCotacaoId(c.id)
      setMessage(`Cotação v${c.versao} criada`)
      invalidate()
    },
    onError: () => setMessage('Erro ao criar cotação'),
  })

  const altMutation = useMutation({
    mutationFn: () =>
      addAlternativa(cotacaoId, {
        rotulo: altRotulo,
        resumo: 'Alternativa de voo',
        valorTotal: Number(altValor),
        origem: 'GRU',
        destino: 'GIG',
      }),
    onSuccess: () => {
      setMessage('Alternativa adicionada')
      invalidate()
    },
  })

  const enviarMutation = useMutation({
    mutationFn: () => enviarCotacao(cotacaoId),
    onSuccess: () => {
      setMessage('Cotação enviada')
      invalidate()
    },
  })

  const aceitarMutation = useMutation({
    mutationFn: () => aceitarCotacao(cotacaoId),
    onSuccess: () => {
      setMessage('Cotação aceita')
      invalidate()
    },
  })

  const vendaMutation = useMutation({
    mutationFn: () => converterVenda(cotacaoId),
    onSuccess: () => setMessage('Venda ABERTA criada a partir da cotação'),
  })

  const cotacao = cotacaoQuery.data

  return (
    <div className={styles.page}>
      <h1>Cotações</h1>
      <p className={styles.lead}>Fluxo comercial mínimo: versão, alternativas, enviar/aceitar e converter venda.</p>

      <section className={styles.card}>
        <h2>Solicitação</h2>
        <select
          value={solicitacaoId}
          onChange={(e) => {
            setSolicitacaoId(e.target.value)
            setCotacaoId('')
          }}
        >
          <option value="">Selecione…</option>
          {solicitacoesQuery.data?.map((s) => (
            <option key={s.id} value={s.id}>
              {s.titulo} ({s.status})
            </option>
          ))}
        </select>
        <div className={styles.row}>
          <label>
            Validade
            <input type="date" value={validade} onChange={(e) => setValidade(e.target.value)} />
          </label>
          <button type="button" disabled={!solicitacaoId || createMutation.isPending} onClick={() => createMutation.mutate()}>
            Nova cotação
          </button>
        </div>
        {cotacoesQuery.data && cotacoesQuery.data.length > 0 && (
          <ul className={styles.list}>
            {cotacoesQuery.data.map((c) => (
              <li key={c.id}>
                <button type="button" className={styles.linkBtn} onClick={() => setCotacaoId(c.id)}>
                  v{c.versao} — {c.status} — {c.moeda} {c.totalBruto}
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      {cotacao && (
        <section className={styles.card}>
          <h2>
            Cotação v{cotacao.versao} <span className={styles.badge}>{cotacao.status}</span>
          </h2>
          <p>Total itens: {cotacao.moeda} {cotacao.totalBruto}</p>
          {cotacao.alternativas.length > 0 && (
            <ul className={styles.list}>
              {cotacao.alternativas.map((a) => (
                <li key={a.id}>
                  {a.rotulo}: {a.resumo} — {a.valorTotal}
                </li>
              ))}
            </ul>
          )}
          {cotacao.status === 'RASCUNHO' && (
            <div className={styles.row}>
              <input value={altRotulo} onChange={(e) => setAltRotulo(e.target.value)} placeholder="Rótulo" />
              <input value={altValor} onChange={(e) => setAltValor(e.target.value)} placeholder="Valor" />
              <button type="button" onClick={() => altMutation.mutate()} disabled={altMutation.isPending}>
                + Voo
              </button>
              <button type="button" onClick={() => enviarMutation.mutate()} disabled={enviarMutation.isPending}>
                Enviar
              </button>
            </div>
          )}
          {cotacao.status === 'ENVIADA' && (
            <button type="button" onClick={() => aceitarMutation.mutate()} disabled={aceitarMutation.isPending}>
              Aceitar
            </button>
          )}
          {cotacao.status === 'ACEITA' && (
            <button type="button" onClick={() => vendaMutation.mutate()} disabled={vendaMutation.isPending}>
              Converter em venda
            </button>
          )}
        </section>
      )}

      {message && <p className={styles.message}>{message}</p>}
    </div>
  )
}
