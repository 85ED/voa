package br.jlturismo.voa.modules.operacoes.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OperacaoComissaoLinhaRepository extends JpaRepository<OperacaoComissaoLinhaEntity, UUID> {

    List<OperacaoComissaoLinhaEntity> findByOperacaoIdOrderByCriadoEmAsc(UUID operacaoId);
}
