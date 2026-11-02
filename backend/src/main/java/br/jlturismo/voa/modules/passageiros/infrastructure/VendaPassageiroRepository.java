package br.jlturismo.voa.modules.passageiros.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VendaPassageiroRepository extends JpaRepository<VendaPassageiroEntity, VendaPassageiroEntity.Key> {

    List<VendaPassageiroEntity> findByVendaId(UUID vendaId);
}
