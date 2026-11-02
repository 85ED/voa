package br.jlturismo.voa.modules.core.config;

import br.jlturismo.voa.modules.auth.infrastructure.UsuarioEntity;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioRepository;
import br.jlturismo.voa.modules.bilhetes.domain.StatusBilhete;
import br.jlturismo.voa.modules.bilhetes.infrastructure.BilheteEntity;
import br.jlturismo.voa.modules.bilhetes.infrastructure.BilheteRepository;
import br.jlturismo.voa.modules.pagamentos.domain.StatusPagamento;
import br.jlturismo.voa.modules.pagamentos.infrastructure.PagamentoEntity;
import br.jlturismo.voa.modules.pagamentos.infrastructure.PagamentoRepository;
import br.jlturismo.voa.modules.promocoes.infrastructure.PromocaoEntity;
import br.jlturismo.voa.modules.promocoes.infrastructure.PromocaoRepository;
import br.jlturismo.voa.modules.solicitacoes.domain.CanalSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteRepository;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaEntity;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Component
@Profile("local")
@Order(50)
@ConditionalOnProperty(prefix = "voa.demo-seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeedRunner.class);
    private static final String DEMO_CLIENT_EMAIL = "demo.cliente@jlturismo.local";

    private final ClienteRepository clienteRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final PromocaoRepository promocaoRepository;
    private final VendaRepository vendaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final BilheteRepository bilheteRepository;
    private final UsuarioRepository usuarioRepository;
    private final VoaProperties properties;

    public DemoDataSeedRunner(
            ClienteRepository clienteRepository,
            SolicitacaoRepository solicitacaoRepository,
            PromocaoRepository promocaoRepository,
            VendaRepository vendaRepository,
            PagamentoRepository pagamentoRepository,
            BilheteRepository bilheteRepository,
            UsuarioRepository usuarioRepository,
            VoaProperties properties) {
        this.clienteRepository = clienteRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.promocaoRepository = promocaoRepository;
        this.vendaRepository = vendaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.bilheteRepository = bilheteRepository;
        this.usuarioRepository = usuarioRepository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        UUID orgId = OrgConstants.JL_ORG_ID;
        if (clienteRepository.search(orgId, DEMO_CLIENT_EMAIL, Pageable.ofSize(1)).hasContent()) {
            return;
        }

        Instant now = Instant.now();
        LocalDate hoje = LocalDate.now();

        UsuarioEntity admin = usuarioRepository
                .findByEmailIgnoreCase(properties.seed().adminEmail())
                .orElse(null);
        UUID vendedorId = admin != null ? admin.getId() : null;

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(UUID.randomUUID());
        cliente.setOrganizacaoId(orgId);
        cliente.setNome("Marina Costa");
        cliente.setEmail(DEMO_CLIENT_EMAIL);
        cliente.setTelefone("+55 11 99999-0001");
        cliente.setCriadoEm(now);
        cliente.setAtualizadoEm(now);
        clienteRepository.save(cliente);

        seedPromocoes(orgId, hoje, now);
        seedSolicitacoes(orgId, cliente.getId(), vendedorId, now);
        if (vendedorId != null) {
            seedOperacional(orgId, cliente.getId(), vendedorId, now);
        }

        log.info("Dados de demonstração VOA inseridos (profile local)");
    }

    private void seedPromocoes(UUID orgId, LocalDate hoje, Instant now) {
        String[][] promos = {
            {"demo-lisboa", "Lisboa em 7 dias", "Voos + hotel central + city tour. Saídas até dezembro.", "true"},
            {"demo-orlando", "Orlando família", "Ingressos parques + hospedagem. Condições especiais de câmbio.", "false"},
            {"demo-nordeste", "Nordeste all inclusive", "Resorts selecionados com transfer incluso.", "false"},
        };
        for (String[] row : promos) {
            PromocaoEntity p = new PromocaoEntity();
            p.setId(UUID.randomUUID());
            p.setOrganizacaoId(orgId);
            p.setSlug(row[0]);
            p.setTitulo(row[1]);
            p.setDescricao(row[2]);
            p.setDestaque(Boolean.parseBoolean(row[3]));
            p.setAtivo(true);
            p.setValidoDe(hoje.minusDays(7));
            p.setValidoAte(hoje.plusMonths(6));
            p.setCriadoEm(now);
            p.setAtualizadoEm(now);
            promocaoRepository.save(p);
        }
    }

    private void seedSolicitacoes(UUID orgId, UUID clienteId, UUID criadorId, Instant now) {
        SolicitacaoEntity rascunho = novaSolicitacao(orgId, clienteId, criadorId, now);
        rascunho.setTitulo("Viagem corporativa — SP x Recife");
        rascunho.setDestino("Recife, PE");
        rascunho.setStatus(StatusSolicitacao.RASCUNHO);
        solicitacaoRepository.save(rascunho);

        SolicitacaoEntity cotacao = novaSolicitacao(orgId, clienteId, criadorId, now);
        cotacao.setTitulo("Lua de mel — Portugal");
        cotacao.setDestino("Lisboa e Porto");
        cotacao.setStatus(StatusSolicitacao.EM_COTACAO);
        cotacao.setDataIda(LocalDate.now().plusMonths(2));
        cotacao.setDataVolta(LocalDate.now().plusMonths(2).plusDays(10));
        solicitacaoRepository.save(cotacao);
    }

    private SolicitacaoEntity novaSolicitacao(UUID orgId, UUID clienteId, UUID criadorId, Instant now) {
        SolicitacaoEntity s = new SolicitacaoEntity();
        s.setId(UUID.randomUUID());
        s.setOrganizacaoId(orgId);
        s.setClienteId(clienteId);
        s.setCanal(CanalSolicitacao.INTERNO);
        s.setCriadoPorId(criadorId);
        s.setCriadoEm(now);
        s.setAtualizadoEm(now);
        return s;
    }

    private void seedOperacional(UUID orgId, UUID clienteId, UUID vendedorId, Instant now) {
        VendaEntity aguardando = vendaManual(orgId, clienteId, vendedorId, "VOA-DEMO-001", StatusVenda.CONFIRMADA, "6200.00", now);
        vendaRepository.save(aguardando);
        pagamentoPendente(orgId, aguardando.getId(), "6200.00", now);

        VendaEntity parcial = vendaManual(orgId, clienteId, vendedorId, "VOA-DEMO-002", StatusVenda.PAGAMENTO_PARCIAL, "9800.00", now);
        vendaRepository.save(parcial);
        pagamentoParcial(orgId, parcial.getId(), "9800.00", now);

        VendaEntity emissao = vendaManual(orgId, clienteId, vendedorId, "VOA-DEMO-003", StatusVenda.AGUARDANDO_EMISSAO, "4100.00", now);
        vendaRepository.save(emissao);
        bilhete(orgId, emissao.getId(), StatusBilhete.PENDENTE, now);

        VendaEntity emitida = vendaManual(orgId, clienteId, vendedorId, "VOA-DEMO-004", StatusVenda.EM_EMISSAO, "3500.00", now);
        vendaRepository.save(emitida);
        bilhete(orgId, emitida.getId(), StatusBilhete.EMITIDA, now);
    }

    private VendaEntity vendaManual(
            UUID orgId, UUID clienteId, UUID vendedorId, String codigo, StatusVenda status, String total, Instant now) {
        VendaEntity v = new VendaEntity();
        v.setId(UUID.randomUUID());
        v.setOrganizacaoId(orgId);
        v.setClienteId(clienteId);
        v.setCodigo(codigo);
        v.setStatus(status);
        v.setTotal(new BigDecimal(total));
        v.setMoeda("BRL");
        v.setVendedorId(vendedorId);
        v.setManual(true);
        v.setCriadoEm(now);
        v.setAtualizadoEm(now);
        return v;
    }

    private void pagamentoPendente(UUID orgId, UUID vendaId, String valor, Instant now) {
        PagamentoEntity p = new PagamentoEntity();
        p.setId(UUID.randomUUID());
        p.setOrganizacaoId(orgId);
        p.setVendaId(vendaId);
        p.setForma("PIX");
        p.setValorTotal(new BigDecimal(valor));
        p.setStatus(StatusPagamento.PENDENTE);
        p.setCriadoEm(now);
        p.setAtualizadoEm(now);
        pagamentoRepository.save(p);
    }

    private void pagamentoParcial(UUID orgId, UUID vendaId, String valor, Instant now) {
        PagamentoEntity p = new PagamentoEntity();
        p.setId(UUID.randomUUID());
        p.setOrganizacaoId(orgId);
        p.setVendaId(vendaId);
        p.setForma("CARTAO");
        p.setValorTotal(new BigDecimal(valor));
        p.setStatus(StatusPagamento.PARCIAL);
        p.setCriadoEm(now);
        p.setAtualizadoEm(now);
        pagamentoRepository.save(p);
    }

    private void bilhete(UUID orgId, UUID vendaId, StatusBilhete status, Instant now) {
        BilheteEntity b = new BilheteEntity();
        b.setId(UUID.randomUUID());
        b.setOrganizacaoId(orgId);
        b.setVendaId(vendaId);
        b.setTipo("AEREO");
        b.setStatus(status);
        b.setCriadoEm(now);
        b.setAtualizadoEm(now);
        bilheteRepository.save(b);
    }
}
