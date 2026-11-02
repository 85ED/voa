# VOA — JL Turismo

Plataforma operacional para gestão comercial de viagens (solicitações, cotações, vendas, pagamentos e auditoria).

## Repositório

Monorepo dedicado — **não** confundir com [Guia de Corrida](https://github.com/85ED/GuiaDeCorrida).

## Início rápido

### Pré-requisitos

- Java 21+, Node 22+, Docker (Postgres local e Testcontainers)

### Banco local

```bash
docker compose up -d
```

Postgres em `localhost:5433` (user/senha/db: `voa` / `voa_dev` / `voa`).

### Backend

```bash
./mvnw -f backend test
# ou
cd backend && ./mvnw test
```

Subir API local:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw -f backend spring-boot:run
```

- Health: http://localhost:8080/actuator/health  
- OpenAPI: http://localhost:8080/swagger-ui.html  
- Seed admin (profile `local`): `admin@jlturismo.local` / ver `application-local.yml`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Proxy Vite → API em `:8080`.

## Variáveis

| Variável | Descrição |
|----------|-----------|
| `JWT_ACCESS_SECRET` | Segredo HMAC (≥ 32 bytes) |
| `VOA_SEED_ADMIN_EMAIL` | Bootstrap admin se DB vazio |
| `VOA_SEED_ADMIN_PASSWORD` | Senha inicial admin |
| `VITE_API_BASE_URL` | URL da API no build do frontend |
| `PUBLIC_BASE_URL` | URL pública do SPA (links parceiro) |
| `VOA_PUBLIC_POST_RATE_LIMIT_PER_MINUTE` | Limite POST formulários públicos |
| `VOA_LOGIN_RATE_LIMIT_PER_MINUTE` | Limite tentativas login/minuto |

Copie `.env.example` para `.env` (gitignored) quando necessário.

## Deploy

Railway — ver `docs/deployment.md` e `railway.toml`.

## Documentação

- [Escopo](docs/project-scope.md)
- [Arquitetura](docs/architecture.md)
- [API](docs/api.md)
- [Plano de implementação](docs/implementation-plan.md)

## Licença

Proprietário — JL Turismo.
