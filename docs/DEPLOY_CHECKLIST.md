# 部署检查清单 — 卓然舆情

> 基于现有项目配置生成。不确定的部署项标记为 **待确认**。

## 环境依赖

### 软件环境
| 依赖 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 1.8+ | 运行 Spring Boot |
| MySQL | 5.7+ | 主数据库 |
| Redis | 4.0+ | 缓存 + Session 存储 |
| Nginx | [待确认] | 反向代理 |
| Elasticsearch | 外部 HTTP API | 全文检索（通过 HTTP API 调用 `dx2.stonedt.com:7121`） |
| Kafka | 外部 HTTP API | 消息推送（通过 HTTP API 调用 `dx2.stonedt.com:7189`） |
| NLP Service | 外部 HTTP API | NLP 分析服务（通过 HTTP API 调用 `nlp.stonedt.com`） |
| 写作宝 | 外部 HTTP API | AI 写作（通过 HTTP API 调用 `xie.stonedt.com`） |
| DeepSeek API | 外部 HTTP API | 校园多语言研判、AI 报告生成 |
| TikHub API | 外部 HTTP API | 校园社媒数据接入（可选） |
| 百度千帆 Web Search | 外部 HTTP API | 校园网页/多语言舆情接入（可选） |
| Jina Reader | 托管或自部署 HTTP API | 校园公开网页正文增强（可选） |

✅ **确认说明**：当前开源版本（数据展示模块）直接依赖 **MySQL + Redis**。Elasticsearch、Kafka、NLP 服务、写作宝均通过 HTTP API 调用，无需在本地部署这些中间件。RabbitMQ、MongoDB、ClickHouse、DataX 属于数据处理和数据采集模块（闭源/未开源），当前模块不依赖它们。

## 环境变量检查

### config/application.properties
```
token.expire-time=7200000     # Token 过期时间（毫秒）
token.private-key=xxx         # Token 签名私钥（必须修改）
system.url=http://localhost   # 系统访问地址
kafuka.url=http://...         # Kafka 服务地址 [待确认]
insertnewwords.url=http://... # 新词插入服务地址 [待确认]
nlp.url=http://...            # NLP 服务地址 [待确认]
prelaunch.strict=1            # 试点/上线前建议开启，阻止默认弱口令
tikhub.api-key=xxx            # TikHub 接入密钥（使用 TikHub 时必填）
schedule.campus-ingest.open=0 # 校园接入调度开关，确认任务后再开启
schedule.campus-monitor.open=0 # 校园监测调度开关，确认任务后再开启
```

### config/application.yml
| 配置项 | 说明 | 必须修改 |
|--------|------|---------|
| server.port | 服务端口（默认8084） | 按需 |
| spring.datasource.url | MySQL 连接地址 | ✅ |
| spring.datasource.username | MySQL 用户名 | ✅ |
| spring.datasource.password | MySQL 密码 | ✅ |
| spring.redis.host | Redis 地址 | ✅ |
| spring.redis.password | Redis 密码 | ✅（如果设置了） |
| knife4j.basic.username/password | Swagger 访问密码 | 按需 |
| springdoc.swagger-ui.enabled | Swagger UI 开关，生产建议关闭 | ✅ |

