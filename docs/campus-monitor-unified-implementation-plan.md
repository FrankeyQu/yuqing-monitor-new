# 校园监测与新版页面统一实施方案

生成时间：2026-05-17  
工作分支：`claude/batch33-monitor-admin`  
当前 worktree：`D:\PRJ\yuqing`  
当前 HEAD：`0090f98`  
远端跟踪分支：`deploy-vps/claude/batch33-monitor-admin`  
相关 worktree：`D:\PRJ\yuqing-daily-monthly-reports`（分支 `claude/daily-monthly-reports`，本方案不在该 worktree 执行）

## 1. 方案来源与统一结论

本方案融合以下两个方案，作为后续唯一执行口径：

- `docs/campus-monitor-implementation-plan.md`
- `docs/new-ui-visible-feature-consolidation-plan.md`

统一原则：

- 底层业务口径以“监测采集、命中、风险标记、展示和时间排序口径固化”为准。
- 新版页面客户可见项以“显示即有效、入口不重复、权限不虚显”为准。
- 冲突项以监测业务正确性优先，前端收口服从后端统一口径。
- 不删除旧兼容字段和后端保留能力，但新版客户页面不展示无效或未验收入口。

最终结论：

- `/monitor` 默认展示全部关键词命中，不再默认只看风险命中。
- 监测命中只依据 `keywords`，`monitor_subject` 和 `subject_aliases` 只做任务展示和历史兼容。
- 负面词只产生风险标记，不决定普通命中是否展示。
- 默认排序采用“明确发布时间优先，未知发布时间靠后，未知内部按采集时间倒序”。
- 删除“价值度”“网站等级”等当前无稳定后端能力的可见排序项。
- 情感值统一为 `positive/neutral/negative/none`，不再把“疑似/确认”作为情感分类。
- “添加文章”和“新增线索”合并为“新增人工线索”。
- 数据接入页面暂时隐藏给客户，后端接入能力保留。
- 后台导航和路由必须按真实权限展示，不允许权限加载失败时展示全部后台菜单。

## 2. 统一业务口径

### 2.1 采集口径

- 采集层归属 `campus_ingest`，负责从 TikHub、百度、公开网页等来源获取原始内容。
- 接入层必须标准化平台、外部内容 ID、标题、正文/摘要、作者、原文链接、平台发布时间、系统采集时间。
- 接入层必须过滤平台搜索反馈项、按钮项、举报原因、推荐控件、搜索过程说明等非内容文本。
- 采集层不得只因为文本非空就入库为有效内容。
- 数据接入客户页面暂时隐藏，但接入后端、接入任务、监测任务自动接入能力保留。

### 2.2 命中口径

- `monitor_subject`、`task_name` 只代表任务名称和展示对象，不再作为命中条件。
- `subject_aliases` 不参与新命中逻辑，只保留字段兼容历史数据。
- 监测命中只依据 `keywords`。
- 关键词为空的任务不执行有效扫描，应在保存或执行时提示至少配置一个关键词。
- 命中流程固定为：
  1. 标准化标题、正文、摘要、作者等可检索文本。
  2. 判断是否命中 `keywords`。
  3. 未命中关键词则不生成监测结果。
  4. 命中关键词后再识别 `negative_words`。
  5. 命中负面词只生成风险标记，不决定是否展示。

### 2.3 风险口径

- 新增或固化 `riskMarked` 作为是否存在风险标记的展示字段。
- `riskMarked=true` 的来源包括：
  - 命中 `matchedNegativeWords`
  - 后续 AI 研判结果
  - 人工确认结果
- 当前阶段优先使用负面词和已有非普通风险等级判断风险标记。
- `result_status` 是处理状态，不等同于风险命中。
- `hitScope=all` 返回全部关键词命中。
- `hitScope=risk` 返回存在风险标记的关键词命中。

### 2.4 时间口径

- `publishTime`：平台明确给出的发布时间，可以为空。
- `collectTime`：系统采集/入库时间，必须存在。
- `publishTimeStatus`：
  - `known`：平台给出明确发布时间。
  - `missing`：平台未提供发布时间。
  - `inferred`：后续从相对时间等可靠信息推断得到。
