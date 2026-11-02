#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

kill_pidfile() {
  local f="$1"
  if [[ -f "$f" ]]; then
    local pid
    pid="$(cat "$f" 2>/dev/null || true)"
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
    fi
    rm -f "$f"
  fi
}

kill_pidfile "$ROOT/.voa/backend.pid"
kill_pidfile "$ROOT/.voa/frontend.pid"
"$ROOT/scripts/stop-voa.sh" >/dev/null 2>&1 || true

# shellcheck source=compose-env.sh
source "$ROOT/scripts/compose-env.sh"
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  if docker compose version >/dev/null 2>&1; then
    docker compose --profile full down --remove-orphans 2>/dev/null || true
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose down --remove-orphans 2>/dev/null || true
  fi
fi

echo "VOA parado (API/front e containers). Volume do Postgres preservado."
echo "PostgreSQL Homebrew de outros projetos não é encerrado."
