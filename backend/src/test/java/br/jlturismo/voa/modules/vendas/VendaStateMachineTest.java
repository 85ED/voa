package br.jlturismo.voa.modules.vendas;

import br.jlturismo.voa.modules.vendas.domain.StatusVenda;
import br.jlturismo.voa.modules.vendas.domain.VendaStateMachine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VendaStateMachineTest {

    @Test
    void transicoesComerciaisPermitidas() {
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(StatusVenda.ABERTA, StatusVenda.CONFIRMADA));
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(StatusVenda.ABERTA, StatusVenda.CANCELADA));
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(StatusVenda.CONFIRMADA, StatusVenda.CANCELADA));
    }

    @Test
    void transicoesPagamentoEmissao() {
        assertDoesNotThrow(
                () -> VendaStateMachine.assertCanTransition(StatusVenda.CONFIRMADA, StatusVenda.PAGAMENTO_PARCIAL));
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(
                StatusVenda.CONFIRMADA, StatusVenda.PAGAMENTO_CONFIRMADO));
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(
                StatusVenda.PAGAMENTO_CONFIRMADO, StatusVenda.AGUARDANDO_EMISSAO));
        assertDoesNotThrow(
                () -> VendaStateMachine.assertCanTransition(StatusVenda.AGUARDANDO_EMISSAO, StatusVenda.EM_EMISSAO));
        assertDoesNotThrow(() -> VendaStateMachine.assertCanTransition(StatusVenda.EM_EMISSAO, StatusVenda.CONCLUIDA));
    }

    @Test
    void transicoesProibidas() {
        assertThrows(
                IllegalStateException.class,
                () -> VendaStateMachine.assertCanTransition(StatusVenda.CANCELADA, StatusVenda.CONFIRMADA));
        assertThrows(
                IllegalStateException.class,
                () -> VendaStateMachine.assertCanTransition(StatusVenda.CONFIRMADA, StatusVenda.ABERTA));
        assertThrows(
                IllegalStateException.class,
                () -> VendaStateMachine.assertCanTransition(StatusVenda.ABERTA, StatusVenda.PAGAMENTO_CONFIRMADO));
    }

    @Test
    void allowedFromAberta() {
        assertThat(VendaStateMachine.allowed(StatusVenda.ABERTA))
                .containsExactlyInAnyOrder(StatusVenda.CONFIRMADA, StatusVenda.CANCELADA);
    }
}
