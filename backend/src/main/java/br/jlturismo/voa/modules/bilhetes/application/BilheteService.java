package br.jlturismo.voa.modules.bilhetes.application;

import br.jlturismo.voa.modules.audit.application.AuditService;
import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.bilhetes.domain.BilheteStateMachine;
import br.jlturismo.voa.modules.bilhetes.domain.StatusBilhete;
import br.jlturismo.voa.modules.bilhetes.infrastructure.BilheteEntity;
import br.jlturismo.voa.modules.bilhetes.infrastructure.BilheteRepository;
import br.jlturismo.voa.modules.vendas.application.VendaWorkflowService;
import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaEntity;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BilheteService {

    private final BilheteRepository bilheteRepository;
    private final VendaRepository vendaRepository;
    private final VendaWorkflowService vendaWorkflowService;
    private final AuditService auditService;

    public BilheteService(
            BilheteRepository bilheteRepository,
            VendaRepository vendaRepository,
            VendaWorkflowService vendaWorkflowService,
            AuditService auditService) {
        this.bilheteRepository = bilheteRepository;
        this.vendaRepository = vendaRepository;
        this.vendaWorkflowService = vendaWorkflowService;
        this.auditService = auditService;
    }

    @Transactional
    public BilheteView criar(VoaUserPrincipal principal, UUID vendaId, CriarBilheteCommand cmd) {
        VendaEntity venda = findVenda(principal, vendaId);
        assertVendaAceitaEmissao(venda);

        Instant now = Instant.now();
        BilheteEntity bilhete = new BilheteEntity();
        bilhete.setId(UUID.randomUUID());
        bilhete.setOrganizacaoId(principal.getOrganizacaoId());
        bilhete.setVendaId(vendaId);
        bilhete.setTipo(cmd.tipo() != null ? cmd.tipo() : "AEREO");
        bilhete.setStatus(StatusBilhete.PENDENTE);
        bilhete.setLocalizador(cmd.localizador());
        bilhete.setEmissor(cmd.emissor());
        bilhete.setArquivoPath(cmd.arquivoPath());
        bilhete.setArquivoNome(cmd.arquivoNome());
        bilhete.setArquivoMime(cmd.arquivoMime());
        bilhete.setMetadadosJson(cmd.metadadosJson());
        bilhete.setCriadoEm(now);
        bilhete.setAtualizadoEm(now);
        bilheteRepository.save(bilhete);

        auditService.record(
                principal.getOrganizacaoId(),
                principal.getId(),
                "BILHETE_CRIADO",
                "bilhete",
                bilhete.getId(),
                Map.of("vendaId", vendaId),
                null);

        return toView(bilhete);
    }

    @Transactional(readOnly = true)
    public List<BilheteView> listByVenda(VoaUserPrincipal principal, UUID vendaId) {
        findVenda(principal, vendaId);
        return bilheteRepository.findByVendaIdOrderByCriadoEmAsc(vendaId).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public BilheteView get(VoaUserPrincipal principal, UUID id) {
        return toView(find(principal, id));
    }

    @Transactional
    public BilheteView atualizar(VoaUserPrincipal principal, UUID id, AtualizarBilheteCommand cmd) {
        BilheteEntity bilhete = find(principal, id);
        StatusBilhete from = bilhete.getStatus();
        StatusBilhete to = cmd.status() != null ? cmd.status() : from;

        if (cmd.localizador() != null) {
            bilhete.setLocalizador(cmd.localizador());
        }
        if (cmd.emissor() != null) {
            bilhete.setEmissor(cmd.emissor());
        }
        if (cmd.arquivoPath() != null) {
            bilhete.setArquivoPath(cmd.arquivoPath());
        }
        if (cmd.arquivoNome() != null) {
            bilhete.setArquivoNome(cmd.arquivoNome());
        }
        if (cmd.arquivoMime() != null) {
            bilhete.setArquivoMime(cmd.arquivoMime());
        }
        if (cmd.agendadoPara() != null) {
            bilhete.setAgendadoPara(cmd.agendadoPara());
        }

        if (to != from) {
            BilheteStateMachine.assertCanTransition(from, to);
            bilhete.setStatus(to);
            Instant now = Instant.now();
            if (to == StatusBilhete.AGENDADA && bilhete.getAgendadoPara() == null) {
                bilhete.setAgendadoPara(now);
            }
            if (to == StatusBilhete.EMITIDA) {
                bilhete.setEmitidoEm(now);
            }
            if (to == StatusBilhete.ENVIADA_CLIENTE) {
                bilhete.setEnviadoClienteEm(now);
            }
            auditService.record(
                    principal.getOrganizacaoId(),
                    principal.getId(),
                    "BILHETE_STATUS",
                    "bilhete",
                    bilhete.getId(),
                    Map.of("de", from.name(), "para", to.name()),
                    null);
        }

        bilhete.setAtualizadoEm(Instant.now());
        bilheteRepository.save(bilhete);

        if (to != from) {
            if (to == StatusBilhete.AGENDADA || to == StatusBilhete.EMITIDA) {
                vendaWorkflowService.syncEmissaoIniciada(principal, bilhete.getVendaId());
            }
            if (to == StatusBilhete.ENVIADA_CLIENTE) {
                List<BilheteEntity> todos =
                        bilheteRepository.findByVendaIdOrderByCriadoEmAsc(bilhete.getVendaId());
                boolean allDone = !todos.isEmpty()
                        && todos.stream()
                                .allMatch(b -> b.getStatus() == StatusBilhete.ENVIADA_CLIENTE
                                        || b.getStatus() == StatusBilhete.CANCELADA);
                if (allDone) {
                    vendaWorkflowService.syncConcluidaSeAplicavel(principal, bilhete.getVendaId());
                }
            }
        }

        return toView(bilhete);
    }

    private void assertVendaAceitaEmissao(VendaEntity venda) {
        if (venda.getStatus() != StatusVenda.AGUARDANDO_EMISSAO && venda.getStatus() != StatusVenda.EM_EMISSAO) {
            throw new IllegalStateException("Bilhete só após pagamento quitado (AGUARDANDO_EMISSAO ou EM_EMISSAO)");
        }
    }

    private BilheteEntity find(VoaUserPrincipal principal, UUID id) {
        return bilheteRepository
                .findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Bilhete não encontrado"));
    }

    private VendaEntity findVenda(VoaUserPrincipal principal, UUID vendaId) {
        return vendaRepository
                .findByIdAndOrganizacaoId(vendaId, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
    }

    private BilheteView toView(BilheteEntity b) {
        return new BilheteView(
                b.getId(),
                b.getVendaId(),
                b.getTipo(),
                b.getStatus(),
                b.getLocalizador(),
                b.getEmissor(),
                b.getArquivoPath(),
                b.getArquivoNome(),
                b.getArquivoMime(),
                b.getAgendadoPara(),
                b.getEmitidoEm(),
                b.getEnviadoClienteEm(),
                b.getMetadadosJson(),
                b.getCriadoEm(),
                b.getAtualizadoEm());
    }

    public record CriarBilheteCommand(
            String tipo,
            String localizador,
            String emissor,
            String arquivoPath,
            String arquivoNome,
            String arquivoMime,
            String metadadosJson) {}

    public record AtualizarBilheteCommand(
            StatusBilhete status,
            String localizador,
            String emissor,
            String arquivoPath,
            String arquivoNome,
            String arquivoMime,
            Instant agendadoPara) {}

    public record BilheteView(
            UUID id,
            UUID vendaId,
            String tipo,
            StatusBilhete status,
            String localizador,
            String emissor,
            String arquivoPath,
            String arquivoNome,
            String arquivoMime,
            Instant agendadoPara,
            Instant emitidoEm,
            Instant enviadoClienteEm,
            String metadadosJson,
            Instant criadoEm,
            Instant atualizadoEm) {}
}
