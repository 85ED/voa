# Escopo do projeto VOA

VOA é a plataforma operacional da **JL Turismo** para solicitações de viagem, cotações, vendas, pagamentos, bilhetes e auditoria.

## Produto

| Item | Decisão |
|------|---------|
| Cliente | JL Turismo (single tenant no MVP) |
| Stack | Spring Boot 3, PostgreSQL, Flyway, React/Vite/TS |
| Deploy | Railway (projeto separado do Guia de Corrida) |
| Idioma | Português (BR) |

## Escopo desta fundação (Etapas 2–5 MVP)

- Monorepo `backend/` + `frontend/` + `docker-compose.yml`
- Flyway **V1–V9** (auth, comercial, financeiro, parceiros, promoções)
- Fluxo solicitação → cotação → venda → pagamento → bilhete + auditoria
- Portal parceiro `/p/{slug}` + área pública
- Hardening: rate limit, CSP baseline, LGPD stub admin, export CSV vendas
- Testes de integração com **Testcontainers** (Postgres)
- SPA autenticada + landing pública

## Fora de escopo

- GDS, gateway de pagamento, emissão automática de bilhetes
- Cálculo automático de comissão 4% (`COMISSAO_AUTO_ENABLED=false`, ADR-003)
- Alterações no repositório Guia de Corrida

## Documentação

Ver `docs/architecture.md`, `docs/api.md`, `docs/security.md` e ADRs em `docs/decisions/`.
