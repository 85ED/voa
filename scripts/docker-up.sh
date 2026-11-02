#!/usr/bin/env bash
# Sobe a stack VOA completa em Docker (Postgres + API + web). Mais memória (Colima).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=compose-env.sh
source "$ROOT/scripts/compose-env.sh"

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose --profile full "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose "$@"
  else
    echo "Docker Compose não encontrado." >&2
    echo "Mac: brew install docker-compose && export DOCKER_CLI_PLUGIN_EXTRA_DIRS=\"\$(brew --prefix)/lib/docker/cli-plugins\"" >&2
    exit 127
  fi
}

images_ready() {
  local api_ok=0 web_ok=0
  if docker image inspect ghcr.io/85ed/voa-api:latest >/dev/null 2>&1 \
    || docker image inspect voa-api:latest >/dev/null 2>&1 \
    || docker image inspect voa-api >/dev/null 2>&1; then
    api_ok=1
  fi
  if docker image inspect ghcr.io/85ed/voa-web:latest >/dev/null 2>&1 \
    || docker image inspect voa-web:latest >/dev/null 2>&1 \
    || docker image inspect voa-web >/dev/null 2>&1; then
    web_ok=1
  fi
  [[ "$api_ok" == "1" && "$web_ok" == "1" ]]
}

print_first_build_banner() {
  cat <<'EOF'

══════════════════════════════════════════════════════════════════
  PRIMEIRA VEZ nesta máquina: build ou download das imagens.
  Pode levar vários minutos. NÃO abra localhost:5173 ainda.

  • Feche Spring/mvnw no IntelliJ ou VS Code (evita 2 downloads).
  • Depois que aparecer "VOA no ar", aí sim abra o navegador.
  • Caminho leve (sem Colima): ./scripts/voa-up.sh
══════════════════════════════════════════════════════════════════

EOF
}

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker CLI não encontrado." >&2
  echo "Mac (sem Desktop): brew install colima docker docker-compose && colima start" >&2
  exit 127
fi

if ! docker info >/dev/null 2>&1; then
  echo "Daemon Docker inativo. Rode: ./scripts/ensure-docker-engine.sh (inicia Colima, ~4 GiB)." >&2
  exit 1
fi

"$ROOT/scripts/free-voa-ports.sh"

echo "==> Parando stack anterior..."
compose down --remove-orphans 2>/dev/null || true
docker rm -f voa-postgres 2>/dev/null || true

rebuild="${VOA_REBUILD:-${VOA_DOCKER_BUILD:-0}}"

if [[ "$rebuild" == "1" ]]; then
  echo "==> Rebuild forçado (VOA_REBUILD=1)..."
  print_first_build_banner
  compose build --pull api web
  compose up -d
elif images_ready; then
  echo "==> Imagens já existem — subindo sem rebuild (rápido)..."
  compose pull api web 2>/dev/null || true
  compose up -d --no-build
else
  print_first_build_banner
  echo "==> Tentando baixar imagens prontas (GitHub)..."
  if compose pull api web 2>/dev/null && images_ready; then
    echo "==> Imagens baixadas; subindo..."
    compose up -d --no-build
  else
    echo "==> Build local (só esta vez, se o pacote GH ainda não existir)..."
    compose build api web
    compose up -d
  fi
fi

echo "==> Aguardando API..."
export BACKEND_HEALTH_RETRIES=120
"$ROOT/scripts/check-backend.sh"

echo "==> Aguardando web na porta 5173..."
for _ in $(seq 1 60); do
  if curl -sf -o /dev/null --max-time 2 http://localhost:5173/ 2>/dev/null; then
    break
  fi
  sleep 2
done

echo ""
echo "VOA no ar (Docker completo):"
echo "  App:    http://localhost:5173"
echo "  Health: http://localhost:8080/actuator/health"
echo "  Login:  admin@jlturismo.local / AdminSenhaSegura12"
echo ""
echo "Não rode npm run dev em paralelo — o front já sobe no container."
echo "Rebuild completo (raro): VOA_REBUILD=1 ./scripts/docker-up.sh"
echo "Parar: ./scripts/docker-down.sh"
echo "Caminho leve (menos RAM): ./scripts/voa-up.sh"
