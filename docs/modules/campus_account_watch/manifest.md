# campus_account_watch Manifest

## 模块定位

重点账号模块负责保存平台、账号、主页链接、授权依据、关注任务和公开动态。监测任务可引用重点账号或指定链接作为“任务内监控范围”。

## 依赖模块

- `campus_monitor`：任务内重点账号/链接过滤和一键加入。
- `campus_ingest`：账号公开动态可从接入记录转换。

## 数据模型

- `campus_account`
- `campus_account_task`
- `campus_account_content`
- `campus_account_relation`
- `campus_monitor_watch_target`（由 `campus_monitor` 维护，引用账号/链接）

## API 契约

- `/campus/account/**`
- `/campus/monitor/watch-target/**`

## 权限

- 菜单：重点账号后台页。
- 操作账号新增、审核、状态变更必须保留审计日志。

## 测试影响

- 一键加入重点账号必须填充来源依据、授权范围、关注起止时间。
- DPI 或其它平台推送账号时必须走同一保存 API，不允许直接写库。

## 禁止事项

- 禁止未授权抓取账号私域内容。
- 禁止没有来源依据的账号入库。
