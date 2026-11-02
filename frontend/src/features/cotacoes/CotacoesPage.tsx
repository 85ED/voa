import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { PageHeader } from '../../shared/ui/PageHeader'
import { cotacaoStatusLabel } from '../solicitacoes/solicitacaoLabels'
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
      setMessage(`Proposta versão ${c.versao} criada em rascunho.`)
      invalidate()
    },
    onError: () => setMessage('Não foi possível criar a proposta.'),
  })

  const altMutation = useMutation({
    mutationFn: () =>
      addAlternativa(cotacaoId, {
        rotulo: altRotulo,
        resumo: 'Combinação de voo e serviços',
        valorTotal: Number(altValor),
        origem: 'GRU',
        destino: 'GIG',
      }),
    onSuccess: () => {
      setMessage('Opção de preço adicionada.')
      invalidate()
    },
  })

  const enviarMutation = useMutation({
    mutationFn: () => enviarCotacao(cotacaoId),
    onSuccess: () => {
      setMessage('Proposta marcada como enviada ao cliente.')
      invalidate()
    },
  })

  const aceitarMutation = useMutation({
    mutationFn: () => aceitarCotacao(cotacaoId),
    onSuccess: () => {
      setMessage('Cliente aceitou a proposta.')
      invalidate()
    },
  })

  const vendaMutation = useMutation({
    mutationFn: () => converterVenda(cotacaoId),
    onSuccess: () => setMessage('Venda aberta criada. Continue em Operações.'),
  })

  const cotacao = cotacaoQuery.data

  return (
    <div className={styles.page}>
      <PageHeader
        title="Cotações"
        subtitle="Monte a proposta comercial para uma solicitação: escolha o pedido, crie a versão, inclua opções de preço, envie ou registre o aceite e converta em venda."
      />

      <ol className={styles.steps}>
        <li>Selecione a solicitação em cotação.</li>
        <li>Crie uma proposta com data de validade.</li>
        <li>Inclua opções de valor (ex.: voos ou pacotes).</li>
        <li>Envie ao cliente, registre aceite e abra a venda.</li>
      </ol>

      <section className={styles.card}>
        <h2>1. Solicitação</h2>
        <p className={styles.hint}>Somente pedidos já em fase de cotação aparecem aqui.</p>
        <select
          value={solicitacaoId}
          onChange={(e) => {
            setSolicitacaoId(e.target.value)
            setCotacaoId('')
          }}
        >
          <option value="">Selecione o pedido…</option>
          {solicitacoesQuery.data?.map((s) => (
            <option key={s.id} value={s.id}>
              {s.titulo} — {s.destino ?? 'sem destino'}
            </option>
          ))}
        </select>
        <div className={styles.row}>
          <label>
            Validade da proposta
            <input type="date" value={validade} onChange={(e) => setValidade(e.target.value)} />
          </label>
          <button type="button" disabled={!solicitacaoId || createMutation.isPending} onClick={() => createMutation.mutate()}>
            Criar proposta
          </button>
        </div>
        {cotacoesQuery.data && cotacoesQuery.data.length > 0 && (
          <ul className={styles.list}>
            {cotacoesQuery.data.map((c) => (
              <li key={c.id}>
                <button type="button" className={styles.linkBtn} onClick={() => setCotacaoId(c.id)}>
                  Versão {c.versao} · {cotacaoStatusLabel(c.status)} · {c.moeda}{' '}
                  {c.totalBruto.toLocaleString('pt-BR')}
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      {cotacao && (
        <section className={styles.card}>
          <h2>
            2. Proposta versão {cotacao.versao}{' '}
            <span className={styles.badge}>{cotacaoStatusLabel(cotacao.status)}</span>
          </h2>
          <p>Soma dos itens: {cotacao.moeda} {cotacao.totalBruto.toLocaleString('pt-BR')}</p>
          {cotacao.alternativas.length > 0 && (
            <ul className={styles.list}>
              {cotacao.alternativas.map((a) => (
                <li key={a.id}>
                  {a.rotulo}: {a.resumo} — {cotacao.moeda} {a.valorTotal.toLocaleString('pt-BR')}
                </li>
              ))}
            </ul>
          )}
          {cotacao.status === 'RASCUNHO' && (
            <div className={styles.row}>
              <input value={altRotulo} onChange={(e) => setAltRotulo(e.target.value)} placeholder="Nome da opção" />
              <input value={altValor} onChange={(e) => setAltValor(e.target.value)} placeholder="Valor total" />
              <button type="button" onClick={() => altMutation.mutate()} disabled={altMutation.isPending}>
                Adicionar opção
              </button>
              <button type="button" onClick={() => enviarMutation.mutate()} disabled={enviarMutation.isPending}>
                Enviar ao cliente
              </button>
            </div>
          )}
          {cotacao.status === 'ENVIADA' && (
            <button type="button" onClick={() => aceitarMutation.mutate()} disabled={aceitarMutation.isPending}>
              Registrar aceite do cliente
            </button>
          )}
          {cotacao.status === 'ACEITA' && (
            <button type="button" onClick={() => vendaMutation.mutate()} disabled={vendaMutation.isPending}>
              Abrir venda
            </button>
          )}
        </section>
      )}

      {message && <p className={styles.message}>{message}</p>}
    </div>
  )
}
