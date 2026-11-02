# Diagnóstico do ambiente local — VOA

Data da medição inicial: 2026-11-02. Host: macOS 26.2 (Darwin arm64), **8 GB de RAM física**.

Este relatório usa apenas medições observadas nesta sessão. Caminhos pessoais foram reduzidos a `~`. Nenhum segredo, credencial ou dump foi copiado.

**Como ler memória.** RSS = páginas residentes (consumo real). VSZ = espaço de endereçamento virtual (no macOS/ARM costuma ser enorme e **não** é RAM usada). Compressor e swap explicam por que o Monitor de Atividade pode sugerir ~9 GB mesmo com só 8 GB físicos.

---

## 1. Sistema operacional e recursos (ocioso, stack VOA parada)

| Recurso | Valor observado |
|---|---|
| RAM física | 8,00 GB (`hw.memsize`) |
| Livre | ~0,06 GB |
| Active | ~1,4 GB |
| Wired | ~1,9 GB |
| Inactive (recuperável) | ~1,3 GB |
| Compressor ocupado | ~3,3 GB |
| Equivalente descomprimido no compressor | **~8,8 GB** |
| Swap | 2,00 GB total, **~1,06 GB em uso** |
| Disco (`/System/Volumes/Data`) | 228 GB, ~11 GB livres (95% cheio) |
| CPUs | 8 |

A máquina já estava sob pressão **antes** de subir o VOA nesta sessão. O equivalente descomprimido no compressor (~8,8 GB) mais o swap (~1 GB) é a medição mais próxima do “~9 GB” relatado. Isso **não** era um processo Java único.

### Processos com maior RSS (sessão ociosa)

| Processo | RSS aproximado | Pertence ao VOA? | Classificação | Causa possível | Ação | Risco | Economia |
|---|---|---|---|---|---|---|---|
| Cursor — extension-host de **outro** workspace | ~265 MB | Não | necessário (outro projeto) | Segunda janela do editor | Não encerrar daqui | Alto se encerrar | — |
| Cursor Helper (Renderer) | ~250 MB | Compartilhado | necessário | UI do editor | Não encerrar | Alto | — |
| Cursor — extension-host Agents | ~230 MB | Compartilhado | necessário | Janela de agentes | Não encerrar | Alto | — |
| Cursor — extension-host **voa** | ~140 MB | Sim (IDE) | necessário | Workspace VOA | Manter | — | — |
| Cursor (processo principal + auxiliares) | **~1,4 GB no total** | Compartilhado | necessário / otimizável | Três workspaces abertos | Fechar workspaces que não forem VOA (manual) | Médio | ~0,3–0,7 GB se fechar o outro repo |
| Google Chrome | ~0,5 GB (soma) | Não | fora de escopo | Navegador do usuário | Não encerrar | Alto | — |
| Spotify | ~0,3 GB | Não | fora de escopo | App do usuário | Não encerrar | — | — |
| n8n via PM2 | ~30 MB | Não | fora de escopo | Serviço local de outro uso | Não encerrar | Alto | — |
| PostgreSQL Homebrew 14 (`:5432`) | ~2 MB + filhos | Não (servidor compartilhado) | necessário para outros DBs; **reutilizável** pelo VOA | Já estava `started` | Não parar o serviço | Alto (outros bancos) | — |
| Postgres.app menu helper | ~7 MB | Não | fora de escopo | App de menu | Não encerrar | — | — |
| Java / Maven / JDT Language Server | **0 processos** | — | — | Backend e LS Java **não estavam em execução** | — | — | — |
| Node/Vite do VOA | **0 processos** | — | — | Frontend não estava em execução | — | — | — |
| Docker / Colima | parado (status `Stopped`) | — | — | VM não montada nesta sessão | — | — | — |
| `com.docker.vmnetd` | ~2 MB | Compartilhado | necessário ao Docker | Helper de rede | Não encerrar | — | — |

**Limitação.** Não foi possível reproduzir 9 GB **atribuídos ao backend VOA** nesta sessão, porque a API, o Vite, o Maven e o Colima estavam parados. A seção 6 registra a medição **depois** de subir o caminho leve.

---

## 2. Backend Java e Maven

