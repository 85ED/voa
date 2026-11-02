#!/usr/bin/env bash
# Encerra API/front do VOA. Preserva Postgres e processos que não forem do projeto.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=compose-env.sh
source "$ROOT/scripts/compose-env.sh"

stop_matching() {
  local port="$1"
  local pattern="$2"
  local pids
  pids="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null || true)"
  if [[ -z "$pids" ]]; then
    return 0
  fi
  local pid cmd
  for pid in $pids; do
    cmd="$(ps -p "$pid" -o args= 2>/dev/null || true)"
    if [[ -z "$cmd" ]]; then
      continue
    fi
    if echo "$cmd" | grep -Eq "$pattern"; then
      echo "==> Encerrando PID $pid na porta $port"
      kill "$pid" 2>/dev/null || true
    else
      echo "==> Preservado PID $pid na porta $port (não identificado como VOA)"
    fi
  done
}

if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  if docker compose version >/dev/null 2>&1; then
    docker compose --profile full stop api web 2>/dev/null || true
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose stop api web 2>/dev/null || true
  fi
fi

stop_matching 8080 'mvnw|spring-boot|voa-.*\.jar|voa/backend|backend/mvnw'
stop_matching 5173 'vite|voa/frontend'

echo "API/front parados (Postgres mantido)."
echo "Parar também o Postgres Docker: ./scripts/docker-down.sh"
echo "Não para o PostgreSQL Homebrew de outros projetos."
