package br.jlturismo.voa.modules.vendas.api;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.vendas.application.VendaService;
import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @PostMapping("/cotacoes/{cotacaoId}/converter-venda")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public VendaService.VendaDetailView converter(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID cotacaoId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.converterDeCotacao(principal, cotacaoId, idempotencyKey);
    }

    @PostMapping("/vendas/manual")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public VendaService.VendaDetailView manual(
            @AuthenticationPrincipal VoaUserPrincipal principal, @Valid @RequestBody ManualVendaBody body) {
        List<VendaService.ManualItemCommand> itens = body.itens().stream()
                .map(i -> new VendaService.ManualItemCommand(i.tipo(), i.descricao(), i.quantidade(), i.valorUnitario()))
                .toList();
        return service.criarManual(
                principal,
                new VendaService.ManualVendaCommand(
                        body.solicitacaoId(), body.clienteId(), itens, body.passageiroIds()));
    }

    @GetMapping("/vendas")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR','PARTNER')")
    public Page<VendaService.VendaSummaryView> list(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @RequestParam(required = false) StatusVenda status,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) UUID parceiroId,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        return service.list(principal, status, codigo, parceiroId, pageable);
    }

    @GetMapping(value = "/vendas/export.csv", produces = "text/csv;charset=UTF-8")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','FINANCEIRO','AUDITOR')")
    public org.springframework.http.ResponseEntity<String> exportCsv(
            @AuthenticationPrincipal VoaUserPrincipal principal) {
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"vendas.csv\"")
                .body(service.exportCsv(principal));
    }

    @GetMapping("/vendas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL','OPERACOES','FINANCEIRO','AUDITOR','PARTNER')")
    public VendaService.VendaDetailView get(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal, id);
    }

    @PostMapping("/vendas/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public VendaService.VendaDetailView confirmar(
            @AuthenticationPrincipal VoaUserPrincipal principal, @PathVariable UUID id) {
        return service.confirmar(principal, id);
    }

    @PostMapping("/vendas/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN','COMERCIAL')")
    public VendaService.VendaDetailView cancelar(
            @AuthenticationPrincipal VoaUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return service.cancelar(principal, id, motivo);
    }

    public record ManualVendaBody(
            UUID solicitacaoId,
            @NotNull UUID clienteId,
            @NotNull List<ManualItemBody> itens,
            List<UUID> passageiroIds) {}

    public record ManualItemBody(
            @NotBlank String tipo,
            @NotBlank String descricao,
            BigDecimal quantidade,
            @NotNull BigDecimal valorUnitario) {}
}
