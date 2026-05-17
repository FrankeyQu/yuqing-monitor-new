# Batch 27 白名单公开网页采集器预留实施方案 V0.2

## 1. 背景

Batch21-26 已经完成媒体接入平台的执行骨架、TikHub 安全适配、调度、标准化、检测联动、额度和审计治理。后续学校可能需要接入学校官网、学院网站、通知公告、公开新闻页等公开网页。

本批只做“合规公开网页采集器预留”，让系统具备后续扩展公开网页采集能力的安全边界和配置模型，不做真实爬虫能力。

## 2. 本批目标

- 建立公开网页白名单表，明确允许域名、路径前缀、授权依据、责任部门、频率限制和 robots 策略说明。
- 新增白名单后端 CRUD，保存、停用、删除必须审计。
- 新增 `public_web_pull` 适配器占位，只做配置校验和白名单校验，不发起真实网络请求。
- 接入任务 `fetchConfig` 可引用白名单 ID 和目标 URL。
- 运行公开网页任务时，如果 URL 不在白名单、协议不是 HTTP/HTTPS、缺少授权依据或白名单停用，应失败并写运行日志。
- `public_web_pull` 当前返回空结果和明确提示，后续接真实抓取器时仍必须复用白名单校验。

## 3. 本批明确不做

- 不做真实网页抓取。
- 不绕登录、不绕验证码、不绕反爬、不生成签名、不使用 Cookie。
- 不做代理池、账号池、浏览器指纹、设备模拟。
- 不抓取需要登录或非公开页面。
- 不抓取私信、通讯录、后台系统、个人隐私数据。
- 不自动发现学生账号或私人账号。
- 不接 Playwright/Selenium 浏览器采集。
- 不把网页内容自动定性、自动处置或自动转事件。

## 4. 主线程技术决策

Batch27 只做“白名单 + 适配器占位 + 校验闭环”：

```text
campus_public_web_whitelist
  -> PublicWebWhitelistService CRUD + 审计
  -> PublicWebIngestAdapter
  -> PublicWebFetchConfig 校验
  -> 白名单命中后返回 empty response
```

原因：

- 学校试运行前必须先把“能采什么、为什么能采、多久采一次、谁负责”固化。
- 真正采集网页涉及 robots、频率、版权、页面结构、动态渲染等问题，应在授权目标明确后再开发。
- 当前系统需要的是后续能力预留，不是万能爬虫。

## 5. 数据库方案

新增迁移：

```text
src/main/resources/db/migration/V1.18__CampusPublicWebWhitelist.sql
```

新增表 `campus_public_web_whitelist`：

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `whitelist_id` | bigint | 白名单业务 ID |
| `site_name` | varchar(255) | 站点名称 |
| `site_domain` | varchar(255) | 允许域名，不含协议 |
| `base_url` | varchar(1024) | 站点基础 URL |
| `allowed_path_prefix` | varchar(512) | 允许路径前缀 |
| `authorization_basis` | varchar(1024) | 授权或来源依据 |
| `authorization_scope` | varchar(1024) | 授权范围 |
| `robots_policy` | varchar(1024) | robots/站点规则说明 |
| `rate_limit_seconds` | int | 建议最小采集间隔秒 |
| `max_depth` | int | 预留最大深度，默认 0 |
| `responsible_department_id` | bigint | 责任部门 |
| `enabled` | tinyint | 是否启用 |
| `remark` | varchar(1024) | 备注 |
| `deleted` | tinyint | 逻辑删除 |

字典：

- 确认 `ingest_adapter_type.public_web_pull` 继续可用。
- 如不存在则补写公开网页拉取字典项。

## 6. 后端设计

### 6.1 白名单管理

新增：

```text
entity/campus/CampusPublicWebWhitelist.java
dao/campus/CampusPublicWebWhitelistDao.java
mapper/campus/CampusPublicWebWhitelistMapper.xml
service/campus/CampusPublicWebWhitelistService.java
service/impl/campus/CampusPublicWebWhitelistServiceImpl.java
controller/campus/CampusPublicWebWhitelistController.java
```

接口：

- `GET /campus/ingest/public-web/whitelist/list`
- `POST /campus/ingest/public-web/whitelist/save`
- `POST /campus/ingest/public-web/whitelist/delete`
- `POST /campus/ingest/public-web/whitelist/update-status`

校验：

- 域名不能为空，不能包含协议、路径、查询参数。
- `baseUrl` 必须是 `http://` 或 `https://`。
- `allowedPathPrefix` 默认 `/`。
- 授权依据和授权范围不能为空。
- `rateLimitSeconds` 最小 60 秒。
- `maxDepth` 默认 0，本批不使用。

### 6.2 公开网页适配器占位

新增：

```text
service/campus/ingest/publicweb/PublicWebFetchConfig.java
service/campus/ingest/publicweb/PublicWebWhitelistValidator.java
service/campus/ingest/PublicWebIngestAdapter.java
```

`fetchConfig` 示例：

```json
{
  "whitelistId": 1001,
  "url": "https://www.example.edu.cn/news/",
  "mode": "metadata_only"
}
```

适配器行为：

- 校验 `whitelistId` 存在且启用。
- 校验 `url` 是 HTTP/HTTPS。
- 校验 `url.host` 与白名单 `siteDomain` 一致，或是其子域名。
- 校验 `url.path` 命中 `allowedPathPrefix`。
- 校验通过后返回空记录和提示：`public web fetcher is reserved, no network request executed`。
- 校验失败抛出业务异常，进入接入运行日志。

