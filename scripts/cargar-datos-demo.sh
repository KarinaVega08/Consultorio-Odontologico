#!/usr/bin/env bash
# Levanta el backend con el perfil demo: H2 en memoria + datos ficticios (Ley 25.326).
# No necesita PostgreSQL ni Mailpit. El seeder es idempotente.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "Levantando backend en http://localhost:8080 con datos demo..."
echo "Login de prueba: DNI 99999999 / contraseña demo1234 (paciente FICTICIO)."
echo "CTRL+C para detener."

cd "$ROOT/backend"
../gradlew bootRun --args='--spring.profiles.active=demo'
