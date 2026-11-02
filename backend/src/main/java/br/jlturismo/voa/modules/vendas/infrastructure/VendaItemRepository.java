package br.jlturismo.voa.modules.vendas.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VendaItemRepository extends JpaRepository<VendaItemEntity, UUID> {

    List<VendaItemEntity> findByVendaIdOrderByOrdemAsc(UUID vendaId);
}
