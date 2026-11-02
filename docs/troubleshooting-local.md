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

## Causas frequentes

| Sintoma | Solução |
|---------|---------|
| `Connection refused` / `ConnectException` (5433) | Postgres off: na raiz `./scripts/start-postgres.sh` — Docker Desktop aberto |
| `Port 8080 was already in use` | `lsof -i :8080` → matar processo ou `SERVER_PORT=8081 SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run` |
| `release version 21 not supported` ou `UnsupportedClassVersionError` | `source ./scripts/check-java.sh`; executar o script sem `source` não altera o Java do terminal atual |
| Flyway / migration | Postgres vazio OK; se DB corrompido: `docker rm -f voa-postgres` e `./scripts/start-postgres.sh` de novo |

## Checklist mínimo

```bash
docker ps | grep voa-postgres
source ./scripts/check-java.sh
cd backend && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
curl http://localhost:8080/actuator/health
```
