# Batch 24 数据标准化与去重实施方案 V0.2

## 1. 背景

Batch21 已建立媒体接入执行框架，Batch22 已完成 TikHub 安全适配，Batch23 已完成本地调度和运行日志增强。当前系统已经能把适配器返回的 `CampusIngestItem` 写入 `campus_ingest_record`，并通过 `source_id + external_id`、`source_id + content_hash` 做最小幂等。

现状问题：

- 标准化逻辑分散在 `CampusIngestServiceImpl.buildRecord` 和各适配器 mapper 中。
- 内容哈希只基于原始链接、标题、正文、发布时间，缺少稳定文本归一化。
- 去重命中只返回 `false`，运行日志只能看到 `successCount` 偏少，看不到“重复跳过”原因。
- `raw_data` 有留存，但缺少统一脱敏、截断和标准化状态说明。
- 检测引擎已经读取 `campus_ingest_record`，Batch24 必须保持检测读取兼容，为 Batch25 自动检测联动打基础。

## 2. 本批目标

- 抽出统一的接入记录标准化组件，集中处理字段清洗、长度限制、风险默认值、平台/内容类型兜底。
- 抽出内容哈希策略，使用稳定归一化文本计算 SHA-256。
- 增强去重结果表达，区分 `inserted`、`duplicate_external_id`、`duplicate_content_hash`、`invalid`。
- 保持 `campus_ingest_record` 原有字段和接口兼容。
- 增加最小数据质量字段，便于后续排查和前端展示。
- 增加单元测试覆盖标准化、脱敏、哈希和去重判定。

## 3. 本批明确不做

- 不接真实外部 API。
- 不新增爬虫能力。
- 不做检测任务自动触发，留给 Batch25。
- 不改变 `campus_ingest_record` 主业务字段含义。
- 不重写 TikHub endpoint mapper 的字段路径。
- 不做跨来源全局去重，避免不同授权来源之间互相污染。
- 不清理历史数据，不改旧迁移。

## 4. 主线程技术决策

Batch24 采用“轻量标准化服务 + 兼容性字段增强”的方案：

```text
CampusIngestItem
  -> CampusIngestRecordNormalizer
  -> CampusIngestNormalizedRecord
  -> CampusIngestDedupService / DAO
  -> campus_ingest_record
```

原因：

- 不把标准化继续塞进 `CampusIngestServiceImpl`，避免后续多平台接入时膨胀。
- 不先做独立宽表或 ES 索引，本批仍以 MySQL 标准记录为主。
- 不引入新依赖，继续使用 Java 8、Fastjson、Apache Commons Lang3。
- 先做源内去重，跨平台相似内容聚合后续可以进入事件聚合或辅助研判。

## 5. 数据库方案

新增迁移：

```text
src/main/resources/db/migration/V1.15__CampusIngestNormalizationDedup.sql
```

### 5.1 `campus_ingest_record` 保持兼容

Batch24 不新增 `campus_ingest_record` 字段。

原因：

- 已有唯一键 `uk_campus_ingest_record_external(source_id, external_id)` 和 `uk_campus_ingest_record_hash(source_id, content_hash)` 保留。
- 重复记录不入库，给 record 表加 `dedup_status` 没有稳定对象可承载。
- `raw_data_hash` 和 `normalize_version` 更偏排查字段，先留给 Batch28 详情页或后续治理增强。
- 不改 record 表可降低 Flyway 和 Mapper 扩散风险。

### 5.2 `campus_ingest_run_log` 增强

新增字段：

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `duplicate_count` | int | 本次运行去重跳过数量 |
| `invalid_count` | int | 本次运行标准化无效数量 |

前端 Batch24 只做类型兼容，完整展示留给 Batch28。

## 6. 后端设计

新增包：

```text
src/main/java/com/stonedt/intelligence/service/campus/ingest/normalize/
```

新增类：

- `CampusIngestRecordNormalizer`
- `CampusIngestDedupResult`
- `CampusIngestDedupStatus`
- `CampusIngestHashUtil`
- `CampusIngestRawDataSanitizer`

