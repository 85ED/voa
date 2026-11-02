import { useParams } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import styles from './PartnerPublicPage.module.css'
import { fetchPublicParceiro, submitPartnerSolicitacao } from './partnerPublicApi'

export function PartnerPublicPage() {
  const { slug = '' } = useParams()
  const partnerQuery = useQuery({
    queryKey: ['public-parceiro', slug],
    queryFn: () => fetchPublicParceiro(slug),
    retry: false,
  })

  const [tipoViagem, setTipoViagem] = useState<'SOMENTE_IDA' | 'IDA_VOLTA'>('SOMENTE_IDA')
  const [origem, setOrigem] = useState('')
  const [destino, setDestino] = useState('')
  const [dataIda, setDataIda] = useState('')
  const [dataVolta, setDataVolta] = useState('')
  const [qtdPassageiros, setQtdPassageiros] = useState('1')
  const [nome, setNome] = useState('')
  const [telefone, setTelefone] = useState('')
  const [email, setEmail] = useState('')
  const [observacoes, setObservacoes] = useState('')
  const [consent, setConsent] = useState(false)
  const [website, setWebsite] = useState('')

  const mutation = useMutation({
    mutationFn: () =>
      submitPartnerSolicitacao(slug, {
        tipoViagem,
        origem,
        destino,
        dataIda,
        dataVolta: tipoViagem === 'IDA_VOLTA' ? dataVolta : undefined,
        qtdPassageiros: Number(qtdPassageiros),
        nome,
        telefone,
        email: email || undefined,
        observacoes: observacoes || undefined,
        consentimentoLgpd: consent,
        website,
      }),
  })

  if (partnerQuery.isError) {
    return (
      <div className={styles.page}>
        <p className={styles.error}>Este link de parceiro não está disponível.</p>
      </div>
    )
  }

  const partner = partnerQuery.data

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1>{partner?.nomeExibicao ?? 'Carregando…'}</h1>
        {partner?.subtitulo && <p>{partner.subtitulo}</p>}
        <p className={styles.disclaimer}>
          Você está enviando uma <strong>solicitação de cotação</strong> — não é emissão de bilhete.
        </p>
      </header>

      <form
        className={styles.form}
        onSubmit={(e) => {
          e.preventDefault()
          mutation.mutate()
        }}
      >
        <input
          className={styles.honeypot}
          tabIndex={-1}
          autoComplete="off"
          value={website}
          onChange={(e) => setWebsite(e.target.value)}
          aria-hidden
        />
        <fieldset>
          <legend>Tipo de viagem</legend>
          <label>
            <input
              type="radio"
              checked={tipoViagem === 'SOMENTE_IDA'}
              onChange={() => setTipoViagem('SOMENTE_IDA')}
            />
            Somente ida
          </label>
          <label>
            <input
              type="radio"
              checked={tipoViagem === 'IDA_VOLTA'}
              onChange={() => setTipoViagem('IDA_VOLTA')}
            />
            Ida e volta
          </label>
        </fieldset>
        <label>
          Origem
          <input required value={origem} onChange={(e) => setOrigem(e.target.value)} />
        </label>
        <label>
          Destino
          <input required value={destino} onChange={(e) => setDestino(e.target.value)} />
        </label>
        <label>
          Data ida
          <input required type="date" value={dataIda} onChange={(e) => setDataIda(e.target.value)} />
        </label>
        {tipoViagem === 'IDA_VOLTA' && (
          <label>
            Data volta
            <input type="date" value={dataVolta} onChange={(e) => setDataVolta(e.target.value)} />
          </label>
        )}
        <label>
          Passageiros
          <input
            required
            type="number"
            min={1}
            value={qtdPassageiros}
            onChange={(e) => setQtdPassageiros(e.target.value)}
          />
        </label>
        <label>
          Nome
          <input required value={nome} onChange={(e) => setNome(e.target.value)} />
        </label>
        <label>
          Telefone / WhatsApp
          <input required value={telefone} onChange={(e) => setTelefone(e.target.value)} />
        </label>
        <label>
          E-mail (opcional)
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label>
          Observações
          <textarea rows={3} value={observacoes} onChange={(e) => setObservacoes(e.target.value)} />
        </label>
        <label className={styles.consent}>
          <input type="checkbox" checked={consent} onChange={(e) => setConsent(e.target.checked)} />
          Autorizo o tratamento dos meus dados para esta cotação (LGPD).
        </label>
        {mutation.isError && <p className={styles.error}>Não foi possível enviar. Tente novamente.</p>}
        {mutation.isSuccess && (
          <p className={styles.success}>Solicitação recebida. Entraremos em contato em breve.</p>
        )}
        <button type="submit" disabled={!consent || mutation.isPending}>
          Enviar solicitação
        </button>
      </form>
    </div>
  )
}
