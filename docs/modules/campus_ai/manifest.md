# campus_ai Manifest

## 模块定位

AI 能力管理模块负责统一管理校园舆情中可配置的智能能力：大模型、搜索增强、正文提取、社媒接入、历史 AI 能力的启停、模型绑定、提示词模板和调用日志。它不直接拥有业务数据，只为报告、研判、接入、词云等模块提供配置和审计能力。

## 依赖模块

- `campus_report`：报告生成和自动报告读取 AI 功能绑定、模型和提示词模板。
- `campus_clue`：少数民族语言研判、关键词/词云提取读取 AI 功能绑定。
- `campus_ingest`：TikHub、百度千帆、Jina Reader 等外部智能接入读取供应商接入点配置。
- `campus_monitor`：监测信息词云、监测任务 AI 体检、监测命中 AI 分析和后续 AI 摘要增强读取 AI 功能绑定。

## 数据模型

- `campus_ai_provider`：供应商/接入点配置，保存供应商类型、Base URL、密钥引用、启停、超时和额度。
- `campus_ai_model`：大模型配置，保存模型编码、上下文长度、默认温度、默认最大 token、是否支持流式。
- `campus_ai_feature_binding`：业务功能与供应商/模型绑定，保存启停、失败策略、额度和日志策略。
- `campus_ai_prompt_template`：提示词模板和版本，保存 system/user prompt、输出格式和启用状态。
- `campus_ai_call_log`：AI 管理模块统一调用日志，保存功能、供应商、模型、状态、耗时、错误和脱敏快照。

密钥不保存明文，数据库只保存 `credential_ref`，实际值由环境变量或受控配置注入。

## API 契约

- `/campus/ai/provider/**`
- `/campus/ai/model/**`
- `/campus/ai/feature/**`
- `/campus/ai/prompt/**`
- `/campus/ai/call-log/**`
- `/campus/ai/overview`
- `/campus/ai/provider/test`

## 前端入口

- `/admin/settings/ai`：后台 AI 能力管理页，展示能力总览、功能绑定、供应商、模型、提示词和调用日志。

## 权限

- 菜单：`campus:ai:view`
- 只读：`campus:ai:read`
- 操作：`campus:ai:operate`

## 测试影响

- AI 供应商保存不得写入明文密钥；只能写 `credentialRef`。
- DeepSeek 报告、少数民族研判和词云提取应优先读取 `campus_ai_feature_binding`，不可用时按功能失败策略降级。
- 监测任务 AI 体检使用功能码 `monitor_task_diagnosis`；监测命中 AI 分析使用功能码 `monitor_result_analysis`；AI 输出只能作为辅助建议，业务数据写入由 `campus_monitor` Service 控制。
- TikHub、百度千帆、Jina Reader 读取 `campus_ai_provider` 的 Base URL 和密钥引用，保留原 Spring 配置作为兼容兜底。
- AI 调用日志必须脱敏，不能保存完整密钥、Authorization header 或超长原文。
- DeepSeek 词云提取失败时必须回退到现有命中词/关键词统计，不影响首页访问。

## 禁止事项

- 禁止在数据库保存真实 API Key、Token、Cookie、设备指纹、签名参数。
- 禁止让 AI 研判结果直接替代人工确认；AI 输出只能作为辅助建议。
- 禁止把历史写作宝、OCR、旧 NLP 分词强行接入当前校园主流程；P2 只做状态管理和后续兼容预留。