| Item | Evidência |
|---|---|
| `java` no PATH | OpenJDK 17.0.19 (Homebrew) |
| `JAVA_HOME` da sessão | Homebrew 17 |
| JDKs instalados | 24, 22, **21** (Microsoft, Corretto, Oracle), 17 (Homebrew e Microsoft) |
| `java_home -v 21` | Microsoft 21.0.9 em `~/Library/Java/JavaVirtualMachines/` |
| Homebrew `openjdk@21` | Presente em `/opt/homebrew/opt/openjdk@21` |
| `mvn` global | **Não instalado** (`mvn not found`) |
| Maven Wrapper | `backend/mvnw` + `backend/.mvn/wrapper/`; raiz `./mvnw` só encaminha |
| Versão do wrapper | 3.9.9 (`distributionUrl` em `maven-wrapper.properties`) |
| Cache do wrapper | `~/.m2/wrapper/dists/apache-maven-3.9.9` **já existe** (~10 MB) |
| `~/.m2` total | ~476 MB (wrapper ~186 MB, repository ~290 MB) — **cache compartilhado** |
| Heap da JVM do app | **Não configurado** (sem `Xmx` no `pom.xml`, Dockerfile, scripts ou `jvm.config`) |
| Spring Boot DevTools | **Ausente** do `pom.xml` |
| Processos Java simultâneos | Nenhum no momento da medição |
| Extensões Java no Cursor | **Não instaladas** (pack recomendado em `.vscode/extensions.json`, mas a pasta de extensões não contém Java/Spring) |
| Language Server JDT | Não estava em execução |

### O Maven está sendo reinstalado a cada execução?

**Não.** O wrapper baixa a distribuição 3.9.9 **uma vez** para `~/.m2/wrapper/dists/` e reutiliza. Várias versões (3.8.x–3.9.16) existem nesse cache porque **outros projetos** também usam o wrapper. Apagar `~/.m2` forçaria novos downloads e **não** reduziria RAM, só disco e tempo de build.

O que *parece* “Maven de novo” no log é:

1. `./mvnw` validando a dist já baixada;
2. `docker compose up --build` reconstruindo a imagem `maven:3.9-eclipse-temurin-21` **dentro do Colima**, o que baixa dependências **na imagem**, não no host.

### Causa estrutural de pico de memória (quando a stack Docker sobe)

1. **Colima configurado com 4 GiB reservados** (`colima list`: 2 CPUs, **4 GiB**, 60 GiB disco) — metade da RAM do Mac, mesmo ocioso.
2. **`scripts/docker-up.sh` sempre usa `compose up --build -d`**, reconstruindo API (Maven) e frontend (`npm ci` + `vite build`) a cada subida.
3. **`scripts/start-postgres.sh` fazia `docker compose up -d` sem filtrar o serviço**, o que no compose atual sobe **postgres + api + web**, não só o banco.
4. A task **VOA: Subir tudo** dispara Postgres via compose **e** `spring-boot:run` nativo **e** Vite — risco de **duas APIs** (container + host) e dois fronts.
5. Imagem `backend/Dockerfile` usa Maven sem `MAVEN_OPTS`; o daemon Maven pode reservar ~1/4 da RAM da VM.
6. `java -jar` na imagem final sem teto de heap.
7. PATH com JDK 17 enquanto o `pom.xml` exige **21** — gera rebuilds/erros, não RAM por si, mas empurra o usuário para o caminho Docker (mais pesado).

Estimativa **somente quando o caminho Docker completo está ativo** (não medido nesta sessão ociosa, inferido da config):

- Colima: **4 GB reservados** (dado direto de `colima list`)
- Build Maven no container: pico típico 0,5–1,5 GB **dentro** desses 4 GB
- API Spring + nginx: adicional dentro da VM
- Cursor ~1,4 GB no host
- Total percebido no Monitor de Atividade: facilmente ≥ RAM física → swap + compressor ~9 GB

---

## 3. Frontend e serviços auxiliares

| Item | Evidência | Classificação |
|---|---|---|
| `frontend/node_modules` | 184 MB, uma cópia | necessário para `npm run dev` / testes |
| `frontend/dist` | 524 KB | regenerável |
| Vite duplicado | nenhum processo | — |
| Playwright/coverage | ausentes | — |
| Container Postgres VOA (`:5433`) | porta livre | — |
| Homebrew Postgres (`:5432`) | em execução, ~2 MB | compartilhado; reutilizável |
| Redis / filas | não instalados / não no compose | não iniciar |
| Terminais com backend/front VOA | nenhum listener em 8080/5173/5433 | — |

