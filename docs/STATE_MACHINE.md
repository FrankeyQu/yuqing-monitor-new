# 状态机文档 — 卓然舆情

> 当前项目未定义清晰的状态机体系。本文档基于数据库中已有的 status/flag/del_status 字段梳理候选状态。

## 用户状态（user.status）

| 状态值 | 含义 | 说明 |
|--------|------|------|
| 0 | 禁止登录 | 账号被禁用 |
| 1 | 正常 | 可正常登录使用 |
| 2 | 注销 | 账号已注销 |

**状态流转**：
```
注册 → 1(正常) ↔ 0(禁用)
1(正常) → 2(注销) [不可逆]
```

**触发途径**：
- 注册/创建用户 → status=1
- [待确认] 管理员手动禁用/启用
- 账号到期（term_of_validity）→ 前端校验，不影响 status 值

**权限要求**：[待确认] 用户状态管理当前未找到独立管理页面

**是否审计**：否（当前系统日志记录操作但不记录状态变更）

## 方案状态（project.del_status）

| 状态值 | 含义 |
|--------|------|
| 0 | 正常（未删除） |
| 1 | 软删除 |

**状态流转**：
```
0(正常) → 1(删除) [不可逆]
```

**触发 API**：
- `POST /project/delProject` — 删除单方案
- `POST /project/delProjectDetail` — 详情页删除
- `POST /project/batchUpdateProject` — 批量删除

## 方案类型（project.project_type）

| 状态值 | 含义 |
|--------|------|
| 1 | 普通方案 |
| 2 | 高级方案 |

**说明**：创建时确定，修改时可变更。普通方案的人物词/事件词/地域词为空，高级方案所有字段可用。

## 分析任务状态（project_task.analysis_flag / volume_flag）

| 状态值 | 含义 |
|--------|------|
| 0 | 未处理/待处理 |
| 1 | [待确认] 已处理/处理中 |

**说明**：schedule 定时任务检测 flag=0 的任务，执行分析后更新。分析结果写入 monitor_analysis 表。

**触发流程**：
1. 用户新建/修改方案 → project_task 记录创建/更新
2. Quartz 定时任务（AnalysisQuartz 等）扫描 flag=0 的记录
3. 执行分析 → 更新 flag=1

## 报告状态（report_custom.report_status）

| 状态值 | 含义 |
|--------|------|
| 0 | 已生成任务 |
| 1 | 正在编制 |
| 2 | 编制成功 |
| 3 | 编制失败 |

**状态流转**（推测）：
```
0(已生成任务) → 1(正在编制) → 2(编制成功)
                                    ↘ 3(编制失败) [可重试]
```

**触发途径**：
- 用户请求生成报告 → 创建 status=0 的记录
- Quartz 定时任务（ReportDataSchedule 等）处理编制
- 编制完成 → status=2 或 3

**权限要求**：用户只能查看自己 user_id 对应的报告 [待确认]

## 预警状态（warning_setting.warning_status）

| 状态值 | 含义 |
|--------|------|
| 0 | 关闭 |
| 1 | 开启 |

**状态流转**：
```
0(关闭) ↔ 1(开启)
```

**触发 API**：
- `POST /system/updateWarningStatusById` — 切换开关

**前置条件**：
- 开启前必须设置预警词（否则校验失败）

## 预警文章状态（warning_article.[待确认]）

[待确认] 预警文章表示用户已读/未读预警消息。可能通过 article_read 表或 warning_article 表的状态字段控制。

## 文章已读状态（article_read）

| 字段 | 含义 |
|------|------|
| article_id | 文章ID |
| user_id | 用户ID |
| type | [待确认] |

**触发 API**：
- `POST /monitor/edit/read` — 切换已读/未读

## 舆情研判任务状态（publicoptionevent.status）

| 状态值 | 含义 |
|--------|------|
| 1 | 创建失败 |
| 2 | 正在创建 |
| 3 | 创建成功 |

