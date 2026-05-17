# campus_lexicon Manifest

## 模块定位

词库模块统一管理情感词、负面词、风险词、风险等级、校园事件主题、教育新闻词、政策词和招生政策词。当前复用 `campus_dict_type` / `campus_dict_item`，避免新增平行词库系统。

## 依赖模块

- `campus_monitor`：监测扫描读取负面词、风险词。
- `campus_education_intel`：教育专题读取教育新闻、政策、招生词。

## 数据模型

- `campus_dict_type`
- `campus_dict_item`

## 字典类型

- `campus_negative_word`
- `campus_positive_word`
- `campus_risk_word`
- `risk_level`
- `campus_event_topic`
- `campus_education_news_word`
- `campus_policy_word`
- `campus_admission_policy_word`

## API 契约

- `/campus/dict/type/**`
- `/campus/dict/item/**`

## 权限

沿用数据字典后台权限；词库变更视为高影响配置变更。

## 测试影响

- 词库项 `item_value` 优先作为匹配词，缺失时使用 `item_name`。
- 多语言词库通过 `description` 标记语言，后续可演进为独立字段。
- 风险等级统一编码为 `normal/concern/major/urgent`；旧 `higher` 只做历史兼容，不再作为可选项展示。
- 校园事件主题第一版使用固定字典 `campus_event_topic`，后续线索、监测命中和事件应复用同一分类口径。
- 主题分类服务优先读取 `campus_event_topic` 字典项，未配置时仅使用代码内最小兜底词表，避免业务流程因字典缺失中断。

## 禁止事项

- 禁止在业务代码中硬编码大量长期词表。
- 禁止删除旧字典项造成历史任务不可解释。
