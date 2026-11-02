import { useState } from 'react'
import { Link } from 'react-router-dom'
import styles from './PublicQuotePage.module.css'
import { submitPublicSolicitacao } from './publicApi'

export function PublicQuotePage() {
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [telefone, setTelefone] = useState('')
  const [titulo, setTitulo] = useState('')
  const [destino, setDestino] = useState('')
  const [dataIda, setDataIda] = useState('')
  const [observacoes, setObservacoes] = useState('')
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const result = await submitPublicSolicitacao({
        nome,
        email,
        telefone: telefone || undefined,
        titulo,
        destino,
        dataIda: dataIda || undefined,
        observacoes: observacoes || undefined,
      })
      setStatus(result.status)
    } catch {
      setError('Não foi possível enviar. Tente novamente em instantes.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <form className={styles.card} onSubmit={onSubmit}>
        <Link to="/" className={styles.back}>
          ← Voltar
        </Link>
        <h1>Solicitar viagem</h1>
        <p className={styles.sub}>Responderemos com uma cotação pela equipe comercial.</p>
        <label>
          Nome
          <input required value={nome} onChange={(e) => setNome(e.target.value)} />
        </label>
        <label>
          E-mail
          <input required type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label>
          Telefone
          <input value={telefone} onChange={(e) => setTelefone(e.target.value)} />
        </label>
        <label>
          Título da viagem
          <input required value={titulo} onChange={(e) => setTitulo(e.target.value)} />
        </label>
        <label>
          Destino
          <input required value={destino} onChange={(e) => setDestino(e.target.value)} />
        </label>
        <label>
          Data ida
          <input type="date" value={dataIda} onChange={(e) => setDataIda(e.target.value)} />
        </label>
        <label>
          Observações
          <textarea rows={3} value={observacoes} onChange={(e) => setObservacoes(e.target.value)} />
        </label>
        {error && <p className={styles.error}>{error}</p>}
        {status && <p className={styles.success}>Solicitação {status}. Entraremos em contato em breve.</p>}
        <button type="submit" disabled={loading}>
          {loading ? 'Enviando…' : 'Enviar solicitação'}
        </button>
      </form>
    </div>
  )
}
