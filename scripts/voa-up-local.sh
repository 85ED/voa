#!/usr/bin/env bash
# Sobe VOA no Mac como no IntelliJ: Postgres no Docker + API/Front no host (~/.m2 e npm).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p "$ROOT/.voa"

# shellcheck source=compose-env.sh
source "$ROOT/scripts/compose-env.sh"

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  else
    docker-compose "$@"
  fi
}

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
"$ROOT/scripts/ensure-docker-engine.sh"
"$ROOT/scripts/ensure-docker-config.sh"
"$ROOT/scripts/free-voa-ports.sh"

kill_pidfile "$ROOT/.voa/backend.pid"
kill_pidfile "$ROOT/.voa/frontend.pid"

echo "==> Postgres (Docker, imagem leve)..."
compose up -d postgres

if ! "$ROOT/scripts/check-java.sh"; then
  echo "Configure JDK 21 e rode de novo." >&2
  exit 1
fi

echo "==> API Spring (usa ~/.m2 — igual IntelliJ; sem Maven dentro do Docker)..."
(
  cd "$ROOT/backend"
  export SPRING_PROFILES_ACTIVE=local
  exec ./mvnw -q spring-boot:run -DskipTests
) >>"$ROOT/.voa/backend.log" 2>&1 &
echo $! >"$ROOT/.voa/backend.pid"

export BACKEND_HEALTH_RETRIES=120
if ! "$ROOT/scripts/check-backend.sh"; then
  echo "Backend não subiu. Últimas linhas do log:" >&2
  tail -30 "$ROOT/.voa/backend.log" >&2 || true
  exit 1
fi

if [[ ! -d "$ROOT/frontend/node_modules/@fontsource" ]]; then
  echo "==> npm ci no frontend (só na 1ª vez)..."
  (cd "$ROOT/frontend" && npm ci --no-audit --no-fund)
fi

echo "==> Frontend Vite (proxy /api → :8080)..."
(
  cd "$ROOT/frontend"
  exec npm run dev -- --host 127.0.0.1
) >>"$ROOT/.voa/frontend.log" 2>&1 &
echo $! >"$ROOT/.voa/frontend.pid"

for _ in $(seq 1 45); do
  if curl -sf -o /dev/null --max-time 2 http://127.0.0.1:5173/ 2>/dev/null; then
    break
  fi
  sleep 2
done

echo ""
echo "VOA no ar (modo local — rápido):"
echo "  App:    http://localhost:5173"
echo "  Health: http://localhost:8080/actuator/health"
echo "  Login:  admin@jlturismo.local / AdminSenhaSegura12"
echo ""
echo "Logs: tail -f .voa/backend.log .voa/frontend.log"
echo "Parar: ./scripts/docker-down.sh"
echo ""
echo "Stack 100% Docker (lento na 1ª vez): VOA_DOCKER=1 ./scripts/voa-up.sh"
