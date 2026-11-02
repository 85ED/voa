package br.jlturismo.voa.modules.bilhetes.infrastructure;

import br.jlturismo.voa.modules.bilhetes.domain.StatusBilhete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BilheteRepository extends JpaRepository<BilheteEntity, UUID> {

    Optional<BilheteEntity> findByIdAndOrganizacaoId(UUID id, UUID organizacaoId);

    List<BilheteEntity> findByVendaIdOrderByCriadoEmAsc(UUID vendaId);

    long countByOrganizacaoIdAndStatusIn(UUID organizacaoId, List<StatusBilhete> statuses);

    long countByVendaIdAndStatusNot(UUID vendaId, StatusBilhete status);
}
