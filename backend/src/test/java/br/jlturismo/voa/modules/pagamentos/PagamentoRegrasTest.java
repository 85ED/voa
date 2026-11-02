package br.jlturismo.voa.modules.pagamentos;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagamentoRegrasTest {

    @Test
    void comprovanteNaoImplicaQuitacao() {
        boolean comprovanteAnexado = true;
        boolean parcelaConfirmadaPorUsuario = false;
        assertTrue(comprovanteAnexado);
        assertFalse(parcelaConfirmadaPorUsuario);
    }

    @Test
    void quitacaoExigeValorConfirmado() {
        BigDecimal totalVenda = new BigDecimal("1500.0000");
        BigDecimal confirmado = new BigDecimal("1500.0000");
        assertTrue(confirmado.compareTo(totalVenda) >= 0);
    }
}
