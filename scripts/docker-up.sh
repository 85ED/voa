#!/usr/bin/env bash
# Sobe a stack VOA (Postgres + API + web) com Docker Compose.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=compose-env.sh
source "$ROOT/scripts/compose-env.sh"

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose "$@"
  else
    echo "Docker Compose não encontrado." >&2
    echo "Mac: brew install docker-compose && export DOCKER_CLI_PLUGIN_EXTRA_DIRS=\"\$(brew --prefix)/lib/docker/cli-plugins\"" >&2
    exit 127
  fi
}

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker CLI não encontrado." >&2
  echo "Mac (sem Desktop): brew install colima docker docker-compose && colima start" >&2
  exit 127
fi

if ! docker info >/dev/null 2>&1; then
  echo "Daemon Docker inativo. Rode: ./scripts/voa-up.sh (inicia Colima automaticamente)." >&2
  exit 1
fi

"$ROOT/scripts/free-voa-ports.sh"

echo "==> Parando stack anterior e liberando container legado voa-postgres..."
compose down --remove-orphans 2>/dev/null || true
docker rm -f voa-postgres 2>/dev/null || true

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
echo "Não rode 'npm run dev' — o front já sobe no Docker."
echo "Logs: docker compose logs -f api"
echo "Parar: ./scripts/docker-down.sh"
