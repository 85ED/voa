#!/usr/bin/env bash
# Exige JDK 21+ para o backend VOA.
set -euo pipefail

is_sourced=false
if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
  is_sourced=true
fi

stop_here() {
  if $is_sourced; then
    return "$1"
  fi
  exit "$1"
}

ver="$(java -version 2>&1 | head -1 || true)"
echo "$ver"

if java -version 2>&1 | grep -qE 'version "21'; then
  echo "Java 21 OK."
  stop_here 0
  return 0 2>/dev/null || exit 0
fi

if /usr/libexec/java_home -V 2>&1 | grep -q '21\.'; then
  export JAVA_HOME
  JAVA_HOME="$(/usr/libexec/java_home -v 21)"
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "Use JDK 21 neste terminal:"
  echo "  JAVA_HOME=$JAVA_HOME"
  echo "  java -version"
  stop_here 0
  return 0 2>/dev/null || exit 0
fi

echo ""
echo "JDK 21 não encontrado. No Mac (Homebrew):"
echo "  brew install openjdk@21"
echo "  export JAVA_HOME=\"\$(/usr/libexec/java_home -v 21 2>/dev/null || echo /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home)\""
echo "  export PATH=\"\$JAVA_HOME/bin:\$PATH\""
echo "  java -version"
stop_here 1
return 1 2>/dev/null || exit 1
