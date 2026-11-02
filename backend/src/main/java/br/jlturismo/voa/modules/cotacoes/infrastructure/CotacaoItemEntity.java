package br.jlturismo.voa.modules.cotacoes.infrastructure;

import br.jlturismo.voa.modules.cotacoes.domain.TipoCotacaoItem;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cotacao_item")
public class CotacaoItemEntity {

    @Id
    private UUID id;

    @Column(name = "cotacao_id", nullable = false)
    private UUID cotacaoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCotacaoItem tipo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false)
    private BigDecimal valorUnitario;

    @Column(nullable = false, length = 3)
    private String moeda;

    @Column(nullable = false)
    private int ordem;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCotacaoId() {
        return cotacaoId;
    }

    public void setCotacaoId(UUID cotacaoId) {
        this.cotacaoId = cotacaoId;
    }

    public TipoCotacaoItem getTipo() {
        return tipo;
    }

    public void setTipo(TipoCotacaoItem tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
}
