package br.jlturismo.voa.modules.operacoes.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.core.config.VoaProperties;
import br.jlturismo.voa.modules.operacoes.infrastructure.OperacaoComissaoLinhaEntity;
import br.jlturismo.voa.modules.operacoes.infrastructure.OperacaoComissaoLinhaRepository;
import br.jlturismo.voa.modules.operacoes.infrastructure.OperacaoEntity;
import br.jlturismo.voa.modules.operacoes.infrastructure.OperacaoRepository;
import br.jlturismo.voa.modules.parceiros.application.ParceiroService;
import br.jlturismo.voa.modules.parceiros.application.PartnerScope;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaEntity;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaRepository;
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
public class OperacaoService {

    private final OperacaoRepository operacaoRepository;
    private final OperacaoComissaoLinhaRepository linhaRepository;
    private final VendaRepository vendaRepository;
    private final ParceiroService parceiroService;
    private final VoaProperties properties;

    public OperacaoService(
            OperacaoRepository operacaoRepository,
            OperacaoComissaoLinhaRepository linhaRepository,
            VendaRepository vendaRepository,
            ParceiroService parceiroService,
            VoaProperties properties) {
        this.operacaoRepository = operacaoRepository;
        this.linhaRepository = linhaRepository;
        this.vendaRepository = vendaRepository;
        this.parceiroService = parceiroService;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public Page<OperacaoView> list(VoaUserPrincipal principal, UUID parceiroId, Pageable pageable) {
        UUID filter = PartnerScope.effectiveParceiroFilter(principal, parceiroId);
        Page<OperacaoEntity> page = filter == null
                ? operacaoRepository.findByOrganizacaoId(principal.getOrganizacaoId(), pageable)
                : operacaoRepository.findByOrganizacaoIdAndParceiroId(
                        principal.getOrganizacaoId(), filter, pageable);
        return page.map(this::toView);
    }

    @Transactional(readOnly = true)
    public OperacaoDetailView get(VoaUserPrincipal principal, UUID id) {
        OperacaoEntity operacao = find(principal, id);
        return toDetail(operacao);
    }

    @Transactional
    public OperacaoDetailView vincular(VoaUserPrincipal principal, VincularOperacaoCommand cmd) {
        VendaEntity venda = vendaRepository
                .findByIdAndOrganizacaoId(cmd.vendaId(), principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));

        if (cmd.parceiroId() != null) {
            parceiroService.find(principal, cmd.parceiroId());
        }

        OperacaoEntity operacao = operacaoRepository.findByVendaId(venda.getId()).orElseGet(() -> {
            OperacaoEntity created = new OperacaoEntity();
            created.setId(UUID.randomUUID());
            created.setOrganizacaoId(principal.getOrganizacaoId());
            created.setVendaId(venda.getId());
            created.setCodigo(venda.getCodigo());
            created.setCriadoEm(Instant.now());
            return created;
        });

        operacao.setParceiroId(cmd.parceiroId());
        if (cmd.observacoes() != null) {
            operacao.setObservacoes(cmd.observacoes());
        }
        operacaoRepository.save(operacao);
        return toDetail(operacao);
    }

    @Transactional
    public ComissaoLinhaView addComissaoLinha(
            VoaUserPrincipal principal, UUID operacaoId, AddComissaoLinhaCommand cmd) {
        OperacaoEntity operacao = find(principal, operacaoId);
        BigDecimal valor = resolveValorManual(operacao, cmd);
        OperacaoComissaoLinhaEntity linha = new OperacaoComissaoLinhaEntity();
        linha.setId(UUID.randomUUID());
        linha.setOperacaoId(operacao.getId());
        linha.setDescricao(cmd.descricao());
        linha.setValor(valor);
        linha.setManual(!cmd.usarCalculoAutomatico());
        linha.setCriadoEm(Instant.now());
        return toLinhaView(linhaRepository.save(linha));
    }

    @Transactional(readOnly = true)
    public List<ComissaoLinhaView> listComissaoLinhas(VoaUserPrincipal principal, UUID operacaoId) {
        find(principal, operacaoId);
        return linhaRepository.findByOperacaoIdOrderByCriadoEmAsc(operacaoId).stream()
                .map(this::toLinhaView)
                .toList();
    }

    private BigDecimal resolveValorManual(OperacaoEntity operacao, AddComissaoLinhaCommand cmd) {
        if (cmd.usarCalculoAutomatico()) {
            if (!properties.comissao().autoEnabled()) {
                throw new IllegalStateException(
                        "Cálculo automático de comissão desabilitado (COMISSAO_AUTO_ENABLED=false)");
            }
            if (operacao.getParceiroId() == null) {
                throw new IllegalArgumentException("Operação sem parceiro para cálculo automático");
            }
            VendaEntity venda = vendaRepository
                    .findById(operacao.getVendaId())
                    .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
            var parceiro = parceiroService.findInOrg(operacao.getOrganizacaoId(), operacao.getParceiroId());
            if (parceiro.getPercentualReferencia() == null) {
                throw new IllegalArgumentException("Parceiro sem percentual de referência");
            }
            return venda.getTotal()
                    .multiply(parceiro.getPercentualReferencia())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }
        if (cmd.valor() == null) {
            throw new IllegalArgumentException("Valor manual é obrigatório");
        }
        return cmd.valor();
    }

    private OperacaoEntity find(VoaUserPrincipal principal, UUID id) {
        OperacaoEntity operacao = operacaoRepository
                .findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Operação não encontrada"));
        PartnerScope.assertCanAccessParceiro(principal, operacao.getParceiroId());
        return operacao;
    }

    private OperacaoView toView(OperacaoEntity e) {
        return new OperacaoView(e.getId(), e.getVendaId(), e.getParceiroId(), e.getCodigo(), e.getCriadoEm());
    }

    private OperacaoDetailView toDetail(OperacaoEntity e) {
        List<ComissaoLinhaView> linhas = linhaRepository.findByOperacaoIdOrderByCriadoEmAsc(e.getId()).stream()
                .map(this::toLinhaView)
                .toList();
        return new OperacaoDetailView(
                e.getId(), e.getVendaId(), e.getParceiroId(), e.getCodigo(), e.getObservacoes(), e.getCriadoEm(), linhas);
    }

    private ComissaoLinhaView toLinhaView(OperacaoComissaoLinhaEntity l) {
        return new ComissaoLinhaView(l.getId(), l.getDescricao(), l.getValor(), l.isManual(), l.getCriadoEm());
    }

    public record VincularOperacaoCommand(UUID vendaId, UUID parceiroId, String observacoes) {}

    public record AddComissaoLinhaCommand(String descricao, BigDecimal valor, boolean usarCalculoAutomatico) {}

    public record OperacaoView(UUID id, UUID vendaId, UUID parceiroId, String codigo, Instant criadoEm) {}

    public record OperacaoDetailView(
            UUID id,
            UUID vendaId,
            UUID parceiroId,
            String codigo,
            String observacoes,
            Instant criadoEm,
            List<ComissaoLinhaView> comissaoLinhas) {}

    public record ComissaoLinhaView(
            UUID id, String descricao, BigDecimal valor, boolean manual, Instant criadoEm) {}
}
