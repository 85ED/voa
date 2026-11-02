package br.jlturismo.voa.modules.pagamentos.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.pagamentos.application.PagamentoService;
import br.jlturismo.voa.modules.pagamentos.domain.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PagamentoController {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @PostMapping("/vendas/{vendaId}/pagamentos")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public PagamentoService.PagamentoDetailView registrar(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID vendaId,
            @Valid @RequestBody RegistrarPagamentoBody body) {
        List<PagamentoService.ParcelaCommand> parcelas = body.parcelas().stream()
                .map(p -> new PagamentoService.ParcelaCommand(p.vencimento(), p.valor()))
                .toList();
        return service.registrar(
                principal,
                vendaId,
                new PagamentoService.RegistrarPagamentoCommand(body.forma(), body.observacoes(), parcelas));
    }

    @GetMapping("/vendas/{vendaId}/pagamentos")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public List<PagamentoService.PagamentoDetailView> listByVenda(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID vendaId) {
        return service.listByVenda(principal, vendaId);
    }

    @GetMapping("/pagamentos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR')")
    public PagamentoService.PagamentoDetailView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal, id);
    }

    @PatchMapping("/parcelas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO','OPERACOES')")
    public PagamentoService.ParcelaView atualizarParcela(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String ref = body != null ? body.get("comprovanteRef") : null;
        return service.atualizarParcela(principal, id, new PagamentoService.AtualizarParcelaCommand(ref));
    }

    @PostMapping("/parcelas/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public PagamentoService.PagamentoDetailView confirmarParcela(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.confirmarParcela(principal, id);
    }

    @PostMapping("/pagamentos/{id}/confirmar-quitacao")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public PagamentoService.PagamentoDetailView confirmarQuitacao(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.confirmarQuitacao(principal, id);
    }

    @PostMapping("/pagamentos/{id}/estornar")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCEIRO')")
    public PagamentoService.PagamentoDetailView estornar(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return service.estornar(principal, id, motivo);
    }

    public record RegistrarPagamentoBody(
            @NotNull FormaPagamento forma,
            String observacoes,
            @NotEmpty List<ParcelaBody> parcelas) {}

    public record ParcelaBody(@NotNull LocalDate vencimento, @NotNull BigDecimal valor) {}
}
