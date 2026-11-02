#!/usr/bin/env bash
# Verifica se o backend Spring Boot está no ar (porta 8080).
set -euo pipefail

url="${VOA_BACKEND_HEALTH_URL:-${BACKEND_HEALTH_URL:-http://localhost:8080/actuator/health}}"
retries="${BACKEND_HEALTH_RETRIES:-60}"

echo "Verificando backend: $url"

for ((attempt = 1; attempt <= retries; attempt++)); do
  if response="$(curl --fail --silent --show-error "$url" 2>&1)"; then
    echo "Backend OK."
    echo "$response"
    exit 0
  fi
  sleep 2
done

echo ""
echo "Backend não respondeu em $url após $retries tentativas."
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
