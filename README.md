# TalentFlow ATS — 智能招聘管理系统

企业级 ATS（Applicant Tracking System）：Spring Boot 3 + Vue 3 + MySQL，支持 AI 人岗匹配、语音初试、模拟面试、日程协同、合规审计等。

## 架构概览

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Vue 3 前端  │────▶│ Spring Boot  │────▶│   MySQL 8   │
│ Vercel/Nginx │     │   REST API   │     │  持久化存储  │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                    DeepSeek API（AI）
```

| 环境 | 数据库 | 启动方式 |
|------|--------|----------|
| 本地开发 | H2 内存库 | `start-all.bat` |
| 企业生产 | MySQL 8 | `docker compose up -d` |
| 前端托管 | — | Vercel + 独立后端 |

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2、JPA、MySQL / H2 |
| 前端 | Vue 3、Vite、Element Plus、ECharts |
| AI | DeepSeek API（OpenAI 兼容） |
| 部署 | Docker Compose、Nginx、GitHub Actions、Vercel |

## 快速启动（本地开发）

双击 **`start-all.bat`**，或：

```bash
# 后端（8080）
cd backend && mvn spring-boot:run

# 前端（5180）
cd frontend && npm install && npm run dev
```

本地 AI 密钥：复制 `backend/src/main/resources/application-local.yml.example` 为 `application-local.yml` 并填写 API Key。

## 企业部署（Docker 推荐）

**一条命令启动 MySQL + 后端 + Nginx 前端：**

```bash
cp .env.example .env          # 填写 AI_API_KEY
docker compose up -d --build
```

Windows 可双击 **`deploy-docker.bat`**。

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost |
| 后端健康检查 | http://localhost:8080/webapi/health |
| MySQL | localhost:3306 / 库名 `talentflow` |

**生产环境变量（`.env`）：**

| 变量 | 说明 |
|------|------|
| `AI_API_KEY` | DeepSeek API 密钥 |
| `DB_PASSWORD` | MySQL 密码 |
| `SEED_DEMO_DATA` | 首次 `true` 写入演示数据，上线后改 `false` |
| `CORS_ALLOWED_ORIGINS` | 允许的前端域名，逗号分隔 |

## 前端部署到 Vercel

1. 后端先部署到云服务器 / Railway / Render，记下 API 地址  
2. GitHub 导入项目，**Root Directory** 设为 `frontend`  
3. 环境变量：`VITE_API_BASE=https://你的后端/webapi`  
4. 或修改 `frontend/vercel.json` 用 rewrites 代理 `/webapi`

## 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 求职者 | candidate | candidate123 |
| HR 管理员 | admin | admin123 |
| 面试官 | interviewer | interview123 |
| 部门 | dept_hr | dept123 |
| 管理层 | executive | exec123 |

## 项目结构

```
第二次/
├── backend/              # Spring Boot API
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.yml          # 公共 + local profile
│       ├── application-prod.yml     # 生产 MySQL
│       └── application-local.yml.example
├── frontend/             # Vue 3 SPA
│   ├── Dockerfile
│   ├── nginx.conf
│   └── vercel.json
├── docker-compose.yml    # 企业全栈编排
├── .env.example
├── .github/workflows/ci.yml
└── deploy-docker.bat
```

## CI

推送到 `main` / `master` 时自动执行 Maven 构建与前端 `npm run build`。

## 本地 vs 企业差异

| 项 | 本地 (profile: local) | 企业 (profile: prod) |
|----|----------------------|----------------------|
| 数据库 | H2 内存 | MySQL 持久化 |
| 演示数据 | 自动 seed | 由 `SEED_DEMO_DATA` 控制 |
| CORS | 允许全部 | 白名单域名 |
| 文件上传 | `uploads/` 本地目录 | Docker 卷 `/data/uploads` |
| SQL 日志 | 开启 | 关闭 |

## API

基础路径：`/webapi` · 健康检查：`GET /webapi/health` · 统一响应 `{ code, message, data }`

## 更多文档

- [AI 辅助开发记录](docs/AI开发记录.md)
