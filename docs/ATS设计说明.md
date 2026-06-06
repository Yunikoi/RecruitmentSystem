# TalentFlow ATS - 互联网顶尖招聘系统

> 人才生态与组织发展引擎 · 三端联动（C/B/M）

## 系统定位

不是简单的「简历收集器」，而是面向互联网行业的 **Applicant Tracking System (ATS)**，平衡：
- **C端**：求职者极致体验
- **B端**：HR 百倍效率
- **M端**：管理层数据决策

## 三端功能矩阵

| 阶段 | 求职者 (C) | HR/面试官 (B) | 管理层 (M) |
|------|-----------|--------------|-----------|
| 吸引投递 | 一键智能投递、简历AI解析、24h智能答疑 | 多渠道漏斗、AI匹配评分、查重 | 渠道ROI、预算分析 |
| 筛选评估 | 外卖式进度追踪、AI音视频初试 | 结构化面评、面试安排、淘汰反馈 | 漏斗流失分析、HC预警 |
| 录用分析 | 个性化反馈、岗位推荐、人才库激活 | 人才池管理、协同面试 | 招聘ROI、人才继任 |

## 测试账号

| 角色 | 账号 | 密码 | 入口 |
|------|------|------|------|
| 求职者 | candidate | candidate123 | 公开岗位 → 一键投递 |
| 招聘HR | admin | admin123 | 招聘漏斗 |
| 面试官 | interviewer | interview123 | 招聘漏斗 |
| 管理层 | executive | exec123 | 管理驾驶舱 |
| 部门 | dept_hr / dept_tech | dept123 | 岗位管理 |

## 启动

```bash
# 后端
cd backend && mvn -DskipTests package && java -jar target/position-management-1.0.0.jar

# 前端
cd frontend && npm run dev
```

访问 http://localhost:5180

## 核心 API

| 端 | 路径 | 说明 |
|----|------|------|
| C | POST /webapi/candidate/apply | 智能投递 |
| C | GET /webapi/candidate/applications | 进度追踪 |
| B | GET /webapi/recruiter/applications | 招聘漏斗 |
| B | PUT /webapi/recruiter/applications/{id}/stage | 阶段流转 |
| M | GET /webapi/management/dashboard | 管理驾驶舱 |

## AI 真实能力（DeepSeek）

已接入 **DeepSeek Chat API**，以下功能优先调用 LLM，失败时自动回退规则引擎：

| 功能 | 说明 |
|------|------|
| 简历解析 | 上传/粘贴简历 → 提取姓名、邮箱、技能 |
| 人岗匹配 | 投递时 AI 打分 + 亮点/风险 |
| 智能答疑 | C 端向 AI 咨询薪酬、福利等 |
| AI 初试 | 根据岗位+简历生成 5 道面试题 |
| 淘汰反馈 | HR 淘汰时 AI 生成感谢信 |

**配置文件：** `backend/src/main/resources/application-local.yml`

```yaml
ai:
  enabled: true
  api-key: sk-你的DeepSeek密钥
  base-url: https://api.deepseek.com
  model: deepseek-chat
```

修改后需 **重启后端**（先关闭旧 8080 进程，再 `start-all.bat` 或 `start-backend.bat`）。
