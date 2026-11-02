package br.jlturismo.voa.modules.passageiros.infrastructure;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "venda_passageiro")
@IdClass(VendaPassageiroEntity.Key.class)
public class VendaPassageiroEntity {

    @Id
    @Column(name = "venda_id", nullable = false)
    private UUID vendaId;

    @Id
    @Column(name = "passageiro_id", nullable = false)
    private UUID passageiroId;

    public UUID getVendaId() {
        return vendaId;
    }

    public void setVendaId(UUID vendaId) {
        this.vendaId = vendaId;
    }

    public UUID getPassageiroId() {
        return passageiroId;
    }

    public void setPassageiroId(UUID passageiroId) {
        this.passageiroId = passageiroId;
    }

    public static class Key implements Serializable {
        private UUID vendaId;
        private UUID passageiroId;

        public Key() {}

        public Key(UUID vendaId, UUID passageiroId) {
            this.vendaId = vendaId;
            this.passageiroId = passageiroId;
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
            return Objects.equals(vendaId, key.vendaId) && Objects.equals(passageiroId, key.passageiroId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vendaId, passageiroId);
        }
    }
}
