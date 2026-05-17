# 校园监测采集与展示口径固化实施方案

生成时间：2026-05-17  
工作分支：`claude/batch33-monitor-admin`  
当前 worktree：`D:\PRJ\yuqing`  
当前 HEAD：`0090f98`  
相关 worktree：`D:\PRJ\yuqing-daily-monthly-reports`（分支 `claude/daily-monthly-reports`，本方案未在该 worktree 修改）

## 1. 背景与当前现状

本方案用于固化校园舆情监测模块的采集、命中、风险标记、展示和时间排序口径，避免后续开发中反复把“监测任务主体词”“关键词命中”“负面词风险标记”“搜索过程内容”混在一起。

截至 2026-05-17 的排查结论：

- 当前线上 `/monitor` 已调整为默认请求 `hitScope=all`，监测列表应展示全部命中结果，而不是只展示风险词命中结果。
- 当前有效展示任务为“新疆大学”，`monitor_subject`、`keywords` 目前基本一致，因此历史命中整体可保留。
- 监测记录约 297 条，其中大量记录是普通关键词命中，只有少量记录命中了负面词。
- 快手结果中存在明显搜索反馈 / UI 文案混入，例如“与搜索词无关”“内容过时”“封面质量差”“不再看到该作者”等。
- B 站、小红书、快手存在较多发布时间缺失记录；这些内容可以展示，但不能用采集时间伪装成发布时间参与新旧排序。
- 现有文档中仍有“主体词 / 别名参与命中”“默认只看风险命中”等旧口径，需要同步修正。

## 2. 最终业务口径

### 2.1 采集口径

- 采集层归属 `campus_ingest`，负责从 TikHub、百度、公开网页等来源获取原始内容。
- 接入层必须尽量标准化以下字段：
  - 平台：`platform`
  - 平台内容 ID：`external_id`
  - 标题：`title`
  - 正文 / 摘要：`content`
  - 作者：`author`
  - 原文链接：`url`
  - 平台发布时间：`publishTime`
  - 系统采集时间：`collectTime`
- 接入层必须过滤平台搜索反馈项、按钮项、举报原因、推荐控件、搜索过程说明等非内容文本。
- 采集层不得只因为文本非空就入库为有效内容。

### 2.2 命中口径

- `monitor_subject` / `task_name` 只代表监测任务名称和展示对象，不再作为命中条件。
- `subject_aliases` 不参与命中；可保留字段兼容历史数据，但不得作为筛选或风险判断依据。
- 监测命中只依据 `keywords`。
- 关键词为空的任务不应执行有效扫描，应提示至少配置一个关键词。
- 命中流程固定为：
  1. 对标题、正文、摘要、作者等可检索文本做标准化。
  2. 判断是否命中 `keywords`。
  3. 未命中关键词则不生成监测结果。
  4. 命中关键词后，再识别 `negative_words`。
  5. 命中负面词只生成风险标记，不决定是否展示。

### 2.3 风险口径

- 负面词命中是风险标记，不是列表展示门槛。
- 风险标记字段建议统一为 `riskMarked`。
- 风险原因来自：
  - `matchedNegativeWords`
  - 后续 AI 研判结果
  - 人工确认结果
- 当前阶段优先使用负面词标记；短词误判可由人工确认和后续 AI 能力修正。
- `result_status` 是处理状态，不应直接等同于风险命中。

### 2.4 展示口径

- `/monitor` 默认展示所有关键词命中的监测结果。
- `hitScope=all`：全部关键词命中结果。
- `hitScope=risk`：关键词命中且存在风险标记的结果。
- 风险词只在列表中标记出来，不能导致普通命中记录被隐藏。
- 首页或态势页如果只展示风险预览，必须显式传 `hitScope=risk`，不能影响监测列表默认行为。

### 2.5 时间口径

- `publishTime`：平台明确给出的发布时间，可以为空。
- `collectTime`：系统采集 / 入库时间，必须存在。
- `publishTimeStatus`：
  - `known`：平台给出明确发布时间。
  - `missing`：平台未提供发布时间。
  - `inferred`：后续从相对时间等信息推断得到。
