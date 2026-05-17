# Batch 28 多平台监测配置和前端管理页实施方案 V0.2

状态：Done。

## 1. 背景

Batch21-27 已完成媒体接入平台后端骨架、TikHub 安全适配、调度运行、标准化去重、检测联动、额度审计，以及公开网页白名单预留。当前前端 `/ingest` 页面仍偏“基础表格 CRUD”，任务配置还需要手写 JSON，不适合学校值班人员长期使用。

Batch28 的目标是把现有能力组织成一个可试运行的“媒体接入与监测配置中心”。

## 2. 本批目标

- 重构 `/ingest` 为“媒体接入中心”，第一屏展示接入总览、运行状态、额度和失败概况。
- 保留并增强接入来源、接入任务、接入记录能力。
- 新增公开网页白名单管理入口。
- 新增全局运行日志入口，支持按任务、状态、错误类型筛选。
- 新增 API 调用日志入口，展示 provider、endpoint、credentialRef、状态、HTTP 状态、costUnits 和错误类型。
- 接入任务表单改为“结构化配置 + JSON 预览”，减少手写 JSON。
- 对 TikHub 和公开网页白名单提供安全配置模板。
- 任务配置继续禁止真实密钥、Cookie、Token、代理、账号池、浏览器指纹、签名参数。

## 3. 本批明确不做

- 不接入真实 TikHub Key。
- 不新增真实外部媒体 API 端点。
- 不实现公开网页真实抓取。
- 不做 Cookie、代理、账号池、浏览器采集、登录态采集。
- 不做学生私人账号自动发现。
- 不把检测命中自动定性为事件结论。
- 不改旧 Thymeleaf 前台页面。

## 4. 主线程技术决策

本批以前端为主，后端只补一个管理页必要的只读能力：

```text
后端：
  CampusIngestRunLog 全局分页/筛选接口

前端：
  /ingest 媒体接入中心
    -> 总览卡片
    -> 接入来源
    -> 监测任务
    -> 公开网页白名单
    -> 接入记录
    -> 运行日志
    -> API调用日志
```

前端实现先保守落在现有 `IngestView.vue`，避免一次大拆分引入路由和权限风险；同时把新增的配置生成逻辑封装为独立函数，后续再拆组件。

## 5. 后端设计

新增或增强：

- `CampusIngestRunLogDao.list(...)`
- `CampusIngestService.listRunLogPage(...)`
- `GET /campus/ingest/run/page`

筛选参数：

- `pageNum`
- `pageSize`
- `taskId`
- `runStatus`
- `errorType`
- `triggerType`

返回：

- `PageInfo<CampusIngestRunLog>`

不新增表结构。

## 6. 前端设计

### 6.1 总览区

页面顶部展示：

- 接入来源总数
- 启用任务数
- 公开网页白名单数
- 最近失败运行数
- 今日 API 调用次数
- 今日 API costUnits

统计来源优先使用现有列表接口和日志接口，避免为 Batch28 新增复杂统计表。

### 6.2 Tab 信息架构

Tab 顺序：

1. 总览
2. 接入来源
3. 监测任务
4. 公开网页白名单
5. 接入记录
6. 运行日志
7. API 调用日志

### 6.3 接入来源

增强点：

- 来源类型文案校园化。
- 平台选项提供常用值：抖音、微博、小红书、B站、微信公众号、视频号、学校官网、学院网站、上级移交。
- 保留自由输入，避免限制后续扩展。

### 6.4 监测任务

增强点：

- 来源选择改为下拉，显示来源名称和平台，不要求手输来源 ID。
- 适配器切换时显示对应配置面板。
- TikHub 配置面板：
  - 平台：抖音
  - endpointKey：`douyin_search_video_v2`
  - 关键词
  - limit
  - sortType / publishTime / contentType
  - credentialRef 只读显示 `TIKHUB_API_KEY`
- 公开网页配置面板：
  - 选择白名单
  - 目标 URL
  - mode 固定 `metadata_only`
- 手动推送配置：
  - 不需要 fetchConfig。
- 表单保存前自动生成 `fetchConfig` JSON。
- 仍允许查看 JSON 预览，但不鼓励手写。

### 6.5 公开网页白名单

支持：

- 列表、筛选、新增、编辑、启停、删除。
- 字段：站点名称、域名、baseUrl、路径前缀、授权依据、授权范围、robots 策略、频率、责任部门、状态、备注。
- 页面文案明确“当前为合规预留，不执行真实网页抓取”。

### 6.6 运行日志

