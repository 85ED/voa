package br.jlturismo.voa.modules.pagamentos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PagamentoEventoRepository extends JpaRepository<PagamentoEventoEntity, UUID> {}