- 发布时间缺失时，不得显示为“未采集”；应显示“发布时间未知”，并辅助展示采集时间。
- 默认排序：
  1. 有明确发布时间的内容优先，按 `publishTime desc`。
  2. 发布时间未知的内容排在明确发布时间之后。
  3. 未知发布时间内部按 `collectTime desc`。
- `collectTimeStart/End` 只过滤采集时间。
- `publishTimeStart/End` 只过滤明确发布时间；设置发布时间筛选时，未知发布时间记录默认不纳入。

## 3. 详细实施路径

### 3.1 文档先行

先将本文件作为项目口径基准，再同步更新以下文档：

- `docs/modules/campus_monitor/manifest.md`
  - 删除主体词 / 别名参与命中的旧描述。
  - 固化“关键词命中、负面词标记、默认全部展示”的模块契约。
- `docs/modules/campus_ingest/manifest.md`
  - 增加 UI 噪声过滤责任。
  - 增加发布时间字段标准化要求。
- `docs/API_CONTRACT.md`
  - 记录 `/campus/monitor/information/list` 默认 `hitScope=all`。
  - 记录 `riskMarked`、`publishTimeStatus`、`collectTime` 等响应字段。
- `docs/STATE_MACHINE.md`
  - 区分风险标记和处理状态。
- `docs/TEST_CHECKLIST.md`
  - 加入主体词不参与命中、默认全部展示、UI 噪声为 0、发布时间未知排序等回归项。
- `docs/AI_PROGRESS.md`
  - 记录本次方案和当前口径，作为后续 AI / 人工开发交接依据。

### 3.2 调整监测命中逻辑

- 在监测扫描逻辑中停止使用 `monitor_subject` 和 `subject_aliases` 作为命中条件。
- 只使用 `keywords` 生成 `matchedKeywords`。
- 命中关键词后再匹配 `negative_words`，生成 `matchedNegativeWords` 和 `riskMarked`。
- 保留历史字段兼容旧数据，但新增代码不得再依赖 `matchedSubjects` 判断是否展示。
- 对关键词为空的任务增加配置校验或执行期保护。

### 3.3 调整监测列表 API

- 后端 `normalizeInformationHitScope` 默认值调整为 `all`。
- `hitScope=all` 返回全部关键词命中结果。
- `hitScope=risk` 返回风险标记结果。
- 查询排序不再使用 `COALESCE(publish_time, create_time)` 作为单一 `info_time` 直接排序。
- 响应中明确区分：
  - `publishTime`
  - `collectTime`
  - `publishTimeStatus`
  - `riskMarked`
  - `matchedKeywords`
  - `matchedNegativeWords`

### 3.4 调整前端展示

- `/monitor` 保持默认 `hitScope=all`。
- 风险筛选控件只作为筛选，不改变默认口径。
- 发布时间为空时显示“发布时间未知”，并显示采集时间。
- 普通关键词命中记录正常展示，不因未命中负面词隐藏。
- 首页或态势页风险预览继续显式请求 `hitScope=risk`。

### 3.5 增加采集噪声过滤

在 `campus_ingest` 层增加统一噪声过滤规则，至少覆盖当前发现的快手 UI 文案：

- `与搜索词无关`
- `内容过时`
- `封面质量差`
- `不再看到该作者`
- `不再看到该作品`
- `内容违规、血腥、低俗`
- `与其他结果相似`
- `不够权威`
- `其他`

同时过滤以下明显搜索控件记录：

- `external_id` 类似 `search_*`
- URL 类似 `/short-video/search_*`
- 标题和正文完全等于平台反馈项

历史数据处理采用软修复：

- 不做物理删除。
- 将明确 UI 噪声的接入记录和监测结果标记为不可见或删除态。
- 执行前先备份数据库。
- 执行后记录影响数量和验证 SQL。

### 3.6 发布时间缺失修复

