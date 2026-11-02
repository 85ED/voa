package br.jlturismo.voa.modules.dashboard.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.bilhetes.domain.StatusBilhete;
import br.jlturismo.voa.modules.bilhetes.infrastructure.BilheteRepository;
import br.jlturismo.voa.modules.pagamentos.domain.StatusPagamento;
import br.jlturismo.voa.modules.pagamentos.infrastructure.PagamentoRepository;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final VendaRepository vendaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final BilheteRepository bilheteRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public DashboardService(
            VendaRepository vendaRepository,
            PagamentoRepository pagamentoRepository,
            BilheteRepository bilheteRepository,
            SolicitacaoRepository solicitacaoRepository) {
        this.vendaRepository = vendaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.bilheteRepository = bilheteRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumo resumo(VoaUserPrincipal principal) {
        UUID orgId = principal.getOrganizacaoId();

        long vendasAguardandoPagamento =
                vendaRepository.countByOrganizacaoIdAndStatus(orgId, StatusVenda.CONFIRMADA);
        long vendasPagamentoParcial =
                vendaRepository.countByOrganizacaoIdAndStatus(orgId, StatusVenda.PAGAMENTO_PARCIAL);
        long emissaoPendente = vendaRepository.countByOrganizacaoIdAndStatusIn(
                orgId, List.of(StatusVenda.AGUARDANDO_EMISSAO, StatusVenda.EM_EMISSAO));
        long bilhetesPendentes = bilheteRepository.countByOrganizacaoIdAndStatusIn(
                orgId, List.of(StatusBilhete.PENDENTE, StatusBilhete.AGENDADA));
        long bilhetesAguardandoEnvio =
                bilheteRepository.countByOrganizacaoIdAndStatusIn(orgId, List.of(StatusBilhete.EMITIDA));
        long pagamentosAbertos = pagamentoRepository.countByOrganizacaoIdAndStatusIn(
                orgId, List.of(StatusPagamento.PENDENTE, StatusPagamento.PARCIAL));
        long solicitacoesAtivas = solicitacaoRepository.countByOrganizacaoIdAndStatusIn(
                orgId, List.of(StatusSolicitacao.RASCUNHO, StatusSolicitacao.EM_COTACAO));

        return new DashboardResumo(
                vendasAguardandoPagamento,
                vendasPagamentoParcial,
                emissaoPendente,
                bilhetesPendentes,
                bilhetesAguardandoEnvio,
                pagamentosAbertos,
                solicitacoesAtivas);
    }

    public record DashboardResumo(
            long vendasAguardandoPagamento,
            long vendasPagamentoParcial,
            long emissaoPendente,
            long bilhetesPendentes,
            long bilhetesAguardandoEnvio,
            long pagamentosAbertos,
            long solicitacoesAtivas) {}
}
