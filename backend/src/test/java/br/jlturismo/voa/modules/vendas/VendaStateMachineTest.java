package br.jlturismo.voa.modules.vendas;

import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.domain.VendaStateMachine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VendaStateMachineTest {

    @Test
    void transicoesPermitidas() {
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(StatusVenda.ABERTA, StatusVenda.CONFIRMADA));
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(StatusVenda.ABERTA, StatusVenda.CANCELADA));
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(StatusVenda.CONFIRMADA, StatusVenda.CANCELADA));
    }

    @Test
    void transicoesProibidas() {
        assertThrows(
                IllegalStateException.class,
                () -> VendaStateMachine.assertCanTransition(StatusVenda.CANCELADA, StatusVenda.CONFIRMADA));
        assertThrows(
                IllegalStateException.class,
                () -> VendaStateMachine.assertCanTransition(StatusVenda.CONFIRMADA, StatusVenda.ABERTA));
    }

    @Test
    void allowedFromAberta() {
        assertThat(VendaStateMachine.allowed(StatusVenda.ABERTA))
                .containsExactlyInAnyOrder(StatusVenda.CONFIRMADA, StatusVenda.CANCELADA);
    }
}
