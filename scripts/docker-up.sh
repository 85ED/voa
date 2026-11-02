#!/usr/bin/env bash
# Sobe a stack VOA (Postgres + API + web) com Docker Compose.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose "$@"
  else
    echo "Instale Docker Compose (Docker Desktop no Mac inclui 'docker compose')." >&2
    exit 127
  fi
}

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker CLI não encontrado." >&2
  echo "Mac: brew install --cask docker && open -a Docker" >&2
  exit 127
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop/daemon não está rodando. Abra o Docker e tente de novo." >&2
  exit 1
fi

echo "==> Parando stack anterior e liberando container legado voa-postgres..."
compose down --remove-orphans 2>/dev/null || true
docker rm -f voa-postgres 2>/dev/null || true

for port in 5433 8080 5173; do
  if command -v lsof >/dev/null 2>&1 && lsof -i ":$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "AVISO: porta $port em uso. Feche IntelliJ/Vite ou: lsof -i :$port" >&2
  fi
done

echo "==> Build e start (pode levar alguns minutos na primeira vez)..."
compose up --build -d

echo "==> Aguardando API..."
export BACKEND_HEALTH_RETRIES=90
"$ROOT/scripts/check-backend.sh"

echo ""
echo "VOA no ar:"
echo "  App:    http://localhost:5173"
echo "  Health: http://localhost:8080/actuator/health"
echo "  Login:  admin@jlturismo.local / AdminSenhaSegura12"
echo ""
echo "Logs: docker compose logs -f api"
echo "Parar: docker compose down"
