package br.jlturismo.voa.modules.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenHashAndUsadoEmIsNull(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            UPDATE PasswordResetTokenEntity t
            SET t.usadoEm = :now
            WHERE t.usuarioId = :usuarioId AND t.usadoEm IS NULL
            """)
    void invalidateActiveForUser(@Param("usuarioId") UUID usuarioId, @Param("now") Instant now);
}
