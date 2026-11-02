package br.jlturismo.voa.modules.publico.application;

import br.jlturismo.voa.modules.core.config.OrgConstants;
import br.jlturismo.voa.modules.core.util.TextSanitizer;
import br.jlturismo.voa.modules.notificacoes.application.NotificacaoService;
import br.jlturismo.voa.modules.solicitacoes.domain.CanalSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteRepository;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PublicSolicitacaoService {

    private final ClienteRepository clienteRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final NotificacaoService notificacaoService;

    public PublicSolicitacaoService(
            ClienteRepository clienteRepository,
            SolicitacaoRepository solicitacaoRepository,
            NotificacaoService notificacaoService) {
        this.clienteRepository = clienteRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public PublicSolicitacaoView criar(PublicSolicitacaoCommand cmd) {
        UUID orgId = OrgConstants.JL_ORG_ID;
        Instant now = Instant.now();

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(UUID.randomUUID());
        cliente.setOrganizacaoId(orgId);
        cliente.setNome(TextSanitizer.plainTextRequired(cmd.nome(), "Nome"));
        cliente.setEmail(TextSanitizer.plainTextRequired(cmd.email(), "E-mail"));
        cliente.setTelefone(TextSanitizer.plainText(cmd.telefone()));
        cliente.setDocumento(null);
        cliente.setCriadoEm(now);
        cliente.setAtualizadoEm(now);
        clienteRepository.save(cliente);

        SolicitacaoEntity solicitacao = new SolicitacaoEntity();
        solicitacao.setId(UUID.randomUUID());
        solicitacao.setOrganizacaoId(orgId);
        solicitacao.setClienteId(cliente.getId());
        solicitacao.setTitulo(TextSanitizer.plainTextRequired(cmd.titulo(), "Título"));
        solicitacao.setDestino(TextSanitizer.plainTextRequired(cmd.destino(), "Destino"));
        solicitacao.setDataIda(cmd.dataIda());
        solicitacao.setDataVolta(cmd.dataVolta());
        solicitacao.setObservacoes(TextSanitizer.plainText(cmd.observacoes()));
        solicitacao.setCanal(CanalSolicitacao.PUBLICO);
        solicitacao.setStatus(StatusSolicitacao.RASCUNHO);
        solicitacao.setCriadoPorId(null);
        solicitacao.setResponsavelId(null);
        solicitacao.setCriadoEm(now);
        solicitacao.setAtualizadoEm(now);
        solicitacaoRepository.save(solicitacao);

        notificacaoService.criarParaOrganizacao(
                orgId,
                "Nova solicitação pública",
                "Cliente %s solicitou viagem para %s".formatted(cmd.nome(), cmd.destino()),
                "SOLICITACAO",
                solicitacao.getId());

        return new PublicSolicitacaoView(solicitacao.getId(), "RECEBIDA");
    }

    public record PublicSolicitacaoCommand(
            String nome,
            String email,
            String telefone,
            String titulo,
            String destino,
            LocalDate dataIda,
            LocalDate dataVolta,
            String observacoes) {}

    public record PublicSolicitacaoView(UUID solicitacaoId, String status) {}
}
