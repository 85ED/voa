package br.jlturismo.voa.modules.bilhetes.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.bilhetes.application.BilheteService;
import br.jlturismo.voa.modules.bilhetes.domain.StatusBilhete;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class BilheteController {

    private final BilheteService service;

    public BilheteController(BilheteService service) {
        this.service = service;
    }

    @GetMapping("/vendas/{vendaId}/bilhetes")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public List<BilheteService.BilheteView> listByVenda(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID vendaId) {
        return service.listByVenda(principal, vendaId);
    }

    @PostMapping("/vendas/{vendaId}/bilhetes")
    @PreAuthorize("hasAnyRole('ADMIN','OPERACOES')")
    public BilheteService.BilheteView criar(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID vendaId,
            @RequestBody CriarBilheteBody body) {
        return service.criar(
                principal,
                vendaId,
                new BilheteService.CriarBilheteCommand(
                        body.tipo(),
                        body.localizador(),
                        body.emissor(),
                        body.arquivoPath(),
                        body.arquivoNome(),
                        body.arquivoMime(),
                        body.metadadosJson()));
    }

    @GetMapping("/bilhetes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public BilheteService.BilheteView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal, id);
    }

    @PatchMapping("/bilhetes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERACOES')")
    public BilheteService.BilheteView atualizar(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody AtualizarBilheteBody body) {
        return service.atualizar(
                principal,
                id,
                new BilheteService.AtualizarBilheteCommand(
                        body.status(),
                        body.localizador(),
                        body.emissor(),
                        body.arquivoPath(),
                        body.arquivoNome(),
                        body.arquivoMime(),
                        body.agendadoPara()));
    }

    public record CriarBilheteBody(
            String tipo,
            String localizador,
            String emissor,
            String arquivoPath,
            String arquivoNome,
            String arquivoMime,
            String metadadosJson) {}

    public record AtualizarBilheteBody(
            StatusBilhete status,
            String localizador,
            String emissor,
            String arquivoPath,
            String arquivoNome,
            String arquivoMime,
            Instant agendadoPara) {}
}
