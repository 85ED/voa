package br.jlturismo.voa.modules.vendas.application;

import br.jlturismo.voa.modules.audit.application.AuditService;
import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.domain.VendaStateMachine;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaEntity;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class VendaWorkflowService {

    private final VendaRepository vendaRepository;
    private final AuditService auditService;

    public VendaWorkflowService(VendaRepository vendaRepository, AuditService auditService) {
        this.vendaRepository = vendaRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void syncPagamentoParcial(VoaUserPrincipal principal, UUID vendaId) {
        transition(principal, vendaId, StatusVenda.PAGAMENTO_PARCIAL);
    }

    @Transactional
    public void syncPagamentoQuitado(VoaUserPrincipal principal, UUID vendaId) {
        transition(principal, vendaId, StatusVenda.PAGAMENTO_CONFIRMADO);
        transition(principal, vendaId, VendaStateMachine.afterPagamentoConfirmado());
    }

    @Transactional
    public void syncEmissaoIniciada(VoaUserPrincipal principal, UUID vendaId) {
        VendaEntity venda = find(principal, vendaId);
        if (venda.getStatus() == StatusVenda.AGUARDANDO_EMISSAO) {
            transition(principal, vendaId, StatusVenda.EM_EMISSAO);
        }
    }

    @Transactional
    public void syncConcluidaSeAplicavel(VoaUserPrincipal principal, UUID vendaId) {
        VendaEntity venda = find(principal, vendaId);
        if (venda.getStatus() == StatusVenda.EM_EMISSAO) {
            transition(principal, vendaId, StatusVenda.CONCLUIDA);
        }
    }

    @Transactional
    public void transitionWithAudit(
            VoaUserPrincipal principal, UUID vendaId, StatusVenda to, String motivo) {
        VendaEntity venda = find(principal, vendaId);
        StatusVenda from = venda.getStatus();
        VendaStateMachine.assertCanTransition(from, to);
        venda.setStatus(to);
        venda.setAtualizadoEm(Instant.now());
        if (to == StatusVenda.CONFIRMADA) {
            venda.setFechadaEm(Instant.now());
        }
        if (to == StatusVenda.CANCELADA) {
            venda.setMotivoCancelamento(motivo);
        }
        vendaRepository.save(venda);
        auditService.record(
                principal.getOrganizacaoId(),
                principal.getId(),
                "VENDA_STATUS",
                "venda",
                vendaId,
                Map.of("de", from.name(), "para", to.name()),
                null);
    }

    private void transition(VoaUserPrincipal principal, UUID vendaId, StatusVenda to) {
        VendaEntity venda = find(principal, vendaId);
        StatusVenda from = venda.getStatus();
        if (from == to) {
            return;
        }
        VendaStateMachine.assertCanTransition(from, to);
        venda.setStatus(to);
        venda.setAtualizadoEm(Instant.now());
        vendaRepository.save(venda);
        auditService.record(
                principal.getOrganizacaoId(),
                principal.getId(),
                "VENDA_STATUS",
                "venda",
                vendaId,
                Map.of("de", from.name(), "para", to.name()),
                null);
    }

    private VendaEntity find(VoaUserPrincipal principal, UUID id) {
        return vendaRepository
                .findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
    }
}
