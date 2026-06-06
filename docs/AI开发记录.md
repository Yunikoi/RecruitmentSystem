# AI 辅助开发记录

## 项目信息
- **项目名称**：招聘岗位管理系统
- **开发方式**：借助 Cursor AI 编码助手完成需求分析、系统设计与代码生成
- **完成日期**：2026年5月

## 开发过程

### 1. 需求分析
将 PDF《招聘岗位管理系统需求规格说明书》作为输入，AI 助手解析出：
- 必做：岗位 CRUD、前端页面、Excel 导入
- 选做：审批、统计、生命周期、搜索过滤
- 技术规范：Spring Boot + RESTful API + Vue 3

### 2. 系统设计
AI 助手协助完成：
- **数据模型**：Position 实体（id、title、description、status、createdAt、updatedAt、approver、approvalComment、publishedAt）
- **状态枚举**：DRAFT → PENDING → PUBLISHED → CLOSED
- **API 设计**：基础路径 `/webapi`，统一 JSON 响应 `{ code, message, data }`
- **前端路由**：岗位列表、表单、详情、Excel 导入、统计、公开浏览

### 3. 代码生成
通过多轮 AI 对话生成：
- 后端：Entity、Repository、Service、Controller、DTO、异常处理、CORS 配置、Excel 解析（Apache POI）
- 前端：Vue 3 组件、Axios 封装、Element Plus UI、ECharts 统计图表

### 4. 关键 AI 协作点
| 环节 | AI 作用 |
|------|---------|
| 实体与枚举设计 | 根据 PDF 字段要求生成 JPA 实体 |
| REST 接口 | 按规范生成 CRUD + 扩展接口 |
| Excel 导入 | 生成 POI 解析逻辑，校验模板列名 |
| 审批流程 | 实现状态流转与业务校验 |
| 前端页面 | 生成表格、表单、上传、图表组件 |
| 异常处理 | 统一 BusinessException 与全局处理器 |

### 5. 人工调整
- 路由顺序调整（statistics 路径需在 `{id}` 之前）
- H2 内存数据库作为默认配置，便于零配置运行
- 初始化示例数据便于演示

## 运行验证步骤
1. 启动后端：`cd backend && mvn spring-boot:run`
2. 启动前端：`cd frontend && npm install && npm run dev`
3. 访问 `http://localhost:5180` 验证各功能（端口被占用时以终端实际输出为准）

## 功能演示建议（录制视频用）
1. 岗位列表：搜索、筛选、查看详情
2. 新增岗位：填写表单提交
3. Excel 导入：下载模板、上传、查看结果
4. 审批流程：对待审批岗位通过/驳回
5. 生命周期：草稿提交、已发布关闭
6. 数据统计：查看数字卡片与图表
7. 公开浏览：查看已发布岗位

## 说明
本记录可作为实验报告中「AI 辅助开发过程」部分的参考素材。建议同时保留 Cursor 对话截图作为附件。
