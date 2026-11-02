package br.jlturismo.voa.modules.dashboard.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.dashboard.application.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public DashboardService.DashboardResumo resumo(@AuthenticationPrincipal VoaUserPrincipal principal) {
        return dashboardService.resumo(principal);
    }
}
