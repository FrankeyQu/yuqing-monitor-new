# 卓然舆情 — Claude Code 项目配置

## 项目身份

- **名称**：卓然舆情（Zhuoran Insight）
- **语言**：中文（所有输出、注释、文档均使用中文）
- **协议**：GPLv3
- **技术栈**：Spring Boot 2.1.4 + MyBatis + MySQL + Redis + Thymeleaf + Vue 2 + ECharts
- **JDK**：1.8
- **包名基础**：`com.stonedt.intelligence`

## 核心文档（新会话请先阅读）

| 文档 | 说明 |
|------|------|
| [AGENTS.md](AGENTS.md) | AI 协作规范（主控/子线程分工规则） |
| [主线prompt.md](%E4%B8%BB%E7%BA%BFprompt.md) | 主控 Agent 总控 Prompt |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | 架构文档（模块边界、技术栈、包结构） |
| [docs/CONVENTIONS.md](docs/CONVENTIONS.md) | 开发规范（命名、文件组织、代码风格） |
| [docs/API_CONTRACT.md](docs/API_CONTRACT.md) | API 契约（接口定义） |
| [docs/STATE_MACHINE.md](docs/STATE_MACHINE.md) | 状态机（用户/方案/报告/预警状态流转） |
| [docs/PERMISSION_RULES.md](docs/PERMISSION_RULES.md) | 权限规则（认证方式、安全限制） |
| [docs/TEST_CHECKLIST.md](docs/TEST_CHECKLIST.md) | 测试清单（测试命令、检查项） |
| [docs/DEPLOY_CHECKLIST.md](docs/DEPLOY_CHECKLIST.md) | 部署清单（环境依赖、配置检查） |
| [docs/AI_PROGRESS.md](docs/AI_PROGRESS.md) | 进度记录（当前状态、风险、下一步） |

## 关键约束

1. **不跳过测试**：禁止修改 `pom.xml` 中的 `skipTests` 配置，禁止 `-DskipTests=true` / `-Dmaven.test.skip=true`
2. **不修改核心鉴权类**（除非专门任务）：
   - `interceptor/LoginHandlerInterceptor.java`
   - `util/UserUtil.java`、`util/JWTUtils.java`
   - `context/Context.java`
3. **新增 API 默认需要 token 鉴权**（除非明确公开）
4. **不创建/切换 Git 分支**（由主控 Agent 管理）
5. **代码风格**：遵循 [docs/CONVENTIONS.md](docs/CONVENTIONS.md)
6. **状态变更**：涉及用户/方案/报告/预警状态修改时，先查阅 [docs/STATE_MACHINE.md](docs/STATE_MACHINE.md)

## 常用命令

```bash
mvn compile                    # 编译检查
mvn test -DskipTests=false     # 执行测试
cd large_screen && npm run build  # 大屏构建
```

## 外部依赖（当前模块通过 HTTP API 调用）

- **Elasticsearch**：`dx2.stonedt.com:7121`（`sendPostEsSearch()`）
- **Kafka**：`dx2.stonedt.com:7189`（`doPostKafka()`）
- **NLP 服务**：`nlp.stonedt.com`
- **写作宝**：`xie.stonedt.com`

无需本地部署 ES/Kafka/RabbitMQ/MongoDB/ClickHouse/DataX。
