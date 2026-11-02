package br.jlturismo.voa.modules.vendas.domain;

import java.util.EnumSet;
import java.util.Set;

public final class VendaStateMachine {

    private VendaStateMachine() {}

    public static void assertCanTransition(StatusVenda from, StatusVenda to) {
        if (!allowed(from).contains(to)) {
            throw new IllegalStateException(
                    "Transição de venda não permitida: " + from + " → " + to);
        }
    }

    public static Set<StatusVenda> allowed(StatusVenda from) {
        return switch (from) {
            case ABERTA -> EnumSet.of(StatusVenda.CONFIRMADA, StatusVenda.CANCELADA);
            case CONFIRMADA -> EnumSet.of(
                    StatusVenda.PAGAMENTO_PARCIAL,
                    StatusVenda.PAGAMENTO_CONFIRMADO,
                    StatusVenda.CANCELADA);
            case PAGAMENTO_PARCIAL -> EnumSet.of(
                    StatusVenda.PAGAMENTO_PARCIAL,
                    StatusVenda.PAGAMENTO_CONFIRMADO,
                    StatusVenda.CANCELADA);
            case PAGAMENTO_CONFIRMADO -> EnumSet.of(StatusVenda.AGUARDANDO_EMISSAO, StatusVenda.CANCELADA);
            case AGUARDANDO_EMISSAO -> EnumSet.of(StatusVenda.EM_EMISSAO, StatusVenda.CANCELADA);
            case EM_EMISSAO -> EnumSet.of(StatusVenda.CONCLUIDA, StatusVenda.CANCELADA);
            case CONCLUIDA, CANCELADA -> EnumSet.noneOf(StatusVenda.class);
        };
    }

    /** Após pagamento quitado, encadeia transição operacional para emissão. */
    public static StatusVenda afterPagamentoConfirmado() {
        return StatusVenda.AGUARDANDO_EMISSAO;
    }
}