### config/config.properties（校园 AI/接入）
| 配置项 | 环境变量 | 说明 | 必须修改 |
|--------|----------|------|---------|
| deepseek.api.url | `DEEPSEEK_API_URL` | DeepSeek Chat Completions 地址 | 按需 |
| deepseek.api.key | `DEEPSEEK_API_KEY` | 多语言研判/AI 报告密钥 | 使用 AI 时必须 |
| baidu.api.url | `BAIDU_API_URL` | 百度千帆 Web Search 地址 | 按需 |
| baidu.api.key | `BAIDU_API_KEY` | 百度搜索接入密钥 | 使用百度接入时必须 |
| content.extraction.enabled | `CONTENT_EXTRACTION_ENABLED` | 是否启用正文提取增强 | 使用 Jina Reader 时必须设为 `true` |
| content.extraction.provider | `CONTENT_EXTRACTION_PROVIDER` | 正文提取供应商，当前为 `jina` | 使用 Jina Reader 时必须 |
| jina.reader.api.url | `JINA_READER_API_URL` | Jina Reader 地址；可用托管 `https://r.jina.ai` 或内网自部署地址 | 使用 Jina Reader 时必须 |
| jina.reader.api.key | `JINA_READER_API_KEY` | Jina Reader 托管服务密钥；自部署无鉴权时可空 | 按部署方式 |
| jina.reader.timeout-ms | `JINA_READER_TIMEOUT_MS` | Reader 默认超时，默认 15000ms，最大 30000ms | 按需 |
| jina.reader.max-content-length | `JINA_READER_MAX_CONTENT_LENGTH` | 单篇正文最大入库长度，默认 20000 | 按需 |
| tikhub.base-url | `TIKHUB_BASE_URL` | TikHub API 地址 | 按需 |
| tikhub.api-key | `TIKHUB_API_KEY` | TikHub API 密钥 | 使用 TikHub 时必须 |
| spring.flyway.placeholder-replacement | `SPRING_FLYWAY_PLACEHOLDER_REPLACEMENT` | 建议设为 `false`，避免报告模板中的 `${...}` 文本被 Flyway 误识别为迁移占位符 | ✅ |

### 试点上线强制项
- [ ] `TOKEN_PRIVATE_KEY` 已替换为正式随机密钥，不使用默认 `change-this-token-private-key-before-deploy`
- [ ] `PRELAUNCH_STRICT=1` 已开启
- [ ] 默认管理员密码已修改或已禁用默认弱口令
- [ ] 仅开启真实需要的校园调度：`SCHEDULE_CAMPUS_INGEST_OPEN`、`SCHEDULE_CAMPUS_MONITOR_OPEN`
- [ ] DeepSeek/TikHub/百度/Jina Reader 密钥已按最小权限配置，并确认调用额度/限流策略
- [ ] 如使用后台 AI 能力管理，`/admin/settings/ai` 中供应商 `credentialRef` 与服务器环境变量名称一致，数据库不保存明文密钥
- [ ] 如启用 Jina Reader，已确认使用托管 `r.jina.ai` 还是自部署 `jina-ai/reader`；正式政企/学校环境优先使用自部署或受控出口
- [ ] 公开网页 `jina_reader` 任务只配置白名单内公开 URL，未配置登录页、内网地址、IP 地址或含 Cookie/Token/签名参数的 URL

## 数据库连接检查

### MySQL 检查
- [ ] MySQL 服务运行正常
- [ ] 数据库 `stonedt_portal` 已创建（DataSourceConfig 会自动创建）
- [ ] 用户权限正确（有建表权限给 Flyway）
- [ ] 字符集 `utf8mb4` 支持
- [ ] 连接 URL 中的 `serverTimezone=Asia/Shanghai` 正确
- [ ] `max_allowed_packet` 足够大（长文本存储）

### Redis 检查
- [ ] Redis 服务运行正常
- [ ] 密码配置一致
- [ ] 数据库索引（database: 0）正确

## Migration 检查

- [ ] Flyway 自动执行 `V1.0__InitTableAndData.sql`
- [ ] Flyway 自动执行到当前最新迁移 `V1.39__CampusAiCapabilityManagement.sql`
- [ ] `SPRING_FLYWAY_PLACEHOLDER_REPLACEMENT=false` 已配置，避免旧报告模板迁移中的 `${...}` 文本触发 placeholder 校验错误
- [ ] 如果已有数据库表结构，检查版本冲突
- [ ] 初始数据是否已经导入（如测试方案、测试用户）
- [ ] 默认管理员账号可用：13900000000 / stonedt
- [ ] 校园权限初始化数据存在：`campus_admin` / `campus_operator` / `campus_viewer`
- [ ] `campus_permission_api` 中已有 `/campus/**` 核心接口权限；新增接口上线前必须补迁移
- [ ] `campus_monitor_watch_target`、`campus_school_subject` 表已创建，监测结果/接入记录互动指标字段已存在
- [ ] Flyway 已执行 `V1.28__CampusMonitorDisplayAutoIngest.sql`，`campus_monitor_task` 已具备 `display_enabled/auto_ingest_enabled/last_collect_time/last_match_count/last_error_message/ingest_capability_status`
- [ ] 教育专题菜单 `/admin/education` 和 `campus:education:*` 权限已初始化
- [ ] AI 能力管理菜单 `/admin/settings/ai`、`campus:ai:view/read/operate` 权限已初始化

