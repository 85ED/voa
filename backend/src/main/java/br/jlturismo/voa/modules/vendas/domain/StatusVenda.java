package br.jlturismo.voa.modules.vendas.domain;

public enum StatusVenda {
    ABERTA,
    CONFIRMADA,
    PAGAMENTO_PARCIAL,
    PAGAMENTO_CONFIRMADO,
    AGUARDANDO_EMISSAO,
    EM_EMISSAO,
    CONCLUIDA,
    CANCELADA
}
