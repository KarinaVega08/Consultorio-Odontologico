#!/usr/bin/env bash
# Levanta el backend con el perfil demo: H2 en memoria + datos ficticios (Ley 25.326).
# No necesita PostgreSQL ni Mailpit. El seeder es idempotente.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "Levantando backend en http://localhost:8080 con datos demo..."
echo "15 pacientes FICTICIOS - contraseña demo1234 para todos:"
echo "  99999999 Juan Perez | 38901234 Camila Pereyra | 40123789 Lucas Sosa"
echo "  35678123 Valentina Quiroga | 41234567 Federico Moya | 30555123 Romina Duarte (sin Gmail)"
echo "  37654321 Nicolas Aguirre | 33445678 Lucia Barsotti | 40222333 Martin Caceres"
echo "  36111222 Sofia Delgado | 39333444 Agustin Espinoza | 34555666 Jazmin Flores"
echo "  40888777 Tomas Gimenez | 31222999 Elena Lopez | 39777111 Bruno Martinez"
echo "CTRL+C para detener."

cd "$ROOT/backend"
../gradlew bootRun --args='--spring.profiles.active=demo'
