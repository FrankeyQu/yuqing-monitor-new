# 架构文档 — 卓然舆情

## 项目整体定位

卓然舆情（Zhuoran Insight）是一套面向校园、教育主管部门和属地宣传网信场景的舆情监测、研判、处置与报告平台。系统围绕“公开合规接入、任务化监测、线索化研判、事件化处置、报告化沉淀”建设，当前仓库同时包含存量舆情能力、校园业务后台、校园 Vue 前端和态势大屏。

## 技术栈识别

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.1.4.RELEASE |
| JDK | Java | 1.8 |
| ORM | MyBatis + MyBatis-Spring-Boot-Starter | 1.3.2 |
| 数据库连接池 | Druid | 1.1.10 |
| 数据库 | MySQL | 5.7+ (Connector 8.0.28) |
| 缓存 | Redis + Spring Data Redis | 4.0+ / 2.1.6 |
| 数据库迁移 | Flyway | 随 Spring Boot 2.1.4 |
| 分页 | PageHelper | 1.2.3 |
| JWT | nimbus-jose-jwt | 9.11.1 |
| JSON | FastJSON | 2.0.21 |
| API 文档 | SpringDoc OpenAPI + Knife4j | 1.7.0 / 4.4.0 |
| NLP | HanLP (portable) | 1.8.2 |
| 前端模板 | Thymeleaf | 随 Spring Boot 2.1.4 |
| 前端大屏 | Vue 2 + Element UI + ECharts | Vue 2.6.14 |
| 消息队列 | Kafka | 通过 HTTP API 调用（`dx2.stonedt.com:7189`） |
| 搜索引擎 | Elasticsearch + IK分词 | 通过 HTTP API 调用（`dx2.stonedt.com:7121`） |
| NLP 服务 | HanLP + 外部 NLP | HanLP 本地 + HTTP 调用 `nlp.stonedt.com` |
| 写作宝 | IntellectWrite | 通过 HTTP API 调用 `xie.stonedt.com` |
| 定时任务 | Spring Quartz | 随 Spring Boot |
| AOP | Spring AOP | 随 Spring Boot |
| 工具 | Hutool、Lombok、POI、Jsoup | 见 pom.xml |

## 前端架构

### 主前端（Thymeleaf + Bootstrap）
- 路径：`src/main/resources/templates/`
- 服务端渲染，Bootstrap 页面
- 页面目录结构：
  - `user/` — 登录、注册、忘记密码
  - `monitor/` — 数据监测列表、详情
  - `setting/` — 系统设置（预警、偏好、收藏夹、账号管理、反馈）
  - `projectCenter/` — 方案管理（列表、创建、编辑、详情）
  - `report/` — 报告列表、报告详情
  - `search/` — 全文搜索
  - `displayboard/` — 综合看板
  - `volume/` — 声量监测
  - `lawyer/`、`executionPerson/`、`professor/`、`doctor/` — 全文搜索子模块详情页

### 大屏前端（Vue 2）
- 路径：`large_screen/`
- 独立的 Vue 2 项目
- 路由模式：history，base path 为 `/opinion_screen/`
- 单页面应用，主要页面 `views/home/`
- 组件：舆情态势/来源分布/地图/热门资讯/敏感信息/发布主体互动
- API 调用通过 `/system/*` 和 `/analysis/*` 后端接口

### 校园前端（Vue 3）
- 路径：`campus-web/`
- 独立的 Vue 3 + Vite 项目
- 主要页面：工作台、态势大屏、监测任务（统一监测信息/线索处理入口）、重点账号、预警中心、事件处置、检测任务、媒体接入、AI 能力管理、辅助研判、报告归档、自动报告、系统设置
- API 调用集中在 `campus-web/src/services/`
- 类型定义集中在 `campus-web/src/types/api.ts`
- 本地开发默认通过 Vite proxy 转发到后端 `http://127.0.0.1:8084/`

### Nginx 代理配置
```
listen 8085 → / → proxy_pass 8084 (Spring Boot 主应用)
           → /opinion_screen → 推荐 Nginx 直接代理 large_screen/dist/ 静态文件
                               (Vue history 模式需 try_files fallback)
```

## 后端架构

### 分层结构
```
Controller → Service(Impl) → DAO(Mapper) → MyBatis XML → MySQL
                                     ↘ Redis (缓存/锁)
```

### 包结构
```
com.stonedt.intelligence
├── controller/     — 21 个控制器
├── service/        — 30 个 Service 接口
├── service/impl/   — 30 个 Service 实现
├── dao/            — 30 个 DAO 接口
├── entity/         — 30 个实体类
├── dto/            — 10 个数据传输对象
├── vo/             — 16 个视图对象
├── config/         — 9 个配置类
├── interceptor/    — 2 个拦截器 + WebConfigurer
├── aop/            — 1 个 AOP 日志切面 + 1 个注解定义
├── util/           — 35 个工具类
├── constant/       — 9 个常量类
├── api/            — REST API 控制器
├── nlp/            — NLP 服务封装
├── quartz/         — 13 个定时任务
├── websocket/      — WebSocket 客户端
└── context/        — 线程上下文（存储当前用户）
```

