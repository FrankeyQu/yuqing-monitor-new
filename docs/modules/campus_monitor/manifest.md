# campus_monitor Manifest

## 模块定位

监测任务模块负责“任务配置 → 自动编排合法接入任务 → 授权接入记录扫描 → 监测命中结果 → 告警/忽略/转线索”的闭环。它不直接外呼外部平台 API，也不直接写接入记录；需要采集时只能通过 `campus_ingest` Service 创建/复用/运行接入任务，外部数据必须先进入 `campus_ingest_record`。

前端“监测信息”工作台归属本模块，只展示已命中监测任务的 `campus_monitor_result`；普通线索、搜索页沉淀内容和手工新增线索留在线索/研判/搜索相关入口处理，避免再次污染监测任务口径。

后台“监测任务管理”页归属本模块，只负责监测任务配置、自动采集开关、前台展示开关、调度参数、运行状态、接入能力诊断以及任务内重点账号/链接维护；普通表单不再要求人工选择接入任务。前台 `/monitor` 继续保持监测信息工作台定位。

## 依赖模块

- `campus_ingest`：通过 Service 创建/复用/运行合法接入任务，并读取对应接入记录。
- `campus_ai`：首页词云热词提取、监测任务 AI 体检、监测命中 AI 分析和后续摘要增强读取功能绑定、模型和失败策略；不可用时必须按本模块原有统计或规则回退。
- `campus_clue`：将有价值的监测命中转入线索库。
- `campus_account_watch`：任务内重点账号/链接约束和一键加入重点账号。
- `campus_lexicon`：读取负面词、风险词等统一词库。

## 数据模型

- `campus_monitor_task`
- `campus_monitor_result`
- `campus_monitor_run_log`
- `campus_monitor_ingest_task_relation`
- `campus_monitor_watch_target`
- `CampusMonitorInformation`（只读 DTO，不新增表；合并 `campus_monitor_result` 与未被命中结果引用的 `campus_clue`）

`CampusMonitorInformation` 的平台、发布时间、采集时间、作者、正文完整性和互动指标以监测结果为主，并可从关联 `campus_ingest_record` 回填更完整的正文、原文链接和互动数。统一列表必须排除普通线索、搜索沉淀内容和手工新增线索；新命中只以 `keywords` 为条件，`monitor_subject/subject_aliases` 只保留展示与历史兼容语义。

`campus_monitor_result` 记录学校相关性和主题分类字段：`school_relevance_score/school_relevance_reason/matched_school_terms/excluded_reason/topic_category/topic_sub_category/topic_reason`。这些字段用于解释为什么一条公开内容与学校相关、归入哪个校园事件主题，并在转线索、转预警和报表治理指标中继续传递。

`campus_monitor_result` AI 辅助字段：`ai_summary/ai_hit_recommendation/ai_hit_reason/ai_confidence/ai_analysis_time/ai_provider_code/ai_model_code`。这些字段只记录人工触发 AI 分析后的辅助结论；`ai_hit_recommendation=not_hit` 不等于自动忽略，仍需人工操作结果状态。

`campus_monitor_task` 新增治理字段：`display_enabled` 控制 active 任务数据是否进入前台监测信息；`auto_ingest_enabled` 控制是否自动维护接入任务；`last_collect_time/last_match_count/display_result_count/last_error_message/ingest_capability_status` 用于后台任务列表可观测。`task_status=paused/disabled` 的任务不再进入前台监测信息和平台统计。

## API 契约

- `/campus/monitor/task/**`
- `/campus/monitor/information/**`
- `/campus/monitor/result/**`
- `/campus/monitor/alert/**`
- `/campus/monitor/watch-target/**`

## 前端入口

- `/monitor`：监测信息工作台。
- `/admin/monitor-tasks`：后台监测任务管理页。

## 权限

- 菜单：`campus:monitor:view`
- 只读：`campus:monitor:read`
- 操作：`campus:monitor:operate`

## 测试影响

