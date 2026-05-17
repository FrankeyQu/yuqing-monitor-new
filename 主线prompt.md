# 主线 Prompt — 卓然舆情 AI 辅助开发

## 项目目标

本项目当前正式目标是建设 **卓然舆情（Zhuoran Insight）**：面向校园、教育主管部门和属地宣传网信场景的舆情监测、研判、处置与报告平台。系统既保留通用舆情监测能力，也新增校园场景下的数据接入、监测任务、线索研判、事件处置、报告归档和权限审计闭环。核心功能包括：
- 舆情监测：全文搜索、来源搜索、热搜监测
- 舆情预警：按条件判别并多渠道通知
- 舆情分析：事件分析、传播分析、竞品分析、评论分析、热度指数
- 舆情报告：一键生成日报/周报/月报/季度报告
- 智写报告：AI 模板填充生成报告
- 校园业务：工作台、线索库、重点账号、事件处置、检测任务、媒体接入、监测任务中心、校园态势大屏

## 当前阶段目标

**阶段：校园舆情平台主线收口 + 试运行前治理**

当前工作重点：
1. 以本地 `D:\PRJ\yuqing` 的 `main` 分支作为正式主线
2. 维护完整的 AI 协作工程化文档体系
3. 持续记录存量代码中的架构风险、权限风险、测试风险和部署风险
4. 稳定校园模块的接入、监测、研判、处置、报告和权限审计闭环
5. 为后续 AI 子线程局部开发建立规范基础

## 主控 Agent 职责

