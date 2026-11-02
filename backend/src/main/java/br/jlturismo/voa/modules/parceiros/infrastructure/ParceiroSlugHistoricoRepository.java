package br.jlturismo.voa.modules.parceiros.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParceiroSlugHistoricoRepository extends JpaRepository<ParceiroSlugHistoricoEntity, UUID> {}
