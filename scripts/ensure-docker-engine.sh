#!/usr/bin/env bash
# Garante daemon Docker ativo sem Docker Desktop (Colima / Podman no terminal).
set -euo pipefail

docker_ready() {
  command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1
}

if docker_ready; then
  exit 0
fi

install_hint() {
  cat >&2 <<'EOF'

Docker daemon inativo. No Mac, use Colima (só terminal, sem Docker Desktop):

  brew install colima docker docker-compose
  colima start

Depois rode de novo: ./scripts/voa-up.sh

EOF
}

if ! command -v docker >/dev/null 2>&1; then
  if command -v brew >/dev/null 2>&1 && ! command -v colima >/dev/null 2>&1; then
    echo "==> Instalando Colima + Docker CLI (Homebrew)..." >&2
    brew install colima docker docker-compose
  fi
  if ! command -v docker >/dev/null 2>&1; then
    echo "Docker CLI não encontrado." >&2
    install_hint
    exit 127
  fi
fi

if command -v colima >/dev/null 2>&1; then
  echo "==> Iniciando Colima (Docker no terminal)..."
  if colima status 2>/dev/null | grep -qi 'colima is running'; then
    : # already running
  else
    colima start
  fi
  for _ in $(seq 1 45); do
    if docker_ready; then
      exit 0
    fi
    sleep 2
  done
  echo "Colima iniciou, mas docker info ainda não responde." >&2
  exit 1
fi

if command -v podman >/dev/null 2>&1; then
  echo "==> Iniciando Podman machine..."
  podman machine start 2>/dev/null || true
  for _ in $(seq 1 30); do
    if docker_ready; then
      exit 0
    fi
    sleep 2
  done
fi

install_hint
exit 1
