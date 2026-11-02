#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker CLI não encontrado." >&2
  echo "Instale e abra o Docker Desktop no Mac:" >&2
  echo "  brew install --cask docker" >&2
  echo "  open -a Docker" >&2
  echo "Depois confirme com: docker --version" >&2
  exit 127
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker CLI encontrado, mas o Docker Desktop/daemon não está em execução." >&2
  echo "Abra o Docker Desktop e aguarde ficar pronto; depois rode este script novamente." >&2
  exit 1
fi

docker compose up --build -d
"$ROOT/scripts/check-backend.sh"
