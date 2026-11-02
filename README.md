# VOA — JL Turismo

Plataforma operacional para gestão comercial de viagens (solicitações, cotações, vendas, pagamentos e auditoria).

## Repositório

Monorepo dedicado — **não** confundir com [Guia de Corrida](https://github.com/85ED/GuiaDeCorrida).

## Subir o projeto (caminho leve — recomendado)

Na raiz do repositório. **Não** inicia Colima (a VM reserva ~4 GiB neste Mac).

Comando único (Postgres + API nativa + um Vite):

```bash
./scripts/voa-up.sh
```

Ou cada parte separada:

```bash
./scripts/start-postgres.sh      # Homebrew :5432 ou Docker só o banco
./scripts/start-backend.sh       # terminal A — API nativa (JDK 21, heap 512m)
./scripts/start-frontend.sh      # terminal B — um Vite
```

Parar API e front (Postgres permanece):

```bash
./scripts/stop-voa.sh
```

Abra **http://localhost:5173**. Login de teste: `admin@jlturismo.local` / `AdminSenhaSegura12`.

Instalação **uma vez**:

```bash
./scripts/bootstrap-once.sh
```

## Stack Docker completa (opcional)

Usa Colima (~4 GiB) e imagens GHCR quando existirem. Force rebuild com `VOA_REBUILD=1`.

```bash
VOA_DOCKER=1 ./scripts/voa-up.sh
./scripts/docker-down.sh         # para a stack; volume do banco é preservado
```

## Rodar no VS Code (jeito simples)

1. **Abrir a pasta** do repositório: *File → Open Folder…* → escolha a pasta `voa` (raiz deste repo).
2. Instale as extensões sugeridas quando o VS Code pedir (Java, Spring Boot, Docker, ESLint).
3. **Terminal integrado** — **cole um comando por vez** (evita erro `parse error` no zsh):

   ```bash
   git pull origin main
   ./scripts/check-setup.sh
   chmod +x scripts/*.sh
   ./scripts/voa-up.sh
   ```

4. **Dois terminais** (ou duas abas):

   **Terminal A — backend**

   ```bash
   ./scripts/start-backend.sh
   ```

   **Terminal B — frontend**

   ```bash
   ./scripts/start-frontend.sh
   ```

   Ou *Run Task → **VOA: Subir tudo (leve)*** (Postgres + backend + um Vite). Não rode isso junto com `docker-up.sh`.

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
./scripts/start-postgres.sh
```

Postgres Docker em `localhost:5433`, ou Homebrew já em execução na `5432` (user/senha/db: `voa` / `voa_dev` / `voa`). O script **não** sobe API nem frontend.

### Backend

```bash
./mvnw -f backend test
./scripts/start-backend.sh
```

### Frontend

```bash
./scripts/start-frontend.sh
```

Memória e ambiente local: [docs/local-environment-diagnostics.md](docs/local-environment-diagnostics.md).

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
