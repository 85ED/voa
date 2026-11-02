#!/usr/bin/env bash
# Uma instância nativa do Spring Boot (profile local). Requer JDK 21.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if lsof -nP -iTCP:8080 -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Porta 8080 já em uso. Não inicia segunda API."
  echo "Health: curl -sf http://localhost:8080/actuator/health"
  echo "Para encerrar a instância VOA: ./scripts/stop-voa.sh"
  exit 1
fi

# shellcheck source=check-java.sh
source "$ROOT/scripts/check-java.sh"
# shellcheck source=load-local-db.sh
source "$ROOT/scripts/load-local-db.sh"

if [[ ! -x "$ROOT/backend/mvnw" ]]; then
  echo "Maven Wrapper ausente em backend/mvnw" >&2
  exit 1
fi

echo "==> Backend nativo (profile local, Postgres :${VOA_DB_PORT}, heap máx. 512m)"
cd "$ROOT/backend"
exec env SPRING_PROFILES_ACTIVE=local VOA_DB_PORT="$VOA_DB_PORT" ./mvnw spring-boot:run
