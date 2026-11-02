package br.jlturismo.voa.modules.bilhetes;

import br.jlturismo.voa.modules.bilhetes.domain.BilheteStateMachine;
import br.jlturismo.voa.modules.bilhetes.domain.StatusBilhete;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BilheteStateMachineTest {

    @Test
    void fluxoEmissao() {
        assertDoesNotThrow(
                () -> BilheteStateMachine.assertCanTransition(StatusBilhete.PENDENTE, StatusBilhete.AGENDADA));
        assertDoesNotThrow(
                () -> BilheteStateMachine.assertCanTransition(StatusBilhete.AGENDADA, StatusBilhete.EMITIDA));
        assertDoesNotThrow(() -> BilheteStateMachine.assertCanTransition(
                StatusBilhete.EMITIDA, StatusBilhete.ENVIADA_CLIENTE));
    }

    @Test
    void enviadaTerminal() {
        assertThrows(
                IllegalStateException.class,
                () -> BilheteStateMachine.assertCanTransition(
                        StatusBilhete.ENVIADA_CLIENTE, StatusBilhete.PENDENTE));
    }
}
