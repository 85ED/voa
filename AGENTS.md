# VOA — Guia para agentes

## Regras absolutas

1. **Repositório:** trabalhar apenas em `/home/ubuntu/voa` (ou clone dedicado VOA). **Nunca** modificar `/workspace` (Guia de Corrida).
2. **Stack:** modular monolith Java 21 + Spring Boot 3, PostgreSQL, Flyway, React/Vite/TS.
3. **Migrations:** forward-only em `backend/src/main/resources/db/migration/` — owner serializa versões Flyway.
4. **Auth:** JWT + refresh em cookies; ver ADR-002 e `docs/security.md`.
5. **Commits:** autor `85ED <edsonfelixnet@gmail.com>`, mensagens em português imperativo, `git add` explícito (sem `git add .`).

## Estrutura

```
backend/src/main/java/br/jlturismo/voa/modules/
  core/ auth/ solicitacoes/ cotacoes/ vendas/ pagamentos/ bilhetes/ audit/
frontend/src/
  app/ features/ shared/
docs/
```

## Comandos

```bash
./scripts/start-postgres.sh
cd backend && ./mvnw test
cd frontend && npm test && npm run build
./scripts/start-backend.sh
./scripts/start-frontend.sh
```

## Testcontainers

Docker API ≥ 1.44: em ambientes com daemon recente, configure `~/.docker-java.properties` com `api.version=1.44` ou use CI GitHub Actions.

## Papéis RBAC

`ADMIN`, `COMERCIAL`, `OPERACOES`, `FINANCEIRO`, `AUDITOR`, `PARTNER`

## Hardening (VOA-9)

Rate limit público/login, headers CSP API, sanitização formulários públicos, export CSV vendas, stub LGPD admin — ver `docs/security.md`.

## Estado

Etapas comerciais/financeiras MVP implementadas; deploy Railway **manual** (`docs/deployment.md`).
