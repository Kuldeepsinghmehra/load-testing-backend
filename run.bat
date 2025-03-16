@echo off
echo Starting Spring Boot Application...
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed
    pause
    exit /b %ERRORLEVEL%
)
echo Build successful, starting application...
call mvn spring-boot:run
pause 