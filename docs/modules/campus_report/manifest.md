# campus_report Manifest

## 模块定位

报告模块负责基于线索、事件、态势统计和治理指标生成校园舆情报告。报告数据源以 `campus_clue`、`campus_event` 和 `campus_dashboard` 统计口径为准，不直接消费原始采集记录或未转线索的监测命中。

## 依赖模块

- `campus_clue`：报告基础内容、媒体、情感、热词和热点文章。
- `campus_event`：事件复盘报告的关联事件。
- `campus_monitor`：仅通过已转入线索的数据进入报告。
- `campus_ai`：AI 报告生成模式和提示词能力。

## 数据模型

- `campus_report_template`
- `campus_report`
- `campus_report_event`
- `campus_report_job`
- `campus_report_generation_log`

## API 契约

- `/campus/report/template/**`
- `/campus/report/list/detail/save/generate/archive/download/delete`
- `/campus/report/event/list`
- `/campus/auto-report/**`

## 权限

沿用报告模块既有校园 API 权限；新增模板种子不新增绕权入口。

## 测试影响

- 默认模板应渲染 `${governanceTable}`，输出逾期任务、即将到期任务、待处理预警、复核/归档事件和主题风险分布。
- 报告生成不得直接统计未转线索的监测命中。
- 事件复盘模板应能展示事件标题、风险等级、状态、走势、媒体、情感、热词、热点文章和治理复盘。

## 禁止事项

- 禁止报告直接抓取外部网页或第三方 API。
- 禁止把原始采集记录直接作为报告统计口径。
