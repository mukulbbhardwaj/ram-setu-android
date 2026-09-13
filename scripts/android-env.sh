# Shared JDK/SDK paths for Ram Setu Gradle scripts (macOS + Android Studio default).
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$SDK/platform-tools:$SDK/emulator:$PATH"

if [ ! -x "$JAVA_HOME/bin/java" ]; then
  echo "JDK not found at JAVA_HOME=$JAVA_HOME"
  echo "Install Android Studio or set JAVA_HOME to JDK 17+."
  exit 1
fi
