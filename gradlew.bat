@rem Gradle wrapper script (delegado a instalación local de Gradle 8.7)
@rem Portable: busca cualquier directorio de caché del wrapper y, si no hay, usa gradle del PATH.
@if defined GRADLE_CMD goto :run
set "GRADLE_CMD="
for /d %%D in ("%USERPROFILE%\.gradle\wrapper\dists\gradle-8.7-bin\*") do (
    if exist "%%D\gradle-8.7\bin\gradle.bat" set "GRADLE_CMD=%%D\gradle-8.7\bin\gradle.bat"
)
if defined GRADLE_CMD goto :run
where gradle >nul 2>nul
if errorlevel 1 (
    echo Error: no se encontro Gradle 8.7. Instalalo o definí GRADLE_CMD con la ruta a gradle.bat.
    exit /b 1
)
set "GRADLE_CMD=gradle.bat"

:run
"%GRADLE_CMD%" %*
