#!/usr/bin/env sh

# Gradle wrapper script (delegado a instalación local de Gradle 8.7)
# Portable: busca cualquier hash de caché del wrapper y, si no hay, usa gradle del PATH.
if [ -n "$GRADLE_CMD" ]; then
    exec "$GRADLE_CMD" "$@"
fi

for candidate in "$HOME"/.gradle/wrapper/dists/gradle-8.7-bin/*/gradle-8.7/bin/gradle; do
    if [ -x "$candidate" ]; then
        exec "$candidate" "$@"
    fi
done

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

echo "Error: no se encontró Gradle 8.7. Instalalo o definí GRADLE_CMD con la ruta a gradle." >&2
exit 1
