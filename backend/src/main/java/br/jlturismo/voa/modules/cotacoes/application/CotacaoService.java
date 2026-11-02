package br.jlturismo.voa.modules.cotacoes.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.cotacoes.domain.CotacaoStateMachine;
import br.jlturismo.voa.modules.cotacoes.domain.StatusCotacao;
import br.jlturismo.voa.modules.cotacoes.domain.TipoCotacaoItem;
import br.jlturismo.voa.modules.cotacoes.infrastructure.*;
import br.jlturismo.voa.modules.parceiros.application.PartnerScope;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
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
public class CotacaoService {

    private final CotacaoRepository cotacaoRepository;
    private final CotacaoItemRepository itemRepository;
    private final CotacaoAlternativaRepository alternativaRepository;
    private final CotacaoEventoRepository eventoRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public CotacaoService(
            CotacaoRepository cotacaoRepository,
            CotacaoItemRepository itemRepository,
            CotacaoAlternativaRepository alternativaRepository,
            CotacaoEventoRepository eventoRepository,
            SolicitacaoRepository solicitacaoRepository) {
        this.cotacaoRepository = cotacaoRepository;
        this.itemRepository = itemRepository;
        this.alternativaRepository = alternativaRepository;
        this.eventoRepository = eventoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<CotacaoSummaryView> listBySolicitacao(VoaUserPrincipal principal, UUID solicitacaoId) {
        SolicitacaoEntity solicitacao = findSolicitacao(principal, solicitacaoId);
        return cotacaoRepository.findBySolicitacaoIdOrderByVersaoDesc(solicitacao.getId()).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public CotacaoDetailView create(VoaUserPrincipal principal, UUID solicitacaoId, CreateCotacaoCommand cmd) {
        SolicitacaoEntity solicitacao = findSolicitacao(principal, solicitacaoId);
        if (solicitacao.getStatus() == StatusSolicitacao.CANCELADA
                || solicitacao.getStatus() == StatusSolicitacao.FECHADA) {
            throw new IllegalArgumentException("Solicitação não aceita novas cotações neste status");
        }
        if (cmd.validade().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("validade deve ser futura ou hoje");
        }

        int versao = cotacaoRepository.findMaxVersao(solicitacaoId) + 1;
        Instant now = Instant.now();

        CotacaoEntity entity = new CotacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(principal.getOrganizacaoId());
        entity.setSolicitacaoId(solicitacaoId);
        entity.setVersao(versao);
        entity.setValidade(cmd.validade());
        entity.setStatus(StatusCotacao.RASCUNHO);
        entity.setTotalBruto(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        entity.setMoeda("BRL");
        entity.setObservacoes(cmd.observacoes());
        entity.setCriadoPorId(principal.getId());
        entity.setCriadoEm(now);
        entity.setAtualizadoEm(now);
        cotacaoRepository.save(entity);

        if (cmd.copiarUltimaVersao()) {
            copyFromPreviousVersion(solicitacaoId, versao, entity.getId());
        }

        recalcTotal(entity.getId());
        registrarEvento(entity.getId(), null, StatusCotacao.RASCUNHO, principal.getId(), "Cotação criada");

        if (solicitacao.getStatus() == StatusSolicitacao.RASCUNHO) {
            solicitacao.setStatus(StatusSolicitacao.EM_COTACAO);
            solicitacao.setAtualizadoEm(now);
            solicitacaoRepository.save(solicitacao);
        }

        return getDetail(principal, entity.getId());
    }

    @Transactional(readOnly = true)
    public CotacaoDetailView getDetail(VoaUserPrincipal principal, UUID id) {
        CotacaoEntity cotacao = findCotacao(principal, id);
        return buildDetail(cotacao);
    }

    @Transactional
    public CotacaoDetailView update(VoaUserPrincipal principal, UUID id, UpdateCotacaoCommand cmd) {
        CotacaoEntity cotacao = findCotacao(principal, id);
        if (!CotacaoStateMachine.isEditable(cotacao.getStatus())) {
            throw new IllegalStateException("Cotação não pode ser editada no status " + cotacao.getStatus());
        }
        if (cmd.validade() != null) {
            if (cmd.validade().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("validade deve ser futura ou hoje");
            }
            cotacao.setValidade(cmd.validade());
        }
        if (cmd.observacoes() != null) {
            cotacao.setObservacoes(cmd.observacoes());
        }
        cotacao.setAtualizadoEm(Instant.now());
        cotacaoRepository.save(cotacao);
        return buildDetail(cotacao);
    }

    @Transactional
    public CotacaoDetailView addItem(VoaUserPrincipal principal, UUID cotacaoId, ItemCommand cmd) {
        CotacaoEntity cotacao = findCotacao(principal, cotacaoId);
        assertEditable(cotacao);
        CotacaoItemEntity item = newItem(cotacaoId, cmd, nextItemOrdem(cotacaoId));
        itemRepository.save(item);
        recalcTotal(cotacaoId);
        cotacao.setAtualizadoEm(Instant.now());
        cotacaoRepository.save(cotacao);
        return buildDetail(findCotacao(principal, cotacaoId));
    }

    @Transactional
    public CotacaoDetailView updateItem(
            VoaUserPrincipal principal, UUID cotacaoId, UUID itemId, ItemCommand cmd) {
        CotacaoEntity cotacao = findCotacao(principal, cotacaoId);
        assertEditable(cotacao);
        CotacaoItemEntity item = itemRepository
                .findByIdAndCotacaoId(itemId, cotacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));
        applyItem(item, cmd);
        itemRepository.save(item);
        recalcTotal(cotacaoId);
        cotacao.setAtualizadoEm(Instant.now());
        cotacaoRepository.save(cotacao);
        return buildDetail(findCotacao(principal, cotacaoId));
    }

    @Transactional
    public CotacaoDetailView removeItem(VoaUserPrincipal principal, UUID cotacaoId, UUID itemId) {
        CotacaoEntity cotacao = findCotacao(principal, cotacaoId);
        assertEditable(cotacao);
        itemRepository.deleteByCotacaoIdAndId(cotacaoId, itemId);
        recalcTotal(cotacaoId);
        cotacao.setAtualizadoEm(Instant.now());
        cotacaoRepository.save(cotacao);
        return buildDetail(findCotacao(principal, cotacaoId));
    }

    @Transactional
    public CotacaoDetailView addAlternativa(
            VoaUserPrincipal principal, UUID cotacaoId, AlternativaCommand cmd) {
        CotacaoEntity cotacao = findCotacao(principal, cotacaoId);
        assertEditable(cotacao);
        int ordem = alternativaRepository.findByCotacaoIdOrderByOrdemAsc(cotacaoId).size();
        CotacaoAlternativaEntity alt = new CotacaoAlternativaEntity();
        alt.setId(UUID.randomUUID());
        alt.setCotacaoId(cotacaoId);
        alt.setRotulo(cmd.rotulo());
        alt.setResumo(cmd.resumo());
        alt.setOrigem(cmd.origem());
        alt.setDestino(cmd.destino());
        alt.setValorTotal(cmd.valorTotal().setScale(4, RoundingMode.HALF_UP));
        alt.setMoeda(cmd.moeda() != null ? cmd.moeda() : "BRL");
        alt.setDetalhesJson(cmd.detalhesJson());
        alt.setOrdem(ordem);
        alternativaRepository.save(alt);
        cotacao.setAtualizadoEm(Instant.now());
        cotacaoRepository.save(cotacao);
        return buildDetail(findCotacao(principal, cotacaoId));
    }

    @Transactional
    public CotacaoDetailView enviar(VoaUserPrincipal principal, UUID id) {
        return transition(principal, id, StatusCotacao.ENVIADA, null);
    }

    @Transactional
    public CotacaoDetailView aceitar(VoaUserPrincipal principal, UUID id) {
        CotacaoDetailView detail = transition(principal, id, StatusCotacao.ACEITA, null);
        CotacaoEntity cotacao = findCotacao(principal, id);
        SolicitacaoEntity solicitacao = findSolicitacao(principal, cotacao.getSolicitacaoId());
        solicitacao.setStatus(StatusSolicitacao.FECHADA);
        solicitacao.setAtualizadoEm(Instant.now());
        solicitacaoRepository.save(solicitacao);
        return detail;
    }

    @Transactional
    public CotacaoDetailView recusar(VoaUserPrincipal principal, UUID id, String motivo) {
        return transition(principal, id, StatusCotacao.RECUSADA, motivo);
    }

    @Transactional(readOnly = true)
    public List<CotacaoEventoView> historico(VoaUserPrincipal principal, UUID id) {
        findCotacao(principal, id);
        return eventoRepository.findByCotacaoIdOrderByCriadoEmDesc(id).stream()
                .map(e -> new CotacaoEventoView(
                        e.getId(),
                        e.getDeStatus(),
                        e.getParaStatus(),
                        e.getActorId(),
                        e.getObservacao(),
                        e.getCriadoEm()))
                .toList();
    }

    public CotacaoEntity findCotacaoEntity(VoaUserPrincipal principal, UUID id) {
        return findCotacao(principal, id);
    }

    private CotacaoDetailView transition(
            VoaUserPrincipal principal, UUID id, StatusCotacao to, String observacao) {
        CotacaoEntity cotacao = findCotacao(principal, id);
        StatusCotacao from = cotacao.getStatus();
        CotacaoStateMachine.assertCanTransition(from, to);

        if (to == StatusCotacao.ENVIADA) {
            validateReadyToSend(cotacao);
            if (cotacao.getValidade().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("validade expirada");
            }
        }

        cotacao.setStatus(to);
        cotacao.setAtualizadoEm(Instant.now());
        cotacaoRepository.save(cotacao);
        registrarEvento(cotacao.getId(), from, to, principal.getId(), observacao);
        return buildDetail(cotacao);
    }

    private void validateReadyToSend(CotacaoEntity cotacao) {
        List<CotacaoItemEntity> itens = itemRepository.findByCotacaoIdOrderByOrdemAsc(cotacao.getId());
        List<CotacaoAlternativaEntity> alts =
                alternativaRepository.findByCotacaoIdOrderByOrdemAsc(cotacao.getId());
        if (itens.isEmpty() && alts.isEmpty()) {
            throw new IllegalArgumentException("Cotação precisa de itens ou alternativas de voo");
        }
        if (cotacao.getTotalBruto().compareTo(BigDecimal.ZERO) <= 0 && itens.isEmpty()) {
            throw new IllegalArgumentException("Total da cotação deve ser positivo");
        }
    }

    private void copyFromPreviousVersion(UUID solicitacaoId, int newVersao, UUID newCotacaoId) {
        if (newVersao <= 1) {
            return;
        }
        List<CotacaoEntity> list = cotacaoRepository.findBySolicitacaoIdOrderByVersaoDesc(solicitacaoId);
        CotacaoEntity previous = list.stream()
                .filter(c -> c.getVersao() == newVersao - 1)
                .findFirst()
                .orElse(null);
        if (previous == null) {
            return;
        }
        for (CotacaoItemEntity item : itemRepository.findByCotacaoIdOrderByOrdemAsc(previous.getId())) {
            CotacaoItemEntity copy = new CotacaoItemEntity();
            copy.setId(UUID.randomUUID());
            copy.setCotacaoId(newCotacaoId);
            copy.setTipo(item.getTipo());
            copy.setDescricao(item.getDescricao());
            copy.setQuantidade(item.getQuantidade());
            copy.setValorUnitario(item.getValorUnitario());
            copy.setMoeda(item.getMoeda());
            copy.setOrdem(item.getOrdem());
            itemRepository.save(copy);
        }
        for (CotacaoAlternativaEntity alt :
                alternativaRepository.findByCotacaoIdOrderByOrdemAsc(previous.getId())) {
            CotacaoAlternativaEntity copy = new CotacaoAlternativaEntity();
            copy.setId(UUID.randomUUID());
            copy.setCotacaoId(newCotacaoId);
            copy.setRotulo(alt.getRotulo());
            copy.setResumo(alt.getResumo());
            copy.setOrigem(alt.getOrigem());
            copy.setDestino(alt.getDestino());
            copy.setValorTotal(alt.getValorTotal());
            copy.setMoeda(alt.getMoeda());
            copy.setDetalhesJson(alt.getDetalhesJson());
            copy.setOrdem(alt.getOrdem());
            alternativaRepository.save(copy);
        }
    }

    private void recalcTotal(UUID cotacaoId) {
        BigDecimal total = itemRepository.findByCotacaoIdOrderByOrdemAsc(cotacaoId).stream()
                .map(i -> i.getValorUnitario().multiply(i.getQuantidade()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        CotacaoEntity cotacao = cotacaoRepository.findById(cotacaoId).orElseThrow();
        cotacao.setTotalBruto(total);
        cotacaoRepository.save(cotacao);
    }

    private int nextItemOrdem(UUID cotacaoId) {
        return itemRepository.findByCotacaoIdOrderByOrdemAsc(cotacaoId).size();
    }

    private CotacaoItemEntity newItem(UUID cotacaoId, ItemCommand cmd, int ordem) {
        CotacaoItemEntity item = new CotacaoItemEntity();
        item.setId(UUID.randomUUID());
        item.setCotacaoId(cotacaoId);
        applyItem(item, cmd);
        item.setOrdem(ordem);
        return item;
    }

    private void applyItem(CotacaoItemEntity item, ItemCommand cmd) {
        item.setTipo(cmd.tipo());
        item.setDescricao(cmd.descricao());
        item.setQuantidade(cmd.quantidade() != null ? cmd.quantidade() : BigDecimal.ONE);
        item.setValorUnitario(cmd.valorUnitario().setScale(4, RoundingMode.HALF_UP));
        item.setMoeda(cmd.moeda() != null ? cmd.moeda() : "BRL");
    }

    private void assertEditable(CotacaoEntity cotacao) {
        if (!CotacaoStateMachine.isEditable(cotacao.getStatus())) {
            throw new IllegalStateException("Cotação não editável no status " + cotacao.getStatus());
        }
    }

    private void registrarEvento(
            UUID cotacaoId, StatusCotacao de, StatusCotacao para, UUID actorId, String observacao) {
        CotacaoEventoEntity evento = new CotacaoEventoEntity();
        evento.setId(UUID.randomUUID());
        evento.setCotacaoId(cotacaoId);
        evento.setDeStatus(de != null ? de.name() : null);
        evento.setParaStatus(para.name());
        evento.setActorId(actorId);
        evento.setObservacao(observacao);
        evento.setCriadoEm(Instant.now());
        eventoRepository.save(evento);
    }

    private CotacaoDetailView buildDetail(CotacaoEntity cotacao) {
        List<ItemView> itens = itemRepository.findByCotacaoIdOrderByOrdemAsc(cotacao.getId()).stream()
                .map(i -> new ItemView(
                        i.getId(),
                        i.getTipo(),
                        i.getDescricao(),
                        i.getQuantidade(),
                        i.getValorUnitario(),
                        i.getMoeda(),
                        i.getOrdem()))
                .toList();
        List<AlternativaView> alternativas =
                alternativaRepository.findByCotacaoIdOrderByOrdemAsc(cotacao.getId()).stream()
                        .map(a -> new AlternativaView(
                                a.getId(),
                                a.getRotulo(),
                                a.getResumo(),
                                a.getOrigem(),
                                a.getDestino(),
                                a.getValorTotal(),
                                a.getMoeda(),
                                a.getDetalhesJson(),
                                a.getOrdem()))
                        .toList();
        List<CotacaoEventoView> historico = eventoRepository.findByCotacaoIdOrderByCriadoEmDesc(cotacao.getId())
                .stream()
                .map(e -> new CotacaoEventoView(
                        e.getId(),
                        e.getDeStatus(),
                        e.getParaStatus(),
                        e.getActorId(),
                        e.getObservacao(),
                        e.getCriadoEm()))
                .toList();
        return new CotacaoDetailView(
                cotacao.getId(),
                cotacao.getSolicitacaoId(),
                cotacao.getVersao(),
                cotacao.getValidade(),
                cotacao.getStatus(),
                cotacao.getTotalBruto(),
                cotacao.getMoeda(),
                cotacao.getObservacoes(),
                cotacao.getCriadoPorId(),
                cotacao.getCriadoEm(),
                cotacao.getAtualizadoEm(),
                itens,
                alternativas,
                historico);
    }

    private CotacaoSummaryView toSummary(CotacaoEntity c) {
        return new CotacaoSummaryView(
                c.getId(),
                c.getSolicitacaoId(),
                c.getVersao(),
                c.getValidade(),
                c.getStatus(),
                c.getTotalBruto(),
                c.getMoeda(),
                c.getCriadoEm());
    }

    private SolicitacaoEntity findSolicitacao(VoaUserPrincipal principal, UUID id) {
        SolicitacaoEntity entity = solicitacaoRepository
                .findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        PartnerScope.assertCanAccessParceiro(principal, entity.getParceiroId());
        return entity;
    }

    private CotacaoEntity findCotacao(VoaUserPrincipal principal, UUID id) {
        CotacaoEntity cotacao = cotacaoRepository
                .findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Cotação não encontrada"));
        findSolicitacao(principal, cotacao.getSolicitacaoId());
        return cotacao;
    }

    public record CreateCotacaoCommand(LocalDate validade, String observacoes, boolean copiarUltimaVersao) {}

    public record UpdateCotacaoCommand(LocalDate validade, String observacoes) {}

    public record ItemCommand(
            TipoCotacaoItem tipo,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario,
            String moeda) {}

    public record AlternativaCommand(
            String rotulo,
            String resumo,
            String origem,
            String destino,
            BigDecimal valorTotal,
            String moeda,
            Map<String, Object> detalhesJson) {}

    public record CotacaoSummaryView(
            UUID id,
            UUID solicitacaoId,
            int versao,
            LocalDate validade,
            StatusCotacao status,
            BigDecimal totalBruto,
            String moeda,
            Instant criadoEm) {}

    public record ItemView(
            UUID id,
            TipoCotacaoItem tipo,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario,
            String moeda,
            int ordem) {}

    public record AlternativaView(
            UUID id,
            String rotulo,
            String resumo,
            String origem,
            String destino,
            BigDecimal valorTotal,
            String moeda,
            Map<String, Object> detalhesJson,
            int ordem) {}

    public record CotacaoEventoView(
            UUID id,
            String deStatus,
            String paraStatus,
            UUID actorId,
            String observacao,
            Instant criadoEm) {}

    public record CotacaoDetailView(
            UUID id,
            UUID solicitacaoId,
            int versao,
            LocalDate validade,
            StatusCotacao status,
            BigDecimal totalBruto,
            String moeda,
            String observacoes,
            UUID criadoPorId,
            Instant criadoEm,
            Instant atualizadoEm,
            List<ItemView> itens,
            List<AlternativaView> alternativas,
            List<CotacaoEventoView> historico) {}
}
