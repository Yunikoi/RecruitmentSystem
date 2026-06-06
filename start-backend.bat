@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0backend"

echo [Backend] Building...
call mvn -q -DskipTests package
if errorlevel 1 (
  echo [Backend] Build failed. Close old backend window and retry.
  pause
  exit /b 1
)

echo [Backend] Running on http://localhost:8080
echo Wait for: Started RecruitmentApplication
echo.
java -jar target\position-management-1.0.0.jar
pause