核心接口：

```java
CampusIngestRecord normalize(Long runId,
                             CampusIngestTask task,
                             CampusIngestSource source,
                             CampusIngestItem item,
                             Long operatorUserId);
```

标准化规则：

- `platform`：优先 item，其次 source，统一 trim 和小写。
- `contentType`：空值按 `article` 兜底；TikHub 抖音视频仍为 `video`。
- `title`：trim，最长 512；空标题从正文前 80 字生成。
- `content`：trim，保留换行，最长按数据库 mediumtext 自然承载，不做过短截断。
- `originalUrl`：trim，最长 1024。
- `authorName`：trim，最长 255，不做账号归因推断。
- `keywords`：trim，最长 512。
- `riskLevel`：空值默认 `normal`。
- `rawData`：统一脱敏后留存，避免密钥、Cookie、token、session、设备指纹进入库。
- `contentHash`：优先适配器显式传入；否则用标准化后的 `platform|contentType|externalId|originalUrl|title|content|publishTime` 计算。

无效记录：

- 标题、正文、原始链接、外部 ID 全为空时视为 invalid。
- invalid 不入库，计入运行日志 `invalid_count`。

## 7. 去重策略

查询顺序：

1. 如果 `externalId` 非空，先按 `source_id + external_id` 查重。
2. 如果 `contentHash` 非空，再按 `source_id + content_hash` 查重。
3. 如果都未命中，尝试 insert。
4. insert 发生唯一键冲突时再次查重并计入 duplicate。

去重状态：

| 状态 | 含义 |
| --- | --- |
| `inserted` | 新记录入库 |
| `duplicate_external_id` | 外部 ID 已存在 |
| `duplicate_content_hash` | 内容哈希已存在 |
| `invalid` | 标准化后无有效内容 |
| `failed` | 入库或标准化异常失败 |

运行日志：

- `fetched_count`：适配器返回总数。
- `success_count`：实际入库数量。
- `duplicate_count`：查重跳过数量。
- `invalid_count`：标准化无效数量。
- `fail_count`：异常失败数量。

## 8. Mapper 和 Service 调整

`CampusIngestRecordDao` 新增：

- `selectDuplicateByExternalId(sourceId, externalId)`
- `selectDuplicateByContentHash(sourceId, contentHash)`

保留：

- `selectDuplicate(sourceId, externalId, contentHash)` 兼容旧调用，或内部改为委托。

`CampusIngestRunLogDao.finish` 增加：

- `duplicateCount`
- `invalidCount`

`CampusIngestServiceImpl` 调整：

- 删除内置 `buildRecord` 和 `buildContentHash` 的主要职责，改用 normalizer。
- `insertAdapterRecord` 改为返回 `CampusIngestDedupResult`。
- 运行统计新增 duplicate / invalid。
- 仍捕获 `DuplicateKeyException`，避免并发下重复写入报错。

## 9. 前端最小策略

Batch24 只做运行日志字段兼容和最小展示：

- `CampusIngestRunLog` 类型补充 `duplicateCount`、`invalidCount`。
- `IngestView` 运行日志弹窗在“失败”后展示“重复”“无效”两列。
- 不在接入记录表展示 hash、版本、重复源记录 ID，完整展示留给 Batch28。

## 10. 权限、审计和合规

- 标准化不得推断学生真实身份。
- 标准化不得从正文中提取手机号、身份证、私密联系方式形成结构化画像。
- `rawData` 必须脱敏：token、apiKey、cookie、session、password、authorization、deviceId、fingerprint、xBogus、aBogus 等字段值必须替换。
- 去重只在同 `source_id` 下生效，避免不同授权来源之间的信息合并造成越权。
- 自动任务产生的标准化记录仍使用系统用户 ID `0`。

## 11. 验收步骤

后端：