- 任务保存必须校验密钥/Token/Cookie/签名字段不可进入任务配置。
- 多语言关键词必须兼容旧字段 `keywords` / `negative_words`。
- 监测信息统一列表必须能在默认“本年 + 发布时间全部”条件下返回本年度数据，并展示固定平台标签：全部、抖音、小红书、知乎、新闻/网页、微博、微信公众号、B站、快手；“全部”标签不拼接年份或时间范围；无数据平台应区分“未接入 / 未启用 / 0 条”。
- 监测信息 API 默认 `hitScope=all`，展示全部关键词命中；切换 `hitScope=risk` 时只展示 `riskMarked=true` 的风险标记命中。
- 前端 `/monitor` 菜单入口默认使用 `hitScope=all` 展示完整监测记录；允许通过 `?hitScope=risk` 直接进入风险命中口径；切换口径时应重置平台、情感、状态、关键词和时间等窄筛选，避免把预览或历史筛选误认为全量记录。
- 公开论坛不再作为独立固定平台入口；历史论坛、贴吧、豆瓣数据按新闻/网页口径兼容统计，`count-by-sub-platform` 仅作为历史子来源接口保留。
- 监测信息必须展示 `contentCaptureStatus/contentCaptureLabel`，区分完整正文、摘要/标题、详情失败/摘要和未采集；转线索和告警内容应优先使用详情增强后的正文。
- 监测命中必须满足有效关键词；主体词/别名不能作为新命中条件，负面/风险词只生成风险标记。
- 监测信息必须返回 `collectTime/publishTimeStatus/riskMarked`；发布时间未知内容显示“发布时间未知”，默认排序按明确发布时间优先、未知发布时间按采集时间倒序。
- 监测信息情感值统一为 `positive/neutral/negative/none`，前后端兼容历史“疑似/确认”中文值但不得继续写入。
- 监测信息页支持人工修改单条或批量监测命中情感；写入必须走 `/campus/monitor/result/sentiment`，已转线索时同步 `campus_clue.sentiment`，已归档线索关联的监测命中不得修改情感。
- 后台 `/admin/monitor-tasks` 支持手动 AI 体检，接口 `/campus/monitor/task/ai-diagnose` 只返回任务配置建议，不写回任务、不展示具体采集内容。
- 监测信息页支持单条、选中批量和当前页 AI 分析，接口 `/campus/monitor/result/ai-analyze` 最多一次处理 20 条；AI 可更新情感、一句话摘要、学校相关性、主题分类和辅助风险等级，但不得自动转预警、自动忽略、自动删除或改变结果状态机。
- 风险等级口径：普通关键词只判断监测命中归属，不直接推高 `risk_level`；只有负面词/风险词、原始非普通风险、负面情感，或 AI 分析明确确认 `riskLevel=concern` 时，监测结果才进入“一般预警”风险等级。
- AI 判断“不建议命中”必须只作为 `ai_hit_recommendation` 和理由展示，后续是否忽略由学校线下/人工判断后手动操作。
- 已转未归档线索在监测命中 AI 分析成功后同步情感、学校相关性和主题字段；已归档线索关联的监测命中必须跳过写入并返回明确反馈。
- `similarDedup=true` 必须按 `content_hash → 有效原文链接 → 标题+平台` 合并相似展示，并保持列表分页与平台统计口径一致。
- 微博自动监测只接收真实帖子候选：必须能解析微博帖子 ID 且含正文文本；搜索页、超话/话题统计卡、账号资料卡等对象不能入库为舆情内容。微博详情通过 TikHub `weibo_post_detail_v2` 按帖子 ID 增强同一接入记录；监测信息页只展示真实帖子链接，隐藏旧 profile/search/external 链接数据。
- 监测信息“详情”必须展示站内已同步内容；外部原文只能通过详情中的“查看原链接”打开，列表行详情不得直接跳转外站；非 `http/https` 原文链接不得作为可点击外链。
- 首页词云优先可用 DeepSeek 从近期线索正文提取结构化热词，AI 失败、停用或密钥缺失时必须回退 `campus_clue.keywords` 频次统计，不能影响工作台访问。
- `alert_mode=all_hits` 兼容旧值但业务语义为“风险命中告警”，普通主题命中不得自动进入已预警。
- 风险等级筛选、展示和入库必须统一使用 `normal/concern/major/urgent`；历史中文值和 `higher` 只允许在服务层兼容归一，不得从前端继续提交。
- 监测命中转线索必须保留正文、来源、作者、语言、命中词和原文链接；接入记录已转线索时必须复用已有 `clueId`。
- 监测命中转线索必须同步保留学校相关性和主题分类字段。
- 监测告警必须写入结构化 `evidenceJson`，至少包含来源对象、风险等级、风险分、学校相关性、主题分类、命中词和原文链接。
- 任务内重点账号/链接必须有页面级输入路径，且支持从监测结果一键加入后回到本任务继续扫描。
- 后台 `/admin/monitor-tasks` 必须能完成任务新增、编辑、启停、前台展示切换、删除、手动运行、自动接入诊断和重点目标维护；写操作按钮受 `campus:monitor:operate` 控制；不得展示具体监测内容、命中结果或运行日志。
- 监测任务运行时必须先通过自动绑定的 `target_type=monitor_scan` 接入任务采集，再扫描对应接入记录；若平台未接入或调用失败，必须写入 `ingest_capability_status/last_error_message`，不能静默成为孤立配置。
- 已禁用/下线的历史演示监测任务不得继续向前台贡献内容；旧食品安全演示任务的数据清理采用逻辑删除，任务本身保留为 disabled 便于审计。

## 禁止事项

- 禁止在本模块直接调用外部平台 API；自动采集必须走 `CampusIngestService`。
- 禁止绕过 `campus_ingest` 读取未授权数据。
- 禁止将监测命中直接作为报表数据，必须先转入线索库或事件。
