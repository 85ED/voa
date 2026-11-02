package br.jlturismo.voa.modules.pagamentos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParcelaRepository extends JpaRepository<ParcelaEntity, UUID> {

    List<ParcelaEntity> findByPagamentoIdOrderByNumeroAsc(UUID pagamentoId);

    Optional<ParcelaEntity> findByIdAndPagamentoId(UUID id, UUID pagamentoId);

    long countByPagamentoIdAndConfirmadoEmIsNull(UUID pagamentoId);
}
