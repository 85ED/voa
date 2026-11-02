package br.jlturismo.voa.modules.solicitacoes.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.solicitacoes.application.SolicitacaoService;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService service;

    public SolicitacaoController(SolicitacaoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public Page<SolicitacaoService.SolicitacaoView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @RequestParam(required = false) StatusSolicitacao status,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        return service.list(principal, status, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public SolicitacaoService.SolicitacaoView create(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @Valid @RequestBody CreateSolicitacaoBody body) {
        return service.create(principal, new SolicitacaoService.CreateSolicitacaoCommand(
                body.clienteId(),
                body.titulo(),
                body.destino(),
                body.dataIda(),
                body.dataVolta(),
                body.observacoes()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public SolicitacaoService.SolicitacaoView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal, id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public SolicitacaoService.SolicitacaoView update(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody UpdateSolicitacaoBody body) {
        return service.update(principal, id, new SolicitacaoService.UpdateSolicitacaoCommand(
                body.titulo(),
                body.destino(),
                body.dataIda(),
                body.dataVolta(),
                body.observacoes(),
                body.status(),
                body.responsavelId()));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public SolicitacaoService.SolicitacaoView cancelar(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return service.cancelar(principal, id, motivo);
    }

    public record CreateSolicitacaoBody(
            @NotNull UUID clienteId,
            @NotBlank String titulo,
            String destino,
            LocalDate dataIda,
            LocalDate dataVolta,
            String observacoes) {}

    public record UpdateSolicitacaoBody(
            String titulo,
            String destino,
            LocalDate dataIda,
            LocalDate dataVolta,
            String observacoes,
            StatusSolicitacao status,
            UUID responsavelId) {}
}
