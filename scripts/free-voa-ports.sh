#!/usr/bin/env bash
# Libera portas do VOA (Vite, Spring local, Postgres legado) antes do Docker Compose.
set -euo pipefail

if ! command -v lsof >/dev/null 2>&1; then
  exit 0
fi

free_port() {
  local port="$1"
  local pids
  pids="$(lsof -ti ":${port}" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -z "${pids}" ]]; then
    return 0
  fi
  echo "==> Liberando porta ${port} (processos locais: ${pids//$'\n'/ })..."
  # shellcheck disable=SC2086
  kill ${pids} 2>/dev/null || true
  sleep 1
  pids="$(lsof -ti ":${port}" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -n "${pids}" ]]; then
    # shellcheck disable=SC2086
    kill -9 ${pids} 2>/dev/null || true
  fi
}

free_port 5173
free_port 8080
free_port 5433

docker rm -f voa-postgres 2>/dev/null || true
