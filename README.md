# VOA — JL Turismo

Plataforma operacional para gestão comercial de viagens (solicitações, cotações, vendas, pagamentos e auditoria).

## Repositório

Monorepo dedicado — **não** confundir com [Guia de Corrida](https://github.com/85ED/GuiaDeCorrida).

## Subir com Docker (recomendado Mac)

O fluxo Docker exige o **Docker Desktop instalado e aberto**. Se `docker: command not found`
aparecer, instale-o no Mac e abra o aplicativo:

```bash
brew install --cask docker
open -a Docker
docker --version
```

Depois que o Docker Desktop estiver pronto, um único comando sobe PostgreSQL, backend Spring e frontend:

```bash
./scripts/docker-up.sh
```

O comando equivalente, incluindo a construção das imagens, é:

```bash
docker compose up --build
```

Abra **http://localhost:5173** no navegador. A API fica disponível em
http://localhost:8080/actuator/health e o PostgreSQL usa a porta `5433` no host.
O login de teste da agência é `admin@jlturismo.local` com a senha
`AdminSenhaSegura12`.

Para validar o backend e os fluxos do navegador:

```bash
./scripts/check-backend.sh
cd frontend
npm install
npx playwright install chromium
npm run test:e2e
```

Para parar a stack:

```bash
docker compose down
```

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

- **Java 21 (JDK)** — erro `release version 21 not supported` = JDK antigo no PATH  
- Node 22+, Docker (Postgres local e Testcontainers)

**Mac — instalar Java 21:**

```bash
brew install openjdk@21
export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || echo /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home)"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Confira e carregue o JDK no terminal atual: `source ./scripts/check-java.sh` (após `git pull`).

### Banco local

```bash
docker compose up -d
```

Postgres em `localhost:5433` (user/senha/db: `voa` / `voa_dev` / `voa`).

### Backend

```bash
./mvnw -f backend test
source ./scripts/check-java.sh
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
