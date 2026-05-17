# Batch 26 API 密钥、额度、失败重试、审计实施方案 V0.2

## 1. 背景

Batch22 已经把 TikHub 接入限制为安全适配器：

```text
fetch_config 只保存 provider / endpointKey / query / credentialRef 等配置
真实 Key 只允许从环境变量读取
禁止 Cookie、Token、设备指纹、签名参数等绕过类字段
```

Batch23 已经具备任务调度、执行锁、失败重试状态和运行日志错误分类。Batch24/25 又补齐了标准化、去重和接入后检测联动。

当前还缺少生产试运行前必须有的治理能力：

- 第三方 API 每次调用没有独立审计日志。
- 任务缺少额度上限和当日用量保护。
- 保存任务、运行任务的审计参数仍可能记录完整 `fetchConfig`。
- 连续失败过多时缺少自动暂停保护。
- 前端暂未展示额度和调用治理字段。

## 2. 本批目标

- DB 只保存密钥引用，不保存真实 API Key。
- 对第三方媒体 API 调用建立调用日志，记录供应商、端点、任务、运行、耗时、状态、错误类型和消耗额度。
- 接入任务增加日额度上限、当日已用额度、额度日期、连续失败自动暂停阈值。
- 任务运行前检查当日额度，额度不足时不外呼。
- TikHub 调用成功或失败都写入调用日志，错误摘要必须脱敏。
- 调度连续失败达到阈值时自动暂停任务，避免错误配置造成持续外呼。
- 审计日志写入前对 `fetchConfig`、`credentialRef`、错误信息做统一脱敏。
- 前端最小展示额度、连续失败和调用日志入口，完整配置体验留给 Batch28。

## 3. 本批明确不做

- 不接真实 TikHub Key。
- 不做真实外部高频调用验收。
- 不引入 Vault、KMS、Nacos 等外部密钥系统。
- 不支持账号池、Cookie 池、代理池、签名生成、设备注册、验证码绕过。
- 不新增互动类接口，不做点赞、关注、评论、私信。
- 不做完整供应商管理前端，供应商/端点配置中心留给 Batch28。
- 不改变已有检测联动人工研判边界。

## 4. 主线程技术决策

Batch26 使用保守的单体内治理方案：

```text
campus_ingest_task
  -> 增加 task-level quota / fail guard 字段

CampusIngestServiceImpl.runTaskInternal
  -> 运行前校验额度
  -> 外部 API 调用由 adapter/client 写 api_call_log
  -> 成功后累加 task 当日额度
  -> 失败后根据 consecutive_fail_count + auto_pause_after_fail_count 决定是否暂停

CampusAuditLogServiceImpl
  -> 所有 requestParams/failureReason 入库前统一脱敏
```

理由：

- 本地云电脑无 Docker 和外部依赖，先保证可运行。
- 当前仅有 TikHub 一个真实第三方 adapter，先从最小治理闭环开始。
- 额度先按任务级别计数，后续可扩展到供应商级、学校级或合同级。

## 5. 数据库方案

新增迁移：

```text
src/main/resources/db/migration/V1.17__CampusIngestGovernance.sql
```

### 5.1 `campus_ingest_task` 增强

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `daily_quota_limit` | int | 单任务每日最大外部 API 调用额度，0 或空表示不启用额度限制 |
| `daily_quota_used` | int | 当前额度日期已使用次数 |
| `quota_stat_date` | date | 额度统计日期 |
| `auto_pause_after_fail_count` | int | 连续失败达到该数量后自动暂停，0 表示不自动暂停 |
| `governance_remark` | varchar(1024) | 额度、密钥引用、失败治理说明 |

### 5.2 新增 `campus_ingest_api_call_log`

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `call_id` | bigint | 调用日志业务 ID |
| `run_id` | bigint | 接入运行 ID |
| `task_id` | bigint | 接入任务 ID |
| `source_id` | bigint | 接入来源 ID |
| `provider` | varchar(64) | 供应商，如 tikhub |
| `endpoint_key` | varchar(128) | 端点白名单 key |
| `credential_ref` | varchar(128) | 密钥引用，只允许引用名 |
| `request_time` | datetime | 调用开始时间 |
| `duration_ms` | bigint | 调用耗时 |
| `call_status` | varchar(32) | success / failed |
| `http_status` | int | HTTP 状态码 |
| `error_type` | varchar(64) | 错误分类 |
| `error_message` | varchar(2048) | 脱敏错误摘要 |
| `cost_units` | int | 本次消耗额度，默认 1 |
| `create_time` | datetime | 创建时间 |

索引：

- `task_id, request_time`
- `run_id`
- `provider, endpoint_key, request_time`
- `call_status, request_time`

