package br.jlturismo.voa.modules.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByOrganizacaoIdAndEmailIgnoreCase(UUID organizacaoId, String email);

    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);

    long count();
}