## Build 检查

```bash
# 后端打包
mvn clean package -DskipTests=true
# 生成 target/stonedt-portal-0.5.3-SNAPSHOT.jar

# 校园 Vue 3 前端构建
cd campus-web
npm install
npm run build
# 生成 campus-web/dist/ 目录

# 前端大屏构建
cd large_screen
npm install
npm run build
# 生成 large_screen/dist/ 目录
```

- [ ] 后端打包成功
- [ ] 校园前端构建成功
- [ ] 前端大屏构建成功（如果部署大屏功能）
- [ ] 如启用测试门禁，`mvn test -DskipTests=false` 稳定结束

## Nginx / Proxy / Docker 检查

### Nginx 配置（nginx_config/nginx.conf）
```nginx
server {
    listen       8085;
    server_name  127.0.0.1;
    
    location / {
        proxy_pass http://127.0.0.1:8084;  # Spring Boot
    }

    # 校园前端：推荐 Nginx 直接代理 campus-web/dist
    location /campus-web/ {
        alias /path/to/campus-web/dist/;
        try_files $uri $uri/ /campus-web/index.html;
    }

    # 校园后端 API：保留给 Spring Boot
    location /campus/ {
        proxy_pass http://127.0.0.1:8084;
    }
    
    # 大屏：推荐 Nginx 直接代理静态文件（无需 Tomcat）
    location /opinion_screen {
        alias /path/to/large_screen/dist/;
        try_files $uri $uri/ /opinion_screen/index.html;  # Vue history 模式 fallback
    }
}
```

- [ ] Nginx 配置正确（监听端口、代理地址）
- [ ] 静态资源路径正确
- [ ] 校园前端 history fallback 正确，刷新子路由不 404
- [ ] `/campus/**` 代理到 Spring Boot，不被静态前端吞掉
- [ ] 大屏使用 Nginx 直接代理 `large_screen/dist/`（推荐）或 Tomcat 8080 端口代理

### Docker 部署
> 当前主线建议使用 JAR + Nginx 方式部署；Docker 镜像需在卓然舆情品牌和配置收口后重新打包发布。

- [ ] Docker 镜像版本正确（待重新打包）
- [ ] 端口映射正确
- [ ] 外部配置文件挂载（如果需要）

## 日志检查

- [ ] 日志级别配置正确（`logging.level.com.stonedt.intelligence.dao: info`）
- [ ] 日志文件路径存在且可写
- [ ] 日志轮转配置（[待确认] 当前使用控制台输出为主）

## 权限初始化检查

- [ ] 默认管理员账号是否存在
- [ ] 默认管理员密码是否已修改
- [ ] Token 签名私钥是否已修改（`token.private-key`）
- [ ] Swagger 访问密码是否已修改（`knife4j.basic`）
- [ ] 校园管理员账号已绑定 `campus_admin`
- [ ] 运营账号仅绑定 `campus_operator` 所需菜单/API
- [ ] 查看账号仅绑定 `campus_viewer`，不可访问写接口

## 备份检查

- [ ] 数据库备份策略已配置
- [ ] MySQL dump 可正常导出
- [ ] 配置文件备份（application.properties, application.yml）

## 回滚方案

### 应用回滚
```bash
# 保留上一个版本的 jar
mv stonedt-portal-0.5.3-SNAPSHOT.jar stonedt-portal-0.5.2-SNAPSHOT.jar.bak
# 如果新版本异常，恢复旧版本
mv stonedt-portal-0.5.2-SNAPSHOT.jar.bak stonedt-portal-0.5.2-SNAPSHOT.jar
nohup java -jar stonedt-portal-0.5.2-SNAPSHOT.jar &
```

### 数据库回滚
- Flyway 默认不支持回滚（需手动执行逆向 SQL）
- [ ] 上线前准备回滚 SQL
- [ ] 回滚 SQL 经过测试

