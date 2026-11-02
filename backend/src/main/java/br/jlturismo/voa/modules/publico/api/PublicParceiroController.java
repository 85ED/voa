package br.jlturismo.voa.modules.publico.api;

import br.jlturismo.voa.modules.parceiros.application.ParceiroService;
import br.jlturismo.voa.modules.publico.application.PublicParceiroSolicitacaoService;
import br.jlturismo.voa.modules.publico.application.PublicSolicitacaoService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/public/parceiros")
public class PublicParceiroController {

    private final ParceiroService parceiroService;
    private final PublicParceiroSolicitacaoService solicitacaoService;

    public PublicParceiroController(
            ParceiroService parceiroService, PublicParceiroSolicitacaoService solicitacaoService) {
        this.parceiroService = parceiroService;
        this.solicitacaoService = solicitacaoService;
    }

    @GetMapping("/{slug}")
    public ParceiroService.PublicParceiroView get(@PathVariable String slug) {
        try {
            return parceiroService.resolvePublicBySlug(slug);
        } catch (IllegalArgumentException ex) {
            throw mapNotFound(ex);
        } catch (IllegalStateException ex) {
            throw mapNotFound(ex);
        }
    }

    @PostMapping("/{slug}/solicitacoes")
    public PublicSolicitacaoService.PublicSolicitacaoView solicitar(
            @PathVariable String slug, @RequestBody PartnerSolicitacaoBody body) {
        try {
            return solicitacaoService.criar(
                    slug,
                    new PublicParceiroSolicitacaoService.PartnerSolicitacaoCommand(
                            body.tipoViagem(),
                            body.origem(),
                            body.destino(),
                            body.dataIda(),
                            body.dataVolta(),
                            body.flexibilidade(),
                            body.qtdPassageiros(),
                            body.faixaEtaria(),
                            body.preferencias(),
                            body.bagagem(),
                            body.nome(),
                            body.telefone(),
                            body.email(),
                            body.observacoes(),
                            body.consentimentoLgpd(),
                            body.website()));
        } catch (IllegalArgumentException ex) {
            if ("Parceiro não encontrado".equals(ex.getMessage()) || ex.getMessage().contains("indisponível")) {
                throw mapNotFound(ex);
            }
            throw ex;
        } catch (IllegalStateException ex) {
            throw mapNotFound(ex);
        }
    }

    private static ResponseStatusException mapNotFound(RuntimeException ex) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    public record PartnerSolicitacaoBody(
            @NotBlank String tipoViagem,
            @NotBlank String origem,
            @NotBlank String destino,
            @NotNull LocalDate dataIda,
            LocalDate dataVolta,
            String flexibilidade,
            @NotNull @Min(1) Integer qtdPassageiros,
            String faixaEtaria,
            String preferencias,
            String bagagem,
            @NotBlank String nome,
            @NotBlank String telefone,
            String email,
            String observacoes,
            boolean consentimentoLgpd,
            String website) {}
}
