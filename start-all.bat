@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

echo ========================================
echo   TalentFlow ATS
echo   Backend  http://localhost:8080
echo   Frontend http://localhost:5180
echo ========================================
echo.

for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":8080" ^| findstr "LISTENING"') do (
  echo Kill old backend PID=%%a
  taskkill /F /PID %%a >nul 2>&1
)

echo [1/2] Starting backend...
start "TalentFlow-Backend" cmd /k "%~dp0start-backend.bat"

echo Waiting 25 seconds for backend...
ping -n 26 127.0.0.1 >nul

echo [2/2] Starting frontend...
start "TalentFlow-Frontend" cmd /k "%~dp0start-frontend.bat"

echo.
echo ========================================
echo   Done! Open http://localhost:5180
echo.
echo   candidate / candidate123
echo   admin / admin123
echo   executive / exec123
echo ========================================
echo.

ping -n 6 127.0.0.1 >nul
start "" http://localhost:5180

pause
