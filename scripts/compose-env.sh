#!/usr/bin/env bash
# Homebrew docker-compose plugin (Colima / Mac sem Desktop).
if command -v brew >/dev/null 2>&1; then
  _brew_prefix="$(brew --prefix 2>/dev/null || true)"
  if [[ -n "${_brew_prefix}" && -d "${_brew_prefix}/lib/docker/cli-plugins" ]]; then
    _plug="${_brew_prefix}/lib/docker/cli-plugins"
    if [[ -z "${DOCKER_CLI_PLUGIN_EXTRA_DIRS:-}" ]]; then
      export DOCKER_CLI_PLUGIN_EXTRA_DIRS="$_plug"
    elif [[ ":${DOCKER_CLI_PLUGIN_EXTRA_DIRS}:" != *":${_plug}:"* ]]; then
      export DOCKER_CLI_PLUGIN_EXTRA_DIRS="${DOCKER_CLI_PLUGIN_EXTRA_DIRS}:${_plug}"
    fi
  fi
fi
