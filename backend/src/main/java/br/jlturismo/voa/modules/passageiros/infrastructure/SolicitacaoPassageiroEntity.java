package br.jlturismo.voa.modules.passageiros.infrastructure;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "solicitacao_passageiro")
@IdClass(SolicitacaoPassageiroEntity.Key.class)
public class SolicitacaoPassageiroEntity {

    @Id
    @Column(name = "solicitacao_id", nullable = false)
    private UUID solicitacaoId;

    @Id
    @Column(name = "passageiro_id", nullable = false)
    private UUID passageiroId;

    public UUID getSolicitacaoId() {
        return solicitacaoId;
    }

    public void setSolicitacaoId(UUID solicitacaoId) {
        this.solicitacaoId = solicitacaoId;
    }

    public UUID getPassageiroId() {
        return passageiroId;
    }

    public void setPassageiroId(UUID passageiroId) {
        this.passageiroId = passageiroId;
    }

    public static class Key implements Serializable {
        private UUID solicitacaoId;
        private UUID passageiroId;

        public Key() {}

        public Key(UUID solicitacaoId, UUID passageiroId) {
            this.solicitacaoId = solicitacaoId;
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
            return Objects.equals(solicitacaoId, key.solicitacaoId)
                    && Objects.equals(passageiroId, key.passageiroId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(solicitacaoId, passageiroId);
        }
    }
}
