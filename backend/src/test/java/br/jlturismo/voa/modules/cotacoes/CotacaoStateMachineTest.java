package br.jlturismo.voa.modules.cotacoes;

import br.jlturismo.voa.modules.cotacoes.domain.CotacaoStateMachine;
import br.jlturismo.voa.modules.cotacoes.domain.StatusCotacao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CotacaoStateMachineTest {

    @Test
    void transicoesPermitidas() {
        assertDoesNotThrow(() -> CotacaoStateMachine.assertCanTransition(StatusCotacao.RASCUNHO, StatusCotacao.ENVIADA));
        assertDoesNotThrow(() -> CotacaoStateMachine.assertCanTransition(StatusCotacao.ENVIADA, StatusCotacao.ACEITA));
        assertDoesNotThrow(() -> CotacaoStateMachine.assertCanTransition(StatusCotacao.ENVIADA, StatusCotacao.RECUSADA));
    }

    @Test
    void transicoesProibidas() {
        assertThrows(
                IllegalStateException.class,
                () -> CotacaoStateMachine.assertCanTransition(StatusCotacao.RASCUNHO, StatusCotacao.ACEITA));
        assertThrows(
                IllegalStateException.class,
                () -> CotacaoStateMachine.assertCanTransition(StatusCotacao.ACEITA, StatusCotacao.ENVIADA));
    }

    @Test
    void allowedSets() {
        assertThat(CotacaoStateMachine.allowed(StatusCotacao.ENVIADA))
                .containsExactlyInAnyOrder(StatusCotacao.ACEITA, StatusCotacao.RECUSADA, StatusCotacao.EXPIRADA);
    }
}
