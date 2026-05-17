# 校园舆情系统 Codex 开发拆分清单

本清单由主线程依据实施方案拆分，后续开发按批次推进。每批次完成后必须检查 `git diff`、运行可执行测试，并给出手工验收步骤。

## 当前进度

- Batch 0：已完成代码整改，编译待 JDK 环境恢复后验证。
- Batch 1：已完成基础表迁移。
- Batch 2：已完成组织机构、字典、增强审计后端骨架。
- Batch 3：已完成线索库后端闭环。
- Batch 4：已完成重点关注账号库后端闭环。
- Batch 5：已完成事件与处置流转后端闭环。
- Batch 6：已完成预警规则、敏感词、预警记录、基础驾驶舱统计后端。
- Batch 7：已完成报告模板、报告归档、事件报告关联、Markdown/HTML/Text 下载接口。
- Batch 8：已完成公开/授权数据来源、接入任务、标准化记录、运行日志、线索/账号动态转换后端。
- Batch 9：已完成辅助研判任务、结果、复核采纳状态和本地启发式分析后端。
- Batch 10：已完成自动报告任务、生成日志、手动触发生成并归档到报告库。
- Batch 11：已完成校园检测引擎后端，支持检测主题、检测规则、检测任务、检测命中、运行日志和自动转预警。
- Batch 12：已完成 `campus-web` 前端地基，支持校园登录页、主布局、路由守卫、接口封装和工作台。
- Batch 13：已完成工作台快捷入口、部门管理、数据字典、审计日志前端页面。
- Batch 14：已完成线索库、重点账号登记审核、账号公开动态前端页面。
- Batch 15：已完成预警中心、事件处置、分派任务、处置反馈前端页面。
- Batch 16：已完成检测任务与数据接入前端页面，支持检测主题/规则/任务/命中、接入来源/任务/记录、运行日志和记录转换。
- Batch 17：已完成辅助研判、报告归档、自动报告前端页面，支持分析复核、报告生成下载和自动报告运行日志。
- Batch 18：已完成校园权限模型，支持角色、菜单、接口权限、菜单树、权限拦截和权限管理前端。
- Batch 19：已完成校园态势大屏，使用真实业务统计、预警、检测命中、事件接口并支持自动刷新。
- Batch 20：已完成联调验收收口，包含演示数据脚本、验收操作手册、残余风险和二开建议。
- Batch 21：已完成媒体接入平台基础执行框架，支持适配器 SPI、手动运行、运行日志、最小幂等和前端运行入口。
- Batch 22：已完成 TikHub 安全适配器，支持 endpoint allowlist、环境变量密钥引用、抖音视频搜索 V2 标准化 mapper、脱敏和无密钥失败日志验证。
- Batch 23：已完成接入任务调度与运行日志增强，支持本地调度扫描器、DB 执行锁、失败重试状态、错误分类、耗时和调度节点记录。
- Batch 24：已完成数据标准化与去重，支持统一 normalizer、rawData 脱敏、源内去重诊断、duplicate/invalid 运行日志统计和前端最小展示。
- Batch 25：已完成接入后自动检测联动，支持显式绑定检测任务、run_id 精确扫描、检测运行来源和接入运行检测汇总。
- Batch 26：已完成 API 密钥、额度、失败重试、审计，支持环境变量密钥引用、API 调用日志、任务级日额度、额度阻断、调度失败自动暂停和审计脱敏。
- Batch 27：已完成白名单公开网页采集器预留，支持公开网页白名单 CRUD、`public_web_pull` 占位适配器、URL 安全校验、非白名单阻断和审计。
- Batch 28：已完成多平台监测配置和前端管理页，支持媒体接入中心总览、结构化任务配置、公开网页白名单、全局运行日志和 API 调用日志。
- Batch 29：已完成试运行前配置治理与学校初始化，支持默认配置整改、权限最小化迁移、旧任务默认关闭、交付文档清理和学校初始化模板。
- 整体校验：XML 与空白静态校验通过；Java 8 Maven 构建通过；Flyway `V1.19` 本地迁移成功；`campus-web` 构建通过；浏览器验证通过；Batch22/24/25/26/27/28 单测通过；Batch29 严格模式负向检查通过。
- 下一阶段：Batch 21-29 已收口，进入学校真实信息确认、试运行账号初始化和按钮级权限优化。
- 主线程控制台：`docs/codex-main-thread-control-board.md`，用于持续监控 Batch 11-28。
- 从 Batch 21 起，每批先形成独立方案文档，方案通过后再进入实现。

## Batch 0：安全整改与开发地基

目标：先把后续开发依赖的基础风险处理掉。

任务：