**状态流转**（推测）：
```
2(正在创建) → 3(创建成功)
→ 1(创建失败) [可重试]
```

**说明**：可能涉及 AI/LLM 生成研判分析，后端异步处理。

## 收藏状态（data_favorite.status）

| 状态值 | 含义 |
|--------|------|
| 1 | 正常（已收藏） |
| 2 | 删除（取消收藏） |

**状态流转**：
```
1(已收藏) → 2(取消收藏)
2 → 1 [可能支持重新收藏]
```

## 文章标记状态（article_status + monitor/article_status）

| type 值 | 含义 |
|---------|------|
| 1 | 设为失效 |
| 2 | 设为未失效 |

**触发 API**：`POST /monitor/edit/status`

## 校园线索状态（campus_clue.clue_status）

| 状态值 | 含义 | 来源 |
|--------|------|------|
| `pending_judge` | 待研判 | `CampusClueServiceImpl` 默认值、`V1.2__CampusClueTables.sql` |
| `judged` | 已研判 | `POST /campus/clue/judge`、自动研判写回 |
| `converted` | 已转事件 | `CampusEventServiceImpl.createFromClue()` 调用 `campusClueDao.markConverted` |
| `archived` | 已归档 | `POST /campus/clue/archive` |

**允许流转**：
```
新建/接入转线索 → pending_judge
pending_judge → judged
judged → converted
pending_judge / judged / converted → archived
```

**触发 API / Service**：
- `POST /campus/clue/save`：创建或保存线索
- `POST /campus/clue/judge`：人工研判
- `CampusIngestServiceImpl.convertRecordToClue(...)`：接入记录转线索
- `CampusEventServiceImpl.createFromClue(...)`：线索转事件并标记 `converted`
- `POST /campus/clue/archive`：归档

**权限要求**：登录 + `campus_permission_api` 对应接口权限；写操作会记录 `campus_audit_log`。

**禁止流转**：
- 已归档线索不应再回到 `pending_judge` 或 `judged`，除非后续明确设计“反归档”接口。
- 已转事件线索不得再次研判或重复转事件；如需结束存档，只允许从 `converted` 进入 `archived`。
- 删除为 `deleted=1` 软删除，不应与业务状态混用。

## 校园监测结果状态与风险标记（campus_monitor_result）

| 字段/状态 | 含义 | 说明 |
|-----------|------|------|
| `result_status=pending` | 待处理 | 关键词命中后的默认处理状态 |
| `result_status=alerted` | 已生成预警 | 人工或规则生成过 `campus_alert` 预警单 |
| `result_status=ignored` | 已取消预警 | 人工确认取消预警；风险等级恢复 `normal`，风险分归 `0` |
| `result_status=converted` | 已转线索 | 已沉淀到 `campus_clue` |
| `riskMarked=true` | 风险标记 | 由负面词、非普通风险等级、已预警等条件推导，不是独立状态 |

**允许流转**：
```
关键词命中 → pending
pending → alerted / ignored / converted
alerted → ignored / converted
```

**统一口径**：
- 新监测命中只以 `keywords` 为条件；`monitor_subject/subject_aliases` 只作任务展示和历史兼容。
- `negative_words` 只生成 `riskMarked` 与风险展示，不决定普通命中是否进入列表。
- `hitScope=all` 展示全部关键词命中；`hitScope=risk` 只展示 `riskMarked=true`。
- 情感值统一为 `positive/neutral/negative/none`；“疑似/确认”由研判状态表达，不再作为情感状态。
- UI 噪声和历史错误接入记录采用 `deleted=1` 软隐藏，不做物理删除。
- 疑似误预警治理只允许把仍为待处理预警、无负面词/负面情感、原始风险普通、来源于历史 `all_hits` 口径的监测结果从 `alerted` 批量转为 `ignored`；默认跳过已关联线索的记录，不删除原始监测信息。