- 发布时间缺失时，前端显示“发布时间未知”，并辅助展示采集时间。
- 默认排序口径：
  1. 有明确发布时间的内容优先，按 `publishTime DESC`。
  2. 发布时间未知的内容排在明确发布时间之后。
  3. 未知发布时间内部按 `collectTime DESC`。
- `collectTimeStart/End` 只过滤采集时间。
- `publishTimeStart/End` 只过滤明确发布时间；设置发布时间筛选时，未知发布时间记录默认不纳入。

### 2.5 情感口径

- 数据库存储推荐值：
  - `positive`：正面
  - `neutral`：中性
  - `negative`：负面
  - 空值或 `none`：未知
- 前端展示值：
  - 正面
  - 中性
  - 负面
  - 未知
- “疑似/确认”不再作为情感分类，归入研判状态表达：
  - 待研判表示未确认。
  - 已研判表示人工确认。
- 后端查询需兼容历史中文值：
  - `疑似负面/确认负面/负面/negative` 归为负面。
  - `疑似中性/确认中性/中性/neutral` 归为中性。
  - `疑似正面/确认正面/正面/positive` 归为正面。

## 3. 实施范围与关键改动

### 3.1 后端监测核心

- 修改监测扫描逻辑，停止使用 `monitor_subject` 和 `subject_aliases` 作为命中条件。
- 只使用 `keywords` 生成 `matchedKeywords`。
- 命中关键词后再匹配 `negative_words`，生成 `matchedNegativeWords` 和 `riskMarked`。
- 后端 `normalizeInformationHitScope` 默认值改为 `all`。
- `hitScope=risk` 使用 `riskMarked` 或等价风险条件过滤，不再复用“主体词 only”排除逻辑作为默认展示门槛。
- 响应中固化 `publishTime`、`collectTime`、`publishTimeStatus`、`riskMarked`。
- `infoTime` 可保留为兼容字段，但不得再作为唯一排序口径。

### 3.2 采集清洗与历史数据

- 在 `campus_ingest` 层增加统一噪声过滤规则，覆盖当前发现的快手 UI 文案：
  - `与搜索词无关`
  - `内容过时`
  - `封面质量差`
  - `不再看到该作者`
  - `不再看到该作品`
  - `内容违规、血腥、低俗`
  - `与其他结果相似`
  - `不够权威`
  - `其他`
- 同时过滤 `external_id` 类似 `search_*`、URL 类似 `/short-video/search_*`、标题和正文完全等于平台反馈项的记录。
- 历史 UI 噪声采用软隐藏或逻辑删除，不做物理删除。
- 已有关联线索、人工确认、告警记录的历史数据不直接删除。
- 执行历史修正前先备份数据库，执行后记录影响数量和验证 SQL。

### 3.3 新版监测页展示

- `/monitor` 默认 `hitScope=all`。
- 风险筛选只作为筛选，不改变默认展示口径。
- 发布时间为空时显示“发布时间未知”，同时展示采集时间。
- 普通关键词命中和风险命中在同一列表中都可见。
- 风险词、风险等级、风险标记只做视觉标识和筛选条件。
- 首页或态势页风险预览必须显式请求 `hitScope=risk`。

### 3.4 筛选、排序与相似合并

- 删除可见排序项“价值度”“网站等级”。
- 推荐排序项：
  - 默认/发布时间：`publishTime`，按统一时间口径排序。
  - 采集时间：`collectTime`，按系统采集时间倒序。
  - 相关度：`relevance`。
  - 情感：`sentiment`。
- 后端继续兼容旧请求 `value/siteLevel`，统一落到默认排序，不报错。
- 将“相似信息”改为“合并相似信息”，默认关闭。
- `similarDedup=true` 时只影响列表展示、分页总数和统计口径，不删除任何数据。
- 相似合并优先级：
  1. `content_hash`
  2. 有效原文链接
  3. 标题 + 平台
- 每组相似内容展示最新一条，最新判断遵循统一时间口径。

### 3.5 情感统一

- 监测页情感筛选改为：全部、正面、中性、负面、未知。
- 前端展示、筛选、表单复用统一情感映射。
- RuleJudgmentEngine、AiJudgmentEngine、监测结果构建和线索保存不得继续写入“疑似负面/疑似中性”等作为情感值。
- 新增迁移脚本规范化历史情感值。