- 排查 TikHub / 平台原始响应中是否存在发布时间字段。
- 如果原始字段存在但 mapper 未映射，则补齐映射。
- 如果平台确实未提供，则设置 `publishTimeStatus=missing`。
- 对相对时间可可靠解析的记录，可后续设置 `publishTimeStatus=inferred`。
- 默认排序改为“明确发布时间优先，未知发布时间靠后”。
- 前端不再把未知发布时间显示为“未采集”。

### 3.7 历史数据修正

- 当前主体词和关键词相同的历史命中整体保留。
- 明确 UI 噪声记录软隐藏。
- 无负面词、无人工处理痕迹、无有效告警关联的历史记录，不再作为风险标记展示。
- 已有关联线索、人工确认、告警记录的历史数据不直接删除。
- 所有迁移脚本必须可重复执行，并记录执行结果。

## 4. API / 数据接口变更

建议在 `CampusMonitorInformation` 响应中增加或固化以下字段：

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
- `infoTime` 如需保留，应标注为旧兼容字段，不能再承担唯一排序口径。

## 5. 测试与验收

### 5.1 后端测试

- 主体词命中但关键词未命中：不生成监测结果。
- 关键词命中、负面词未命中：生成普通监测结果。
- 关键词命中、负面词命中：生成风险标记。
- `subject_aliases` 不参与命中。
- 不传 `hitScope` 时按 `all` 返回。
- `hitScope=risk` 只返回风险标记记录。
- 发布时间未知记录不会排在明确发布时间的新内容前面。

### 5.2 采集清洗测试

- 快手 UI 文案样本全部被过滤。
- 正常快手内容不被误杀。
- `search_*` 外部 ID 反馈项不进入有效内容。
- 小红书、B 站、快手发布时间字段映射有对应样本测试。

### 5.3 前端测试

- `/monitor` 默认展示全部关键词命中。
- 风险筛选只影响筛选结果，不影响默认列表。
- 发布时间未知显示为“发布时间未知 / 采集于...”，不显示“未采集”。
- 普通命中和风险命中在同一列表中都可见。

### 5.4 构建门禁

- 后端最低门禁：`.\mvnw.cmd -DskipTests compile`
- 条件允许时执行完整测试：`.\mvnw.cmd test -DskipTests=false`
- 修改 `campus-web` 后执行：`npm run build`

### 5.5 生产验证

- `/monitor` 默认请求应为 `hitScope=all`，或后端不传时按 `all` 处理。
- 全部命中数量应接近当前有效监测总量，不应只剩风险词命中的少量记录。
- 快手 UI 噪声查询结果应为 0。
- 发布时间未知记录应靠后展示，不干扰明确发布时间内容排序。

## 6. 分阶段落地建议

### Phase 1：文档与契约固化

- 落地本方案文档。
- 更新模块 manifest、API 契约、测试清单、AI_PROGRESS。
- 不改业务代码。

### Phase 2：后端口径修复

- 修复命中逻辑。
- 修复 API 默认 `hitScope`。
- 增加 `riskMarked`、`publishTimeStatus`、`collectTime`。
- 修复发布时间排序。

### Phase 3：采集清洗与历史修复

- 增加接入层 UI 噪声过滤。
- 补齐平台发布时间映射。
- 软隐藏历史 UI 噪声。
- 保守修正历史风险标记。

### Phase 4：前端展示与验收

- 修复发布时间未知展示。
- 确认全部命中默认展示。
- 确认风险筛选和风险标签行为。
- 完成构建、部署和生产验证。

## 7. 明确假设

- 当前任务“新疆大学”的主体词和关键词一致，因此历史命中整体可作为有效关键词命中保留。
- 本轮不引入 AI 风险研判，只为后续 AI 接入预留字段和口径。
- 本轮不删除 `subject_aliases` 字段，只废弃其命中语义。
- 历史数据清理采用软隐藏，不做不可逆物理删除。
- 后续任何采集、监测、前端列表改动，都必须以本文档口径为准。
