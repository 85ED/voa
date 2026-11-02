package br.jlturismo.voa.modules.notificacoes.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface NotificacaoInternaRepository extends JpaRepository<NotificacaoInternaEntity, UUID> {

    @Query("""
            SELECT n FROM NotificacaoInternaEntity n
            WHERE n.organizacaoId = :orgId
              AND (n.usuarioId IS NULL OR n.usuarioId = :usuarioId)
            ORDER BY n.criadoEm DESC
            """)
    Page<NotificacaoInternaEntity> findForUser(
            @Param("orgId") UUID orgId, @Param("usuarioId") UUID usuarioId, Pageable pageable);

    @Query("""
            SELECT COUNT(n) FROM NotificacaoInternaEntity n
            WHERE n.organizacaoId = :orgId
              AND n.lida = false
              AND (n.usuarioId IS NULL OR n.usuarioId = :usuarioId)
            """)
    long countNaoLidas(@Param("orgId") UUID orgId, @Param("usuarioId") UUID usuarioId);

    Optional<NotificacaoInternaEntity> findByIdAndOrganizacaoId(UUID id, UUID organizacaoId);

    @Modifying
    @Query("""
            UPDATE NotificacaoInternaEntity n SET n.lida = true
            WHERE n.organizacaoId = :orgId
              AND n.lida = false
              AND (n.usuarioId IS NULL OR n.usuarioId = :usuarioId)
            """)
    int marcarTodasLidas(@Param("orgId") UUID orgId, @Param("usuarioId") UUID usuarioId);
}
