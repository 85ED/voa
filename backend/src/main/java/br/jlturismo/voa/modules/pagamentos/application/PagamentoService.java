package br.jlturismo.voa.modules.pagamentos.application;

import br.jlturismo.voa.modules.audit.application.AuditService;
import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.pagamentos.domain.FormaPagamento;
import br.jlturismo.voa.modules.pagamentos.domain.StatusPagamento;
import br.jlturismo.voa.modules.pagamentos.infrastructure.*;
import br.jlturismo.voa.modules.vendas.application.VendaWorkflowService;
import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaEntity;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final ParcelaRepository parcelaRepository;
    private final PagamentoEventoRepository pagamentoEventoRepository;
    private final VendaRepository vendaRepository;
    private final VendaWorkflowService vendaWorkflowService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            ParcelaRepository parcelaRepository,
            PagamentoEventoRepository pagamentoEventoRepository,
            VendaRepository vendaRepository,
            VendaWorkflowService vendaWorkflowService,
            AuditService auditService,
            ObjectMapper objectMapper) {
        this.pagamentoRepository = pagamentoRepository;
        this.parcelaRepository = parcelaRepository;
        this.pagamentoEventoRepository = pagamentoEventoRepository;
        this.vendaRepository = vendaRepository;
        this.vendaWorkflowService = vendaWorkflowService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PagamentoDetailView registrar(
            VoaUserPrincipal principal, UUID vendaId, RegistrarPagamentoCommand cmd) {
        VendaEntity venda = findVenda(principal, vendaId);
        assertVendaAceitaPagamento(venda);

        if (cmd.parcelas() == null || cmd.parcelas().isEmpty()) {
            throw new IllegalArgumentException("Pagamento precisa de ao menos uma parcela");
        }

        BigDecimal somaParcelas = cmd.parcelas().stream()
                .map(ParcelaCommand::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        Instant now = Instant.now();
        PagamentoEntity pagamento = new PagamentoEntity();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrganizacaoId(principal.getOrganizacaoId());
        pagamento.setVendaId(vendaId);
        pagamento.setForma(cmd.forma().name());
        pagamento.setValorTotal(somaParcelas);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setObservacoes(cmd.observacoes());
        pagamento.setCriadoEm(now);
        pagamento.setAtualizadoEm(now);
        pagamentoRepository.save(pagamento);

        int numero = 1;
        for (ParcelaCommand p : cmd.parcelas()) {
            ParcelaEntity parcela = new ParcelaEntity();
            parcela.setId(UUID.randomUUID());
            parcela.setPagamentoId(pagamento.getId());
            parcela.setNumero(numero++);
            parcela.setVencimento(p.vencimento());
            parcela.setValor(p.valor().setScale(4, RoundingMode.HALF_UP));
            parcelaRepository.save(parcela);
        }

        appendEvento(pagamento.getId(), principal.getId(), "REGISTRO", Map.of("valorTotal", somaParcelas));
        auditService.record(
                principal.getOrganizacaoId(),
                principal.getId(),
                "PAGAMENTO_REGISTRADO",
                "pagamento",
                pagamento.getId(),
                Map.of("vendaId", vendaId, "valorTotal", somaParcelas),
                null);

        return buildDetail(pagamento);
    }

    @Transactional(readOnly = true)
    public PagamentoDetailView get(VoaUserPrincipal principal, UUID id) {
        return buildDetail(findPagamento(principal, id));
    }

    @Transactional(readOnly = true)
    public List<PagamentoDetailView> listByVenda(VoaUserPrincipal principal, UUID vendaId) {
        findVenda(principal, vendaId);
        return pagamentoRepository.findByVendaIdOrderByCriadoEmAsc(vendaId).stream()
                .map(this::buildDetail)
                .toList();
    }

    @Transactional
    public ParcelaView atualizarParcela(
            VoaUserPrincipal principal, UUID parcelaId, AtualizarParcelaCommand cmd) {
        ParcelaEntity parcela = parcelaRepository
                .findById(parcelaId)
                .orElseThrow(() -> new IllegalArgumentException("Parcela não encontrada"));
        PagamentoEntity pagamento = findPagamento(principal, parcela.getPagamentoId());
        if (pagamento.getStatus() == StatusPagamento.ESTORNADO) {
            throw new IllegalStateException("Pagamento estornado não pode ser alterado");
        }
        if (parcela.isConfirmada()) {
            throw new IllegalStateException("Parcela já confirmada; comprovante não pode ser alterado");
        }
        parcela.setComprovanteRef(cmd.comprovanteRef());
        parcelaRepository.save(parcela);
        appendEvento(
                pagamento.getId(),
                principal.getId(),
                "COMPROVANTE_ANEXADO",
                Map.of("parcelaId", parcelaId, "ref", cmd.comprovanteRef()));
        return toParcelaView(parcela);
    }

    @Transactional
    public PagamentoDetailView confirmarParcela(VoaUserPrincipal principal, UUID parcelaId) {
        ParcelaEntity parcela = parcelaRepository
                .findById(parcelaId)
                .orElseThrow(() -> new IllegalArgumentException("Parcela não encontrada"));
        PagamentoEntity pagamento = findPagamento(principal, parcela.getPagamentoId());
        if (pagamento.getStatus() == StatusPagamento.ESTORNADO) {
            throw new IllegalStateException("Pagamento estornado");
        }
        if (parcela.isConfirmada()) {
            return buildDetail(pagamento);
        }

        Instant now = Instant.now();
        parcela.setPagoEm(now);
        parcela.setConfirmadoPorId(principal.getId());
        parcela.setConfirmadoEm(now);
        parcelaRepository.save(parcela);

        recalcularStatusPagamento(pagamento);
        pagamento.setAtualizadoEm(now);
        pagamentoRepository.save(pagamento);

        VendaEntity venda = findVenda(principal, pagamento.getVendaId());
        if (pagamento.getStatus() == StatusPagamento.PARCIAL) {
            vendaWorkflowService.syncPagamentoParcial(principal, venda.getId());
        }

        appendEvento(
                pagamento.getId(),
                principal.getId(),
                "PARCELA_CONFIRMADA",
                Map.of("parcelaId", parcelaId, "valor", parcela.getValor()));
        auditService.record(
                principal.getOrganizacaoId(),
                principal.getId(),
                "PARCELA_CONFIRMADA",
                "parcela",
                parcelaId,
                Map.of("pagamentoId", pagamento.getId(), "vendaId", venda.getId()),
                null);

        return buildDetail(pagamento);
    }

    @Transactional
    public PagamentoDetailView confirmarQuitacao(VoaUserPrincipal principal, UUID pagamentoId) {
        PagamentoEntity pagamento = findPagamento(principal, pagamentoId);
        if (pagamento.getStatus() == StatusPagamento.ESTORNADO) {
            throw new IllegalStateException("Pagamento estornado");
        }
        if (pagamento.getStatus() == StatusPagamento.QUITADO) {
            return buildDetail(pagamento);
        }

        List<ParcelaEntity> parcelas = parcelaRepository.findByPagamentoIdOrderByNumeroAsc(pagamentoId);
        if (parcelas.isEmpty()) {
            throw new IllegalStateException("Pagamento sem parcelas");
        }
        if (parcelas.stream().anyMatch(p -> !p.isConfirmada())) {
            throw new IllegalStateException("Todas as parcelas devem estar confirmadas antes da quitação");
        }

        BigDecimal confirmado = parcelas.stream()
                .map(ParcelaEntity::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (confirmado.compareTo(pagamento.getValorTotal()) < 0) {
            throw new IllegalStateException("Valor confirmado inferior ao total do pagamento");
        }

        VendaEntity venda = findVenda(principal, pagamento.getVendaId());
        BigDecimal totalConfirmadoVenda = totalConfirmadoNaVenda(venda.getId());
        if (totalConfirmadoVenda.compareTo(venda.getTotal()) < 0) {
            throw new IllegalStateException(
                    "Quitação exige valor confirmado na venda >= total da venda ("
                            + totalConfirmadoVenda + " < " + venda.getTotal() + ")");
        }

        Instant now = Instant.now();
        pagamento.setStatus(StatusPagamento.QUITADO);
        pagamento.setAtualizadoEm(now);
        pagamentoRepository.save(pagamento);

        vendaWorkflowService.syncPagamentoQuitado(principal, venda.getId());

        appendEvento(pagamento.getId(), principal.getId(), "QUITACAO_CONFIRMADA", Map.of());
        auditService.record(
                principal.getOrganizacaoId(),
                principal.getId(),
                "PAGAMENTO_QUITADO",
                "pagamento",
                pagamento.getId(),
                Map.of("vendaId", venda.getId()),
                null);

        return buildDetail(pagamento);
    }

    @Transactional
    public PagamentoDetailView estornar(VoaUserPrincipal principal, UUID pagamentoId, String motivo) {
        PagamentoEntity pagamento = findPagamento(principal, pagamentoId);
        if (pagamento.getStatus() == StatusPagamento.ESTORNADO) {
            return buildDetail(pagamento);
        }
        Instant now = Instant.now();
        pagamento.setStatus(StatusPagamento.ESTORNADO);
        pagamento.setEstornadoEm(now);
        pagamento.setEstornadoPorId(principal.getId());
        pagamento.setAtualizadoEm(now);
        pagamentoRepository.save(pagamento);

        appendEvento(pagamento.getId(), principal.getId(), "ESTORNO", Map.of("motivo", motivo));
        auditService.record(
                principal.getOrganizacaoId(),
                principal.getId(),
                "PAGAMENTO_ESTORNADO",
                "pagamento",
                pagamento.getId(),
                Map.of("motivo", motivo != null ? motivo : ""),
                null);

        return buildDetail(pagamento);
    }

    private void recalcularStatusPagamento(PagamentoEntity pagamento) {
        List<ParcelaEntity> parcelas =
                parcelaRepository.findByPagamentoIdOrderByNumeroAsc(pagamento.getId());
        BigDecimal confirmado = parcelas.stream()
                .filter(ParcelaEntity::isConfirmada)
                .map(ParcelaEntity::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (confirmado.compareTo(BigDecimal.ZERO) == 0) {
            pagamento.setStatus(StatusPagamento.PENDENTE);
        } else if (confirmado.compareTo(pagamento.getValorTotal()) >= 0
                && parcelas.stream().allMatch(ParcelaEntity::isConfirmada)) {
            pagamento.setStatus(StatusPagamento.PARCIAL);
        } else {
            pagamento.setStatus(StatusPagamento.PARCIAL);
        }
    }

    private BigDecimal totalConfirmadoNaVenda(UUID vendaId) {
        return pagamentoRepository.findByVendaIdOrderByCriadoEmAsc(vendaId).stream()
                .filter(p -> p.getStatus() != StatusPagamento.ESTORNADO)
                .flatMap(p -> parcelaRepository.findByPagamentoIdOrderByNumeroAsc(p.getId()).stream())
                .filter(ParcelaEntity::isConfirmada)
                .map(ParcelaEntity::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertVendaAceitaPagamento(VendaEntity venda) {
        if (venda.getStatus() != StatusVenda.CONFIRMADA
                && venda.getStatus() != StatusVenda.PAGAMENTO_PARCIAL) {
            throw new IllegalStateException(
                    "Pagamento só pode ser registrado em venda CONFIRMADA ou PAGAMENTO_PARCIAL");
        }
    }

    private PagamentoEntity findPagamento(VoaUserPrincipal principal, UUID id) {
        return pagamentoRepository
                .findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado"));
    }

    private VendaEntity findVenda(VoaUserPrincipal principal, UUID vendaId) {
        return vendaRepository
                .findByIdAndOrganizacaoId(vendaId, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
    }

    private PagamentoDetailView buildDetail(PagamentoEntity pagamento) {
        List<ParcelaView> parcelas = parcelaRepository.findByPagamentoIdOrderByNumeroAsc(pagamento.getId()).stream()
                .map(this::toParcelaView)
                .toList();
        return new PagamentoDetailView(
                pagamento.getId(),
                pagamento.getVendaId(),
                pagamento.getForma(),
                pagamento.getValorTotal(),
                pagamento.getStatus(),
                pagamento.getObservacoes(),
                pagamento.getCriadoEm(),
                pagamento.getAtualizadoEm(),
                parcelas);
    }

    private ParcelaView toParcelaView(ParcelaEntity p) {
        return new ParcelaView(
                p.getId(),
                p.getNumero(),
                p.getVencimento(),
                p.getValor(),
                p.getComprovanteRef(),
                p.getPagoEm(),
                p.getConfirmadoPorId(),
                p.getConfirmadoEm());
    }

    private void appendEvento(UUID pagamentoId, UUID actorId, String tipo, Map<String, Object> payload) {
        PagamentoEventoEntity ev = new PagamentoEventoEntity();
        ev.setId(UUID.randomUUID());
        ev.setPagamentoId(pagamentoId);
        ev.setActorId(actorId);
        ev.setTipo(tipo);
        try {
            ev.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (Exception ignored) {
            ev.setPayloadJson("{}");
        }
        ev.setCriadoEm(Instant.now());
        pagamentoEventoRepository.save(ev);
    }

    public record ParcelaCommand(LocalDate vencimento, BigDecimal valor) {}

    public record RegistrarPagamentoCommand(
            FormaPagamento forma, String observacoes, List<ParcelaCommand> parcelas) {}

    public record AtualizarParcelaCommand(String comprovanteRef) {}

    public record ParcelaView(
            UUID id,
            int numero,
            LocalDate vencimento,
            BigDecimal valor,
            String comprovanteRef,
            Instant pagoEm,
            UUID confirmadoPorId,
            Instant confirmadoEm) {}

    public record PagamentoDetailView(
            UUID id,
            UUID vendaId,
            String forma,
            BigDecimal valorTotal,
            StatusPagamento status,
            String observacoes,
            Instant criadoEm,
            Instant atualizadoEm,
            List<ParcelaView> parcelas) {}
}