## 校园事件状态（campus_event.event_status）

| 状态值 | 含义 | 来源 |
|--------|------|------|
| `pending_judge` | 待研判/待定级 | `CampusEventServiceImpl` 默认值 |
| `rated` | 已定级 | `CampusEventMapper.rate` |
| `assigned` | 已分派 | `CampusEventServiceImpl.assign` |
| `processing` | 处理中/已记录线下处置 | `CampusEventServiceImpl.returnTask` 或 `recordOfflineDisposal` |
| `feedback` | 已反馈（多人派单兼容状态） | `CampusEventServiceImpl.feedback` |
| `reviewed` | 已复核/已处置（多人派单兼容状态） | `CampusEventServiceImpl.confirm` |
| `archived` | 已归档 | `CampusEventMapper.archive` |

**允许流转**：
```
pending_judge → rated → assigned → feedback → reviewed → archived
rated → assigned        # 可追加分派
assigned → processing → feedback
feedback/reviewed → processing   # 退回重办

# 单用户台账模式
pending_judge/rated/assigned/feedback/reviewed → processing   # 记录线下处置
任意非 archived 状态 → archived                             # 填写归档结论后归档
```

**触发 API / Service**：
- `POST /campus/event/save`：创建/保存，默认 `pending_judge`
- `POST /campus/event/clue/add`：将已有线索加入已有事件，线索进入 `converted` 并写入事件关系
- `POST /campus/event/rate`：风险定级，进入 `rated`
- `POST /campus/event/assign`：分派处置，进入 `assigned`
- `POST /campus/event/feedback`：处置反馈，进入 `feedback`
- `POST /campus/event/return`：退回重办，进入 `processing`
- `POST /campus/event/confirm`：复核确认，进入 `reviewed`
- `POST /campus/event/record/add`：单用户模式记录线下处置，服务层生成本地处置记录并进入 `processing`
- `POST /campus/event/archive`：归档，进入 `archived`

**权限要求**：登录 + 对应校园 API 权限；上述写操作均有审计记录。

**禁止流转**：
- `archived` 不应再回到处置中状态。
- 事件保存、关联账号和处置流转均不得继续操作 `archived` 事件。
- `rate` 仅允许 `pending_judge/rated`；`assign` 仅允许 `rated/assigned/processing`；`feedback` 仅允许 `assigned/processing` 事件且任务为 `pending/returned`；`confirm` 仅允许 `feedback` 事件且任务为 `completed`。
- 单用户模式下 `record/add` 和 `archive` 仅禁止 `archived` 事件继续操作；`archive` 必须填写归档结论。
- 分派处置未提交 `dueTime` 时，Service 按事件风险等级生成默认 SLA：`urgent` 30 分钟、`major` 2 小时、`concern` 8 小时、`normal` 24 小时。

## 校园处置任务状态（campus_disposal_task.task_status）

| 状态值 | 含义 |
|--------|------|
| `pending` | 待处置 |
| `completed` | 已反馈/已完成 |
| `returned` | 已退回 |
| `confirmed` | 已确认 |
| `processing` | 数据库注释中存在，当前 Service 未直接写入，待确认 |

**允许流转**：
```
pending → completed → confirmed
completed → returned → completed
```

**触发 API**：
- `POST /campus/event/assign`：创建 `pending`
- `POST /campus/event/feedback`：进入 `completed`
- `POST /campus/event/return`：进入 `returned`
- `POST /campus/event/confirm`：进入 `confirmed`

**禁止流转**：
- `feedback` 只能处理 `pending/returned` 任务。
- `return` 和 `confirm` 只能处理已反馈的 `completed` 任务。

## 校园预警状态（campus_alert.alert_status）

| 状态值 | 含义 |
|--------|------|
| `pending` | 待处理 |
| `handled` | 已处理 |
| `ignored` | 已取消/已忽略 |

