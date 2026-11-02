package br.jlturismo.voa.modules.bilhetes.domain;

import java.util.EnumSet;
import java.util.Set;

public final class BilheteStateMachine {

    private BilheteStateMachine() {}

    public static void assertCanTransition(StatusBilhete from, StatusBilhete to) {
        if (!allowed(from).contains(to)) {
            throw new IllegalStateException(
                    "Transição de bilhete não permitida: " + from + " → " + to);
        }
    }

    public static Set<StatusBilhete> allowed(StatusBilhete from) {
        return switch (from) {
            case PENDENTE -> EnumSet.of(StatusBilhete.AGENDADA, StatusBilhete.EMITIDA, StatusBilhete.CANCELADA);
            case AGENDADA -> EnumSet.of(StatusBilhete.EMITIDA, StatusBilhete.PENDENTE, StatusBilhete.CANCELADA);
            case EMITIDA -> EnumSet.of(StatusBilhete.ENVIADA_CLIENTE, StatusBilhete.CANCELADA);
            case ENVIADA_CLIENTE, CANCELADA -> EnumSet.noneOf(StatusBilhete.class);
        };
    }
}
