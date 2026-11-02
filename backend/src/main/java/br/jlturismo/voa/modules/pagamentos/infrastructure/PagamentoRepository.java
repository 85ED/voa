package br.jlturismo.voa.modules.pagamentos.infrastructure;

import br.jlturismo.voa.modules.pagamentos.domain.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<PagamentoEntity, UUID> {

    Optional<PagamentoEntity> findByIdAndOrganizacaoId(UUID id, UUID organizacaoId);

    List<PagamentoEntity> findByVendaIdOrderByCriadoEmAsc(UUID vendaId);

    long countByOrganizacaoIdAndStatusIn(UUID organizacaoId, List<StatusPagamento> statuses);
}