支持：

- 全局日志列表。
- 按任务、运行状态、触发方式、错误类型筛选。
- 展示拉取、成功、重复、无效、失败、检测触发、命中、预警、耗时和错误。

### 6.7 API 调用日志

支持：

- 按任务、runId、provider、callStatus 筛选。
- 展示 credentialRef 但不展示真实密钥。
- 展示 costUnits 和错误分类。
- 不展示请求体、响应体、Cookie、Authorization。

## 7. 权限、审计、合规约束

- 本批新增的运行日志查询是只读能力，不写审计。
- 白名单 CRUD、来源/任务保存、任务运行继续复用现有审计。
- 前端不得出现 API Key、Cookie、Token、代理、账号池、浏览器指纹、签名输入框。
- TikHub 配置只能写 `credentialRef=TIKHUB_API_KEY`。
- 公开网页配置只能选择白名单，不允许直接绕过白名单保存 URL。

## 8. 验收步骤

后端：

```powershell
.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=PublicWebFetchConfigTest,PublicWebWhitelistValidatorTest,CampusIngestGovernanceServiceTest,CampusIngestAuditSanitizerTest" test
.\mvnw.cmd -DskipTests package
```

前端：

```powershell
cd D:\PRJ\yuqing\campus-web
npm run build
```

本地联调：

- 打开 `/ingest`，页面首屏显示“媒体接入中心”总览。
- 新增公开网页白名单成功。
- 新增 `public_web_pull` 任务时，选择白名单和 URL 后自动生成 JSON。
- 运行白名单内公开网页任务成功且 0 记录、0 API 调用。
- 新增 TikHub 任务时，只能生成 `credentialRef=TIKHUB_API_KEY`，不出现密钥输入框。
- 无 Key 运行 TikHub 任务失败并在 API 调用日志中显示 `credential_missing`。
- 全局运行日志和 API 调用日志可加载。

## 9. 风险和待打磨点

- `IngestView.vue` 已较大，本批若全部放入一个文件会继续变大；主线程优先完成可用闭环，后续可按组件拆分。
- 运行日志全局分页会新增后端只读接口，需要验证 Mapper XML。
- 总览统计来自现有列表，不是强一致实时指标，适合管理页概览，不作为考核口径。

## 10. 实施结果

后端：

- 新增 `GET /campus/ingest/run/page` 全局运行日志分页接口。
- `CampusIngestRunLogDao` 和 Mapper 支持按任务、状态、错误类型、触发方式筛选。
- `CampusIngestService` 提供 `listRunLogPage(...)`，不新增表结构和迁移。

前端：

- `/ingest` 已重构为“媒体接入中心 / 多平台监测配置”。
- 新增总览卡片、最近运行、接入边界、公开网页白名单、全局运行日志、API 调用日志 Tab。
- 接入任务表单已改为结构化配置和只读 JSON 预览。
- TikHub 配置只生成 `credentialRef=TIKHUB_API_KEY`，密钥引用输入框禁用。
- 公开网页配置只生成 `url` 和 `mode=metadata_only`，并要求选择白名单。
- `api_pull`、`rss_pull`、`file_import` 仅作为预留禁用选项，不提供可操作配置。

合规收口：

- 没有新增真实 API Key 输入。
- 没有新增 Cookie、代理、账号池、浏览器指纹、签名参数配置。
- 没有实现公开网页真实抓取。
- API 调用日志只展示 `credentialRef`、provider、endpoint、状态、HTTP 状态、costUnits、耗时和错误类型。

## 11. 验证结果

- 后端关键单测通过：22 个用例，0 失败。
- 后端 `.\mvnw.cmd -DskipTests package` 通过；因运行中的 jar 锁文件导致第一次 repackage 失败，已按本地流程停止服务后重新 package 成功。
- 本地服务已重新启动，Flyway 保持 `V1.18`，schema 已是最新。
- `GET /campus/ingest/run/page?pageNum=1&pageSize=3` 登录后返回 200。
- 前端 `campus-web npm run build` 通过；仅剩 Rollup 依赖注释和 chunk size 警告。
- 浏览器验证 `/ingest` 可渲染，首屏显示“媒体接入中心 / 多平台监测配置”。
- 浏览器验证新增任务表单：
  - 第三方媒体 API 生成 TikHub 安全配置。
  - `TIKHUB_API_KEY` 为禁用密钥引用。
  - JSON 预览为只读。
  - 白名单公开网页配置固定 `metadata_only`，不出现真实抓取能力。
