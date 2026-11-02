package br.jlturismo.voa.modules.parceiros.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParceiroRepository extends JpaRepository<ParceiroEntity, UUID> {

    Page<ParceiroEntity> findByOrganizacaoId(UUID organizacaoId, Pageable pageable);

    Page<ParceiroEntity> findByOrganizacaoIdAndAtivo(UUID organizacaoId, boolean ativo, Pageable pageable);

    Optional<ParceiroEntity> findByIdAndOrganizacaoId(UUID id, UUID organizacaoId);

    Optional<ParceiroEntity> findByOrganizacaoIdAndSlug(UUID organizacaoId, String slug);

    boolean existsByOrganizacaoIdAndSlugAndIdNot(UUID organizacaoId, String slug, UUID id);
}
