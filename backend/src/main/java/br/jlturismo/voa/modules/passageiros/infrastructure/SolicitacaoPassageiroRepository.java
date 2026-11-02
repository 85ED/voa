package br.jlturismo.voa.modules.passageiros.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SolicitacaoPassageiroRepository extends JpaRepository<SolicitacaoPassageiroEntity, SolicitacaoPassageiroEntity.Key> {

    List<SolicitacaoPassageiroEntity> findBySolicitacaoId(UUID solicitacaoId);

    void deleteBySolicitacaoIdAndPassageiroId(UUID solicitacaoId, UUID passageiroId);
}
