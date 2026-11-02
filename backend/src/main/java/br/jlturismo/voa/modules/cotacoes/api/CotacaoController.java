package br.jlturismo.voa.modules.cotacoes.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.cotacoes.application.CotacaoService;
import br.jlturismo.voa.modules.cotacoes.domain.TipoCotacaoItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitacoes/{solicitacaoId}/cotacoes")
class SolicitacaoCotacaoController {

    private final CotacaoService service;

    public SolicitacaoCotacaoController(CotacaoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public List<CotacaoService.CotacaoSummaryView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID solicitacaoId) {
        return service.listBySolicitacao(principal, solicitacaoId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView create(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID solicitacaoId,
            @Valid @RequestBody CreateCotacaoBody body) {
        return service.create(
                principal,
                solicitacaoId,
                new CotacaoService.CreateCotacaoCommand(
                        body.validade(),
                        body.observacoes(),
                        body.copiarUltimaVersao() != null && body.copiarUltimaVersao()));
    }

    public record CreateCotacaoBody(
            @NotNull LocalDate validade, String observacoes, Boolean copiarUltimaVersao) {}
}

@RestController
@RequestMapping("/api/v1/cotacoes")
class CotacaoController {

    private final CotacaoService service;

    CotacaoController(CotacaoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public CotacaoService.CotacaoDetailView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.getDetail(principal, id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView update(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody UpdateCotacaoBody body) {
        return service.update(
                principal, id, new CotacaoService.UpdateCotacaoCommand(body.validade(), body.observacoes()));
    }

    @PostMapping("/{id}/itens")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView addItem(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ItemBody body) {
        return service.addItem(principal, id, toItemCmd(body));
    }

    @PatchMapping("/{id}/itens/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView updateItem(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody ItemBody body) {
        return service.updateItem(principal, id, itemId, toItemCmd(body));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView removeItem(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        return service.removeItem(principal, id, itemId);
    }

    @PostMapping("/{id}/alternativas")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView addAlternativa(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody AlternativaBody body) {
        return service.addAlternativa(
                principal,
                id,
                new CotacaoService.AlternativaCommand(
                        body.rotulo(),
                        body.resumo(),
                        body.origem(),
                        body.destino(),
                        body.valorTotal(),
                        body.moeda(),
                        body.detalhesJson()));
    }

    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView enviar(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.enviar(principal, id);
    }

    @PostMapping("/{id}/aceitar")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView aceitar(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.aceitar(principal, id);
    }

    @PostMapping("/{id}/recusar")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public CotacaoService.CotacaoDetailView recusar(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return service.recusar(principal, id, motivo);
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public List<CotacaoService.CotacaoEventoView> historico(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.historico(principal, id);
    }

    private static CotacaoService.ItemCommand toItemCmd(ItemBody body) {
        return new CotacaoService.ItemCommand(
                body.tipo(), body.descricao(), body.quantidade(), body.valorUnitario(), body.moeda());
    }

    public record UpdateCotacaoBody(LocalDate validade, String observacoes) {}

    public record ItemBody(
            @NotNull TipoCotacaoItem tipo,
            @NotBlank String descricao,
            BigDecimal quantidade,
            @NotNull BigDecimal valorUnitario,
            String moeda) {}

    public record AlternativaBody(
            @NotBlank String rotulo,
            String resumo,
            String origem,
            String destino,
            @NotNull BigDecimal valorTotal,
            String moeda,
            Map<String, Object> detalhesJson) {}
}
