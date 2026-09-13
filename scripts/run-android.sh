#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$SDK/platform-tools:$SDK/emulator:$PATH"

if ! command -v adb >/dev/null; then
  echo "adb not found. Set ANDROID_HOME or install Android SDK platform-tools."
  exit 1
fi

if [ "$(adb devices | grep -c 'device$')" -eq 0 ]; then
  AVD="${1:-Pixel_10_Pro}"
  echo "No device online. Starting emulator: $AVD"
  nohup emulator -avd "$AVD" -no-snapshot-load >> /tmp/ramsetu-emulator.log 2>&1 &
  echo "Waiting for emulator boot (up to 3 min)..."
  for _ in $(seq 1 36); do
    boot="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
    if [ "$boot" = "1" ]; then break; fi
    sleep 5
  done
fi

cd "$ROOT"
./gradlew installDebug
adb shell am start -n com.vanarsena.ramsetu/.MainActivity
echo "Ram Setu launched on device."
