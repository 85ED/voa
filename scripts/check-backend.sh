#!/usr/bin/env bash
# Verifica se o backend Spring Boot está no ar (porta 8080).
set -euo pipefail

URL="${VOA_BACKEND_HEALTH_URL:-http://localhost:8080/actuator/health}"

echo "Verificando backend: $URL"

if response="$(curl -sf "$URL" 2>&1)"; then
  echo "Backend OK."
  echo "$response"
  exit 0
fi

echo ""
echo "Backend não respondeu em $URL."
echo ""
echo "502 no dev server do Vite (proxy /api → localhost:8080) quase sempre significa"
echo "que o backend não está rodando na porta 8080."
echo ""
echo "Como corrigir:"
echo "  ./scripts/check-backend.sh          # este script"
echo "  ./scripts/start-postgres.sh         # se Postgres local (porta 5433)"
echo "  ./scripts/check-java.sh"
echo "  cd backend && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run"
echo "  curl http://localhost:8080/actuator/health"
echo ""
echo "Ver também: docs/troubleshooting-local.md"
exit 1
