@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0frontend"

if not exist node_modules (
  echo [Frontend] npm install...
  call npm install
)

echo [Frontend] Running on http://localhost:5180
echo.
call npm run dev
pause