## 6. 后端设计

### 6.1 API 调用日志

新增：

```text
entity/dao/mapper:
  CampusIngestApiCallLog
  CampusIngestApiCallLogDao
  CampusIngestApiCallLogMapper.xml

service:
  CampusIngestApiCallLogger
```

`TikhubClient.fetch(...)` 在调用前后记录：

- 成功：`call_status=success`，记录 HTTP 状态和耗时。
- HTTP 非 2xx：`call_status=failed`，错误类型为 `http_error`。
- IO/超时：`call_status=failed`，错误类型为 `request_failed`。
- 凭证缺失在外呼前失败，也记录为 `credential_missing`，不记录真实凭证。

### 6.2 额度保护

主线程决策：额度按任务计，单位为“外部请求次数”。

运行前：

- `adapterType != third_party_api` 不扣额度。
- `dailyQuotaLimit` 为空或 0：不限额。
- 日期变化时重置 `daily_quota_used=0`。
- `daily_quota_used >= daily_quota_limit`：运行失败，错误类型 `quota_exceeded`，不外呼。

运行后：

- 每次 TikHub API 调用 `costUnits=1`。
- 调用日志成功或失败都占用外部请求次数，凭证缺失和额度不足不占用。

### 6.3 连续失败保护

保留 Batch23 的失败重试策略，新增自动暂停：

- `auto_pause_after_fail_count` 为空或 0：不自动暂停。
- 调度运行失败后，若连续失败次数达到阈值，将任务状态更新为 `paused`。
- 手动运行失败只记录失败，不自动暂停，避免调试时误停。
- 自动暂停需要写审计或运行日志摘要。

### 6.4 审计脱敏

新增通用脱敏工具：

```text
service/campus/ingest/security/CampusIngestAuditSanitizer.java
```

处理范围：

- `apiKey`、`token`、`authorization`、`cookie`、`session`、`password`、`secret`
- `credentialRef` 只保留引用名，不显示任何值形态的密钥
- `deviceId`、`fingerprint`、`msToken`、`ttwid`、`xBogus`、`aBogus`
- Bearer token、长串疑似密钥

接入点：

- `CampusAuditLogServiceImpl.record(...)` 入库前统一处理 `requestParams` 和 `failureReason`。
- TikHub 错误摘要继续使用 `TikhubSanitizer`，并与通用脱敏工具保持一致。

## 7. 前端最小策略

Batch26 只做最小可见治理信息：

- 任务类型增加：
  - `dailyQuotaLimit`
  - `dailyQuotaUsed`
  - `quotaStatDate`
  - `autoPauseAfterFailCount`
  - `governanceRemark`
- 任务表格展示“额度”和“连续失败”。
- 任务表单增加额度和自动暂停阈值字段。
- 运行日志弹窗继续展示错误分类、重试、节点。

完整供应商、端点、关键词、多平台配置页留给 Batch28。

## 8. 权限、审计、合规约束

- 保存来源、保存任务、运行任务、删除任务、转换记录继续写审计。
- 审计日志不得出现真实 Key、Cookie、Token、Authorization、签名、设备信息。
- 调用日志只能保存 `credential_ref` 引用，不保存凭证值。
- 额度不足时必须阻断外呼。
- 自动暂停只改变任务运行状态，不删除配置和历史记录。
- 任何真实 Key 和真实外部账号启用前必须由用户确认。

## 9. 验收步骤

后端：

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=TikhubResponseMapperTest,CampusIngestGovernanceTest,CampusIngestAuditSanitizerTest" test
.\mvnw.cmd -DskipTests package
```

数据库：

- 启动本地服务，确认 Flyway 迁移到 `V1.17`。
- 检查 `campus_ingest_api_call_log` 存在。
- 检查任务额度字段存在。

功能：

- 无 `TIKHUB_API_KEY` 运行 TikHub 任务：失败、写运行日志、写脱敏调用日志、不外呼敏感信息。
- 设置 `daily_quota_limit=0` 或空：不限制。
- 设置 `daily_quota_limit=1` 且 `daily_quota_used=1`：运行被额度阻断，不调用 TikHub。
- 调度失败达到自动暂停阈值：任务状态变为 `paused`。
- 保存带内联 secret 的任务：仍被拒绝，审计日志中不出现 secret 原文。

前端：

```powershell
cd D:\PRJ\yuqing\campus-web
npm run build
```

## 10. 风险和待打磨点

- 任务级额度无法覆盖供应商合同总额度，后续可新增供应商级汇总。
- TikHub 不同端点真实计费可能不同，本批先用 `costUnits=1`。
- 自动暂停阈值需要学校运维根据试运行频率调整。
- 前端完整配置页留给 Batch28，Batch26 只做治理字段最小入口。

## 11. 子线程摸底结论与主线程 V0.2 决策

主线程采纳：

- 后端只读结果建议调用日志必须在 `TikhubClient` 内部写入，因为这里能拿到 HTTP 状态、耗时和真实外呼结果。
- 后端只读结果建议额度分两层：`runTaskInternal` 在外呼前做 task-level quota precheck，client/logger 在实际外呼后扣减 `costUnits`。
- 后端只读结果建议自动暂停只在 schedule/retry 分支生效，手动运行失败不触发自动暂停。
- 前端只读结果建议不要做完整供应商配置中心，也不新增真实 Key/Cookie/Token 输入框。
- 合规只读结果建议审计统一脱敏必须放到 `CampusAuditLogServiceImpl`，并覆盖转义 JSON 内嵌 `fetchConfig` 的情况。

主线程最终决策：

```text
Batch26 = 环境变量密钥引用 + task-level 日额度 + API 调用审计表
        + schedule/retry 连续失败自动暂停
        + 保存任务阶段拒绝内联密钥/签名/Cookie
        + 审计统一脱敏

