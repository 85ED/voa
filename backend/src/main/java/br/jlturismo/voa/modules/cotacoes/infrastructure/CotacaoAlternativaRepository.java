package br.jlturismo.voa.modules.cotacoes.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CotacaoAlternativaRepository extends JpaRepository<CotacaoAlternativaEntity, UUID> {

    List<CotacaoAlternativaEntity> findByCotacaoIdOrderByOrdemAsc(UUID cotacaoId);

    Optional<CotacaoAlternativaEntity> findByIdAndCotacaoId(UUID id, UUID cotacaoId);
}