**允许流转**：
```
新建/规则命中/转预警 → pending
pending → handled
pending → ignored
```

**监测信息取消预警口径**：`POST /campus/monitor/result/ignore` 保留历史路径名，但业务含义为“取消预警”。该操作将监测结果置为 `result_status=ignored`、清空结果侧 `alert_id`、恢复 `risk_level=normal/risk_score=0`；若存在关联预警，则同步把 `campus_alert.alert_status` 置为 `ignored`。`POST /campus/monitor/result/alert-cleanup/execute` 是受控批量治理入口，后端重新按候选条件筛选，不接受前端直接提交 ID 列表。

**触发 API / Service**：
- `POST /campus/alert/create`
- `POST /campus/alert/evaluate-clue`
- `POST /campus/alert/evaluate-account-content`
- `POST /campus/monitor/result/alert`
- `POST /campus/monitor/result/alert-cleanup/execute`
- `POST /campus/detection/hit/alert`
- `POST /campus/alert/handle`、`POST /campus/monitor/alert/handle`

**禁止流转**：`handled` 与 `ignored` 是否允许互转当前未明确；默认视为终态，除非后续补充“重新打开”设计。

## 校园接入状态（campus_ingest_*）

### 接入任务（campus_ingest_task.task_status）

| 状态值 | 含义 |
|--------|------|
| `active` | 启用，可手动或调度运行 |
| `paused` | 暂停，默认值 |
| `disabled` | 禁用 |

**允许流转**：
```
paused ↔ active
active / paused → disabled
disabled → paused / active [待确认是否应允许]
```

**触发 API**：`POST /campus/ingest/task/save`、`POST /campus/ingest/task/update-status`。

### 接入记录（campus_ingest_record.normalized_status）

| 状态值 | 含义 |
|--------|------|
| `pending` | 待标准化/待转换 |
| `converted` | 已转换为线索或账号动态 |
| `ignored` | 已忽略，数据库预留，当前 Service 未直接写入 |
| `failed` | 转换失败 |

**允许流转**：
```
pending → converted
pending → failed
pending → ignored [待确认]
failed → converted [可通过重试实现，当前未形成专门重试 API]
```

**触发 API / Service**：
- `POST /campus/ingest/record/submit`
- `POST /campus/ingest/record/convert-clue`
- `POST /campus/ingest/record/convert-account-content`
- `CampusIngestServiceImpl.runTask(...)` 自动拉取和转换

### 接入运行日志（campus_ingest_run_log.run_status）

| 状态值 | 含义 |
|--------|------|
| `running` | 运行中 |
| `success` | 成功 |
| `partial_success` | 部分成功：本次运行既有成功入库，也有失败记录 |
| `failed` | 失败 |

**允许流转**：
```
running → success
running → partial_success
running → failed
```

**触发 API / Service**：`POST /campus/ingest/run/start`、`POST /campus/ingest/run/finish`、`POST /campus/ingest/task/run`、调度任务。未显式指定 `runStatus` 时，服务层会按 `successCount/failCount` 推导：有成功且有失败为 `partial_success`，只有失败为 `failed`，否则为 `success`。

## 校园监测状态（campus_monitor_*）

### 监测任务（campus_monitor_task.task_status）

| 状态值 | 含义 |
|--------|------|
| `active` | 启用 |
| `paused` | 暂停 |
| `disabled` | 禁用 |

**允许流转**：同接入任务。

**触发 API**：`POST /campus/monitor/task/save`、`POST /campus/monitor/task/update-status`。

### 监测任务展示状态（campus_monitor_task.display_enabled）

| 状态值 | 含义 |
|--------|------|
| `1` | 前台“监测信息”展示该任务产生的监测命中 |
| `0` | 前台隐藏该任务产生的监测命中，历史数据保留 |

**允许流转**：
```
1 ↔ 0
1 / 0 → deleted=1 且 display_enabled=0
```

