package br.jlturismo.voa.modules.passageiros.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PassageiroRepository extends JpaRepository<PassageiroEntity, UUID> {

    Optional<PassageiroEntity> findByIdAndOrganizacaoIdAndDeletedAtIsNull(UUID id, UUID organizacaoId);

    Page<PassageiroEntity> findByOrganizacaoIdAndDeletedAtIsNull(UUID organizacaoId, Pageable pageable);

    @Query(
            """
            select p from PassageiroEntity p
            where p.organizacaoId = :orgId and p.deletedAt is null
            and (lower(p.nome) like lower(concat('%', :q, '%'))
                 or p.documento like concat('%', :q, '%'))
            """)
    Page<PassageiroEntity> search(UUID orgId, String q, Pageable pageable);
}
