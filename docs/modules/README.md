# 模块 Manifest 索引

本目录按 Odoo 式模块边界记录校园舆情新增能力。每个模块必须先声明业务归属、依赖、数据模型、API、权限、测试和文档影响，再进入代码实现。

## 当前模块

| 模块 | 业务归属 | Manifest |
| --- | --- | --- |
| campus_ai | AI 供应商、模型、功能绑定、提示词和调用日志 | [campus_ai/manifest.md](campus_ai/manifest.md) |
| campus_monitor | 监测任务、监测命中、任务内重点账号/链接 | [campus_monitor/manifest.md](campus_monitor/manifest.md) |
| campus_clue | 线索库、线索详情、研判入口 | [campus_clue/manifest.md](campus_clue/manifest.md) |
| campus_event | 事件定级、聚合和处置流转 | [campus_event/manifest.md](campus_event/manifest.md) |
| campus_report | 报告模板、生成、归档和治理复盘 | [campus_report/manifest.md](campus_report/manifest.md) |
| campus_ingest | 多平台接入、百度搜索、第三方媒体 API 标准化 | [campus_ingest/manifest.md](campus_ingest/manifest.md) |
| campus_account_watch | 重点账号库、账号公开动态 | [campus_account_watch/manifest.md](campus_account_watch/manifest.md) |
| campus_lexicon | 情感词、负面词、教育专题词库 | [campus_lexicon/manifest.md](campus_lexicon/manifest.md) |
| campus_education_intel | 本地教育新闻/政策/招生专题、学校声量排名 | [campus_education_intel/manifest.md](campus_education_intel/manifest.md) |

## 执行约束

1. 新业务必须优先落入已有模块，不能绕开模块边界新增平行实现。
2. 跨模块调用只能通过 Service/API 层，不能让 Controller 直接拼接其它模块的数据写入。
3. 公共层、权限、字典、迁移和前端路由属于高影响范围，修改前必须说明影响。
4. 本目录 manifest 是后续 AI 子线程授权目录的依据之一。