不接真实 Key
不做供应商配置中心
不做账号池/代理池/签名/设备绕过
不把额度阻断写成真实 API 调用日志
```

前端方面，主线程比子线程建议多做了一点：在任务表单开放每日 API 额度、自动暂停阈值和治理说明字段。原因是这些字段不是密钥，不涉及真实外部账号，且可以让本地试运行配置不依赖 SQL；供应商、端点、凭据选择器仍保留到 Batch28。

## 12. 实施结果

状态：Done。

已实现：

- 新增 `V1.17__CampusIngestGovernance.sql`。
- `campus_ingest_task` 已支持 `daily_quota_limit`、`daily_quota_used`、`quota_stat_date`、`auto_pause_after_fail_count`、`governance_remark`。
- 新增 `campus_ingest_api_call_log`，记录 provider、endpoint、credentialRef、run/task/source、耗时、HTTP 状态、错误类型、脱敏错误和 costUnits。
- 新增 `CampusIngestApiCallLog`、DAO、Mapper，并在 `/campus/ingest/api-call/list` 提供只读查询。
- 新增 `CampusIngestGovernanceService`，支持额度重置、额度阻断和实际调用后扣减。
- `TikhubClient` 已接收 `CampusIngestFetchRequest` 上下文，在凭证缺失、HTTP 失败、IO 失败、成功时写调用日志。
- `CampusIngestServiceImpl` 在外呼前做额度检查，保存任务时拒绝内联密钥、Cookie、Token、设备指纹、签名参数。
- 调度失败达到 `auto_pause_after_fail_count` 时自动把任务置为 `paused`；手动运行失败不自动暂停。
- 新增 `CampusIngestAuditSanitizer` 并接入 `CampusAuditLogServiceImpl`，统一脱敏 `requestParams` 和 `failureReason`。
- 前端 `IngestView` 已展示日额度、连续失败，并可配置每日 API 额度、自动暂停阈值和治理说明。
- 前端类型和服务已补充 `CampusIngestApiCallLog` 和调用日志查询封装。

验证：

- `.\mvnw.cmd -DskipTests compile` 通过。
- `.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=CampusIngestGovernanceServiceTest,CampusIngestAuditSanitizerTest,TikhubResponseMapperTest,CampusIngestRecordNormalizerTest,CampusIngestDedupResultTest,CampusIngestDetectionLinkageServiceTest" test` 通过，16 个用例成功。
- `.\mvnw.cmd -DskipTests package` 通过。
- `campus-web` `npm run build` 通过。
- 本地服务启动成功，Flyway 迁移到 `V1.17`。
- 数据库确认 `daily_quota_limit`、`auto_pause_after_fail_count` 和 `campus_ingest_api_call_log` 存在。
- 无 `TIKHUB_API_KEY` 运行 TikHub 验证任务，返回 `TikHub credential is not configured`，运行日志为 `credential_missing`，调用日志写入 `credential_ref=TIKHUB_API_KEY`、`cost_units=0`。
- 设置 `daily_quota_limit=1` 且 `daily_quota_used=1` 后运行任务，返回 `quota_exceeded`，不新增 API 调用日志。
- 保存带内联 `apiKey/signature` 的任务被拒绝，审计日志中对应值为 `[REDACTED]`。

遗留风险：

- 当前额度仍是任务级，不是供应商合同级；Batch28 或试运行后可扩展供应商级额度汇总。
- 自动暂停只写任务状态和运行日志，未额外生成系统通知。
- 前端调用日志查询已封装，暂未做独立调用日志页面，留给 Batch28 管理页集中设计。
