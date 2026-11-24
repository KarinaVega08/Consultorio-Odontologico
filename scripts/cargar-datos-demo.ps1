# Levanta el backend con el perfil demo: H2 en memoria + datos ficticios (Ley 25.326).
# No necesita PostgreSQL ni Mailpit. El seeder es idempotente.
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot

# En algunas máquinas el JDK 21 no está en el PATH; lo buscamos en ~/.jdks.
if (-not $env:JAVA_HOME) {
    $jdk = Get-ChildItem (Join-Path $env:USERPROFILE '.jdks') -Directory -Filter 'ms-21*' -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($jdk) {
        $env:JAVA_HOME = $jdk.FullName
        Write-Host "JAVA_HOME detectado: $env:JAVA_HOME"
    }
}
if (-not $env:JAVA_HOME) {
    Write-Error 'Configurá JAVA_HOME con un JDK 21 antes de correr este script.'
    exit 1
}

Write-Host 'Levantando backend en http://localhost:8080 con datos demo...'
Write-Host 'Login de prueba: DNI 99999999 / contraseña demo1234 (paciente FICTICIO).'
Write-Host 'CTR+C para detener.'

Push-Location (Join-Path $root 'backend')
try {
    & (Join-Path $root 'gradlew.bat') bootRun --args='--spring.profiles.active=demo'
}
finally {
    Pop-Location
}