## 7. 前端策略

Batch27 前端只做类型预留：

- `CampusPublicWebWhitelist` 类型。
- `detectionIngest.ts` 增加白名单 API 封装。

完整页面放在 Batch28 多平台监测配置中统一做，不在 Batch27 新增复杂 UI。

## 8. 权限、审计、合规约束

- 白名单新增、修改、停用、删除必须写审计。
- 审计只记录 URL、域名、授权依据和配置，不记录抓取内容。
- 白名单是前置约束，后续任何公开网页采集实现必须复用此校验。
- 未命中白名单的 URL 必须阻断，不能降级为直接请求。
- 真实网页目标、频率和授权边界必须由用户确认后才能启用真实抓取。

## 9. 验收步骤

后端：

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=PublicWebFetchConfigTest,PublicWebWhitelistValidatorTest" test
.\mvnw.cmd -DskipTests package
```

数据库：

- 启动本地服务，确认 Flyway 迁移到 `V1.18`。
- 检查 `campus_public_web_whitelist` 存在。

功能：

- 新增合法白名单成功。
- 保存缺少授权依据的白名单失败。
- `public_web_pull` 任务引用白名单内 URL，运行成功但不产生记录，并提示未执行网络请求。
- `public_web_pull` 任务引用非白名单 URL，运行失败且写入运行日志。
- `.codex-tools/app.log` 不出现真实网页外呼记录。

前端：

```powershell
cd D:\PRJ\yuqing\campus-web
npm run build
```

## 10. 风险和待打磨点

- 白名单域名匹配要避免 `badexample.edu.cn` 误命中 `example.edu.cn`，必须用 host 等值或 `.example.edu.cn` 后缀。
- 真实抓取器后续要补 robots 解析、限速、内容抽取、版权和重试策略。
- Batch27 不做真实内容抽取，因此不会提升“监测能力”，只是把后续自研采集器的合规边界先固化。

## 11. 子线程摸底结论与主线程 V0.2 决策

主线程采纳：

- 后端只读结果建议 `fetch_config` 继续复用任务字段，不新增任务扩展表。
- 后端只读结果建议 `public_web_pull` 由适配器注册表自动注册，不改调度、标准化和检测联动主链路。
- 后端只读结果建议补强路径边界，避免 `/news` 误命中 `/newsroom`。
- 合规只读结果建议阻断 userinfo、localhost、IP 字面量、路径穿越、敏感 query 参数和非 HTTP/HTTPS URL。
- 前端只读结果建议 Batch27 不做复杂页面，完整管理入口留给 Batch28。

主线程最终决策：

```text
Batch27 = 公开网页白名单表 + CRUD 审计
        + public_web_pull 占位适配器
        + fetchConfig 白名单形态校验
        + URL 安全边界校验
        + 前端类型/service/适配器文案预留

不做真实网页抓取
不做浏览器采集
不做 Cookie / 代理 / 账号池 / 指纹 / 签名
不自动发现学生账号
```

## 12. 实施结果

状态：Done。

已实现：

- 新增 `V1.18__CampusPublicWebWhitelist.sql`。
- 新增 `CampusPublicWebWhitelist`、DAO、Mapper、Service、Controller。
- 新增白名单接口：`list`、`save`、`update-status`、`delete`。
- 新增 `PublicWebFetchConfig`、`PublicWebWhitelistValidator`、`PublicWebIngestAdapter`。
- `public_web_pull` 适配器仅校验并返回空结果，不发起网络请求。
- 白名单保存阶段校验域名、URL、授权依据、授权范围、状态和 `baseUrl` 与白名单的一致性。
- URL 校验阻断 userinfo、localhost、IP 字面量、路径上级目录和敏感 query/fragment。
- 接入任务保存阶段校验 `public_web_pull` 配置，`fetchConfig` 仅允许 `whitelistId/url/mode`，`mode` 仅允许 `metadata_only`。
- 前端已补充 `CampusPublicWebWhitelist` 类型、白名单 API service 和“白名单公开网页”适配器文案。

验证：

- `.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=PublicWebFetchConfigTest,PublicWebWhitelistValidatorTest,CampusIngestAuditSanitizerTest,CampusIngestGovernanceServiceTest,CampusIngestDetectionLinkageServiceTest" test` 通过，22 个用例成功。
- `.\mvnw.cmd -DskipTests package` 通过。
- `campus-web` `npm run build` 通过。
- 本地服务启动成功，Flyway 迁移到 `V1.18`。
- 数据库确认 `campus_public_web_whitelist` 存在。
- API 验收：白名单内 URL 的 `public_web_pull` 任务运行成功，`fetched_count=0`、`success_count=0`、接入记录 0、API 调用日志 0。
- API 验收：非白名单域名运行失败，运行日志 `error_type=validation_error`。
- 静态检查确认 `PublicWebIngestAdapter` 没有引用 OkHttp、RestTemplate、HttpURLConnection、Playwright、Selenium、Jsoup 等真实抓取能力。

遗留风险：

- 公开网页真实采集器仍未实现，后续必须在明确授权、robots、频率、栏目范围后再开发。
- 运行日志当前不显示空适配器返回的提示文案，前端 Batch28 可在任务配置页说明 `public_web_pull` 当前为占位能力。