**触发 API / Service**：
- `POST /campus/monitor/task/save`：默认 `display_enabled=1`
- `POST /campus/monitor/task/update-display`：显式展示/隐藏
- `POST /campus/monitor/task/delete`：软删除任务，同时隐藏前台数据、停用调度、清理任务与接入任务绑定关系

**展示规则**：`/campus/monitor/information/**` 和监测概览统计只展示 `task_status=active` 且 `display_enabled=1`、`deleted=0` 的监测结果；暂停/禁用任务停止调度并隐藏其历史命中。已经转入线索库的数据是否继续用于报表，仍以 `campus_clue` 状态为准。

### 监测任务自动接入状态（campus_monitor_task.auto_ingest_enabled / ingest_capability_status）

| 字段 | 状态值 | 含义 |
|------|--------|------|
| `auto_ingest_enabled` | `1` | 监测任务保存/运行时自动维护并触发接入任务 |
| `auto_ingest_enabled` | `0` | 关闭自动接入，仅保留高级/兼容手动绑定能力 |
| `ingest_capability_status` | `ready` | 所选平台已创建/复用接入任务且最近调用无错误 |
| `ingest_capability_status` | `partial` | 部分平台可用，部分平台未接入或调用失败 |
| `ingest_capability_status` | `unsupported` | 所选平台暂无自动接入适配器 |
| `ingest_capability_status` | `failed` | 最近自动接入调用失败 |
| `ingest_capability_status` | `pending` | 已配置但尚未运行 |

**触发 Service**：`CampusMonitorServiceImpl.saveTask(...)` 维护自动接入来源/任务；`CampusMonitorServiceImpl.runTask(...)` 先调用 `CampusIngestService.runTask(...)`，再扫描绑定的 `campus_ingest_record`。

**边界规则**：监测模块只编排 `campus_ingest` Service，不直接外呼平台 API，不直接写接入记录。

### 监测结果（campus_monitor_result.result_status）

| 状态值 | 含义 |
|--------|------|
| `pending` | 待研判 |
| `alerted` | 已转预警 |
| `ignored` | 已取消预警 |
| `handled` | 对应预警已处理 |
| `converted` | 已转入线索库 |

**允许流转**：
```
pending → alerted
pending → ignored
pending → converted
alerted → handled
alerted → ignored
alerted → converted
```

**触发 API / Service**：
- `POST /campus/monitor/result/alert`
- `POST /campus/monitor/result/ignore`（历史路径名，业务语义为取消预警）
- `POST /campus/monitor/result/convert-clue`
- `POST /campus/monitor/alert/handle`
- `CampusMonitorServiceImpl.runTask(...)`：扫描接入记录并按任务规则自动生成 `pending` 或 `alerted`

**说明**：监测结果是“命中条目”，不是报表分析的最终数据；只有转入 `campus_clue` 后才进入线索库和后续报表分析链路。取消预警不是删除命中，而是把该命中从风险处理队列中退出，并同步回落风险等级。

**结构化字段**：Batch41 起监测结果和线索保存 `schoolRelevanceScore/schoolRelevanceReason/matchedSchoolTerms/topicCategory/topicSubCategory/topicReason`，用于学校相关性解释、主题归类、预警依据和报表治理统计。

**自动预警规则**：`alert_mode=negative_only` 仅负面/风险命中自动预警；`alert_mode=all_hits` 保留旧字段名，但当前业务语义为“风险命中告警”，普通主题词命中先进入 `pending`，只有命中负面词/风险词、风险等级非 `normal` 或风险分达到阈值时才进入 `alerted`；`alert_mode=manual` 不自动预警。

**禁止流转**：
- 主题词、监测主体词、普通关键词不能单独作为负面词触发 `alerted`。
- `alerted` 必须能解释预警依据：负面/风险词、非普通风险等级、高风险分或人工转预警。

