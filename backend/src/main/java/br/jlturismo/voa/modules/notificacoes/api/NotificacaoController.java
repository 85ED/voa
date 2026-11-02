package br.jlturismo.voa.modules.notificacoes.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.notificacoes.application.NotificacaoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notificacoes")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<NotificacaoService.NotificacaoView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        return service.list(principal, pageable);
    }

    @GetMapping("/nao-lidas/count")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> countNaoLidas(@AuthenticationPrincipal VoaUserPrincipal principal) {
        return Map.of("count", service.countNaoLidas(principal));
    }

    @PostMapping("/{id}/marcar-lida")
    @PreAuthorize("isAuthenticated()")
    public NotificacaoService.NotificacaoView marcarLida(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.marcarLida(principal, id);
    }

    @PostMapping("/marcar-todas-lidas")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Integer> marcarTodas(@AuthenticationPrincipal VoaUserPrincipal principal) {
        return Map.of("updated", service.marcarTodasLidas(principal));
    }
}