- `.\mvnw.cmd -DskipTests compile`
- 新增单元测试覆盖 normalizer 和去重结果。
- `.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=CampusIngestRecordNormalizerTest" test`
- `.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=CampusIngestDedupResultTest" test`
- `.\mvnw.cmd -DskipTests package`

数据库：

- 启动本地后端确认 Flyway `V1.15` 迁移成功。
- 检查 `campus_ingest_run_log` 新字段存在。

接口：

- 创建或复用 `manual_push` 验证任务，适配器空返回时不报错。
- 用单测验证重复 externalId、重复 contentHash 不重复入库。
- 无 Key TikHub 任务仍保持 `credential_missing`，不外呼。

前端：

- 如修改类型，执行 `campus-web npm run build`。

## 12. 风险和待打磨点

- `CampusIngestServiceImpl` 当前承担职责较多，抽 normalizer 时要保持小步修改，避免破坏 Batch21-23。
- MySQL 对 `NULL` 唯一键允许多条，本批必须在 Service 层继续防护空 externalId / contentHash。
- 运行日志新增字段需要兼容旧记录默认值。
- 检测引擎读取 `campus_ingest_record` 的字段不能改名，Batch25 才做自动检测触发。

## 13. 子线程摸底任务

主线程将分派三个只读摸底：

- Franklin：后端标准化/去重落点和 Mapper 风险。
- Pasteur：前端类型和 Batch28 展示影响。
- Arendt：合规、脱敏、审计和检测联动边界。

## 14. 子线程摸底结论与主线程 V0.2 决策

主线程采纳：

- 后端只读结果建议最小化迁移面：record 表不新增字段，run log 增加 `duplicate_count` 和 `invalid_count`。
- 前端只读结果建议只补 `CampusIngestRunLog` 类型，并在运行日志弹窗展示“重复 / 无效”。
- 合规只读结果要求 rawData 入库前走统一脱敏，字段覆盖 token、apiKey、authorization、cookie、session、password、deviceId、fingerprint、msToken、ttwid、xBogus、aBogus 等。

主线程最终决策：

```text
Batch24 = 内部标准化组件 + 源内去重诊断 + run log duplicate/invalid 统计
不新增 record 表诊断字段
不提前触发检测任务
不接真实外部 API
```

## 15. 实施结果

状态：Done。

已实现：

- 新增 `V1.15__CampusIngestNormalizationDedup.sql`，给 `campus_ingest_run_log` 增加 `duplicate_count` 和 `invalid_count`。
- 新增 `service/campus/ingest/normalize/` 包：
  - `CampusIngestRecordNormalizer`
  - `CampusIngestRawDataSanitizer`
  - `CampusIngestHashUtil`
  - `CampusIngestDedupResult`
  - `CampusIngestDedupStatus`
- `CampusIngestServiceImpl` 的适配器入库链路已改为统一标准化、统一 rawData 脱敏、源内 externalId/contentHash 去重。
- 运行日志已统计 `fetchedCount`、`successCount`、`duplicateCount`、`invalidCount`、`failCount`。
- 前端 `CampusIngestRunLog` 类型已补充 `duplicateCount`、`invalidCount`，运行日志弹窗已展示“重复”“无效”。
- 新增 `CampusIngestRecordNormalizerTest` 和 `CampusIngestDedupResultTest`。

验证：

- `.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=TikhubResponseMapperTest,CampusIngestRecordNormalizerTest,CampusIngestDedupResultTest" test` 通过，7 个用例成功。
- `.\mvnw.cmd -DskipTests package` 通过。
- `campus-web` `npm run build` 通过。
- 本地服务启动成功，Flyway 从 `V1.14` 迁移到 `V1.15`。
- 数据库已确认 `campus_ingest_run_log.duplicate_count` 和 `campus_ingest_run_log.invalid_count` 存在。

遗留风险：

- Batch24 只做源内去重和运行级诊断，不做跨平台相似内容聚合。
- Batch24 不自动触发检测任务，接入后自动检测联动进入 Batch25。
- 完整前端归一化详情、hash 查看和去重筛选留给 Batch28。