### 3.6 人工线索入口合并

- 删除“添加文章”按钮和弹窗。
- 保留一个入口，命名为“新增人工线索”。
- 原“新增线索”弹窗标题改为“新增人工线索”。
- 默认 `clueSource=manual`。
- 保存成功提示改为“人工线索已新增”。
- 自动监测命中仍通过“转线索”进入线索体系。
- 人工补录内容不伪装成普通监测命中。

### 3.7 批量操作收口

- 选中数据后分两类统计：
  - 监测命中：有 `monitorResultId`。
  - 已转线索：有 `clueId`。
- 监测命中支持批量转线索、批量转预警、批量忽略。
- 已转线索支持批量研判、批量加入事件。
- 混合选择时，动作按钮显示可处理数量。
- 不适用动作禁用并显示原因。
- 执行结果必须展示成功、失败、跳过数量。
- 第一版可复用现有单条接口循环执行，不强制新增批量 API。

### 3.8 数据接入客户入口隐藏

- 后台导航移除“数据接入”。
- `/admin/ingest` 直接访问时重定向到 `/admin/monitor-tasks`。
- 后端菜单种子增加迁移，把数据接入菜单设为不可见。
- 保留 `IngestView.vue`、接入 Service、接入 Controller 和接入表结构。
- 文档说明数据接入为后端保留能力，客户页面暂不展示。

### 3.9 权限与导航完善

- 路由进入受保护页面前调用 `/campus/system/current-user` 校验真实 session。
- 校验失败时清理 localStorage 并跳转登录页。
- 后台导航不再在权限加载失败或过滤为空时 fallback 到全部菜单。
- 没有后台菜单权限的用户，主侧边栏隐藏“后台管理”。
- `/admin/**` 路由按权限跳转：
  - 有权限进入目标页。
  - 无目标页权限进入第一个有权限的后台页。
  - 完全无后台权限跳回首页。
- 后端 `CampusPermissionInterceptor` 仍作为最终权限边界。

## 4. API 与数据契约

### 4.1 `/campus/monitor/information/list`

请求参数推荐值：

| 参数 | 推荐值 | 说明 |
| --- | --- | --- |
| `hitScope` | `all` / `risk` | 默认 `all` |
| `sortBy` | `publishTime` / `collectTime` / `relevance` / `sentiment` | `value/siteLevel` 仅兼容旧请求 |
| `sentiment` | `positive` / `neutral` / `negative` / `none` | 中文历史值后端兼容 |
| `similarDedup` | `true` / `false` | 合并相似展示，默认 false |

响应字段固化：

| 字段 | 含义 |
| --- | --- |
| `riskMarked` | 是否存在风险标记 |
| `matchedKeywords` | 命中的关键词 |
| `matchedNegativeWords` | 命中的负面词 |
| `publishTime` | 平台发布时间，可为空 |
| `collectTime` | 系统采集时间 |
| `publishTimeStatus` | `known` / `missing` / `inferred` |

兼容说明：

- `monitorSubject` 继续表示任务对象。
- `matchedSubjects` 如仍返回，只作为历史兼容字段，不用于新逻辑。
- `infoTime` 如保留，只作为旧兼容字段。

### 4.2 文档同步

必须同步更新：

- `docs/API_CONTRACT.md`
- `docs/PERMISSION_RULES.md`
- `docs/STATE_MACHINE.md`
- `docs/TEST_CHECKLIST.md`
- `docs/modules/campus_monitor/manifest.md`
- `docs/modules/campus_ingest/manifest.md`
- `docs/modules/campus_clue/manifest.md`
- `docs/AI_PROGRESS.md`

## 5. 分阶段执行顺序

### Phase 1：文档与契约固化

- 以本文件作为唯一综合方案。
- 更新 API、权限、状态机、测试清单和模块 manifest。
- 记录 AI_PROGRESS。
- 不改业务代码。

### Phase 2：后端监测口径修复

