package br.jlturismo.voa.modules.audit.api;

import br.jlturismo.voa.modules.audit.application.AuditService;
import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/eventos")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public Page<AuditService.AuditEventView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @RequestParam(required = false) String entidadeTipo,
            @RequestParam(required = false) UUID entidadeId,
            @RequestParam(required = false) UUID actorId,
            @PageableDefault(size = 50, sort = "criadoEm") Pageable pageable) {
        return auditService.list(
                principal.getOrganizacaoId(), entidadeTipo, entidadeId, actorId, pageable);
    }
}
