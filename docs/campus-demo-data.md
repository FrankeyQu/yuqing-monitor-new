# 校园舆情演示数据说明

## 1. 执行方式

确保 MariaDB 已启动后执行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/demo/seed-campus-demo-data.ps1
```

脚本可重复执行，使用固定业务 ID 和 `ON DUPLICATE KEY UPDATE` 更新演示数据。

## 2. 演示数据边界

演示数据全部为模拟场景：

- 不包含真实学生账号。
- 不包含私信、密码、通讯录、定位、非公开个人信息。
- 来源说明为“公开网页/授权人员人工录入/上级移交演示”。
- 重点用于学校试运行前的功能验收和培训演示。

## 3. 核心链路业务 ID

| 环节 | 表 | 业务 ID | 说明 |
| --- | --- | --- | --- |
| 接入来源 | `campus_ingest_source` | `200201` | 校园公开网络信息演示源 |
| 接入任务 | `campus_ingest_task` | `200202` | 人工推送演示任务 |
| 接入记录 | `campus_ingest_record` | `200203` | 食品安全公开讨论 |
| 检测主题 | `campus_detection_topic` | `200301` | 校园食品安全 |
| 检测规则 | `campus_detection_rule` | `200302` | 食品安全关键词命中 |
| 检测任务 | `campus_detection_task` | `200303` | 食品安全公开舆情检测任务 |
| 检测命中 | `campus_detection_hit` | `200305` | 已转预警 |
| 预警 | `campus_alert` | `200401` | 检测命中预警 |
| 线索 | `campus_clue` | `200501` | 食品安全舆情线索 |
| 事件 | `campus_event` | `200601` | 食品安全处置事件 |
| 处置任务 | `campus_disposal_task` | `200603` | 后勤核验任务 |
| 辅助研判 | `campus_analysis_result` | `200702` | 已人工采纳 |
| 报告 | `campus_report` | `200802` | 食品安全事件专报 |
| 自动报告任务 | `campus_report_job` | `200804` | 日报自动生成任务 |
| 监测任务 | `campus_monitor_task` | `201001` | 校园食品安全监测 |
| 监测绑定 | `campus_monitor_ingest_task_relation` | `201001200202` | 监测任务绑定人工推送演示任务 |

> 监测任务默认开启自动扫描配置，但实际自动运行仍需后端设置 `SCHEDULE_CAMPUS_MONITOR_OPEN=1`。

## 4. 页面验证入口

- `/situation`：查看态势大屏指标。
- `/monitor`：运行校园食品安全监测任务，查看绑定接入任务、监测结果和负面告警。
- `/ingest`：查看接入来源、任务、记录。
- `/detection`：查看检测主题、任务和命中结果。
- `/alerts`：查看待处理预警。
- `/clues`：查看线索库。
- `/events`：查看事件处置和处置任务。
- `/analysis`：查看辅助研判结果。
- `/reports`：查看并下载事件专报。
- `/auto-reports`：查看自动报告任务和生成日志。
