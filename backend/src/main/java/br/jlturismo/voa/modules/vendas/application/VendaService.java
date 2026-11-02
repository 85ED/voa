package br.jlturismo.voa.modules.vendas.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.cotacoes.application.CotacaoService;
import br.jlturismo.voa.modules.cotacoes.domain.StatusCotacao;
import br.jlturismo.voa.modules.cotacoes.infrastructure.*;
import br.jlturismo.voa.modules.passageiros.application.PassageiroService;
import br.jlturismo.voa.modules.solicitacoes.application.ClienteService;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.domain.VendaStateMachine;
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

    public VendaService(
            VendaRepository vendaRepository,
            VendaItemRepository vendaItemRepository,
            VendaIdempotenciaRepository idempotenciaRepository,
            CotacaoService cotacaoService,
            CotacaoRepository cotacaoRepository,
            CotacaoItemRepository cotacaoItemRepository,
            SolicitacaoRepository solicitacaoRepository,
            ClienteService clienteService,
            PassageiroService passageiroService) {
        this.vendaRepository = vendaRepository;
        this.vendaItemRepository = vendaItemRepository;
        this.idempotenciaRepository = idempotenciaRepository;
        this.cotacaoService = cotacaoService;
        this.cotacaoRepository = cotacaoRepository;
        this.cotacaoItemRepository = cotacaoItemRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteService = clienteService;
        this.passageiroService = passageiroService;
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
                cotacao.getTotalBruto(),
                cotacao.getMoeda(),
                false);

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
    public Page<VendaSummaryView> list(VoaUserPrincipal principal, StatusVenda status, Pageable pageable) {
        Page<VendaEntity> page = status == null
                ? vendaRepository.findByOrganizacaoId(principal.getOrganizacaoId(), pageable)
                : vendaRepository.findByOrganizacaoIdAndStatus(principal.getOrganizacaoId(), status, pageable);
        return page.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public VendaDetailView get(VoaUserPrincipal principal, UUID id) {
        return buildDetail(principal, find(principal, id));
    }

    @Transactional
    public VendaDetailView confirmar(VoaUserPrincipal principal, UUID id) {
        return transition(principal, id, StatusVenda.CONFIRMADA, null);
    }

    @Transactional
    public VendaDetailView cancelar(VoaUserPrincipal principal, UUID id, String motivo) {
        return transition(principal, id, StatusVenda.CANCELADA, motivo);
    }

    private VendaDetailView transition(
            VoaUserPrincipal principal, UUID id, StatusVenda to, String motivo) {
        VendaEntity venda = find(principal, id);
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
        return buildDetail(principal, venda);
    }

    private VendaEntity newVenda(
            VoaUserPrincipal principal,
            UUID cotacaoId,
            UUID solicitacaoId,
            UUID clienteId,
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
        return vendaRepository
                .findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
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
