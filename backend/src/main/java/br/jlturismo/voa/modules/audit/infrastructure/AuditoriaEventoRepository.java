package br.jlturismo.voa.modules.audit.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEventoEntity, UUID> {

    Page<AuditoriaEventoEntity> findByOrganizacaoIdOrderByCriadoEmDesc(
            UUID organizacaoId, Pageable pageable);

    Page<AuditoriaEventoEntity> findByOrganizacaoIdAndEntidadeTipoAndEntidadeIdOrderByCriadoEmDesc(
            UUID organizacaoId, String entidadeTipo, UUID entidadeId, Pageable pageable);

    Page<AuditoriaEventoEntity> findByOrganizacaoIdAndActorIdOrderByCriadoEmDesc(
            UUID organizacaoId, UUID actorId, Pageable pageable);
}
