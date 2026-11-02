package br.jlturismo.voa.modules.cotacoes.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CotacaoItemRepository extends JpaRepository<CotacaoItemEntity, UUID> {

    List<CotacaoItemEntity> findByCotacaoIdOrderByOrdemAsc(UUID cotacaoId);

    Optional<CotacaoItemEntity> findByIdAndCotacaoId(UUID id, UUID cotacaoId);

    void deleteByCotacaoIdAndId(UUID cotacaoId, UUID id);
}
