package br.jlturismo.voa.modules.vendas.infrastructure;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "venda_idempotencia")
@IdClass(VendaIdempotenciaEntity.Key.class)
public class VendaIdempotenciaEntity {

    @Id
    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Id
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "venda_id", nullable = false)
    private UUID vendaId;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getVendaId() {
        return vendaId;
    }

    public void setVendaId(UUID vendaId) {
        this.vendaId = vendaId;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public static class Key implements Serializable {
        private UUID organizacaoId;
        private String idempotencyKey;

        public Key() {}

        public Key(UUID organizacaoId, String idempotencyKey) {
            this.organizacaoId = organizacaoId;
            this.idempotencyKey = idempotencyKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equals(organizacaoId, key.organizacaoId)
                    && Objects.equals(idempotencyKey, key.idempotencyKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(organizacaoId, idempotencyKey);
        }
    }
}
