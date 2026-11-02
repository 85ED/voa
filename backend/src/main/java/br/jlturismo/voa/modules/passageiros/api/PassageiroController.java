package br.jlturismo.voa.modules.passageiros.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.passageiros.application.PassageiroService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/passageiros")
public class PassageiroController {

    private final PassageiroService service;

    public PassageiroController(PassageiroService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public Page<PassageiroService.PassageiroView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.list(principal, q, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public PassageiroService.PassageiroView create(
            @AuthenticationPrincipal VoaUserPrincipal principal, @Valid @RequestBody CreateBody body) {
        return service.create(
                principal,
                new PassageiroService.CreatePassageiroCommand(
                        body.clienteId(),
                        body.nome(),
                        body.documento(),
                        body.dataNascimento(),
                        body.email(),
                        body.telefone()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public PassageiroService.PassageiroView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal, id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public PassageiroService.PassageiroView update(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody UpdateBody body) {
        return service.update(
                principal,
                id,
                new PassageiroService.UpdatePassageiroCommand(
                        body.clienteId(),
                        body.nome(),
                        body.documento(),
                        body.dataNascimento(),
                        body.email(),
                        body.telefone()));
    }

    public record CreateBody(
            UUID clienteId,
            @NotBlank String nome,
            String documento,
            LocalDate dataNascimento,
            String email,
            String telefone) {}

    public record UpdateBody(
            UUID clienteId,
            String nome,
            String documento,
            LocalDate dataNascimento,
            String email,
            String telefone) {}
}

@RestController
@RequestMapping("/api/v1/solicitacoes/{solicitacaoId}/passageiros")
class SolicitacaoPassageiroController {

    private final PassageiroService service;

    SolicitacaoPassageiroController(PassageiroService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public List<PassageiroService.PassageiroView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID solicitacaoId) {
        return service.listBySolicitacao(principal, solicitacaoId);
    }

    @PostMapping("/{passageiroId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public void link(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID solicitacaoId,
            @PathVariable UUID passageiroId) {
        service.linkToSolicitacao(principal, solicitacaoId, passageiroId);
    }

    @DeleteMapping("/{passageiroId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public void unlink(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID solicitacaoId,
            @PathVariable UUID passageiroId) {
        service.unlinkFromSolicitacao(principal, solicitacaoId, passageiroId);
    }
}
