#!/usr/bin/env bash
# Colima + Homebrew: remove credencial do Docker Desktop e registra plugin compose.
set -euo pipefail

mkdir -p "${HOME}/.docker"

_plug=""
if command -v brew >/dev/null 2>&1; then
  _prefix="$(brew --prefix 2>/dev/null || true)"
  if [[ -n "${_prefix}" && -d "${_prefix}/lib/docker/cli-plugins" ]]; then
    _plug="${_prefix}/lib/docker/cli-plugins"
  fi
fi

export VOA_DOCKER_PLUG="${_plug}"
python3 <<'PY'
import json
import os
from pathlib import Path

config_path = Path.home() / ".docker" / "config.json"
cfg: dict = {}
if config_path.is_file():
    try:
        cfg = json.loads(config_path.read_text())
    except json.JSONDecodeError:
        cfg = {}

changed = False

creds = cfg.get("credsStore")
if creds and "desktop" in str(creds).lower():
    del cfg["credsStore"]
    changed = True

helpers = cfg.get("credHelpers")
if isinstance(helpers, dict):
    for key in list(helpers.keys()):
        if "desktop" in str(helpers[key]).lower():
            del helpers[key]
            changed = True
    if not helpers:
        cfg.pop("credHelpers", None)
        changed = True

plug = os.environ.get("VOA_DOCKER_PLUG", "")
if plug:
    dirs = list(cfg.get("cliPluginsExtraDirs") or [])
    if plug not in dirs:
        dirs.append(plug)
        cfg["cliPluginsExtraDirs"] = dirs
        changed = True

if changed or not config_path.is_file():
    config_path.write_text(json.dumps(cfg, indent=2) + "\n")
    print("Docker config OK (Colima / sem docker-credential-desktop).")
PY

unset VOA_DOCKER_PLUG
