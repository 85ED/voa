package br.jlturismo.voa.modules.solicitacoes.infrastructure;

import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoRepository extends JpaRepository<SolicitacaoEntity, UUID> {

    Page<SolicitacaoEntity> findByOrganizacaoId(UUID organizacaoId, Pageable pageable);

    Page<SolicitacaoEntity> findByOrganizacaoIdAndStatus(UUID organizacaoId, StatusSolicitacao status, Pageable pageable);

    Optional<SolicitacaoEntity> findByIdAndOrganizacaoId(UUID id, UUID orgId);

    long countByOrganizacaoIdAndStatusIn(UUID organizacaoId, List<StatusSolicitacao> statuses);
}