- 修复 API Token 判空逻辑。
- 收紧 token Cookie 安全属性。
- 收紧产品手册 PDF 访问方式，避免 `/pdf/**` 暴露本机文件系统。
- MyBatis Mapper 支持子目录，方便新增 `mapper/campus`。
- 梳理默认账号、默认密钥、外部服务依赖，形成后续整改清单。

验收：

- 未登录 API 请求返回明确 JSON 错误。
- 登录 token Cookie 带 HttpOnly 和 SameSite。
- 产品手册只能通过受控接口访问。
- 后续新增 `mapper/campus/*.xml` 能被扫描。

## Batch 1：基础表结构

目标：新增校园业务基础数据结构。

任务：

- 新增 Flyway 脚本 `V1.1__CampusBaseTables.sql`。
- 建立组织机构、字典、审计增强表。
- 保持旧表和旧迁移不动。

核心表：

- `campus_department`
- `campus_dict_type`
- `campus_dict_item`
- `campus_audit_log`

## Batch 2：基础后端能力

目标：实现组织、字典、审计基础接口。

任务：

- 新增 `campus` 包下 Entity/Dao/Service/Controller。
- 组织机构 CRUD。
- 字典 CRUD。
- 审计日志写入工具。

## Batch 3：线索库

目标：实现线索从录入到研判归档的最小闭环。

任务：

- 新增 `V1.2__CampusClueTables.sql`。
- 线索新增、列表、详情、研判、归档。
- Excel 导入接口预留。
- 关键操作接入审计。

核心表：

- `campus_clue`
- `campus_clue_attachment`
- `campus_clue_operation_log`

## Batch 4：重点关注账号库

目标：实现合规边界清晰的重点关注账号管理。

任务：

- 新增 `V1.3__CampusAccountTables.sql`。
- 账号登记、审核、关注期限、任务编号、来源依据。
- 账号动态记录。
- 账号与线索/事件关联。
- 到期提醒预留。

核心表：

- `campus_account`
- `campus_account_task`
- `campus_account_content`
- `campus_account_relation`

## Batch 5：舆情事件与处置闭环

目标：实现线索转事件、分派处置、反馈归档。

任务：

- 新增 `V1.4__CampusEventWorkflowTables.sql`。
- 线索转事件。
- 多线索合并事件。
- 风险定级。
- 部门分派。
- 处置反馈。
- 复核归档。

核心表：

- `campus_event`
- `campus_event_clue`
- `campus_event_account`
- `campus_disposal_task`
- `campus_disposal_record`

## Batch 6：预警规则和统计分析

目标：先实现规则预警和基础统计，不依赖模型。

任务：

- 新增敏感词、预警规则、预警记录表。
- 线索、账号动态、事件触发规则预警。
- 首页工作台统计接口。
- 大屏数据接口改造。

核心表：

- `campus_sensitive_word`
- `campus_alert_rule`
- `campus_alert`

## Batch 7：报告归档

目标：实现日报、周报、专报和事件归档报告。

任务：

- 新增报告元数据表。
- Java 模板报告初版。
- 报告生成、下载、归档、审计。
- 预留 Python 报告服务接口。

## Batch 8：数据接入服务

目标：参考 BettaFish MindSpider，但只做公开/授权/上级移交数据接入。

任务：

- 主系统先落接入来源、任务、标准化记录、运行日志骨架；后续可拆独立 `campus-ingest-service`。
- 采集任务模型。
- 数据标准化。
- 写入线索库或账号动态库。
- 失败重试和接入日志。

## Batch 9：智能分析服务

目标：参考 InsightEngine 和 SentimentAnalysisModel，提供辅助研判。

任务：

- 情感倾向。
- 风险分类建议。
- 相似线索聚合。
- 事件摘要。
- 所有 AI 结果标记为辅助建议。

## Batch 10：自动报告服务

目标：参考 BettaFish ReportEngine，增强报告质量。

任务：

- 主系统先落自动报告任务和生成日志；后续可拆独立 `campus-report-service`。
- 报告模板库。
- HTML/PDF/Markdown 输出。
- Java 主系统审核归档。

## Batch 11：校园检测引擎

目标：补齐自动发现问题能力，让系统从“手动录入/手动评估”升级为“自动检测命中 + 人工研判处置”。

任务：

- 新增检测主题、检测规则、检测任务、检测命中、检测运行日志表。
- 检测 `campus_ingest_record`、`campus_clue`、`campus_account_content`。
- 支持关键词、排除词、风险等级、平台范围、数据来源范围。
- 命中后自动生成 `campus_alert`，必要时支持转线索。
- 检测运行接入审计日志。

## Batch 12：校园版新前端地基

目标：新建 `campus-web`，不再深改旧 Thymeleaf 页面。

任务：

