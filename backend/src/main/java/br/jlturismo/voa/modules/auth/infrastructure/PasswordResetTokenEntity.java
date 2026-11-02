package br.jlturismo.voa.modules.auth.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetTokenEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "usado_em")
    private Instant usadoEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(Instant expiraEm) {
        this.expiraEm = expiraEm;
    }

    public Instant getUsadoEm() {
        return usadoEm;
    }

    public void setUsadoEm(Instant usadoEm) {
        this.usadoEm = usadoEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
