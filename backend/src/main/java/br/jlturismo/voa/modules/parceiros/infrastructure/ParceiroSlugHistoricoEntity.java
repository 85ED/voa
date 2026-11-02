package br.jlturismo.voa.modules.parceiros.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parceiro_slug_historico")
public class ParceiroSlugHistoricoEntity {

    @Id
    private UUID id;

    @Column(name = "parceiro_id", nullable = false)
    private UUID parceiroId;

    @Column(nullable = false)
    private String slug;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getParceiroId() {
        return parceiroId;
    }

    public void setParceiroId(UUID parceiroId) {
        this.parceiroId = parceiroId;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
