# Batch 22 TikHub 适配器实施方案 V0.2

## 1. 背景

Batch21 已完成统一接入执行骨架：

```text
campus_ingest_source
  -> campus_ingest_task
  -> CampusIngestAdapter SPI
  -> campus_ingest_run_log
  -> campus_ingest_record
```

当前 `third_party_api` 适配器只是合规占位，不调用任何外部接口。Batch22 的目标是在这个框架上实现 TikHub 适配器，让系统具备“通过授权媒体 API 拉取公开内容”的能力。

本方案基于用户提供的 TikHub API 文档目录。实现前主线程仍需逐页核对目标接口的请求方式、鉴权头、参数名、响应结构和计费说明。

## 2. 本批目标

Batch22 只做 TikHub 安全适配的最小可运行闭环：

- 将 `third_party_api` 占位适配器升级为 TikHub 可插拔适配器。
- 新增 TikHub HTTP Client。
- 新增 TikHub endpoint allowlist。
- 新增 TikHub 响应标准化 mapper。
- 通过 `campus_ingest_task.fetch_config` 配置平台、接口、关键词、数量、游标和密钥引用。
- 运行时把公开内容标准化写入 `campus_ingest_record`。
- 不在数据库、日志、审计、前端中保存真实 API Key。

## 3. 本批明确不做

- 不做自动调度，仍只支持手动运行。
- 不做完整供应商/端点表。
- 不做完整前端配置页，只补必要字段说明或最小提示。
- 不接入账号登录态、Cookie、SSO、设备注册、签名生成、浏览器指纹等接口。
- 不接入点赞、关注、收藏、转发、评论发布等互动接口。
- 不采集私信、通讯录、密码、非公开个人资料。
- 不采集粉丝列表、关注列表、点赞列表、收藏列表。
- 不自动识别学生私人账号。
- 不用平台绕过、反爬规避、代理池、账号池。

## 4. 主线程端点选择

Batch22 第一批只允许接入“公开内容发现和公开内容详情”类接口。

优先级：

| 优先级 | 平台 | 类型 | 用途 |
| --- | --- | --- | --- |
| P0 | 抖音 | 综合搜索 / 视频搜索 | 校园关键词公开内容发现 |
| P0 | 微博 | 搜索微博 / 热搜榜 | 校园关键词与热点发现 |
| P1 | 小红书 | 搜索笔记 | 校园关键词公开笔记发现 |
| P1 | Bilibili | 综合搜索 / 视频评论 | 校园相关视频和评论发现 |
| P1 | 微信公众号 | 搜索文章 / 文章详情 | 学校相关公众号文章 |
| P2 | 微信视频号 | 搜索公开视频 / 评论 | 后续扩展 |

首个实现落点由主线程定为：

```text
P0-1：抖音视频搜索 V2
```

原因：

- TikHub 官方文档给出了 `douyin_search_video_v2` 的请求方式、参数和核心响应字段。
- 微博综合搜索文档只给出“搜索结果列表”描述，未给出清晰字段路径，首批不硬猜。
- 先用一个字段明确的公开搜索端点跑通安全适配、脱敏、标准化和幂等链路。

首批 endpoint allowlist：

| endpointKey | 方法 | TikHub 路径 | 状态 |
| --- | --- | --- | --- |
| `douyin_search_video_v2` | POST | `/api/v1/douyin/search/fetch_video_search_v2` | Batch22 实现 |
| `weibo_search_all` | GET | `/api/v1/weibo/app/fetch_search_all` | P1 预留，不实现 mapper |
| `douyin_search_general_v5` | POST | `/api/v1/douyin/search/fetch_general_search_v5` | P1 预留，不实现 mapper |

## 5. 配置模型

Batch22 不新增表，沿用 `campus_ingest_task.fetch_config`。

建议 JSON：

```json
{
  "provider": "tikhub",
  "endpointKey": "douyin_search_video_v2",
  "platform": "douyin",
  "query": "学校 食堂",
  "limit": 20,
  "cursor": 0,
  "sortType": "0",
  "publishTime": "0",
  "filterDuration": "0",
  "contentType": "0",
  "searchId": "",
  "backtrace": "",
  "credentialRef": "TIKHUB_API_KEY",
  "timeoutMs": 10000
}
```

字段规则：

- `provider` 必须为 `tikhub`。
- `endpointKey` 必须命中后端 allowlist。
- `platform` 必须是标准平台编码。
- `query` 必须非空。
- `limit` 默认 20，最大 50。
- `credentialRef` 只能是环境变量名或后续密钥管理引用，不能是真实 Key。
- `timeoutMs` 默认 10000，最大 30000。

`douyin_search_video_v2` 参数映射：

