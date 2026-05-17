# 卓然舆情 Zhuoran Insight

卓然舆情（英文名 **Zhuoran Insight**）是一套面向校园、教育主管部门和属地宣传网信场景的舆情监测、研判、处置与报告平台。系统围绕“公开合规接入、任务化监测、线索化研判、事件化处置、报告化沉淀”设计，帮助业务团队把分散的网络信息转化为可追踪、可协同、可复盘的工作闭环。

## 核心定位

- **监测**：围绕学校、政策、招生、重点账号、指定链接和关键词持续发现风险信号。
- **研判**：将命中内容沉淀为线索，支持情感、风险等级、命中词、互动量和正文追溯。
- **处置**：把线索归并为事件，跟踪预警、处置任务、反馈记录和归档结论。
- **接入**：统一管理外部数据来源、接入任务、运行日志、API 调用日志和授权边界。
- **报告**：面向日报、周报、月报、专题报告提供模板生成和 AI 辅助生成能力。
- **治理**：内置角色、菜单、API 权限、审计日志和 AI 协作文档体系。

## 当前主要能力

- 校园工作台与态势大屏。
- 线索库、预警中心、事件处置、处置任务和报告归档。
- 监测任务中心：支持主体、别名、中文/蒙语/维语关键词、负面词、排除词、平台范围、扫描频率和接入任务绑定。
- 监测结果处理：展示命中标题、正文摘要、平台、作者、命中词、风险等级、互动量，并支持转线索、转预警、忽略。
- 任务内重点目标：支持重点账号、指定链接、从监测结果一键加入，并在目标范围内搜索关键词。
- 媒体接入中心：接入来源、接入任务、标准化记录、运行日志、API 调用日志。
- 教育专题：本地区教育重点新闻、重点政策、招生政策、学校主体维护、学校正负面声量排名。
- 词库模块：负面词、正面词、风险词、教育新闻词、政策词、招生政策词。
- 校园权限：`campus_admin`、`campus_operator`、`campus_viewer` 角色模型，以及菜单/API 权限控制。

## 技术栈

- 后端：Spring Boot 2.1.4、MyBatis、MySQL、Redis、Flyway、Maven。
- 旧前端：Thymeleaf + Bootstrap。
- 校园前端：Vue 3、Vite、TypeScript、Element Plus、ECharts。
- 大屏前端：Vue 2、ECharts。
- 测试：JUnit + Maven Surefire；校园前端使用 `npm run build` 作为当前最低构建门禁。

## 本地构建

后端：

```powershell
.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" test
```

校园前端：

```powershell
cd D:\PRJ\yuqing\campus-web
npm install
npm run build
```

## 关键目录

```text
src/main/java/                         后端源码
src/main/resources/db/migration/        Flyway 数据库迁移
src/main/resources/mapper/              MyBatis Mapper
campus-web/                             Vue 3 校园前端
large_screen/                           Vue 2 大屏
docs/                                   架构、契约、部署、测试和 AI 协作文档
docs/modules/                           Odoo 式模块 manifest
```

## AI 协作与工程文档

后续 AI 辅助开发请先阅读：

- `AGENTS.md`
- `主线prompt.md`
- `docs/AI_PROGRESS.md`
- `docs/ARCHITECTURE.md`
- `docs/API_CONTRACT.md`
- `docs/PERMISSION_RULES.md`
- `docs/TEST_CHECKLIST.md`
- `docs/modules/README.md`

每轮开发结束必须更新 `docs/AI_PROGRESS.md`，涉及 API、权限、状态流转、部署或测试影响时同步更新对应文档。

## 合规边界

- 只处理公开、授权、上级移交或业务中依法获得的数据。
- 不在代码、数据库明文字段或文档中保存真实 API Key、Cookie、Token、生产密码。
- 不接入私信、通讯录、密码、非公开个人资料。
- AI 辅助研判只作为工作建议，最终结论必须人工确认。

## 许可证

本项目延续当前仓库许可证约束，详见 `LICENSE`。
