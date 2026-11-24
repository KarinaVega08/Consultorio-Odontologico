#!/usr/bin/env sh

# Gradle wrapper script (delegado a instalación local de Gradle 8.7)
GRADLE_CMD="${GRADLE_CMD:-$HOME/.gradle/wrapper/dists/gradle-8.7-bin/bhs2wmbdwecv87pi65oeuq5iu/gradle-8.7/bin/gradle}"

exec "$GRADLE_CMD" "$@"
