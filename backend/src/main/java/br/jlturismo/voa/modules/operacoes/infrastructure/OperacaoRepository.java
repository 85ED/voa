package br.jlturismo.voa.modules.operacoes.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperacaoRepository extends JpaRepository<OperacaoEntity, UUID> {

    Page<OperacaoEntity> findByOrganizacaoId(UUID organizacaoId, Pageable pageable);

    Page<OperacaoEntity> findByOrganizacaoIdAndParceiroId(
            UUID organizacaoId, UUID parceiroId, Pageable pageable);

    Optional<OperacaoEntity> findByIdAndOrganizacaoId(UUID id, UUID organizacaoId);

    Optional<OperacaoEntity> findByVendaId(UUID vendaId);
}