- 初始化 Vue 3 + Vite + TypeScript + Element Plus。
- 登录、布局、菜单、路由、接口封装。
- 工作台基础页。
- 字典缓存和统一错误处理。

## Batch 13-20：新前端业务页面、权限、大屏与验收

详见 `docs/campus-frontend-and-detection-implementation-plan.md`。

## Batch 21：媒体接入平台基础框架

状态：Done。

目标：建立可扩展的媒体接入中心，不写死 TikHub，也不把系统绑定为爬虫平台。V0.2 决策为复用现有 `campus_ingest_source/task/record/run_log`，先做适配器 SPI、任务手动运行、最小幂等、运行日志和审计闭环；完整供应商/端点模型后续再抽象。

方案文档：`docs/batch21-media-ingest-platform-plan.md`。

完成内容：

- 新增 `CampusIngestAdapter` SPI、请求/响应/标准化记录 DTO 和适配器注册表。
- 新增 `manual_push` 空执行适配器和 `third_party_api` 合规占位适配器。
- 新增 `POST /campus/ingest/task/run`。
- 新增 `V1.13__CampusIngestExecutionEnhancement.sql`，补充 `run_id`、`content_hash` 和第三方媒体 API 字典项。
- 前端数据接入页面运行按钮已接入完整任务运行闭环，并补充第三方媒体 API 选项。

## Batch 22：TikHub 适配器

状态：Done。

目标：在 Batch 21 的统一接入框架上接入 TikHub，优先支持搜索、热榜、详情、评论等公开内容接口。

方案文档：`docs/batch22-tikhub-adapter-plan.md`。

完成内容：

- 改造 `ThirdPartyApiIngestAdapter`，接入 TikHub provider 分支。
- 新增 TikHub endpoint allowlist，只实现 `douyin_search_video_v2`。
- 新增 `TikhubFetchConfig`、`TikhubClient`、`TikhubResponseMapper`、`TikhubSanitizer` 等安全适配类。
- 真实 Key 仅从 `TIKHUB_API_KEY` 环境变量读取，`fetch_config` 只能保存 `credentialRef`。
- 禁止内联密钥、Cookie、session、设备指纹、签名等绕过类字段。
- 新增 mapper 单测，验证样例响应标准化和密钥拦截。
- 本地接口验证无 Key 时失败日志正常写入，且不外呼。

## Batch 23：接入任务调度与运行日志

状态：Done。

目标：让启用中的接入任务可自动运行，并沉淀运行日志、失败原因和统计指标。

方案文档：`docs/batch23-ingest-scheduling-runlog-plan.md`。

完成内容：

- 新增 `V1.14__CampusIngestSchedulingEnhancement.sql`，补充调度、执行锁、失败重试和运行日志诊断字段。
- 新增本地调度扫描器和调度策略类，调度器默认关闭，通过环境变量显式开启。
- 手动运行和自动调度共用执行锁与运行内核，避免同一任务并发执行。
- 运行日志已记录触发来源、耗时、错误分类、重试次数和调度节点。
- 前端数据接入页已最小展示调度字段和增强运行日志字段。

## Batch 24：数据标准化与去重

状态：Done。

目标：把不同平台的返回结构标准化为统一内容记录，支持平台外部 ID、内容哈希和原始 JSON 留存。

方案文档：`docs/batch24-ingest-normalization-dedup-plan.md`。

完成内容：

- 新增 `V1.15__CampusIngestNormalizationDedup.sql`。
- 新增统一标准化、rawData 脱敏、哈希和去重结果类。
- 接入任务运行时统计实际入库、重复跳过、标准化无效和失败数量。
- 前端运行日志弹窗展示重复和无效数量。

## Batch 25：接入后自动检测联动

状态：Done。

目标：新接入记录入库后自动触发检测任务，命中后进入检测命中和预警中心。

方案文档：`docs/batch25-ingest-detection-linkage-plan.md`。

完成内容：

- 新增 `V1.16__CampusIngestDetectionLinkage.sql`。
- 接入任务可配置 `auto_detect_enabled` 和显式检测任务 ID 列表。
- 检测服务支持 `runIngestRecordTask`，只扫描指定接入运行产生的记录。
- 接入运行日志汇总检测触发、命中、预警和错误摘要。
- 前端运行日志弹窗展示检测联动计数。

## Batch 26：API 密钥、额度、失败重试、审计

状态：Done。

目标：补齐供应商密钥安全存储、额度统计、失败重试、调用审计和敏感操作留痕。

完成内容：

