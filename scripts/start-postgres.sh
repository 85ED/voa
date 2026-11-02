#!/usr/bin/env bash
# Sobe só o Postgres do VOA. Não inicia API nem frontend.
# Ordem: porta 5433 já em uso → Docker (somente serviço postgres) → Homebrew na 5432.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=compose-env.sh
source "$ROOT/scripts/compose-env.sh"

STATE_FILE="$ROOT/.voa-local-db"

write_state() {
  printf 'VOA_DB_PORT=%s\n' "$1" > "$STATE_FILE"
}

port_listening() {
  lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1
}

docker_ready() {
  command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1
}

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose "$@"
  else
    return 127
  fi
}

if port_listening 5433; then
  echo "Postgres VOA já está em localhost:5433."
  write_state 5433
  exit 0
fi

if docker_ready; then
  echo "==> Docker ativo: subindo somente o serviço postgres (sem API/web)."
  compose up -d postgres
  write_state 5433
  echo "Postgres OK em localhost:5433 (user voa / senha voa_dev / db voa)"
  exit 0
fi

if command -v pg_isready >/dev/null 2>&1 && pg_isready -h 127.0.0.1 -p 5432 >/dev/null 2>&1; then
  echo "==> Docker inativo. Reutilizando PostgreSQL Homebrew em :5432."
  echo "    Não altera outros bancos. Cria role/database voa se ainda não existirem."
  psql -h 127.0.0.1 -p 5432 -d postgres -v ON_ERROR_STOP=1 <<'SQL'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'voa') THEN
    CREATE ROLE voa LOGIN PASSWORD 'voa_dev';
  END IF;
END
$$;
SQL
  if ! psql -h 127.0.0.1 -p 5432 -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='voa'" | grep -qx 1; then
    createdb -h 127.0.0.1 -p 5432 -O voa voa
  fi
  write_state 5432
  echo "Postgres OK em localhost:5432 (database voa)."
  echo "Use ./scripts/start-backend.sh — ele lê .voa-local-db."
  exit 0
fi

echo "Nenhum Postgres disponível." >&2
echo "  Caminho leve: brew services start postgresql@14  (depois rode este script de novo)" >&2
echo "  Docker/Colima (reserva ~4 GiB): ./scripts/ensure-docker-engine.sh && ./scripts/start-postgres.sh" >&2
echo "  Stack completa em containers: ./scripts/docker-up.sh" >&2
exit 1
