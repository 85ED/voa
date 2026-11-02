package br.jlturismo.voa.modules.cotacoes.infrastructure;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "cotacao_alternativa")
public class CotacaoAlternativaEntity {

    @Id
    private UUID id;

    @Column(name = "cotacao_id", nullable = false)
    private UUID cotacaoId;

    @Column(nullable = false)
    private String rotulo;

    private String resumo;

    private String origem;

    private String destino;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Column(nullable = false, length = 3)
    private String moeda;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalhes_json")
    private Map<String, Object> detalhesJson;

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

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    public Map<String, Object> getDetalhesJson() {
        return detalhesJson;
    }

    public void setDetalhesJson(Map<String, Object> detalhesJson) {
        this.detalhesJson = detalhesJson;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
}