### 核心 Controller 列表

| Controller | 路由前缀 | 功能 |
|-----------|---------|------|
| LoginController | `/` | 登录/登出/Token |
| MonitorController | `/monitor` | 数据监测列表/详情/搜索 |
| AnalysisController | `/analysis` | 监测分析数据/图表 |
| ProjectController | `/project` | 方案管理 CRUD |
| SystemController | `/system` | 系统设置/预警/偏好 |
| FullSearchController | `/fullsearch` | 全文搜索（资讯/律师/招聘等） |
| ReportController | `/report` | 报告管理 |
| VolumeController | `/volume` | 声量监测 |
| UserController | `/user` | 用户管理 |
| WechatController | `/wechat` | 微信集成 |
| DisplayBoardController | `/displayboard` | 综合看板 |
| ApiController | `/api` | REST API（Token/文章列表/详情） |
| SearchController | `/search` | 全文搜索页面（已简化） |
| 其他 | — | Mail/Mobile/HotNews/PopUp/Platform/... |

## 数据库 / ORM / Migration 情况

- **数据库**：MySQL（`stonedt_portal`）
- **ORM**：MyBatis，XML Mapper 在 `src/main/resources/mapper/`（31 个 XML）
- **迁移**：Flyway，`V1.0__InitTableAndData.sql` 包含全部建表语句和初始数据
- **主键**：大部分表使用 Snowflake 算法生成 ID（`SnowflakeUtil`）

### 核心表清单

| 表名 | 用途 |
|------|------|
| `user` | 用户表 |
| `organization` | 机构表 |
| `project` | 方案表（监测项目） |
| `solution_group` | 方案组表 |
| `opinion_condition` | 偏好设置表 |
| `monitor_analysis` | 监测分析数据表（Json 大字段存储分析结果） |
| `project_task` | 方案任务表（分析/声量标志位） |
| `monitor_article` | 监测文章表 |
| `warning_setting` | 预警设置表 |
| `warning_article` | 预警文章表 |
| `campus_ai_provider/model/feature_binding/prompt_template/call_log` | 校园 AI 能力管理配置与调用日志 |
| `article_read` | 已读标记表 |
| `article_status` | 文章状态表 |
| `data_favorite` | 收藏表 |
| `report_custom` | 报告表 |
| `report_detail` | 报告详情表 |
| `publicoptionevent` | 舆情研判任务表 |
| `publicoption_detail` | 舆情研判详情表 |
| `system_log` | 系统日志表 |
| `user_log` | 用户日志表 |
| `full_menu/full_type/full_polymerization` | 全文搜索菜单 |
| `keywords_handler` | 关键词处理表 |
| `wechat_config` | 微信配置表 |
| `volume_monitor` | 声量监测表 |
| `module/module_method` | 模块方法表 |

## API 层结构

### 旧式 API（Thymeleaf Controller）
- 返回 `ModelAndView` 或 `@ResponseBody String`
- 路由分散在各 Controller 中
- JSON 序列化使用 FastJSON 手动转换

### REST API
- 位于 `@RestController @RequestMapping("/api")` 的 ApiController
- 返回 `ResultVO<T>` 统一格式
- 使用 Swagger 注解
- 包含：获取Token、获取文章列表、获取文章详情
- 鉴权方式：请求头携带 token

## 权限 / 认证结构

详见 [PERMISSION_RULES.md](PERMISSION_RULES.md)

## 配置 / 部署结构

- **主配置**：`application.yml`（端口 8084，数据库，Redis，Flyway，MyBatis，日志，Swagger）
- **外部配置**：`config/application.properties`（token 签名、系统 URL、Kafka 地址等）
- **业务配置**：`config/config.properties`（xml 文件路径、产品手册路径）
- **Docker**：旧镜像不再作为卓然舆情交付入口，需在品牌和配置收口后重新打包发布。
- **Nginx**：`nginx_config/nginx.conf`（反向代理配置）

## 目录结构说明

```
config/               — 运行时配置文件（yml + properties + xml）
large_screen/         — Vue 2 大屏前端项目
nginx_config/         — Nginx 配置
ProIMG/               — 文档图片资源
src/main/java/        — Java 后端源码
src/main/resources/   — 前端模板/静态资源/MyBatis Mapper/Flyway
src/test/             — 测试（当前 10 个 Java 测试文件，33 个后端用例可通过）
```

## 核心模块边界

### 监测管理模块（ProjectController + ProjectService）
- 方案组 CRUD、方案 CRUD
- 方案词处理（主体词/人物词/事件词/地域词/屏蔽词）
- 新增方案时同步创建偏好设置和预警配置
- 消息队列（Kafka）+ IK 热词同步

### 数据监测模块（MonitorController + MonitorService）
- 文章列表查询（支持分页/筛选/排序/相似合并）
- 文章详情（包含相似文章、相关文章）
- 文章状态管理（已读/标记）
- App 端文章列表
- 导出功能

