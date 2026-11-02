package br.jlturismo.voa.modules.promocoes.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.promocoes.application.PromocaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/promocoes")
public class PromocaoController {

    private final PromocaoService service;

    public PromocaoController(PromocaoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PromocaoService.PromocaoView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        return service.listAdmin(principal, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PromocaoService.PromocaoView create(
            @AuthenticationPrincipal VoaUserPrincipal principal, @Valid @RequestBody PromocaoBody body) {
        return service.create(principal, toCmd(body));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PromocaoService.PromocaoView update(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody PromocaoBody body) {
        return service.update(principal, id, toCmd(body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        service.delete(principal, id);
    }

    private PromocaoService.UpsertPromocaoCommand toCmd(PromocaoBody body) {
        return new PromocaoService.UpsertPromocaoCommand(
                body.titulo(),
                body.descricao(),
                body.imagemUrl(),
                body.slug(),
                body.destaque(),
                body.ativo(),
                body.validoDe(),
                body.validoAte());
    }

    public record PromocaoBody(
            @NotBlank String titulo,
            String descricao,
            String imagemUrl,
            String slug,
            Boolean destaque,
            Boolean ativo,
            LocalDate validoDe,
            LocalDate validoAte) {}
}
