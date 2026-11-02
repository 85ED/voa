package br.jlturismo.voa.modules.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizacaoRepository extends JpaRepository<OrganizacaoEntity, UUID> {
}
