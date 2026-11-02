package br.jlturismo.voa.modules.cotacoes.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CotacaoRepository extends JpaRepository<CotacaoEntity, UUID> {

    List<CotacaoEntity> findBySolicitacaoIdOrderByVersaoDesc(UUID solicitacaoId);

    Optional<CotacaoEntity> findByIdAndOrganizacaoId(UUID id, UUID organizacaoId);

    @Query("select coalesce(max(c.versao), 0) from CotacaoEntity c where c.solicitacaoId = :solicitacaoId")
    int findMaxVersao(UUID solicitacaoId);

    Optional<CotacaoEntity> findBySolicitacaoIdAndStatus(UUID solicitacaoId, br.jlturismo.voa.modules.cotacoes.domain.StatusCotacao status);
}
