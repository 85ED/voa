package br.jlturismo.voa.modules.cotacoes.domain;

import java.util.EnumSet;
import java.util.Set;

public final class CotacaoStateMachine {

    private CotacaoStateMachine() {}

    public static void assertCanTransition(StatusCotacao from, StatusCotacao to) {
        if (!allowed(from).contains(to)) {
            throw new IllegalStateException(
                    "Transição de cotação não permitida: " + from + " → " + to);
        }
    }

    public static Set<StatusCotacao> allowed(StatusCotacao from) {
        return switch (from) {
            case RASCUNHO -> EnumSet.of(StatusCotacao.ENVIADA);
            case ENVIADA -> EnumSet.of(StatusCotacao.ACEITA, StatusCotacao.RECUSADA, StatusCotacao.EXPIRADA);
            case ACEITA, RECUSADA, EXPIRADA -> EnumSet.noneOf(StatusCotacao.class);
        };
    }

    public static boolean isEditable(StatusCotacao status) {
        return status == StatusCotacao.RASCUNHO;
    }
}
