package br.jlturismo.voa.modules.promocoes.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromocaoRepository extends JpaRepository<PromocaoEntity, UUID> {

    Page<PromocaoEntity> findByOrganizacaoId(UUID organizacaoId, Pageable pageable);

    Optional<PromocaoEntity> findByIdAndOrganizacaoId(UUID id, UUID organizacaoId);

    @Query("""
            SELECT p FROM PromocaoEntity p
            WHERE p.organizacaoId = :orgId
              AND p.ativo = true
              AND (p.validoDe IS NULL OR p.validoDe <= :hoje)
              AND (p.validoAte IS NULL OR p.validoAte >= :hoje)
            ORDER BY p.destaque DESC, p.criadoEm DESC
            """)
    List<PromocaoEntity> findPublicas(@Param("orgId") UUID orgId, @Param("hoje") LocalDate hoje);
}
