package br.jlturismo.voa.modules.operacoes.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "operacao")
public class OperacaoEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "venda_id", nullable = false)
    private UUID vendaId;

    @Column(name = "parceiro_id")
    private UUID parceiroId;

    @Column(nullable = false)
    private String codigo;

    private String observacoes;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

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

    public UUID getParceiroId() {
        return parceiroId;
    }

    public void setParceiroId(UUID parceiroId) {
        this.parceiroId = parceiroId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
}