| fetch_config 字段 | TikHub 字段 | 默认 |
| --- | --- | --- |
| `query` | `keyword` | 必填 |
| `cursor` | `cursor` | `0` |
| `sortType` | `sort_type` | `"0"` |
| `publishTime` | `publish_time` | `"0"` |
| `filterDuration` | `filter_duration` | `"0"` |
| `contentType` | `content_type` | `"0"` |
| `searchId` | `search_id` | `""` |
| `backtrace` | `backtrace` | `""` |

## 6. 密钥策略

Batch22 只允许从环境变量读取真实 Key：

```text
TIKHUB_API_KEY
```

严禁：

- 在代码中写 Key。
- 在数据库 `fetch_config` 中写 Key。
- 在审计日志中写 Key。
- 在错误信息中回显 Authorization Header。
- 在前端展示 Key。

如果环境变量不存在：

```text
适配器返回失败运行日志：TikHub credential is not configured
不发起外部请求
```

注意：即使环境变量存在，真实调用也必须等用户明确提供合法 Key、授权范围、关键词和频率后再做验收。主线程本批默认只做无 Key 失败链路和 mapper 样例验证。

## 7. 后端设计

### 7.1 文件范围

允许修改：

- `src/main/java/com/stonedt/intelligence/service/campus/ingest/ThirdPartyApiIngestAdapter.java`
- 新增 `src/main/java/com/stonedt/intelligence/service/campus/ingest/tikhub/*.java`
- 如有必要，新增少量配置类或工具类。

原则上不改：

- `CampusIngestServiceImpl`
- 旧迁移文件
- 检测模块
- 权限模块

### 7.2 类设计

建议类：

```text
TikhubFetchConfig
TikhubEndpointDefinition
TikhubEndpointRegistry
TikhubClient
TikhubResponseMapper
TikhubSanitizer
```

职责：

- `TikhubFetchConfig`：解析和校验 `fetch_config`。
- `TikhubEndpointRegistry`：维护 allowlist，不允许任意 URL。
- `TikhubClient`：发起 HTTP 请求，统一超时、鉴权、异常。
- `TikhubResponseMapper`：把不同平台响应转为 `CampusIngestItem`。
- `TikhubSanitizer`：脱敏请求、响应和错误。

实施约束：

- 不新增 Maven 依赖。
- HTTP 使用现有 `OkHttpClient` Bean。
- JSON 使用 Fastjson。
- 不新增第二个 `adapterType=third_party_api` 的 Spring Bean，必须改造现有 `ThirdPartyApiIngestAdapter`。
- `source.accessEndpoint` 不参与真实请求，避免任意 URL SSRF 风险。

### 7.3 allowlist

allowlist 必须硬编码为受控枚举，而不是让用户填任意 URL：

```text
douyin_search_video_v2
douyin_search_general_v5
weibo_search_all
weibo_hot_search
```

Batch22 只实现 `douyin_search_video_v2`，其余可保留定义但返回不支持。

## 8. 标准化映射

统一映射为 `CampusIngestItem`：

| 标准字段 | TikHub 来源 |
| --- | --- |
| `externalId` | 平台作品 ID / 微博 ID / 文章 ID |
| `platform` | `douyin` / `weibo` / `xhs` / `bilibili` / `wechat` |
| `contentType` | `post` / `video` / `article` / `comment` |
| `title` | 标题、描述摘要或正文前 80 字 |
| `content` | 正文、描述、caption |
| `originalUrl` | 原始公开链接 |
| `publishTime` | 发布时间 |
| `authorName` | 公开作者名 |
| `keywords` | 查询关键词 |
| `riskLevel` | 默认 `normal`，后续检测模块判断 |
| `rawData` | 脱敏后的单条原始 JSON |

`douyin_search_video_v2` 首批映射：

| 标准字段 | 字段路径 |
| --- | --- |
| `externalId` | `business_data[].data.aweme_info.aweme_id` |
| `platform` | `douyin` |
| `contentType` | `video` |
| `title` | `aweme_info.desc` 前 80 字 |
| `content` | `aweme_info.desc` |
| `originalUrl` | `aweme_info.share_url` |
| `publishTime` | `aweme_info.create_time` 秒级时间戳 |
| `authorName` | `aweme_info.author.nickname` |
| `keywords` | `fetch_config.query` |
| `rawData` | 脱敏后的 `aweme_info` 单条 JSON |

兼容读取：

- 如果响应不是 `business_data[]`，mapper 可兼容扫描 `data[]` 中的 `aweme_info`。
- 单条缺少 `aweme_id` 且标题/正文为空时跳过。
- 不把粉丝数、点赞数、收藏数等互动统计作为本批核心字段，只允许留存在脱敏 `rawData` 中。

## 9. 错误和日志

运行失败要写入 `campus_ingest_run_log`：

- 缺少 Key。
- endpointKey 不在 allowlist。
- TikHub 返回非 2xx。
- 响应 JSON 解析失败。
- 响应结构不符合预期。
- 单条记录标准化失败。

错误信息必须截断并脱敏。

