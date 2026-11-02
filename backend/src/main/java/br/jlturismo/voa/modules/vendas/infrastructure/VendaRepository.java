package br.jlturismo.voa.modules.vendas.infrastructure;

import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<VendaEntity, UUID> {

    Optional<VendaEntity> findByIdAndOrganizacaoId(UUID id, UUID organizacaoId);

    Optional<VendaEntity> findByCotacaoId(UUID cotacaoId);

    Page<VendaEntity> findByOrganizacaoId(UUID organizacaoId, Pageable pageable);

    Page<VendaEntity> findByOrganizacaoIdAndStatus(UUID organizacaoId, StatusVenda status, Pageable pageable);

    Page<VendaEntity> findByOrganizacaoIdAndCodigoContainingIgnoreCase(
            UUID organizacaoId, String codigo, Pageable pageable);

    Page<VendaEntity> findByOrganizacaoIdAndStatusAndCodigoContainingIgnoreCase(
            UUID organizacaoId, StatusVenda status, String codigo, Pageable pageable);

    Page<VendaEntity> findByOrganizacaoIdAndParceiroId(UUID organizacaoId, UUID parceiroId, Pageable pageable);

    Page<VendaEntity> findByOrganizacaoIdAndParceiroIdAndStatus(
            UUID organizacaoId, UUID parceiroId, StatusVenda status, Pageable pageable);

    long countByOrganizacaoId(UUID organizacaoId);

    long countByOrganizacaoIdAndStatus(UUID organizacaoId, StatusVenda status);

    long countByOrganizacaoIdAndStatusIn(UUID organizacaoId, java.util.List<StatusVenda> statuses);

    java.util.List<VendaEntity> findByOrganizacaoIdOrderByCriadoEmDesc(UUID organizacaoId);
}
