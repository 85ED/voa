package br.jlturismo.voa.modules.parceiros.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.parceiros.application.ParceiroService;
import br.jlturismo.voa.modules.parceiros.application.ParceiroUsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parceiros")
public class ParceiroController {

    private final ParceiroService service;
    private final ParceiroUsuarioService usuarioService;

    public ParceiroController(ParceiroService service, ParceiroUsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO')")
    public Page<ParceiroService.ParceiroView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.list(principal, ativo, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ParceiroService.ParceiroView create(
            @AuthenticationPrincipal VoaUserPrincipal principal, @Valid @RequestBody ParceiroBody body) {
        return service.create(
                principal,
                new ParceiroService.CreateParceiroCommand(
                        body.nome(),
                        body.slug(),
                        body.nomeComercial(),
                        body.subtitulo(),
                        body.email(),
                        body.telefone(),
                        body.documento(),
                        body.percentualReferencia(),
                        body.ativo()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO')")
    public ParceiroService.ParceiroView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal, id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ParceiroService.ParceiroView update(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody ParceiroBody body) {
        return service.update(
                principal,
                id,
                new ParceiroService.UpdateParceiroCommand(
                        body.nome(),
                        body.nomeComercial(),
                        body.subtitulo(),
                        body.email(),
                        body.telefone(),
                        body.documento(),
                        body.percentualReferencia(),
                        body.ativo()));
    }

    @PatchMapping("/{id}/slug")
    @PreAuthorize("hasRole('ADMIN')")
    public ParceiroService.ParceiroView alterarSlug(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody SlugBody body) {
        return service.alterarSlug(principal, id, body.slug());
    }

    @PostMapping("/{id}/acesso")
    @PreAuthorize("hasRole('ADMIN')")
    public ParceiroUsuarioService.AcessoParceiroView criarAcesso(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody AcessoBody body) {
        return usuarioService.criarAcesso(
                principal, id, new ParceiroUsuarioService.CriarAcessoCommand(body.email(), body.nome()));
    }

    @PostMapping("/{id}/acesso/{usuarioId}/reset-senha")
    @PreAuthorize("hasRole('ADMIN')")
    public ParceiroUsuarioService.ResetSenhaView resetSenha(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @PathVariable UUID usuarioId) {
        return usuarioService.resetSenha(principal, id, usuarioId);
    }

    public record ParceiroBody(
            @NotBlank String nome,
            String slug,
            String nomeComercial,
            String subtitulo,
            String email,
            String telefone,
            String documento,
            BigDecimal percentualReferencia,
            Boolean ativo) {}

    public record SlugBody(@NotBlank String slug) {}

    public record AcessoBody(@NotBlank @Email String email, @NotBlank String nome) {}
}