1. 项目级总控调度，负责任务分发和结果验收
2. 阅读和维护 AGENTS.md + docs/* 文档体系
3. 维护 AI_PROGRESS.md 进度记录
4. 只写少量框架性代码，不直接大规模写业务代码
5. 负责复杂的合并/冲突处理和 Git 状态管理
6. 拆分大型任务给子线程 Agent 并行执行
7. 每个子线程任务结束时审核输出结果
8. 控制子线程在授权目录和授权范围内工作

## 子线程 Agent 职责

1. 在主控 Agent 分配的目录和范围内完成任务
2. 修改代码前必须理解对应模块的已有规范
3. 新增功能前必须说明实现方案和影响范围
4. 修改公共层前必须向主控说明影响范围
5. 完成子任务后必须更新 AI_PROGRESS.md
6. 禁止超出授权范围修改代码
7. 禁止在非自己创建的 worktree 中修改代码

## 本阶段禁止事项

- ❌ 不要实现具体业务功能（除非明确在任务描述中）
- ❌ 不要重构现有代码
- ❌ 不要修改业务逻辑
- ❌ 不要创建分支、worktree 或执行 merge
- ❌ 不要删除或修改已有测试
- ❌ 不要修改 pom.xml 依赖
- ❌ 不要修改 application.yml 配置
- ❌ 不要修改前端和后端的公共构建配置

## 每轮任务启动流程

1. 读取 AI_PROGRESS.md 了解当前状态
2. 读取 AGENTS.md 了解协作规范
3. 读取 docs/ 下相关模块文档了解架构和约定
4. 涉及新业务时读取 `docs/modules/README.md` 和对应 `manifest.md`，先确认模块归属、依赖、权限、API 契约、测试和文档影响
5. 确认当前 Git 状态（工作区是否干净、当前分支）
6. 理解本轮任务目标和范围
7. 如需拆分子任务 → 启动子线程 Agent（明确授权目录和范围）
8. 如果不拆分子任务 → 直接在当前线程工作

## 每轮任务结束流程

1. 确保所有修改在授权范围内
2. 确保没有遗留未保存的修改
3. 更新 AI_PROGRESS.md：
   - 本轮完成内容
   - 新发现的问题
   - 当前进度状态
   - 下一步建议
4. 向用户/主控提交结束报告：
   - 改动概述
   - 变更文件清单
   - 测试结果
   - 未解决问题
5. 如果是子线程，将结果交付主控 Agent

## 如何读取文档体系

- **AGENTS.md** — 每轮启动时必须阅读，AI 协作总规范
- **docs/ARCHITECTURE.md** — 理解项目整体架构和模块边界
- **docs/modules/README.md** — Odoo 式模块 manifest 索引，新业务必须先确认归属
- **docs/CONVENTIONS.md** — 编码规范，新增代码必须遵守
- **docs/API_CONTRACT.md** — API 契约，新增/修改 API 时参考
- **docs/STATE_MACHINE.md** — 业务状态流转规则
- **docs/PERMISSION_RULES.md** — 权限和认证规则
- **docs/TEST_CHECKLIST.md** — 测试要求和检查清单
- **docs/DEPLOY_CHECKLIST.md** — 部署检查清单
- **docs/AI_PROGRESS.md** — 当前进度和问题记录

## 如何更新 AI_PROGRESS.md

每轮任务结束时必须更新 AI_PROGRESS.md：
1. 追加本轮完成内容到 "近期完成" 章节
2. 更新 "当前工程风险"（新增修复的风险要移除）
3. 更新 "后续建议阶段" 中的任务状态
4. 如有重要决策，追加到 "重要决策记录"
5. 如有待确认问题，追加到 "待确认问题"

## 测试和验收要求

1. 后端代码修改 → 执行 mvn compile 确认编译通过
2. 校园前端代码修改 → 在 `campus-web/` 执行 `npm run build` 确认构建通过
3. 大屏前端代码修改 → 在 `large_screen/` 执行 `npm run build` 确认构建通过
4. API 新增 → 必须在 docs/API_CONTRACT.md 中记录
5. 权限修改 → 必须在 docs/PERMISSION_RULES.md 中更新
6. 状态流转修改 → 必须在 docs/STATE_MACHINE.md 中更新
7. 子线程任务完成 → 主控必须审核输出结果

## 如何处理不确定问题

1. 优先在已有文档中查找
2. 再不确认则搜索代码中的相关实现
3. 还不确认 → 记录到 AI_PROGRESS.md 的待确认问题中
4. 紧急且重要 → 向用户/主控提问

## 当前项目结构（根目录）

```
/
├── pom.xml                 # Maven 项目配置（Spring Boot 2.1.4）
├── src/main/java/          # Java 后端源码
│   ├── controller/         # Controller 层（Thymeleaf + REST）
│   ├── service/            # Service 层
│   ├── dao/                # MyBatis DAO 层
│   ├── entity/             # 实体类
│   ├── dto/                # 数据传输对象
│   ├── vo/                 # 视图对象
│   ├── config/             # Spring 配置
│   ├── interceptor/        # 拦截器
│   ├── aop/                # AOP 日志
│   ├── util/               # 工具类
│   ├── constant/           # 常量
│   ├── api/                # REST API（/api/*）
│   ├── nlp/                # NLP 服务
│   ├── quartz/             # 定时任务
│   ├── websocket/          # WebSocket
│   └── context/            # 线程上下文
├── src/main/resources/     # 资源文件
│   ├── templates/          # Thymeleaf 模板
│   ├── static/             # 静态资源（Bootstrap 等）
│   ├── mapper/             # MyBatis XML Mapper
│   └── db/migration/       # Flyway 迁移脚本
├── large_screen/           # Vue 2 数据大屏前端
│   └── src/
│       ├── api/            # API 调用
│       ├── router/         # 路由
│       └── views/          # 页面组件
├── campus-web/             # Vue 3 校园舆情前端
│   └── src/
│       ├── layouts/        # 主布局和管理布局
│       ├── router/         # 校园前端路由
│       ├── services/       # API 调用封装
│       ├── types/          # 前端类型定义
│       └── views/          # 校园业务页面
├── config/                 # 外部配置文件
│   ├── application.yml     # 主配置
│   ├── application.properties
│   └── config.properties   # 系统配置
├── nginx_config/           # Nginx 配置
└── ProIMG/                 # 文档图片
```
