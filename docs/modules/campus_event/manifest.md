# campus_event Manifest

## 模块定位

事件处置模块负责“线索聚合 → 事件定级 → 分派处置 → 反馈/退回 → 复核 → 归档”的闭环。它读取线索库和账号库，但不得绕过 Service 直接修改其它模块数据。

## 依赖模块

- `campus_clue`：线索转事件、事件相似线索推荐。
- `campus_account_watch`：事件关联重点账号。
- `campus_lexicon`：复用统一风险等级和校园事件主题口径。

## 数据模型

- `campus_event`
- `campus_event_clue`
- `campus_event_account`
- `campus_disposal_task`
- `campus_disposal_record`

处置任务未提交 `due_time` 时，服务层按风险等级生成默认 SLA：`urgent` 30 分钟、`major` 2 小时、`concern` 8 小时、`normal` 24 小时。

## API 契约

- `/campus/event/list/detail/save/create-from-clue/rate/archive`
- `/campus/event/account/add/list`
- `/campus/event/assign/feedback/return/confirm`
- `/campus/event/clue/list/suggest`
- `/campus/event/task/list`
- `/campus/event/record/list`

## 权限

- 菜单：`campus:event:view`
- 只读：`campus:event:read`，覆盖 `GET /campus/event/**`
- 操作：`campus:event:operate`，覆盖 `POST /campus/event/**`

## 测试影响

- 事件必须先定级再分派，未复核不得归档。
- 线索转事件必须拒绝 `archived`、`converted` 或已有 `event_id` 的线索，避免重复生成事件和关系。
- 分派应生成默认 SLA 截止时间。
- 反馈、退回、复核必须校验任务状态。
- 相似线索推荐必须排除已关联当前事件的线索。

## 禁止事项

- 禁止归档事件继续编辑或进入处置流转。
- 禁止 Controller 直接拼接线索、账号或处置记录写库。