**去重规则**：如果监测结果关联的 `campus_ingest_record` 已经绑定 `target_type=clue,target_id=线索ID`，`convert-clue` 必须复用该线索并把监测结果标记为 `converted`；新建线索成功后也必须回写接入记录为 `normalized_status=converted,target_type=clue,target_id=线索ID`。

### 任务内重点目标（campus_monitor_watch_target.target_status）

| 状态值 | 含义 |
|--------|------|
| `active` | 启用，参与本任务扫描过滤 |
| `paused` | 暂停，暂不参与扫描 |

**允许流转**：
```
active ↔ paused
active / paused → deleted=1
```

**触发 API / Service**：
- `POST /campus/monitor/watch-target/save`
- `POST /campus/monitor/watch-target/create-from-result`
- `POST /campus/monitor/watch-target/delete`

**权限要求**：登录 + `campus:monitor:operate`；从监测结果一键加入时必须记录 `source_object_type/source_object_id` 和 `authorization_scope`。

### 监测运行日志（campus_monitor_run_log.run_status）

| 状态值 | 含义 |
|--------|------|
| `running` | 运行中 |
| `success` | 成功 |
| `failed` | 失败 |

**允许流转**：`running → success/failed`。

## 教育专题状态（campus_school_subject）

### 学校主体（campus_school_subject.status）

| 状态值 | 含义 |
|--------|------|
| `1` | 启用，参与学校声量/正负面排名 |
| `0` | 停用，不参与排名 |

**允许流转**：
```
1 ↔ 0
1 / 0 → deleted=1
```

**触发 API / Service**：
- `POST /campus/education/school/save`
- `POST /campus/education/school/delete`

**说明**：教育专题列表本身无独立状态机，基于 `campus_clue` 已入库线索和 `campus_dict_item` 专题词库动态筛选。

**导入规则**：学校 CSV 导入按 `schoolId` 优先、`schoolName` 兜底去重；已存在则更新，空名称跳过，解析失败计入 `failed`。

### 教育百度接入任务状态

教育专题不直接外呼百度接口，只创建或运行 `campus_ingest_task`：
- `POST /campus/education/baidu-task/create`：创建 `paused` 接入任务，供接入模块后续管理。
- `POST /campus/education/baidu-task/create-and-run`：创建 `active` 临时任务并调用接入模块运行，运行结束后回到 `paused`，实际运行状态记录在 `campus_ingest_run_log.run_status`。

## 校园检测状态（campus_detection_*）

### 检测任务（campus_detection_task.task_status）

| 状态值 | 含义 |
|--------|------|
| `active` | 启用 |
| `paused` | 暂停，默认值 |
| `disabled` | 禁用 |

**允许流转**：同接入任务。

### 检测命中（campus_detection_hit.hit_status）

| 状态值 | 含义 |
|--------|------|
| `pending` | 待研判 |
| `alerted` | 已转预警 |
| `ignored` | 已忽略 |
| `converted` | 已转线索，数据库预留，当前 Service 未直接写入 |

**允许流转**：
```
pending → alerted
pending → ignored
pending → converted [待确认]
```

**触发 API / Service**：
- `POST /campus/detection/hit/alert`
- `POST /campus/detection/hit/ignore`
- `CampusDetectionServiceImpl.runTask(...)`

### 检测运行日志（campus_detection_run_log.run_status）

| 状态值 | 含义 |
|--------|------|
| `running` | 运行中 |
| `success` | 成功 |
| `failed` | 失败 |

**允许流转**：`running → success/failed`。

## 校园重点账号状态（campus_account / campus_account_task）

### 账号审核（campus_account.audit_status）

| 状态值 | 含义 |
|--------|------|
| `pending` | 待审核 |
| `approved` | 审核通过 |
| `rejected` | 审核拒绝 |

### 账号状态（campus_account.account_status）