Indispensáveis para desenvolver a funcionalidade atual: **Postgres**, **API Spring (uma instância)**, **Vite (uma instância)**. Colima + imagens api/web são opcionais (paridade com produção).

---

## 4. Arquivos e caches (fase 2)

| Item | Tamanho | Finalidade | Classe | Ação |
|---|---|---|---|---|
| `frontend/node_modules/` | 184 MB | deps do Vite | 1 — necessário | **não remover** |
| `frontend/dist/` | 524 KB | build estático | 2 — regenerável | opcional; baixo ganho |
| `backend/target/` | 1,3 MB | classes compiladas | 2 — regenerável | manter (acelera o próximo `mvnw`) |
| `target/` na raiz | 0 B | artefato vazio do IDE | 2 — regenerável | remover pasta vazia |
| `~/.m2/repository` | 290 MB | deps Maven de **vários** projetos | 3 — cache compartilhado | **não apagar** |
| `~/.m2/wrapper` | 186 MB | várias dists Maven | 3 — cache compartilhado | **não apagar** |
| `~/.colima` | 4,5 GB em disco | VM Colima | 3/4 — runtime compartilhado | **não apagar**; não reduzir memória da VM automaticamente |
| Volume Docker `voa_pg_data` | n/d (daemon parado) | dados do banco VOA | 4 — persistente | **não apagar** |
| Bancos Homebrew (outros projetos) | n/d | dados locais alheios | 4 — persistente | **não tocar** |
| Logs `*.log` no repo | nenhum | — | — | — |
| Containers/imagens Docker | daemon inativo | não inspecionável agora | 5 se incerto | não podar |

Não foi executado `docker system prune`, limpeza de `~/.m2`, nem alteração de settings globais do Cursor.

---

## 5. Limpeza e otimizações (preenchido após a fase 3)

Ver seção 6 e o histórico Git. Resumo do que foi feito no repositório:

- Caminho diário leve: `./scripts/voa-up.sh` (Postgres Homebrew ou Docker só o banco + API nativa + um Vite). Não inicia Colima.
- Componentes separados: `start-postgres.sh`, `start-backend.sh`, `start-frontend.sh`, `stop-voa.sh`.
- Compose com profile `full` para api/web; `start-postgres.sh` sobe **somente** `postgres`.
- `docker-up.sh` deixa de rebuildar sempre; usa `--build` só se `VOA_DOCKER_BUILD=1` ou se a imagem não existir.
- Teto de heap: Maven (`backend/.mvn/jvm.config`), app (`voa.app.jvm.args` / `java -Xmx512m`), container API.
- Scripts `start-backend.sh`, `start-frontend.sh`, `stop-voa.sh`.
- `.vscode/settings.json` local (não settings globais): teto do JDT LS, exclusão de `node_modules`/`target` do watcher.
- Pasta vazia `target/` da raiz removida.

**Não feito (precisa autorização explícita):** reduzir Colima de 4 GiB para 2 GiB (`colima stop && colima start --memory 2`). Afeta qualquer outro uso de Docker neste Mac.

---

## 6. Validação e comparação (caminho leve, 2026-11-02)

Colima **permaneceu parado**. Postgres Homebrew na porta 5432 (database `voa` criado; outros bancos intocados). Uma API nativa + um Vite.

### Testes

| Suite | Resultado |
|---|---|
| Unitários backend (`CotacaoStateMachineTest`, `VendaStateMachineTest`, `BilheteStateMachineTest`, `PagamentoRegrasTest`, `TextSanitizerTest`) | **13 testes, 0 falhas** (`BUILD SUCCESS`, ~1,8 s). Maven 3.9.9 **reutilizado** de `~/.m2/wrapper`. |
| Integração backend (Testcontainers) | **Não executados neste Mac.** Exigem Docker/Colima (~4 GiB reservados), o que reintroduziria o pico que se quer evitar. Continuam no CI. |
| Frontend `npm test` (Vitest) | **Falhou** com `ERR_REQUIRE_ESM` em `html-encoding-sniffer` / `@exodus/bytes`. Erro pré-existente, independente desta limpeza. |

### Runtime

| Checagem | Resultado |
|---|---|
| `GET /actuator/health` :8080 | `{"status":"UP"}` |
| `GET /actuator/health` via Vite :5173 | `{"status":"UP"}` (proxy OK) |
| `POST /api/v1/auth/login` via Vite | HTTP 200, usuário `admin@jlturismo.local` |
| `GET /api/v1/public/promocoes` via Vite | HTTP 200 |
| Flyway | 11 migrations aplicadas no database `voa` |
| Instâncias | 1 Java API, 1 Maven pai, 1 Vite, 0 Colima, 0 container VOA |

