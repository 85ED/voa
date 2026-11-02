package br.jlturismo.voa.modules.vendas.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendaIdempotenciaRepository extends JpaRepository<VendaIdempotenciaEntity, VendaIdempotenciaEntity.Key> {

    Optional<VendaIdempotenciaEntity> findByOrganizacaoIdAndIdempotencyKey(
            UUID organizacaoId, String idempotencyKey);
}
