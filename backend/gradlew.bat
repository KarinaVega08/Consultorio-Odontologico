@rem Gradle wrapper script for backend module
@if defined GRADLE_CMD (
    set "GRADLE_CMD=%GRADLE_CMD%"
) else (
    set "GRADLE_CMD=%USERPROFILE%\.gradle\wrapper\dists\gradle-8.7-bin\bhs2wmbdwecv87pi65oeuq5iu\gradle-8.7\bin\gradle.bat"
)
cd /d "%~dp0"
"%GRADLE_CMD%" %*
