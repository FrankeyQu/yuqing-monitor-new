# campus_clue Manifest

## 模块定位

线索库模块负责保存“可进入研判、事件、报表分析”的有效数据条目。监测任务下的命中结果不等于线索，只有人工或规则判断有用后才进入线索库。

新版监测页只保留“新增人工线索”入口；人工补录直接进入本模块，不伪装成普通监测命中。

## 依赖模块

- `campus_ingest`：接入记录可转为线索。
- `campus_monitor`：监测命中可转为线索。
- `campus_lexicon`：后续研判可使用词库辅助。

## 数据模型

- `campus_clue`
- `campus_clue_attachment`
- `campus_clue_operation_log`

`campus_clue` 新增学校相关性和主题分类字段：`school_relevance_score/school_relevance_reason/matched_school_terms/excluded_reason/topic_category/topic_sub_category/topic_reason`。监测命中转线索时直接继承；手工或接入记录转线索时由服务层按标题、正文、关键词和账号信息补齐默认分类。

## API 契约

- `/campus/clue/list`
- `/campus/clue/detail`
- `/campus/clue/count-by-media-type`
- `/campus/clue/count-by-sub-platform`
- `/campus/clue/save`
- `/campus/clue/judge`
- `/campus/clue/archive`
- `/campus/clue/delete`

## 权限

沿用线索库已有接口权限；涉及新增入口时必须更新 `docs/PERMISSION_RULES.md`。

## 测试影响

- 详情接口必须能返回正文；正文缺失时允许从关联接入记录兜底。
- 保存线索必须继续执行重复校验。
- 线索保存和人工研判的风险等级必须统一归一为 `normal/concern/major/urgent`，兼容旧中文值但不得继续扩散旧口径。
- 线索保存、规则研判和 AI 研判的情感值必须统一为 `positive/neutral/negative/none`；“疑似/确认”通过 `clue_status` 和研判记录表达，不再写入 `sentiment`。
- 已转线索的监测命中在监测信息页人工改情感时，线索情感同步更新并写入 `campus_clue_operation_log`；已归档线索不得被监测页反向修改。
- 人工研判只允许 `pending_judge/judged` 状态执行；`archived` 和 `converted` 均不得重新研判。
- 已归档线索不得继续编辑或重复归档；已转事件线索不得重复转事件。
- 线索保存必须保留或自动补齐学校相关性与校园事件主题字段，供事件聚合、相似线索推荐和报告治理指标复用。
- 线索转事件后状态流转记录在 `docs/STATE_MACHINE.md`。
- 信息列表的平台/子平台统计必须与列表查询共用同一套筛选口径，避免 tab 数量和表格结果不一致。

## 禁止事项

- 禁止把所有监测命中自动无差别写入线索库。
- 禁止线索详情只展示标题而丢失正文。
