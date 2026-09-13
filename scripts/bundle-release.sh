#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=android-env.sh
source "$ROOT/scripts/android-env.sh"

cd "$ROOT"
./gradlew bundleRelease

VERSION="$(grep -E 'versionName\s*=' app/build.gradle.kts | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
BUILD="$(grep -E 'versionCode\s*=' app/build.gradle.kts | head -1 | sed -E 's/.*=\s*([0-9]+).*/\1/')"
AAB="$ROOT/play-store/bundles/ram-setu-v${VERSION}-build${BUILD}.aab"

if [ -f "$AAB" ]; then
  echo ""
  echo "Upload this bundle to Play Console:"
  echo "$AAB"
else
  echo "Bundle task finished but archive not found at expected path."
  echo "Check app/build/outputs/bundle/release/app-release.aab"
fi
