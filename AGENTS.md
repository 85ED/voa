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
docker compose up -d
cd backend && ./mvnw test
cd frontend && npm test && npm run build
SPRING_PROFILES_ACTIVE=local ./mvnw -f backend/pom.xml spring-boot:run
cd frontend && npm run dev
```

## Testcontainers

Docker API ≥ 1.44: em ambientes com daemon recente, configure `~/.docker-java.properties` com `api.version=1.44` ou use CI GitHub Actions.

## Papéis RBAC

`ADMIN`, `COMERCIAL`, `OPERACOES`, `FINANCEIRO`, `AUDITOR`

## Próximas etapas

Ver `docs/implementation-plan.md` (Etapas 3–5: cotações, vendas, pagamentos, bilhetes).
