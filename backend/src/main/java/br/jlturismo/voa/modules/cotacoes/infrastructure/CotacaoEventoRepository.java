package br.jlturismo.voa.modules.cotacoes.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CotacaoEventoRepository extends JpaRepository<CotacaoEventoEntity, UUID> {

    List<CotacaoEventoEntity> findByCotacaoIdOrderByCriadoEmDesc(UUID cotacaoId);
}
