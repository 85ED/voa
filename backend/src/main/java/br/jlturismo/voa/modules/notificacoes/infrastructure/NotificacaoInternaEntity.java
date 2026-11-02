package br.jlturismo.voa.modules.notificacoes.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notificacao_interna")
public class NotificacaoInternaEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(nullable = false)
    private String titulo;

    private String corpo;

    @Column(nullable = false)
    private boolean lida;

    @Column(name = "entidade_tipo")
    private String entidadeTipo;

    @Column(name = "entidade_id")
    private UUID entidadeId;

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

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public String getEntidadeTipo() {
        return entidadeTipo;
    }

    public void setEntidadeTipo(String entidadeTipo) {
        this.entidadeTipo = entidadeTipo;
    }

    public UUID getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(UUID entidadeId) {
        this.entidadeId = entidadeId;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
