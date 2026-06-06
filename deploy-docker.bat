@echo off
chcp 65001 >nul
echo TalentFlow ATS — Docker 企业部署
echo.

if not exist .env (
  echo [提示] 未找到 .env，从 .env.example 复制...
  copy .env.example .env >nul
  echo 请编辑 .env 填写 AI_API_KEY 后重新运行。
  notepad .env
  pause
  exit /b 1
)

echo 正在构建并启动 MySQL + 后端 + 前端...
docker compose up -d --build

echo.
echo 部署完成：
echo   前端  http://localhost
echo   后端  http://localhost:8080/webapi/health
echo   演示账号 candidate / candidate123
echo.
pause
