package br.jlturismo.voa.modules.solicitacoes.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<ClienteEntity, UUID> {

    Page<ClienteEntity> findByOrganizacaoIdAndDeletedAtIsNull(UUID organizacaoId, Pageable pageable);

    @Query("""
            SELECT c FROM ClienteEntity c
            WHERE c.organizacaoId = :orgId AND c.deletedAt IS NULL
            AND (LOWER(c.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<ClienteEntity> search(@Param("orgId") UUID orgId, @Param("q") String q, Pageable pageable);

    Optional<ClienteEntity> findByIdAndOrganizacaoIdAndDeletedAtIsNull(UUID id, UUID orgId);
}
