package br.jlturismo.voa.modules.audit.application;

import br.jlturismo.voa.modules.audit.infrastructure.AuditoriaEventoEntity;
import br.jlturismo.voa.modules.audit.infrastructure.AuditoriaEventoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditoriaEventoRepository repository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditoriaEventoRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void record(
            UUID organizacaoId,
            UUID actorId,
            String acao,
            String entidadeTipo,
            UUID entidadeId,
            Map<String, Object> payload,
            String ip) {
        AuditoriaEventoEntity row = new AuditoriaEventoEntity();
        row.setId(UUID.randomUUID());
        row.setOrganizacaoId(organizacaoId);
        row.setActorId(actorId);
        row.setAcao(acao);
        row.setEntidadeTipo(entidadeTipo);
        row.setEntidadeId(entidadeId);
        row.setPayloadJson(payload != null ? toJson(payload) : null);
        row.setIp(ip);
        row.setCriadoEm(Instant.now());
        repository.save(row);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventView> list(
            UUID organizacaoId,
            String entidadeTipo,
            UUID entidadeId,
            UUID actorId,
            Pageable pageable) {
        Page<AuditoriaEventoEntity> page;
        if (entidadeTipo != null && entidadeId != null) {
            page = repository.findByOrganizacaoIdAndEntidadeTipoAndEntidadeIdOrderByCriadoEmDesc(
                    organizacaoId, entidadeTipo, entidadeId, pageable);
        } else if (actorId != null) {
            page = repository.findByOrganizacaoIdAndActorIdOrderByCriadoEmDesc(
                    organizacaoId, actorId, pageable);
        } else {
            page = repository.findByOrganizacaoIdOrderByCriadoEmDesc(organizacaoId, pageable);
        }
        return page.map(this::toView);
    }

    private AuditEventView toView(AuditoriaEventoEntity e) {
        return new AuditEventView(
                e.getId(),
                e.getActorId(),
                e.getAcao(),
                e.getEntidadeTipo(),
                e.getEntidadeId(),
                e.getPayloadJson(),
                e.getIp(),
                e.getCriadoEm());
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"serialize\"}";
        }
    }

    public record AuditEventView(
            UUID id,
            UUID actorId,
            String acao,
            String entidadeTipo,
            UUID entidadeId,
            String payloadJson,
            String ip,
            Instant criadoEm) {}
}
