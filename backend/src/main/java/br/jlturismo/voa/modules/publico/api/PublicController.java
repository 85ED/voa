package br.jlturismo.voa.modules.publico.api;

import br.jlturismo.voa.modules.promocoes.application.PromocaoService;
import br.jlturismo.voa.modules.publico.application.PublicSolicitacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final PromocaoService promocaoService;
    private final PublicSolicitacaoService publicSolicitacaoService;

    public PublicController(PromocaoService promocaoService, PublicSolicitacaoService publicSolicitacaoService) {
        this.promocaoService = promocaoService;
        this.publicSolicitacaoService = publicSolicitacaoService;
    }

    @GetMapping("/promocoes")
    public List<PromocaoService.PromocaoView> promocoes() {
        return promocaoService.listPublicas();
    }

    @PostMapping("/solicitacoes-viagem")
    public PublicSolicitacaoService.PublicSolicitacaoView solicitarViagem(
            @Valid @RequestBody PublicSolicitacaoBody body) {
        return publicSolicitacaoService.criar(
                new PublicSolicitacaoService.PublicSolicitacaoCommand(
                        body.nome(),
                        body.email(),
                        body.telefone(),
                        body.titulo(),
                        body.destino(),
                        body.dataIda(),
                        body.dataVolta(),
                        body.observacoes()));
    }

    public record PublicSolicitacaoBody(
            @NotBlank String nome,
            @NotBlank @Email String email,
            String telefone,
            @NotBlank String titulo,
            @NotBlank String destino,
            LocalDate dataIda,
            LocalDate dataVolta,
            String observacoes) {}
}