- 修复关键词命中逻辑。
- 修复默认 `hitScope=all`。
- 增加或固化 `riskMarked`、`publishTimeStatus`、`collectTime`。
- 修复默认时间排序。
- 增加相似合并后端生效逻辑。
- 增加情感值规范化兼容。

### Phase 3：采集清洗与历史修复

- 增加接入层 UI 噪声过滤。
- 补齐平台发布时间映射。
- 软隐藏历史 UI 噪声。
- 规范化历史情感值。
- 保守修正历史风险标记。

### Phase 4：新版页面收口

- 删除无效排序项。
- 改造发布时间未知展示。
- 改造情感筛选。
- 合并“添加文章/新增线索”为“新增人工线索”。
- 改造批量操作。
- 隐藏数据接入入口。
- 收紧权限导航和 session 校验。

### Phase 5：验收与发布

- 执行后端编译和前端构建。
- 完成监测列表、采集清洗、权限导航、人工线索、批量操作的回归。
- 部署后验证生产数据数量、噪声、时间排序和权限入口。

## 6. 并行施工边界

可以并行：

- 采集层噪声过滤与发布时间映射。
- 数据接入入口隐藏。
- 权限导航收紧。
- 文档更新中非同一文件的部分。

必须串行：

- `MonitorView.vue` 的列表、筛选、排序、批量操作。
- `CampusMonitorServiceImpl` 的命中逻辑和 `hitScope` 默认值。
- `CampusMonitorResultMapper.xml` 的列表过滤、排序、相似合并和统计口径。
- `CampusMonitorInformation` 响应字段。
- `docs/API_CONTRACT.md` 中监测信息接口契约。

建议串行顺序：

1. 先改后端监测核心口径。
2. 再改前端监测列表展示和筛选。
3. 最后做权限导航、数据接入隐藏和人工线索入口收口。

## 7. 测试与验收清单

后端：

- 主体词命中但关键词未命中：不生成监测结果。
- 关键词命中、负面词未命中：生成普通监测结果。
- 关键词命中、负面词命中：生成风险标记。
- `subject_aliases` 不参与命中。
- 不传 `hitScope` 时按 `all` 返回。
- `hitScope=risk` 只返回风险标记记录。
- `similarDedup=true` 时列表、分页、平台统计口径一致。
- 发布时间未知记录不会排在明确发布时间的新内容前面。

采集：

- 快手 UI 文案样本全部被过滤。
- 正常快手内容不被误杀。
- `search_*` 外部 ID 反馈项不进入有效内容。
- 小红书、B 站、快手发布时间字段映射有对应样本测试。

前端：

- `/monitor` 默认展示全部关键词命中。
- 风险筛选只影响筛选结果，不影响默认列表。
- 发布时间未知显示为“发布时间未知 / 采集于...”，不显示“未采集”。
- 排序只展示发布时间、采集时间、相关度、情感。
- 情感筛选只展示全部、正面、中性、负面、未知。
- “新增人工线索”能保存，且不被误认为新增普通监测命中。
- 监测命中批量转线索、转预警、忽略可用。
- 已转线索批量研判、加入事件可用。
- 混合勾选时无静默忽略。
- 客户账号看不到“数据接入”。
- 直接访问 `/admin/ingest` 会重定向。
- 没有后台权限的账号看不到“后台管理”。
- session 过期后刷新页面跳转登录页。

门禁：

- 后端最低门禁：`.\mvnw.cmd -DskipTests compile`
- 条件允许时执行完整测试：`.\mvnw.cmd test -DskipTests=false`
- 修改 `campus-web` 后执行：`npm run build`

## 8. 默认假设

- 当前任务“新疆大学”的主体词和关键词一致，因此历史命中整体可作为有效关键词命中保留。
- 本轮不引入 AI 风险研判，只为后续 AI 接入预留字段和口径。
- 本轮不删除 `subject_aliases`、`matchedSubjects`、`infoTime` 等历史字段，只废弃其新逻辑语义。
- 历史数据清理采用软隐藏或逻辑删除，不做不可逆物理删除。
- 数据接入只是客户页面暂时隐藏，不下线后端能力。
- “疑似/确认”以后不作为情感分类，而由研判状态表达。
- 本方案不处理旧页面不可达代码，也不删除历史兼容 API。
