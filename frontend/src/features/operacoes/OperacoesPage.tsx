import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import styles from './OperacoesPage.module.css'
import {
  atualizarBilhete,
  confirmarParcela,
  confirmarQuitacao,
  criarBilhete,
  fetchBilhetes,
  fetchDashboard,
  fetchPagamentos,
  fetchVendas,
  registrarPagamento,
} from './api'

function defaultVencimento() {
  const d = new Date()
  d.setDate(d.getDate() + 7)
  return d.toISOString().slice(0, 10)
}

export function OperacoesPage() {
  const queryClient = useQueryClient()
  const [vendaId, setVendaId] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [codigoFilter, setCodigoFilter] = useState('')
  const [valorParcela, setValorParcela] = useState('1500')
  const [localizador, setLocalizador] = useState('')
  const [message, setMessage] = useState<string | null>(null)

  const dashboardQuery = useQuery({ queryKey: ['dashboard'], queryFn: fetchDashboard })
  const vendasQuery = useQuery({
    queryKey: ['vendas-operacoes', statusFilter, codigoFilter],
    queryFn: () => fetchVendas(statusFilter || undefined, codigoFilter || undefined),
  })
  const pagamentosQuery = useQuery({
    queryKey: ['pagamentos', vendaId],
    queryFn: () => fetchPagamentos(vendaId),
    enabled: Boolean(vendaId),
  })
  const bilhetesQuery = useQuery({
    queryKey: ['bilhetes', vendaId],
    queryFn: () => fetchBilhetes(vendaId),
    enabled: Boolean(vendaId),
  })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    queryClient.invalidateQueries({ queryKey: ['vendas-operacoes'] })
    if (vendaId) {
      queryClient.invalidateQueries({ queryKey: ['bilhetes', vendaId] })
      queryClient.invalidateQueries({ queryKey: ['pagamentos', vendaId] })
    }
  }

  const pagamentoMutation = useMutation({
    mutationFn: () =>
      registrarPagamento(vendaId, {
        forma: 'PIX',
        parcelas: [{ vencimento: defaultVencimento(), valor: Number(valorParcela) }],
      }),
    onSuccess: () => {
      setMessage('Pagamento registrado')
      invalidate()
    },
    onError: () => setMessage('Erro ao registrar pagamento'),
  })

  const confirmarParcelaMutation = useMutation({
    mutationFn: (parcelaId: string) => confirmarParcela(parcelaId),
    onSuccess: () => {
      setMessage('Parcela confirmada')
      invalidate()
    },
  })

  const quitacaoMutation = useMutation({
    mutationFn: (pagamentoId: string) => confirmarQuitacao(pagamentoId),
    onSuccess: () => {
      setMessage('Pagamento quitado — venda pronta para emissão')
      invalidate()
    },
  })

  const bilheteMutation = useMutation({
    mutationFn: () => criarBilhete(vendaId, localizador || 'LOC-' + Date.now()),
    onSuccess: () => {
      setMessage('Bilhete criado')
      invalidate()
    },
  })

  const emitirMutation = useMutation({
    mutationFn: (bilheteId: string) =>
      atualizarBilhete(bilheteId, {
        status: 'EMITIDA',
        arquivoPath: `/data/bilhetes/${bilheteId}.pdf`,
      }),
    onSuccess: () => {
      setMessage('Bilhete emitido')
      invalidate()
    },
  })

  const enviarMutation = useMutation({
    mutationFn: (bilheteId: string) => atualizarBilhete(bilheteId, { status: 'ENVIADA_CLIENTE' }),
    onSuccess: () => {
      setMessage('Bilhete marcado como enviado')
      invalidate()
    },
  })

  const dash = dashboardQuery.data

  return (
    <div className={styles.page}>
      <h1>Operações — pagamentos e emissões</h1>
      {message && <p className={styles.message}>{message}</p>}

      <section className={styles.card}>
        <h2>Pendências (API)</h2>
        {dashboardQuery.isLoading ? (
          <p>Carregando…</p>
        ) : dash ? (
          <ul className={styles.kpiList}>
            <li>Vendas aguardando pagamento: {dash.vendasAguardandoPagamento}</li>
            <li>Pagamento parcial: {dash.vendasPagamentoParcial}</li>
            <li>Emissão pendente: {dash.emissaoPendente}</li>
            <li>Bilhetes pendentes/agendados: {dash.bilhetesPendentes}</li>
            <li>Aguardando envio ao cliente: {dash.bilhetesAguardandoEnvio}</li>
          </ul>
        ) : null}
      </section>

      <section className={styles.card}>
        <h2>Filtros de vendas</h2>
        <div className={styles.filters}>
          <label>
            Status
            <select
              className={styles.select}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">Todos</option>
              <option value="CONFIRMADA">CONFIRMADA</option>
              <option value="PAGAMENTO_PARCIAL">PAGAMENTO_PARCIAL</option>
              <option value="AGUARDANDO_EMISSAO">AGUARDANDO_EMISSAO</option>
              <option value="EM_EMISSAO">EM_EMISSAO</option>
              <option value="CONCLUIDA">CONCLUIDA</option>
            </select>
          </label>
          <label>
            Código contém
            <input
              className={styles.input}
              value={codigoFilter}
              onChange={(e) => setCodigoFilter(e.target.value)}
              placeholder="VND-"
            />
          </label>
        </div>
      </section>

      <section className={styles.card}>
        <h2>Venda</h2>
        <select
          className={styles.select}
          value={vendaId}
          onChange={(e) => setVendaId(e.target.value)}
        >
          <option value="">Selecione…</option>
          {vendasQuery.data?.map((v) => (
            <option key={v.id} value={v.id}>
              {v.codigo} — {v.status} — {v.total} {v.moeda}
            </option>
          ))}
        </select>
      </section>

      {vendaId && (
        <>
          <section className={styles.card}>
            <h2>Pagamento manual</h2>
            <label>
              Valor parcela
              <input
                className={styles.input}
                value={valorParcela}
                onChange={(e) => setValorParcela(e.target.value)}
              />
            </label>
            <button
              type="button"
              className={styles.button}
              disabled={pagamentoMutation.isPending}
              onClick={() => pagamentoMutation.mutate()}
            >
              Registrar pagamento
            </button>
            {pagamentosQuery.data?.map((p) => (
              <div key={p.id}>
                <p>
                  {p.forma} — {p.status} — total {p.valorTotal}
                </p>
                {p.parcelas.map((par) => (
                  <div key={par.id} className={styles.parcelaRow}>
                    <span>
                      #{par.numero} {par.valor}
                      {par.confirmadoEm ? ' ✓' : ''}
                    </span>
                    {!par.confirmadoEm && (
                      <button
                        type="button"
                        className={styles.buttonSmall}
                        onClick={() => confirmarParcelaMutation.mutate(par.id)}
                      >
                        Confirmar parcela
                      </button>
                    )}
                  </div>
                ))}
                {p.status !== 'QUITADO' && p.parcelas.every((par) => par.confirmadoEm) && (
                  <button
                    type="button"
                    className={styles.buttonSecondary}
                    onClick={() => quitacaoMutation.mutate(p.id)}
                  >
                    Confirmar quitação
                  </button>
                )}
              </div>
            ))}
            <p className={styles.hint}>
              Anexar comprovante (PATCH parcela) não confirma pagamento — só usuário autorizado.
            </p>
          </section>

          <section className={styles.card}>
            <h2>Emissão</h2>
            <label>
              Localizador
              <input
                className={styles.input}
                value={localizador}
                onChange={(e) => setLocalizador(e.target.value)}
                placeholder="ABC123"
              />
            </label>
            <button
              type="button"
              className={styles.button}
              disabled={bilheteMutation.isPending}
              onClick={() => bilheteMutation.mutate()}
            >
              Criar bilhete
            </button>
            <ul className={styles.list}>
              {bilhetesQuery.data?.map((b) => (
                <li key={b.id}>
                  {b.localizador ?? b.id.slice(0, 8)} — {b.status}
                  {b.status === 'PENDENTE' || b.status === 'AGENDADA' ? (
                    <button
                      type="button"
                      className={styles.buttonSmall}
                      onClick={() => emitirMutation.mutate(b.id)}
                    >
                      Emitir
                    </button>
                  ) : null}
                  {b.status === 'EMITIDA' ? (
                    <button
                      type="button"
                      className={styles.buttonSmall}
                      onClick={() => enviarMutation.mutate(b.id)}
                    >
                      Enviado ao cliente
                    </button>
                  ) : null}
                </li>
              ))}
            </ul>
          </section>
        </>
      )}
    </div>
  )
}
