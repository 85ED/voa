package br.jlturismo.voa.modules.publico.application;

import br.jlturismo.voa.modules.notificacoes.application.NotificacaoService;
import br.jlturismo.voa.modules.parceiros.application.ParceiroService;
import br.jlturismo.voa.modules.parceiros.infrastructure.ParceiroEntity;
import br.jlturismo.voa.modules.solicitacoes.domain.CanalSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteRepository;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PublicParceiroSolicitacaoService {

    private final ParceiroService parceiroService;
    private final ClienteRepository clienteRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final NotificacaoService notificacaoService;
    private final ObjectMapper objectMapper;

    public PublicParceiroSolicitacaoService(
            ParceiroService parceiroService,
            ClienteRepository clienteRepository,
            SolicitacaoRepository solicitacaoRepository,
            NotificacaoService notificacaoService,
            ObjectMapper objectMapper) {
        this.parceiroService = parceiroService;
        this.clienteRepository = clienteRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.notificacaoService = notificacaoService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PublicSolicitacaoService.PublicSolicitacaoView criar(String slug, PartnerSolicitacaoCommand cmd) {
        if (cmd.website() != null && !cmd.website().isBlank()) {
            throw new IllegalArgumentException("Requisição rejeitada");
        }
        if (!cmd.consentimentoLgpd()) {
            throw new IllegalArgumentException("Consentimento LGPD é obrigatório");
        }

        ParceiroEntity parceiro = parceiroService.findActiveBySlug(slug);
        Instant now = Instant.now();
        UUID orgId = parceiro.getOrganizacaoId();

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(UUID.randomUUID());
        cliente.setOrganizacaoId(orgId);
        cliente.setNome(cmd.nome());
        cliente.setEmail(cmd.email());
        cliente.setTelefone(cmd.telefone());
        cliente.setCriadoEm(now);
        cliente.setAtualizadoEm(now);
        clienteRepository.save(cliente);

        String titulo = cmd.tipoViagem().equals("IDA_VOLTA") ? "Ida e volta" : "Somente ida";
        titulo += ": " + cmd.origem() + " → " + cmd.destino();

        SolicitacaoEntity solicitacao = new SolicitacaoEntity();
        solicitacao.setId(UUID.randomUUID());
        solicitacao.setOrganizacaoId(orgId);
        solicitacao.setClienteId(cliente.getId());
        solicitacao.setParceiroId(parceiro.getId());
        solicitacao.setParceiroSlugSnapshot(parceiro.getSlug());
        solicitacao.setCanal(CanalSolicitacao.PARCEIRO_LINK);
        solicitacao.setOrigem(cmd.origem());
        solicitacao.setTitulo(titulo);
        solicitacao.setDestino(cmd.destino());
        solicitacao.setDataIda(cmd.dataIda());
        solicitacao.setDataVolta(cmd.tipoViagem().equals("IDA_VOLTA") ? cmd.dataVolta() : null);
        solicitacao.setObservacoes(cmd.observacoes());
        solicitacao.setMetadadosJson(toJson(cmd));
        solicitacao.setStatus(StatusSolicitacao.RASCUNHO);
        solicitacao.setCriadoEm(now);
        solicitacao.setAtualizadoEm(now);
        solicitacaoRepository.save(solicitacao);

        notificacaoService.criarParaOrganizacao(
                orgId,
                "Nova solicitação via parceiro",
                "%s enviou pedido pelo link %s".formatted(cmd.nome(), parceiro.getSlug()),
                "SOLICITACAO",
                solicitacao.getId());

        return new PublicSolicitacaoService.PublicSolicitacaoView(solicitacao.getId(), "RECEBIDA");
    }

    private String toJson(PartnerSolicitacaoCommand cmd) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("tipoViagem", cmd.tipoViagem());
        meta.put("flexibilidade", cmd.flexibilidade());
        meta.put("qtdPassageiros", cmd.qtdPassageiros());
        meta.put("faixaEtaria", cmd.faixaEtaria());
        meta.put("preferencias", cmd.preferencias());
        meta.put("bagagem", cmd.bagagem());
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public record PartnerSolicitacaoCommand(
            String tipoViagem,
            String origem,
            String destino,
            LocalDate dataIda,
            LocalDate dataVolta,
            String flexibilidade,
            Integer qtdPassageiros,
            String faixaEtaria,
            String preferencias,
            String bagagem,
            String nome,
            String telefone,
            String email,
            String observacoes,
            boolean consentimentoLgpd,
            String website) {}
}
