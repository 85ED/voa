package br.jlturismo.voa.modules.solicitacoes.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.solicitacoes.application.ClienteService;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.application.SolicitacaoService;
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
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public Page<ClienteService.ClienteView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        return service.list(principal, q, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public ClienteService.ClienteView create(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @Valid @RequestBody CreateClienteBody body) {
        return service.create(principal, new ClienteService.CreateClienteCommand(
                body.nome(), body.email(), body.telefone(), body.documento()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public ClienteService.ClienteView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal, id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES')")
    public ClienteService.ClienteView update(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody UpdateClienteBody body) {
        return service.update(principal, id, new ClienteService.UpdateClienteCommand(
                body.nome(), body.email(), body.telefone(), body.documento()));
    }

    public record CreateClienteBody(@NotBlank String nome, String email, String telefone, String documento) {}

    public record UpdateClienteBody(String nome, String email, String telefone, String documento) {}
}
