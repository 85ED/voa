package br.jlturismo.voa.modules.lgpd.application;

import br.jlturismo.voa.modules.audit.application.AuditService;
import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.solicitacoes.application.ClienteService;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteRepository;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LgpdClienteService {

    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final AuditService auditService;

    public LgpdClienteService(
            ClienteService clienteService,
            ClienteRepository clienteRepository,
            SolicitacaoRepository solicitacaoRepository,
            AuditService auditService) {
        this.clienteService = clienteService;
        this.clienteRepository = clienteRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportar(VoaUserPrincipal principal, UUID clienteId) {
        ClienteEntity cliente = clienteService.find(principal, clienteId);
        List<Map<String, Object>> solicitacoes = solicitacaoRepository
                .findByOrganizacaoIdAndClienteIdOrderByCriadoEmDesc(principal.getOrganizacaoId(), clienteId)
                .stream()
                .map(this::solicitacaoResumo)
                .toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("clienteId", cliente.getId());
        payload.put("nome", cliente.getNome());
        payload.put("email", cliente.getEmail());
        payload.put("telefone", cliente.getTelefone());
        payload.put("documento", cliente.getDocumento());
        payload.put("criadoEm", cliente.getCriadoEm());
        payload.put("solicitacoes", solicitacoes);
        payload.put(
                "aviso",
                "Exportação operacional para atendimento interno; não substitui parecer jurídico nem resposta formal ao titular.");
        return payload;
    }

    @Transactional
    public Map<String, Object> solicitarExclusao(
            VoaUserPrincipal principal, UUID clienteId, String motivo, String ip) {
        ClienteEntity cliente = clienteService.find(principal, clienteId);
        Instant now = Instant.now();
        cliente.setNome("Titular removido");
        cliente.setEmail(null);
        cliente.setTelefone(null);
        cliente.setDocumento(null);
        cliente.setDeletedAt(now);
        cliente.setAtualizadoEm(now);
        clienteRepository.save(cliente);

        auditService.record(
                principal.getOrganizacaoId(),
                principal.getId(),
                "LGPD_ANONIMIZACAO_CLIENTE",
                "cliente",
                clienteId,
                Map.of(
                        "motivo", motivo != null ? motivo : "Solicitação interna",
                        "escopo", "stub-operacional"),
                ip);

        return Map.of(
                "clienteId",
                clienteId,
                "status",
                "ANONIMIZADO",
                "aviso",
                "Stub operacional: revisar obrigações legais de retenção antes de produção com dados reais.");
    }

    private Map<String, Object> solicitacaoResumo(SolicitacaoEntity s) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", s.getId());
        row.put("titulo", s.getTitulo());
        row.put("status", s.getStatus());
        row.put("canal", s.getCanal());
        row.put("criadoEm", s.getCriadoEm());
        return row;
    }
}
