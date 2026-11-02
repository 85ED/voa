#!/usr/bin/env bash
# Um comando: Postgres (Homebrew ou container já ativo) + API nativa + um Vite.
# Não inicia Colima (reserva ~4 GiB neste Mac).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p "$ROOT/.voa"

kill_pidfile() {
  local f="$1"
  if [[ -f "$f" ]]; then
    local pid
    pid="$(cat "$f" 2>/dev/null || true)"
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      sleep 1
      kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$f"
  fi
}

"$ROOT/scripts/check-setup.sh"
"$ROOT/scripts/stop-voa.sh" >/dev/null 2>&1 || true
kill_pidfile "$ROOT/.voa/backend.pid"
kill_pidfile "$ROOT/.voa/frontend.pid"

"$ROOT/scripts/start-postgres.sh"

echo "==> API Spring (host, ~/.m2, heap 512m)..."
(
  exec "$ROOT/scripts/start-backend.sh"
) >>"$ROOT/.voa/backend.log" 2>&1 &
echo $! >"$ROOT/.voa/backend.pid"

export BACKEND_HEALTH_RETRIES=120
if ! "$ROOT/scripts/check-backend.sh"; then
  echo "Backend não subiu. Últimas linhas do log:" >&2
  tail -30 "$ROOT/.voa/backend.log" >&2 || true
  exit 1
fi

echo "==> Frontend Vite..."
(
  exec "$ROOT/scripts/start-frontend.sh"
) >>"$ROOT/.voa/frontend.log" 2>&1 &
echo $! >"$ROOT/.voa/frontend.pid"

for _ in $(seq 1 45); do
  if curl -sf -o /dev/null --max-time 2 http://127.0.0.1:5173/ 2>/dev/null; then
    break
  fi
  sleep 2
done

echo ""
echo "VOA no ar (caminho leve — sem Colima):"
echo "  App:    http://localhost:5173"
echo "  Health: http://localhost:8080/actuator/health"
echo "  Login:  admin@jlturismo.local / AdminSenhaSegura12"
echo ""
echo "Logs: tail -f .voa/backend.log .voa/frontend.log"
echo "Parar API/front: ./scripts/stop-voa.sh"
echo "Stack 100% Docker (mais RAM): VOA_DOCKER=1 ./scripts/voa-up.sh"