| 状态值 | 含义 |
|--------|------|
| `pending` | 待审核/待生效 |
| `active` | 启用 |
| `expired` | 已过期，数据库预留 |
| `cancelled` | 已取消，数据库预留 |
| `rejected` | 已拒绝 |

**允许流转**：
```
pending → active       # auditStatus=approved
pending → rejected     # auditStatus=rejected
active ↔ expired / cancelled [待确认，当前 update-status 可直接写入请求值]
```

**触发 API**：`POST /campus/account/save`、`POST /campus/account/audit`、`POST /campus/account/update-status`。

### 账号任务（campus_account_task.task_status）

| 状态值 | 含义 |
|--------|------|
| `active` | 启用，当前 Service 默认值 |
| `paused` | 暂停，数据库预留 |
| `completed` | 已完成，数据库预留 |
| `cancelled` | 已取消，数据库预留 |

## 校园辅助研判状态（campus_analysis_*）

当前 `/campus/analysis/**` 为规则辅助研判能力，默认模型来源为 `local_heuristic`；结果必须人工复核后才能作为处置参考。

| 字段 | 状态值 | 含义 |
|------|--------|------|
| `campus_analysis_task.task_status` | `pending` | 待运行 |
| `campus_analysis_task.task_status` | `running` | 运行中 |
| `campus_analysis_task.task_status` | `completed` | 已完成 |
| `campus_analysis_task.task_status` | `failed` | 失败 |
| `campus_analysis_result.adoption_status` | `pending` | 待复核 |
| `campus_analysis_result.adoption_status` | `adopted` | 已采纳 |
| `campus_analysis_result.adoption_status` | `rejected` | 已驳回 |

**允许流转**：
```
analysis_task: pending → running → completed
analysis_task: running → failed
analysis_result: pending → adopted / rejected
```

**触发 API**：`POST /campus/analysis/task/create`、`POST /campus/analysis/task/run`、`POST /campus/analysis/result/review`。

## 校园报告状态（campus_report / campus_report_job / campus_report_generation_log）

| 字段 | 状态值 | 含义 |
|------|--------|------|
| `campus_report.report_status` | `draft` | 草稿 |
| `campus_report.report_status` | `generated` | 已生成 |
| `campus_report.report_status` | `archived` | 已归档 |
| `campus_report.report_status` | `published` | 已发布，数据库预留 |
| `campus_report_job.job_status` | `active` | 启用 |
| `campus_report_job.job_status` | `paused` | 暂停，默认值 |
| `campus_report_job.job_status` | `disabled` | 禁用 |
| `campus_report_generation_log.run_status` | `running/success/failed` | 运行中/成功/失败 |
| `campus_report.generation_mode` | `template/ai` | 传统规则/AI 生成 |
| `campus_report_job.generation_mode` | `template/ai` | 自动报告任务生成方式 |

**允许流转**：
```
report: draft → generated → archived
report: draft/generated → generated  # 传统生成或 AI 生成会写入最新 reportContent
report: generated → published [待确认]
job: paused ↔ active; active/paused → disabled
generation_log: running → success/failed
```

**调度规则**：`job_status=active` 且 `next_run_time <= now` 的自动报告任务可被调度器扫描；执行前写入 `schedule_lock_until`，成功或失败后释放锁并刷新下一次运行时间。手动运行同样使用锁避免重复生成。

**AI 失败规则**：AI 调用失败时生成日志进入 `failed`，报告保持草稿或原状态，不允许把失败说明 markdown 写入正式 `report_content` 并标记为 `generated`。AI 生成前会先生成规则统计快照，`ai_user_prompt` 仅作为输出侧补充要求，不改变状态流转。

## 校园权限与基础数据状态

