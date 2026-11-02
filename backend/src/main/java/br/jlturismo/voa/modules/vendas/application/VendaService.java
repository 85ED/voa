package br.jlturismo.voa.modules.vendas.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.parceiros.application.PartnerScope;
import br.jlturismo.voa.modules.operacoes.infrastructure.OperacaoEntity;
import br.jlturismo.voa.modules.operacoes.infrastructure.OperacaoRepository;
import br.jlturismo.voa.modules.cotacoes.application.CotacaoService;
import br.jlturismo.voa.modules.cotacoes.domain.StatusCotacao;
import br.jlturismo.voa.modules.cotacoes.infrastructure.*;
import br.jlturismo.voa.modules.passageiros.application.PassageiroService;
import br.jlturismo.voa.modules.solicitacoes.application.ClienteService;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.infrastructure.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final VendaItemRepository vendaItemRepository;
    private final VendaIdempotenciaRepository idempotenciaRepository;
    private final CotacaoService cotacaoService;
    private final CotacaoRepository cotacaoRepository;
    private final CotacaoItemRepository cotacaoItemRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ClienteService clienteService;
    private final PassageiroService passageiroService;
    private final VendaWorkflowService vendaWorkflowService;
    private final OperacaoRepository operacaoRepository;

    public VendaService(
            VendaRepository vendaRepository,
            VendaItemRepository vendaItemRepository,
            VendaIdempotenciaRepository idempotenciaRepository,
            CotacaoService cotacaoService,
            CotacaoRepository cotacaoRepository,
            CotacaoItemRepository cotacaoItemRepository,
            SolicitacaoRepository solicitacaoRepository,
            ClienteService clienteService,
            PassageiroService passageiroService,
            VendaWorkflowService vendaWorkflowService,
            OperacaoRepository operacaoRepository) {
        this.vendaRepository = vendaRepository;
        this.vendaItemRepository = vendaItemRepository;
        this.idempotenciaRepository = idempotenciaRepository;
        this.cotacaoService = cotacaoService;
        this.cotacaoRepository = cotacaoRepository;
        this.cotacaoItemRepository = cotacaoItemRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteService = clienteService;
        this.passageiroService = passageiroService;
        this.vendaWorkflowService = vendaWorkflowService;
        this.operacaoRepository = operacaoRepository;
    }

    @Transactional
    public VendaDetailView converterDeCotacao(
            VoaUserPrincipal principal, UUID cotacaoId, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotenciaRepository.findByOrganizacaoIdAndIdempotencyKey(
                    principal.getOrganizacaoId(), idempotencyKey.trim());
            if (existing.isPresent()) {
                return get(principal, existing.get().getVendaId());
            }
        }

        CotacaoEntity cotacao = cotacaoService.findCotacaoEntity(principal, cotacaoId);
        if (cotacao.getStatus() != StatusCotacao.ACEITA) {
            throw new IllegalStateException("Venda só pode ser criada de cotação ACEITA");
        }
        if (vendaRepository.findByCotacaoId(cotacaoId).isPresent()) {
            throw new IllegalStateException("Cotação já convertida em venda");
        }

        SolicitacaoEntity solicitacao = solicitacaoRepository
                .findByIdAndOrganizacaoId(cotacao.getSolicitacaoId(), principal.getOrganizacaoId())
                .orElseThrow();

        VendaEntity venda = newVenda(
                principal,
                cotacao.getId(),
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getParceiroId(),
                cotacao.getTotalBruto(),
                cotacao.getMoeda(),
                false);

        if (solicitacao.getParceiroId() != null) {
            ensureOperacao(principal, venda, solicitacao.getParceiroId());
        }

        int ordem = 0;
        for (CotacaoItemEntity item : cotacaoItemRepository.findByCotacaoIdOrderByOrdemAsc(cotacaoId)) {
            vendaItemRepository.save(snapshotItem(venda.getId(), item, ordem++));
        }

        passageiroService.copySolicitacaoPassageirosToVenda(solicitacao.getId(), venda.getId());

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            saveIdempotency(principal.getOrganizacaoId(), idempotencyKey.trim(), venda.getId());
        }

        return buildDetail(principal, venda);
    }

    @Transactional
    public VendaDetailView criarManual(VoaUserPrincipal principal, ManualVendaCommand cmd) {
        clienteService.find(principal, cmd.clienteId());
        if (cmd.itens() == null || cmd.itens().isEmpty()) {
            throw new IllegalArgumentException("Venda manual precisa de itens");
        }
        BigDecimal total = cmd.itens().stream()
                .map(i -> i.valorUnitario().multiply(i.quantidade() != null ? i.quantidade() : BigDecimal.ONE))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        VendaEntity venda = newVenda(
                principal,
                null,
                cmd.solicitacaoId(),
                cmd.clienteId(),
                null,
                total,
                "BRL",
                true);

        int ordem = 0;
        for (ManualItemCommand item : cmd.itens()) {
            VendaItemEntity vi = new VendaItemEntity();
            vi.setId(UUID.randomUUID());
            vi.setVendaId(venda.getId());
            vi.setTipo(item.tipo());
            vi.setDescricao(item.descricao());
            vi.setQuantidade(item.quantidade() != null ? item.quantidade() : BigDecimal.ONE);
            vi.setValorUnitario(item.valorUnitario().setScale(4, RoundingMode.HALF_UP));
            vi.setMoeda("BRL");
            vi.setOrdem(ordem++);
            vendaItemRepository.save(vi);
        }

        passageiroService.linkPassageirosToVenda(principal, venda.getId(), cmd.passageiroIds());
        if (cmd.solicitacaoId() != null) {
            passageiroService.copySolicitacaoPassageirosToVenda(cmd.solicitacaoId(), venda.getId());
        }

        return buildDetail(principal, venda);
    }

    @Transactional(readOnly = true)
    public Page<VendaSummaryView> list(
            VoaUserPrincipal principal,
            StatusVenda status,
            String codigo,
            UUID parceiroId,
            Pageable pageable) {
        UUID filterParceiro = PartnerScope.effectiveParceiroFilter(principal, parceiroId);
        UUID orgId = principal.getOrganizacaoId();
        Page<VendaEntity> page;
        if (filterParceiro != null) {
            page = status == null
                    ? vendaRepository.findByOrganizacaoIdAndParceiroId(orgId, filterParceiro, pageable)
                    : vendaRepository.findByOrganizacaoIdAndParceiroIdAndStatus(
                            orgId, filterParceiro, status, pageable);
        } else if (codigo != null && !codigo.isBlank()) {
            String q = codigo.trim();
            page = status == null
                    ? vendaRepository.findByOrganizacaoIdAndCodigoContainingIgnoreCase(orgId, q, pageable)
                    : vendaRepository.findByOrganizacaoIdAndStatusAndCodigoContainingIgnoreCase(
                            orgId, status, q, pageable);
        } else {
            page = status == null
                    ? vendaRepository.findByOrganizacaoId(orgId, pageable)
                    : vendaRepository.findByOrganizacaoIdAndStatus(orgId, status, pageable);
        }
        return page.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public String exportCsv(VoaUserPrincipal principal) {
        StringBuilder csv = new StringBuilder("codigo,status,total,moeda,cliente_id,manual,criado_em\n");
        for (VendaEntity venda : vendaRepository.findByOrganizacaoIdOrderByCriadoEmDesc(principal.getOrganizacaoId())) {
            PartnerScope.assertCanAccessParceiro(principal, venda.getParceiroId());
            csv.append(escapeCsv(venda.getCodigo()))
                    .append(',')
                    .append(venda.getStatus())
                    .append(',')
                    .append(venda.getTotal())
                    .append(',')
                    .append(venda.getMoeda())
                    .append(',')
                    .append(venda.getClienteId())
                    .append(',')
                    .append(venda.isManual())
                    .append(',')
                    .append(venda.getCriadoEm())
                    .append('\n');
        }
        return csv.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Transactional(readOnly = true)
    public VendaDetailView get(VoaUserPrincipal principal, UUID id) {
        return buildDetail(principal, find(principal, id));
    }

    @Transactional
    public VendaDetailView confirmar(VoaUserPrincipal principal, UUID id) {
        vendaWorkflowService.transitionWithAudit(principal, id, StatusVenda.CONFIRMADA, null);
        return get(principal, id);
    }

    @Transactional
    public VendaDetailView cancelar(VoaUserPrincipal principal, UUID id, String motivo) {
        vendaWorkflowService.transitionWithAudit(principal, id, StatusVenda.CANCELADA, motivo);
        return get(principal, id);
    }

    private VendaEntity newVenda(
            VoaUserPrincipal principal,
            UUID cotacaoId,
            UUID solicitacaoId,
            UUID clienteId,
            UUID parceiroId,
            BigDecimal total,
            String moeda,
            boolean manual) {
        Instant now = Instant.now();
        VendaEntity venda = new VendaEntity();
        venda.setId(UUID.randomUUID());
        venda.setOrganizacaoId(principal.getOrganizacaoId());
        venda.setCotacaoId(cotacaoId);
        venda.setSolicitacaoId(solicitacaoId);
        venda.setClienteId(clienteId);
        venda.setParceiroId(parceiroId);
        venda.setCodigo(gerarCodigo(principal.getOrganizacaoId()));
        venda.setStatus(StatusVenda.ABERTA);
        venda.setTotal(total.setScale(4, RoundingMode.HALF_UP));
        venda.setMoeda(moeda != null ? moeda : "BRL");
        venda.setVendedorId(principal.getId());
        venda.setManual(manual);
        venda.setCriadoEm(now);
        venda.setAtualizadoEm(now);
        return vendaRepository.save(venda);
    }

    private String gerarCodigo(UUID orgId) {
        long seq = vendaRepository.countByOrganizacaoId(orgId) + 1;
        return "V" + String.format("%06d", seq);
    }

    private VendaItemEntity snapshotItem(UUID vendaId, CotacaoItemEntity item, int ordem) {
        VendaItemEntity vi = new VendaItemEntity();
        vi.setId(UUID.randomUUID());
        vi.setVendaId(vendaId);
        vi.setTipo(item.getTipo().name());
        vi.setDescricao(item.getDescricao());
        vi.setQuantidade(item.getQuantidade());
        vi.setValorUnitario(item.getValorUnitario());
        vi.setMoeda(item.getMoeda());
        vi.setOrdem(ordem);
        return vi;
    }

    private void saveIdempotency(UUID orgId, String key, UUID vendaId) {
        VendaIdempotenciaEntity row = new VendaIdempotenciaEntity();
        row.setOrganizacaoId(orgId);
        row.setIdempotencyKey(key);
        row.setVendaId(vendaId);
        row.setCriadoEm(Instant.now());
        idempotenciaRepository.save(row);
    }

    private VendaDetailView buildDetail(VoaUserPrincipal principal, VendaEntity venda) {
        List<VendaItemView> itens = vendaItemRepository.findByVendaIdOrderByOrdemAsc(venda.getId()).stream()
                .map(i -> new VendaItemView(
                        i.getId(),
                        i.getTipo(),
                        i.getDescricao(),
                        i.getQuantidade(),
                        i.getValorUnitario(),
                        i.getMoeda(),
                        i.getOrdem()))
                .toList();
        List<PassageiroService.PassageiroView> passageiros =
                passageiroService.listByVenda(principal, venda.getId());
        return new VendaDetailView(
                venda.getId(),
                venda.getCotacaoId(),
                venda.getSolicitacaoId(),
                venda.getClienteId(),
                venda.getCodigo(),
                venda.getStatus(),
                venda.getTotal(),
                venda.getMoeda(),
                venda.getVendedorId(),
                venda.isManual(),
                venda.getMotivoCancelamento(),
                venda.getCriadoEm(),
                venda.getFechadaEm(),
                itens,
                passageiros);
    }

    private VendaEntity find(VoaUserPrincipal principal, UUID id) {
        VendaEntity venda = vendaRepository
                .findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
        PartnerScope.assertCanAccessParceiro(principal, venda.getParceiroId());
        return venda;
    }

    private void ensureOperacao(VoaUserPrincipal principal, VendaEntity venda, UUID parceiroId) {
        if (operacaoRepository.findByVendaId(venda.getId()).isPresent()) {
            return;
        }
        OperacaoEntity operacao = new OperacaoEntity();
        operacao.setId(UUID.randomUUID());
        operacao.setOrganizacaoId(principal.getOrganizacaoId());
        operacao.setVendaId(venda.getId());
        operacao.setParceiroId(parceiroId);
        operacao.setCodigo(venda.getCodigo());
        operacao.setCriadoEm(Instant.now());
        operacaoRepository.save(operacao);
    }

    private VendaSummaryView toSummary(VendaEntity v) {
        return new VendaSummaryView(
                v.getId(),
                v.getCodigo(),
                v.getStatus(),
                v.getTotal(),
                v.getMoeda(),
                v.getClienteId(),
                v.isManual(),
                v.getCriadoEm());
    }

    public record ManualItemCommand(
            String tipo, String descricao, BigDecimal quantidade, BigDecimal valorUnitario) {}

    public record ManualVendaCommand(
            UUID solicitacaoId, UUID clienteId, List<ManualItemCommand> itens, List<UUID> passageiroIds) {}

    public record VendaItemView(
            UUID id,
            String tipo,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario,
            String moeda,
            int ordem) {}

    public record VendaSummaryView(
            UUID id,
            String codigo,
            StatusVenda status,
            BigDecimal total,
            String moeda,
            UUID clienteId,
            boolean manual,
            Instant criadoEm) {}

    public record VendaDetailView(
            UUID id,
            UUID cotacaoId,
            UUID solicitacaoId,
            UUID clienteId,
            String codigo,
            StatusVenda status,
            BigDecimal total,
            String moeda,
            UUID vendedorId,
            boolean manual,
            String motivoCancelamento,
            Instant criadoEm,
            Instant fechadaEm,
            List<VendaItemView> itens,
            List<PassageiroService.PassageiroView> passageiros) {}
}
