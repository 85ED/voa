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

# Não use pipe com grep -q em java_home -V: com pipefail o SIGPIPE falha o teste.
if JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
  export JAVA_HOME
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "JDK 21 selecionado neste terminal:"
  echo "  JAVA_HOME=$JAVA_HOME"
  java -version 2>&1 | head -1
  stop_here 0
  return 0 2>/dev/null || exit 0
fi

if [[ -d /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ]]; then
  export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "JDK 21 (Homebrew) selecionado:"
  echo "  JAVA_HOME=$JAVA_HOME"
  java -version 2>&1 | head -1
  stop_here 0
  return 0 2>/dev/null || exit 0
fi

echo ""
echo "JDK 21 não encontrado. No Mac (Homebrew):"
echo "  brew install openjdk@21"
echo "  export JAVA_HOME=\"\$(/usr/libexec/java_home -v 21 2>/dev/null || echo /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home)\""
echo "  export PATH=\"\$JAVA_HOME/bin:$PATH\""
echo "  java -version"
stop_here 1
return 1 2>/dev/null || exit 1
