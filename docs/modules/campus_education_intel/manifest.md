# campus_education_intel Manifest

## 模块定位

教育专题模块负责本地区教育相关重点新闻、重点政策、招生政策，以及各学校声量和正负面排名。内容来源优先复用 `campus_ingest` 的百度搜索和已入库线索。

## 依赖模块

- `campus_ingest`：百度搜索接入任务。
- `campus_clue`：教育专题和学校排名基于线索库统计。
- `campus_lexicon`：教育新闻、政策、招生词库。

## 数据模型

- `campus_school_subject`
- 只读统计：`campus_clue`
- 任务创建：`campus_ingest_task`

## API 契约

- `/campus/education/school/**`
- `/campus/education/topic/list`
- `/campus/education/ranking/school-sentiment`
- `/campus/education/baidu-task/create`
- `/campus/education/baidu-task/create-and-run`

## 权限

- 菜单：`campus:education:view`
- 只读：`campus:education:read`
- 操作：`campus:education:operate`

## 测试影响

- 学校排名必须支持别名匹配。
- 没有百度 API Key 时，不能阻塞已有线索统计功能。
- 百度任务创建只写 `credentialRef`，不能写真实密钥。
- 学校主体必须支持模板下载和 CSV 导入，导入按 `schoolId/schoolName` 去重。
- 前端创建百度任务必须选择合法接入来源，可选择创建后立即运行一次。

## 禁止事项

- 禁止在教育专题模块直接外呼百度接口；必须创建或运行 `campus_ingest` 任务。
- 禁止把未入线索库的监测命中直接纳入报表排名。
