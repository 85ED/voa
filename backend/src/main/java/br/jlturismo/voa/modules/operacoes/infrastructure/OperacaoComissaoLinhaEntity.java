package br.jlturismo.voa.modules.operacoes.infrastructure;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "operacao_comissao_linha")
public class OperacaoComissaoLinhaEntity {

    @Id
    private UUID id;

    @Column(name = "operacao_id", nullable = false)
    private UUID operacaoId;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private boolean manual;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOperacaoId() {
        return operacaoId;
    }

    public void setOperacaoId(UUID operacaoId) {
        this.operacaoId = operacaoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
