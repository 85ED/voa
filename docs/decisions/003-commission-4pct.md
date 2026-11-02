# ADR-003 — Regra comercial de comissão 4%

**Status:** Documentado — **implementação bloqueada**  
**Data:** 2026-11-02

## Regra informada (spec)

Comissão de **4%** associada ao fluxo comercial VOA (interpretação pendente de base de cálculo e momento de reconhecimento).

## O que NÃO implementar até confirmação

- Cálculo automático de valor de comissão em cotação/venda.
- Lançamentos financeiros definitivos ou export contábil.
- Split de comissão entre agentes/equipes.

## Modelo de dados preparatório (permitido na fundação)

Entidade `comissao_regra` / campos nullable em `venda` apenas como **placeholder**:

- `percentual_padrao` (default 4.00, **desligado** por feature flag).
- `base_calculo` enum reservado: `BRUTO`, `LIQUIDO_TARIFA`, `MARGEM` — sem default de negócio.

## Perguntas comerciais abertas

### Bloqueantes para implementar cálculo

1. **Base do 4%:** sobre tarifa bruta, tarifa líquida (sem taxas), margem da agência, ou valor total pago pelo cliente?
2. **Incidência:** por item (aéreo/hotel/serviço) uniforme ou tabela por produto/fornecedor?
3. **Taxas e impostos:** DU, RAV, fee de emissão entram ou saem da base?
4. **Repasse:** comissão é receita JL, repasse a vendedor externo, ou ambos (split)?
5. **Momento:** na **venda fechada**, no **pagamento recebido**, ou na **emissão do bilhete**?
6. **Cancelamento/chargeback:** estorno proporcional automático ou ajuste manual?
7. **Conflito com contratos atuais:** existe exceção por cliente corporativo abaixo/acima de 4%?

### Premissas seguras para Etapa 2–3

- Registrar **percentual informado manualmente** em cotação (campo livre, sem validação 4%).
- Relatório MVP lista vendas com valor total; coluna comissão **vazia ou manual**.
- Feature flag `COMISSAO_AUTO_ENABLED=false` no backend.

## Critério de aceite futuro (pós-resposta JL)

- Testes unitários com 3 cenários acordados (aéreo nacional, pacote, cancelamento parcial).
- Auditoria registra alteração de percentual por usuário autorizado.
