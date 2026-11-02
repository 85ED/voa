package br.jlturismo.voa.modules.lgpd.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.lgpd.application.LgpdClienteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/lgpd")
public class LgpdAdminController {

    private final LgpdClienteService service;

    public LgpdAdminController(LgpdClienteService service) {
        this.service = service;
    }

    @GetMapping("/clientes/{id}/export")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> exportCliente(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.exportar(principal, id);
    }

    @PostMapping("/clientes/{id}/solicitar-exclusao")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> solicitarExclusao(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody(required = false) ExclusaoBody body,
            HttpServletRequest request) {
        String motivo = body != null ? body.motivo() : null;
        return service.solicitarExclusao(principal, id, motivo, request.getRemoteAddr());
    }

    public record ExclusaoBody(String motivo) {}
}