### 监测分析模块（AnalysisController + AnalysisService）
- 监测分析概览数据
- 情感/热点/高频词/媒体活跃度/数据来源分析
- 定时分析任务（Quartz）

### 全文搜索模块（FullSearchController + FullSearchService）
- 资讯/律师/被执行人/专家人才/医生/招标/招聘/公告/研报/工商/法律文书/知识产权/投资融资/百度知道/百度学术 等搜索
- 三级分类菜单

### 预警模块（SystemController + EarlyWarningService）
- 预警配置 CRUD
- 预警消息列表
- 预警开关

### 数据报告模块（ReportController + ReportService）
- 报告列表/详情/批量删除/状态修改
- 日报/周报/月报/季度报告

### 大屏模块（DisplayBoardController）
- 综合看板展示

### 微信集成模块（WechatController + WechatService）
- 微信扫码登录/绑定/关注/授权

### NLP 模块（NLPService）
- 封装对外部 NLP 服务的调用

### 智写报告模块（PlatformController + PlatformService）
- 通过 SSE 流对接外部写作宝服务（`xie.stonedt.com`）
- 支持 AI 标题生成和内容生成，Redis 缓存结果
- 支持用户私有密钥和公共账号两种模式

### 校园 Odoo 式模块边界
- manifest 索引：`docs/modules/README.md`
- `campus_ingest`：外部平台/API/百度搜索接入、Jina Reader 正文增强、白名单公开网页读取、标准化记录、互动指标解析。
- `campus_ai`：DeepSeek/TikHub/百度/Jina/历史 AI 能力的供应商、模型、功能绑定、提示词和调用日志配置；不拥有业务数据。
- `campus_monitor`：监测任务、自动接入编排、监测命中、前台展示治理、任务内重点账号/链接、转预警/转线索；自动采集只能调用 `campus_ingest` Service，不直接外呼平台 API。
- `campus_clue`：线索库和报表分析可用数据入口；监测命中只有转线索后才进入该层。
- `campus_account_watch`：重点账号库、账号动态和 DPI/第三方账号推送入口。
- `campus_lexicon`：情感词、负面词、风险词和教育专题词库，当前复用校园字典。
- `campus_education_intel`：本地区教育新闻/政策/招生专题、学校主体和学校声量正负面排名。

## 高风险共享层

1. **interceptor/LoginHandlerInterceptor** — 所有请求鉴权的入口，改动会影响全部 API
2. **util/UserUtil** — 用户工具类，大量 Controller 依赖
3. **util/ProjectUtil** — 方案工具类
4. **context/Context** — 线程上下文（当前用户存储）
5. **vo/ResultVO** — 统一响应结构
6. **constant/** — 常量定义

## 当前架构优点

1. 模块划分清晰，按业务领域命名
2. 大部分 Controller 有统一前缀
3. 数据库设计相对规范（统一字符集 utf8mb4）
4. 使用 Flyway 做迁移管理，易于初始化
5. 提供卓然舆情专用 Docker 镜像和标准化部署脚本
6. Swagger/Knife4j 集成，API 文档可自动生成

## 当前架构风险

1. **分层边界模糊**：大量业务逻辑直接在 Controller 中实现（如 ProjectController.commitproject 有 200+ 行，包含 Kafka 发送、词处理、数据库操作等）
2. **JSON 序列化不统一**：有的返回 ResultVO，有的直接返回 JSONObject.toJSONString()，有的返回 String。前端解析方式不一致
3. **异常处理不完善**：很多 Controller 方法没有统一的异常处理，大量 try-catch 只是 `e.printStackTrace()`
4. **大量重复代码**：多个 Controller 中反复获取 User（userUtil.getuser）、处理方案词逻辑
5. **DTO/Entity 混用**：部分 Entity 直接暴露给前端，部分 DTO 定义在 vo/ 包下，边界模糊
6. **测试覆盖不足**：已有接入模块单测，但核心业务、API、前端测试仍不足；`pom.xml` 默认 `skipTests=true`
7. **权限模型简单**：只有用户状态（正常/禁止/注销）和身份（identity）字段，无基于角色的权限控制
8. **API 路由混乱**：既有 Thymeleaf 页面跳转，又有 @ResponseBody JSON 接口，还有独立 REST API（/api），三种风格共存
9. **JDK 8 过旧**：Java 1.8，无法使用较新的语言特性
10. **FastJSON 安全风险**：FastJSON 2.0.21 存在已知安全漏洞

## 后续架构演进建议

- P0：补充统一异常处理，统一 API 返回格式
- P1：抽取 Service 层业务逻辑，瘦身 Controller
- P1：建立统一鉴权框架（Shiro/Spring Security）
- P2：升级 JDK 版本，重构到 Java 11+
- P2：补充核心模块的单元测试和集成测试
- P2：标准化 JSON 序列化（统一使用 FastJSON 或切换到 Jackson）
- P3：微服务化拆分（将全文搜索、监测分析拆为独立服务）