## 健康检查

- [ ] 应用启动后访问 http://ip:port/login 正常
- [ ] 用户登录正常（13900000000/stonedt）
- [ ] 监测页面正常展示
- [ ] 分析页面图表正常
- [ ] 数据库连接正常（无 `CommunicationsException`）
- [ ] Redis 连接正常
- [ ] Swagger 文档可访问：http://ip:port/swagger-ui
- [ ] Knife4j 文档可访问：http://ip:port/doc.html（[待确认]）
- [ ] 校园前端可访问并能登录
- [ ] `GET /campus/system/current-user` 返回当前用户、角色和权限
- [ ] `GET /campus/system/menu-tree` 返回菜单树
- [ ] `GET /campus/dashboard/overview` 返回工作台数据
- [ ] `GET /campus/clue/list`、`GET /campus/event/list`、`GET /campus/alert/list` 可分页返回
- [ ] 非授权校园账号访问写接口返回 403
- [ ] 如果开启调度，`/campus/ingest/run/page` 和 `/campus/monitor/task/run-log/list` 能看到运行日志

## 常见故障排查

### 数据库连接失败
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```
- 检查 MySQL 服务是否启动
- 检查 `application.yml` 中 URL、用户名、密码
- 检查是否配置了 `useSSL=false`

### Redis 连接失败
```
redis.clients.jedis.exceptions.JedisConnectionException: Failed connecting to host
```
- 检查 Redis 服务是否启动
- 检查配置的 host 和 port
- 检查密码配置

### Flyway 迁移失败
```
org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
```
- 检查 `V1.0__InitTableAndData.sql` 语法
- 检查 `flyway_baseline_on_migrate` 配置
- 如果已有表且版本冲突，考虑清空 `flyway_schema_history` 表

### 页面白屏/404
- Nginx 反向代理配置是否正确
- 静态资源路径是否正确（`src/main/resources/static/`）
- 大屏项目 `dist/` 是否部署到 Tomcat 或 CDN
- 校园前端是否部署了 `campus-web/dist/`，以及 history fallback 是否指向 `index.html`

### 校园接口返回 403
- 检查用户是否已登录
- 检查 `campus_user_role` 是否绑定有效角色
- 检查 `campus_role_api` 是否授权目标接口
- 检查 `campus_permission_api.request_method/request_path` 是否与实际请求一致

### 校园接入/AI 失败
- 检查 `DEEPSEEK_API_KEY`、`TIKHUB_API_KEY`、`BAIDU_API_KEY` 是否配置
- 检查后台 `/admin/settings/ai` 中供应商是否启用、Base URL 是否正确、`credentialRef` 是否对应真实环境变量
- 检查外部 API 调用日志：`GET /campus/ingest/api-call/list`
- 检查调度是否开启以及任务状态是否为 `active`
- 检查额度/限流错误，必要时先关闭 `SCHEDULE_CAMPUS_INGEST_OPEN`

## 上线前必须确认事项

- [ ] Token 签名私钥已修改（必须！）
- [ ] 数据库连接信息已修改（必须！）
- [ ] Redis 连接信息已修改（必须！）
- [ ] 默认管理员密码已修改（强烈建议）
- [ ] 端口是否被占用
- [ ] 防火墙规则是否正确
- [ ] 如需全文搜索/预警推送功能，确认 ES 和 Kafka 的外部 API 地址已配置（当前指向 `dx2.stonedt.com`）
- [ ] 如需 NLP 分析功能，确认 NLP 服务地址已配置（当前指向 `nlp.stonedt.com`）
- [ ] 如需 AI 写作功能，确认写作宝服务地址已配置（当前指向 `xie.stonedt.com`）
- [ ] 不依赖 RabbitMQ/MongoDB/ClickHouse/DataX（这些属于数据处理和采集模块）
- [ ] 如需校园多语言研判/AI 报告，确认 DeepSeek 配置与额度
- [ ] 如需 TikHub/百度接入，确认接入任务先以 `paused` 创建，人工验收后再切到 `active`
- [ ] 校园模块权限、状态机、API 契约已按本次版本更新
