package br.jlturismo.voa.modules.cotacoes.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cotacao_evento")
public class CotacaoEventoEntity {

    @Id
    private UUID id;

    @Column(name = "cotacao_id", nullable = false)
    private UUID cotacaoId;

    @Column(name = "de_status")
    private String deStatus;

    @Column(name = "para_status", nullable = false)
    private String paraStatus;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    private String observacao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

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

    public String getDeStatus() {
        return deStatus;
    }

    public void setDeStatus(String deStatus) {
        this.deStatus = deStatus;
    }

    public String getParaStatus() {
        return paraStatus;
    }

    public void setParaStatus(String paraStatus) {
        this.paraStatus = paraStatus;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
