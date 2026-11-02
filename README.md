# VOA — JL Turismo

Plataforma operacional para gestão comercial de viagens (solicitações, cotações, vendas, pagamentos e auditoria).

## Repositório

Monorepo dedicado — **não** confundir com [Guia de Corrida](https://github.com/85ED/GuiaDeCorrida).

## Rodar no VS Code (jeito simples)

1. **Abrir a pasta** do repositório: *File → Open Folder…* → escolha a pasta `voa` (raiz deste repo).
2. Instale as extensões sugeridas quando o VS Code pedir (Java, Spring Boot, Docker, ESLint).
3. **Terminal integrado** — **cole um comando por vez** (evita erro `parse error` no zsh):

   ```bash
   git pull origin main
   ./scripts/check-setup.sh
   chmod +x scripts/*.sh
   ./scripts/start-postgres.sh
   cd frontend && npm install && cd ..
   ```

4. **Dois terminais** (ou duas abas):

   **Terminal A — backend**

   ```bash
   cd backend
   SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
   ```

   **Terminal B — frontend** (só depois que existir a pasta `frontend/`)

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   Ou use *Run Task → **VOA: Subir tudo*** (Postgres + backend + frontend).

5. **Abrir no navegador:** http://localhost:5173  
   - API: http://localhost:8080/actuator/health  
   - Swagger: http://localhost:8080/swagger-ui.html  

6. **Login dev** (profile `local`):  
   - E-mail: `admin@jlturismo.local`  
   - Senha: `AdminSenhaSegura12` (também em `backend/src/main/resources/application-local.yml`)

O frontend usa proxy do Vite para `/api` → porta `8080` (não precisa configurar CORS no dia a dia).

### Se `cd frontend` der “no such file”

O clone está incompleto (repo vazio ou sem `git pull`):

```bash
cd ~/Freelas/voa
git pull origin main
ls frontend
```

### Atalhos úteis no VS Code

| Ação | Onde |
|------|------|
| Subir stack | `Cmd/Ctrl+Shift+P` → *Tasks: Run Task* → *VOA: Subir tudo* |
| Testes backend | Terminal: `./mvnw -f backend test` |
| Testes frontend | Terminal: `cd frontend && npm test` |

---

## Início rápido (terminal)

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
SPRING_PROFILES_ACTIVE=local ./mvnw -f backend spring-boot:run
```

### Frontend

```bash
cd frontend && npm install && npm run dev
```

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