## 10. 前端最小改动

Batch22 不做完整前端，只做：

- 在接入任务配置说明中给出 TikHub `fetch_config` 示例。
- 保留“第三方媒体API”适配器。
- 运行失败时展示后端错误。

完整可视化供应商/端点配置留给 Batch28。

## 11. 验收口径

必须通过：

- Maven 编译通过。
- 前端构建通过，如有前端改动。
- 不配置 `TIKHUB_API_KEY` 时，运行 TikHub 任务不会发起外部请求，并返回明确失败日志。
- 配置 sample 响应时，mapper 能生成 `CampusIngestItem`。
- 代码中不存在真实 Key。
- 代码中不出现 Cookie、SSO、签名、设备注册、互动接口调用。

如果用户后续提供真实 Key，真实调用验收必须另行确认：

- 确认 Key 来源合法。
- 确认接入平台、关键词、频率、授权范围。
- 确认不会采集非公开数据。

## 12. 主线程自审

V0.2 风险：

- TikHub 响应结构仍可能和文档示例存在层级差异，mapper 必须做保守兼容。
- 微博字段路径不够明确，首批不实现微博 mapper。
- 当前密钥策略只有环境变量，后续 Batch26 必须补密钥引用和脱敏审计。

主线程决策：

```text
Batch22 先实现 TikHub 抖音视频搜索 V2 的安全适配最小闭环。
实现 endpoint allowlist + fetch_config 解析 + OkHttp Client + mapper + 脱敏。
无真实 Key 时不发起外部请求。
有 sample 响应时验证 mapper。
```

## 13. 子线程摸底结论

Curie 文档核对结论：

- `douyin_search_video_v2` 字段最明确，适合首批实现。
- `weibo_search_all` 文档未给清晰字段路径，暂不实现。
- `douyin_search_general_v5` 虽是较新接口，但混合多类型，留作后续扩展。
- 明确排除 SSO、Cookie、设备注册、签名、粉丝/关注/点赞/收藏、互动、直播、电商、广告、星图等接口。

Socrates 后端摸底结论：

- 项目已有 OkHttp 和 Fastjson，不需要新增依赖。
- 改造现有 `ThirdPartyApiIngestAdapter`，不要新增第二个 `third_party_api` Bean。
- TikHub 新类放在 `service/campus/ingest/tikhub/`。
- 真实 Key 只从环境变量读取，`fetch_config` 只放 `credentialRef`。
- TikHub 层必须先脱敏再抛错，否则 run log 和审计会记录敏感信息。

## 14. 实施结果

状态：Done。

主线程实际落点：

- `ThirdPartyApiIngestAdapter` 已改造为 `provider=tikhub` 分支。
- 新增 `TikhubEndpointRegistry`，硬编码 allowlist。
- 新增 `TikhubFetchConfig`，解析和校验 `fetch_config`。
- 新增 `TikhubCredentialResolver`，只读取环境变量 `TIKHUB_API_KEY`。
- 新增 `TikhubClient`，使用项目已有 OkHttp，固定请求 `https://api.tikhub.io` allowlist 路径。
- 新增 `TikhubResponseMapper`，把 `douyin_search_video_v2` 样例结构标准化为 `CampusIngestItem`。
- 新增 `TikhubSanitizer` 和 `TikhubIngestException`，统一截断和脱敏错误、rawData。
- 新增 `TikhubResponseMapperTest`，验证 mapper 样例和内联密钥拦截。

主线程安全补强：

- `fetch_config` 禁止 `apiKey`、`token`、`cookie`、`session`、`deviceId`、`fingerprint`、`xBogus`、`aBogus`、`password` 等敏感或绕过类字段。
- `query` 最大长度限制为 120。
- 非 allowlist endpoint 直接失败。
- 非 Batch22 已实现 endpoint 返回不支持。
- 无 `TIKHUB_API_KEY` 时先失败，不构造外部请求。

验证结果：

- `.\mvnw.cmd -DskipTests compile` 通过。
- `.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=TikhubResponseMapperTest" test` 通过，2 个用例成功。
- 停止旧后端进程后 `.\mvnw.cmd -DskipTests package` 通过。
- 后端无 `TIKHUB_API_KEY` 启动成功，Flyway 保持 `V1.13`。
- 创建 TikHub 验证任务 `2053043464236568576` 后执行 `/campus/ingest/task/run` 返回 `TikHub credential is not configured`。
- `/campus/ingest/run/list?taskId=2053043464236568576` 写入 `failed` 运行日志。
- `/campus/ingest/record/list?taskId=2053043464236568576` 为 0 条。
- `.codex-tools/app.log` 未出现 `api.tikhub.io`、`Authorization`、`Bearer` 输出。

结论：

Batch22 达成本批目标。真实 TikHub Key、真实外部调用验收、额度和重试策略留给后续确认和 Batch26 收口。
