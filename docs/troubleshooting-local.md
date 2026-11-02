# VOA — falha ao subir local (Mac)

## Ver a causa real

O Maven só mostra `Process terminated with exit code: 1`. **Role o log para cima** e procure:

- `APPLICATION FAILED TO START`
- `Caused by:` (última causa raiz)

Ou rode:

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run -e 2>&1 | tee /tmp/voa-boot.log
grep -E 'APPLICATION FAILED|Caused by|Description:' /tmp/voa-boot.log
```

## Docker Compose não sobe

1. **Daemon Docker (sem Desktop):** `./scripts/voa-up.sh` inicia Colima se precisar. Manual: `brew install colima docker docker-compose && colima start`
2. **Portas livres:** feche IntelliJ/Vite; `lsof -i :8080 -i :5173 -i :5433`
3. **Container antigo:** `./scripts/docker-down.sh` depois `./scripts/docker-up.sh`
4. **Logs:** `docker compose logs api --tail 80`
5. **`docker-credential-desktop` not found:** `./scripts/voa-up.sh` corrige `~/.docker/config.json` (Colima). Manual: remova `"credsStore": "desktop"` de `~/.docker/config.json`.
6. **Tela preta Vite / @fontsource:** pare `npm run dev`; use só `./scripts/voa-up.sh`. Front vem do container, não do Vite.
7. **Build da imagem `api` falha em `maven-wrapper.properties`:** atualize a `main`.
8. Primeira build demora (Maven + npm no build da imagem).

## 502 no frontend (Vite dev)

Se o browser mostra **502 Bad Gateway** em `/api/*`, o proxy do Vite não alcançou **`http://localhost:8080`** — backend parado ou crash.

```bash
curl -sf http://localhost:8080/actuator/health
./scripts/check-backend.sh
```

Se o health estiver **UP** e ainda houver 502, reinicie `npm run dev`.

## Causas frequentes

| Sintoma | Solução |
|---------|---------|
| **502** no Vite / `/api` | Backend off — health + `./scripts/check-backend.sh` |
| `Connection refused` (5433) | `./scripts/start-postgres.sh` — Docker Desktop aberto |
| `Port 8080 was already in use` | `lsof -i :8080` → encerrar processo duplicado (IntelliJ + terminal) |
| `release version 21 not supported` | `source ./scripts/check-java.sh` (JDK 21 no PATH) |
| Flyway / DB | `docker rm -f voa-postgres` e `./scripts/start-postgres.sh` |

## Checklist

```bash
./scripts/start-postgres.sh
source ./scripts/check-java.sh
cd backend && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
curl http://localhost:8080/actuator/health
./scripts/check-backend.sh
```