- 新增 `V1.17__CampusIngestGovernance.sql`。
- 新增 `campus_ingest_api_call_log` 和后端查询接口。
- 接入任务支持每日 API 额度、当日已用额度、额度日期、自动暂停阈值和治理说明。
- TikHub 调用链已写入调用日志；凭证缺失不扣额度，真实外呼后扣 `costUnits=1`。
- 保存任务时拒绝内联密钥、Cookie、Token、设备指纹和签名参数。
- 审计日志写入前统一脱敏，覆盖转义 JSON 内嵌 `fetchConfig`。
- 前端接入任务页已展示日额度、连续失败并可配置治理字段。

## Batch 27：白名单公开网页采集器预留

状态：Done。

目标：预留后期自研公开网页采集器接口，只允许白名单公开页面，不做绕登录、绕验证码、绕反爬等能力。

完成内容：

- 新增 `V1.18__CampusPublicWebWhitelist.sql` 和 `campus_public_web_whitelist`。
- 新增公开网页白名单后端 CRUD 与审计。
- 新增 `PublicWebFetchConfig`、`PublicWebWhitelistValidator` 和 `PublicWebIngestAdapter`。
- `public_web_pull` 只做白名单和 URL 校验，返回空结果，不发起真实网络请求。
- URL 校验阻断 userinfo、localhost、IP 字面量、路径穿越、URL/query 中的 token/cookie/signature 等风险。
- 任务保存阶段校验 `public_web_pull` 的 `fetchConfig`，只允许 `whitelistId/url/mode`。
- 前端补充公开网页白名单类型、service 封装和适配器文案。

## Batch 28：多平台监测配置和前端管理页

状态：Done。

目标：前端统一管理媒体接入来源、监测任务、公开网页白名单、接入记录、运行日志和 API 调用日志，并把任务配置从手写 JSON 改为结构化安全配置。

完成内容：

- 新增全局运行日志分页接口 `GET /campus/ingest/run/page`。
- `/ingest` 重构为“媒体接入中心 / 多平台监测配置”。
- 首屏新增接入来源、启用任务、公开网页白名单、最近失败运行、近期 API 调用和 API cost 总览。
- 新增公开网页白名单管理 Tab，支持列表、筛选、新增、编辑、启停、删除。
- 新增全局运行日志 Tab，支持按任务、状态、触发方式、错误类型筛选。
- 新增 API 调用日志 Tab，展示 provider、endpoint、credentialRef、调用状态、HTTP 状态、costUnits、耗时和错误类型。
- 接入任务表单改为结构化配置和只读 JSON 预览。
- TikHub 配置只允许 `credentialRef=TIKHUB_API_KEY`，不提供真实密钥输入。
- 公开网页配置固定 `mode=metadata_only`，当前仍不执行真实网页抓取。
- `api_pull`、`rss_pull`、`file_import` 作为预留禁用选项，不提供灰色采集配置。

验证：

- 后端关键单测 22 个用例通过。
- 后端 `.\mvnw.cmd -DskipTests package` 通过，本地服务重启成功。
- 登录后 `/campus/ingest/run/page` 返回 200。
- 前端 `npm run build` 通过。
- 浏览器验证 `/ingest` 页面、TikHub 结构化配置、公开网页 `metadata_only` 配置和只读 JSON 预览可用。

## Batch 29：试运行前配置治理与学校初始化

状态：Done。

目标：对本地授权开发的校园舆情系统做试运行前上线准备，仅处理本地代码和配置，不做外部扫描、不接真实数据源。

完成内容：

- 登录页移除默认账号、密码和验证码预填。
- 旧定时任务默认关闭，历史外部服务地址改为环境变量，旧实时检索桥接默认关闭。
- Swagger/Knife4j 默认关闭，演示 Basic 账号密码清空。
- 新增 `PrelaunchReadinessValidator`，用于试运行前严格配置校验。
- 新增 `V1.19__CampusPrelaunchGovernance.sql`，停用旧业务通配接口权限，补处置员和查看员最小权限。
- 新增学校 root、常用部门、组织类型、平台、账号类型、敏感词分类字典模板。
- 更新试运行验收手册、前端运行手册、残余风险文档，新增试运行前检查清单和校园版 README。

验证：

- 后端 `.\mvnw.cmd -DskipTests package` 通过。
- 前端 `npm run build` 通过。
- 本地服务启动成功，Flyway 迁移到 `V1.19`。
- 数据库抽查确认权限收紧和学校初始化模板落库。
- `prelaunch.strict=1` 负向检查会阻断演示 token、`root` 数据库账号和本地默认数据库密码。

## 主线程推进规则

- 每次只推进一个 Batch 或一个 Batch 内的一个子模块。
- 子线程如参与，只能领取明确读写范围的局部任务。
- 主线程统一审查子线程结果。
- 涉及权限、审计、合规字段时不得跳过。
- 每批结束后更新本清单状态或补充风险记录。
