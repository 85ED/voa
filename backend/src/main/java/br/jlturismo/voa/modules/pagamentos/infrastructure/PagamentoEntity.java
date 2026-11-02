package br.jlturismo.voa.modules.pagamentos.infrastructure;

import br.jlturismo.voa.modules.pagamentos.domain.StatusPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pagamento")
public class PagamentoEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "venda_id", nullable = false)
    private UUID vendaId;

    @Column(nullable = false)
    private String forma;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    private String observacoes;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "estornado_em")
    private Instant estornadoEm;

    @Column(name = "estornado_por_id")
    private UUID estornadoPorId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public UUID getVendaId() {
        return vendaId;
    }

    public void setVendaId(UUID vendaId) {
        this.vendaId = vendaId;
    }

    public String getForma() {
        return forma;
    }

    public void setForma(String forma) {
        this.forma = forma;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public Instant getEstornadoEm() {
        return estornadoEm;
    }

    public void setEstornadoEm(Instant estornadoEm) {
        this.estornadoEm = estornadoEm;
    }

    public UUID getEstornadoPorId() {
        return estornadoPorId;
    }

    public void setEstornadoPorId(UUID estornadoPorId) {
        this.estornadoPorId = estornadoPorId;
    }
}
