package br.jlturismo.voa.modules.operacoes.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.operacoes.application.OperacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operacoes")
public class OperacaoController {

    private final OperacaoService service;

    public OperacaoController(OperacaoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','PARTNER')")
    public Page<OperacaoService.OperacaoView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @RequestParam(required = false) UUID parceiroId,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        return service.list(principal, parceiroId, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO')")
    public OperacaoService.OperacaoDetailView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal, id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public OperacaoService.OperacaoDetailView vincular(
            @AuthenticationPrincipal VoaUserPrincipal principal, @Valid @RequestBody VincularBody body) {
        return service.vincular(
                principal,
                new OperacaoService.VincularOperacaoCommand(body.vendaId(), body.parceiroId(), body.observacoes()));
    }

    @GetMapping("/{id}/comissao-linhas")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO','COMERCIAL')")
    public List<OperacaoService.ComissaoLinhaView> listLinhas(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.listComissaoLinhas(principal, id);
    }

    @PostMapping("/{id}/comissao-linhas")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public OperacaoService.ComissaoLinhaView addLinha(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ComissaoLinhaBody body) {
        return service.addComissaoLinha(
                principal,
                id,
                new OperacaoService.AddComissaoLinhaCommand(
                        body.descricao(), body.valor(), body.usarCalculoAutomatico()));
    }

    public record VincularBody(@NotNull UUID vendaId, UUID parceiroId, String observacoes) {}

    public record ComissaoLinhaBody(
            @NotBlank String descricao, BigDecimal valor, boolean usarCalculoAutomatico) {}
}