Heap observado nos processos: Maven `-Xmx256m`, app `-Xmx512m` (flags aplicadas).

### Memória — comparado com o diagnóstico ocioso

Valores em RSS, medidos com `ps`. O host continua com pouca RAM livre por causa de **outros** apps (Cursor, Chrome, Spotify) e do compressor; isso **não** é o backend VOA.

| | Ocioso (stack parada) | Depois do caminho leve |
|---|---|---|
| Java API (`VoaApplication`) | 0 | pico ~425 MB no boot; ~206 MB após aquecimento |
| Maven wrapper (`spring-boot:run`) | 0 | ~24 MB |
| Vite (`node`) | 0 | ~84 MB |
| Postgres VOA (Homebrew, compartilhado) | ~2 MB (já rodava) | ~2 MB |
| Colima | parado, 0 reservado | **ainda parado** (economia vs 4 GiB reservados) |
| Soma VOA (API+Maven+Vite) | 0 | **~314 MB RSS** |
| Compressor (equivalente descomprimido, **máquina inteira**) | ~8,4–8,8 GB | ~9,7 GB |
| Swap usado (máquina inteira) | ~1,03 GB | ~1,25 GB |

**Conclusão mensurável.** O VOA no caminho leve ocupa da ordem de **0,3 GB RSS**, com teto de heap de 512 MB na API. Não reproduz 9 GB. O número ~9 GB observado no host é o **compressor + swap da máquina de 8 GB**, inflado principalmente por Cursor com vários workspaces e, quando alguém sobe `docker-up`/`colima start`, pelos **4 GiB reservados da VM** mais o rebuild Maven/npm.

Não é válido afirmar “reduzimos de 9 GB para 0,3 GB” como se fosse o mesmo processo: os 9 GB nunca estiveram nesta sessão como RSS da API. O que foi comprovado:

1. A stack Docker completa **reserva 4 GiB** só com a VM (config Colima).
2. Rebuild Maven+npm a cada `docker-up` soma pico dentro dessa VM.
3. O caminho leve **não reserva esses 4 GiB** e sobe a API em ~200–425 MB RSS.

### UI Vite

O servidor Vite sobe, mas o CSS falha ao importar `@fontsource/ibm-plex-sans/400.css` (ENOENT). Problema pré-existente de assets; **não** impede o proxy `/api`. Pendência manual se a tela ficar sem fonte.

### Outros projetos

Nenhum arquivo fora do clone VOA foi modificado. Caches globais (`~/.m2`, `~/.colima`, settings globais do Cursor, Java/Maven/Node globais) não foram apagados. PostgreSQL Homebrew não foi parado.

---

## 7. Como executar no VS Code / Cursor

1. Abrir **somente** a pasta do repositório VOA (evitar um segundo workspace pesado no mesmo app, neste Mac de 8 GB).
2. Extensão Java (`vscjava.vscode-java-pack`) é **opcional**. Se instalar, o workspace já limita o JDT LS a `-Xmx512m` em `.vscode/settings.json`. Não altere settings globais do usuário.
3. Tasks: **VOA: Subir tudo (leve)** — ou `./scripts/voa-up.sh` (sobe Postgres + API + Vite). Em dois terminais: `start-backend.sh` e `start-frontend.sh` depois de `start-postgres.sh`.
4. **Não** rode a task leve **e** `./scripts/docker-up.sh` ao mesmo tempo (duas APIs na 8080).
5. Parar API/front: `./scripts/stop-voa.sh` ou task **VOA: Parar API e front**.
6. Integração com Testcontainers / stack Docker: só quando precisar, ciente dos 4 GiB do Colima. Reduzir a VM para 2 GiB é decisão manual (`colima stop && colima start --memory 2`) porque afeta outros usos de Docker.

---

## 8. Pendências manuais

- Autorização para reduzir memória do Colima de 4 GiB para 2 GiB (runtime Docker compartilhado).
- Fechar o outro workspace do Cursor se a RAM do Mac continuar justa.
- Corrigir import das fontes `@fontsource` no Vite (pré-existente).
- Vitest `ERR_REQUIRE_ESM` (pré-existente).
- Testes de integração locais exigem Docker; usar CI ou aceitar o custo do Colima.
