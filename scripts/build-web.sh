#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

GIT_COMMIT="$(git rev-parse --short=8 HEAD 2>/dev/null || echo dev)"
BUILD_DATE="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

tmp="$(mktemp)"
sed -E \
  -e "s/(commit: \")[^\"]+(\")/\\1${GIT_COMMIT}\\2/" \
  -e "s/(buildDate: \")[^\"]+(\")/\\1${BUILD_DATE}\\2/" \
  jarvis.html > "$tmp"
mv "$tmp" jarvis.html

echo "Jarvis web preparado: commit ${GIT_COMMIT}, build ${BUILD_DATE}"
