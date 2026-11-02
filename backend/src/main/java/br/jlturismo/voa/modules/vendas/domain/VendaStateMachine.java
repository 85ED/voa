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
            case CONFIRMADA -> EnumSet.of(StatusVenda.CANCELADA);
            case CANCELADA -> EnumSet.noneOf(StatusVenda.class);
        };
    }
}
