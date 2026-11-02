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