| 字段 | 状态值 | 含义 |
|------|--------|------|
| `campus_permission_role.status` | `1/0` | 启用/停用 |
| `campus_permission_menu.status` | `1/0` | 启用/停用 |
| `campus_permission_api.status` | `1/0` | 启用/停用 |
| `campus_department.status` | `1/0` | 启用/停用 |
| `campus_dict_type.status` | `1/0` | 启用/停用 |
| `campus_dict_item.status` | `1/0` | 启用/停用 |
| `campus_sensitive_word.status` | `1/0` | 启用/停用 |
| `campus_report_template.status` | `1/0` | 启用/停用 |

**说明**：这些状态多为开关型字段，不构成复杂状态机；删除通常使用 `deleted=1` 软删除。

## 校园预警依据

`campus_alert.evidence_json` 不是状态字段，但属于预警状态流转的解释性证据。新建预警时应记录来源对象、规则、风险等级、命中词、学校相关性、主题分类、风险分和原文链接等快照；后续处理/取消预警只改变 `alert_status`，不得覆盖历史依据。

## 校园风险等级口径

风险等级不是状态机，但会影响线索研判、预警生成、事件定级和报告统计口径。Batch40 起统一使用：

| 代码值 | 展示名称 | 说明 |
|------|--------|------|
| `normal` | 普通关注 | 与学校相关但暂不需要预警响应 |
| `concern` | 一般预警 | 存在负面或风险信号，需要研判关注 |
| `major` | 重大预警 | 涉及重点风险主题、传播扩散或需要部门响应 |
| `urgent` | 特别重大 | 涉及人身安全、群体性、重大舆论扩散或应急事件 |

**兼容规则**：服务端写入时兼容历史中文值“普通/关注/重大/紧急”和旧编码 `higher`，统一归一到 `normal/concern/major/urgent`；前端筛选和标签展示必须使用同一套编码。

## 校园 AI 能力状态（campus_ai_*）

| 字段 | 状态值 | 含义 |
|------|--------|------|
| `campus_ai_provider.enabled` | `1/0` | 供应商启用/停用 |
| `campus_ai_model.enabled` | `1/0` | 模型启用/停用 |
| `campus_ai_feature_binding.enabled` | `1/0` | 功能绑定启用/停用 |
| `campus_ai_prompt_template.enabled` | `1/0` | 提示词模板启用/停用 |
| `campus_ai_call_log.call_status` | `success/failed` | 单次 AI 调用成功/失败 |

**允许流转**：
```
enabled: 1 ↔ 0
call_log: 请求完成后直接写入 success 或 failed，不做二次流转
```

**边界规则**：AI 能力开关只影响外部 AI/智能接入调用，不直接改变线索、监测结果、报告等业务状态。词云、正文增强等功能停用或失败时必须按各模块失败策略回退，不能把失败状态写成业务成功。

## 禁止的状态流转汇总

- 用户不允许直接从 0(禁止) → 2(注销)
- 已删除的方案不可恢复（del_status=1 不可逆）[待确认]
- [待确认] 文章失效/有效之间是否可逆
- 校园线索/事件/报告/任务的 `archived`、`disabled`、`deleted=1` 默认视为不可继续业务流转，除非新增恢复接口并补充审计
- 校园运行日志 `running` 不应被手动改回，终态只能是 `success` 或 `failed`
- 校园预警、监测结果、检测命中的终态互转规则尚未完整固化，新增互转能力前必须补状态前置校验

## 异常状态处理建议

1. 方案创建失败时（analysis_flag 无法变更），应提供重试机制
2. 报告编制长时间停滞在 status=1，应设置超时自动标记为失败
3. 用户账号到期（term_of_validity）应有主动提醒机制
4. 目前缺乏状态变更的统一审计日志

## 后续梳理建议

1. 在 Service 层统一管理状态变更逻辑，不在 Controller 中直接写 status 值
2. 状态变更提供统一的日志记录（当前只有 @SystemControllerLog 记录操作，不记录具体状态变化）
3. 建议对关键状态（用户、方案、报告、预警）建立状态枚举类
4. 复杂状态流转建议使用设计模式（状态模式）
