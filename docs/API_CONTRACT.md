# API 契约 — 卓然舆情

> ⚠️ 当前项目 API 风格不统一，存在三种模式共存的情况。本文档记录已识别的 API，待补全项标记为 `[待确认]`。

## 路由前缀

| 前缀 | 类型 | 说明 |
|------|------|------|
| `/api` | REST API | Swagger 文档化，返回 ResultVO |
| `/monitor` | 页面+数据 | Thymeleaf Controller，混合 ModelAndView / @ResponseBody |
| `/analysis` | 页面+数据 | Thymeleaf Controller |
| `/project` | 页面+数据 | Thymeleaf Controller |
| `/system` | 页面+数据 | Thymeleaf Controller |
| `/fullsearch` | 页面+数据 | Thymeleaf Controller |
| `/report` | 页面+数据 | Thymeleaf Controller |
| `/volume` | 页面+数据 | Thymeleaf Controller |
| `/user` | 页面+数据 | Thymeleaf Controller |
| `/wechat` | REST | @RestController |
| `/displayboard` | 页面+数据 | Thymeleaf Controller |
| `/search` | 页面 | Thymeleaf Controller（已简化） |
| `/` | 页面 | 登录/登出 |
| `/user/getToken` | Token | 返回 ResultVO（在 LoginController 中） |

## REST API（/api）— 已文档化（Swagger）

### POST /api/getArticle
- **功能**：获取文章列表
- **Content-Type**：application/json
- **请求体**：SearchCondition（分页、筛选、排序等）
- **响应**：`ResultVO<PageInfo<ArticleData>>`
- **鉴权**：请求头 `token`
- **响应状态码**：0=成功，其他=失败

### POST /api/getMergeArticle
- **功能**：获取文章列表（合并相似文章）
- **Content-Type**：application/json
- **请求体**：SearchCondition
- **响应**：`ResultVO<PageInfo<ArticleData>>`
- **鉴权**：请求头 `token`
- **响应状态码**：0=成功

### GET /api/detail
- **功能**：获取文章详情
- **参数**：articleId（文章id）、projectId（方案id）、publish_time（发布时间）
- **响应**：`ResultVO<ArticleDetail>`
- **鉴权**：请求头 `token`
- **响应状态码**：0=成功

### POST /api/getToken
- **功能**：获取 API 访问 Token
- **请求体**：LoginVO（username, password）
- **响应**：`ResultVO<String>`（data 为 token 字符串）
- **鉴权**：无

## 页面 API（已识别，按 Controller 分组）

### 登录模块（LoginController）

| 方法 | 路径 | 功能 | 返回类型 |
|------|------|------|---------|
| GET | `/login` | 登录页面 | ModelAndView |
| POST | `/login` | 登录处理 | JSON（code/msg） |
| GET | `/logout` | 退出登录 | redirect |
| GET | `/forgotpwd` | 忘记密码页面 | ModelAndView |
| GET | `/jumpLogin` | 快捷登录 | redirect |
| GET | `/wechatJumpLogin` | 微信跳转登录 | redirect |
| POST | `/onlinestatistical` | 在线用户统计 | JSON |
| POST | `/user/getToken` | 获取 Token | ResultVO |

### 监测模块（MonitorController）

| 方法 | 路径 | 功能 | 返回类型 |
|------|------|------|---------|
| GET | `/monitor` | 监测列表页面 | ModelAndView |
| GET | `/monitor/detail/{articleid}` | 文章详情页面 | ModelAndView |
| POST | `/monitor/articleDetail` | 文章详情数据 | JSON |
| POST | `/monitor/relatedArticles` | 相关文章 | JSON |
| POST | `/monitor/getCondition` | 获取用户条件 | JSON |
| POST | `/monitor/getarticle` | 文章列表 | ResultVO |
| POST | `/monitor/getSimilarArticle` | 相似文章 | ResultVO |
| POST | `/monitor/getanalysisarticle` | 分析文章列表 | JSON |
| POST | `/monitor/getindustry` | 行业标签 | JSON |
| POST | `/monitor/getevent` | 事件标签 | JSON |
| POST | `/monitor/getprovince` | 省份数据 | JSON |
| POST | `/monitor/getcity` | 城市数据 | JSON |
| POST | `/monitor/getapparticle` | App 端文章列表 | JSON |
| POST | `/monitor/exportarticle` | 导出数据 | 文件流 |
| POST | `/monitor/getgroupname` | 方案组名称 | JSON |
| POST | `/monitor/warningSetting` | 预警设置 | ResultVO |
| GET | `/monitor/warningSetting/{projectId}` | 获取预警设置 | ResultVO |
| POST | `/monitor/edit/read` | 标记已读/未读 | JSON |
| POST | `/monitor/edit/status` | 标记状态 | JSON |

### 分析模块（AnalysisController）

| 方法 | 路径 | 功能 | 返回类型 |
|------|------|------|---------|
| GET | `/analysis` | 监测分析页面 | ModelAndView |
| POST | `/analysis/getAanlysisByProjectidAndTimeperiod` | 分析数据 | JSON |
| POST | `/analysis/opinionScreen/...` | 大屏分析数据 | JSON |
| POST | `/analysis/getAnalysisMonitorProjectid` | 监测分析数据 | JSON |
| POST | `/analysis/latestnews` | 最新资讯 | JSON |
| POST | `/analysis/emotionalproportion` | 情感占比 | JSON |
| POST | `/analysis/planwordhit` | 方案词命中 | JSON |
| POST | `/analysis/popularinformation` | 热门资讯 | JSON |
| POST | `/analysis/emotioncategory` | 情感分类走势 | JSON |
| POST | `/analysis/keywordindex` | 高频词指数 | JSON |
| POST | `/analysis/popularkeyword` | 热点关键词 | JSON |
| GET | `/analysis/updateanalysisdata` | 刷新分析数据 | JSON |

### 方案模块（ProjectController）

| 方法 | 路径 | 功能 | 返回类型 |
|------|------|------|---------|
| GET | `/project` | 方案管理页面 | ModelAndView |
| POST | `/project/updateSolutionGroupStatus` | 删除方案组 | JSON |
| POST | `/project/getProjectCountByGroupId` | 方案数统计 | JSON |
| POST | `/project/names` | 方案/组名称 | JSON |
| POST | `/project/groupandproject` | 方案组列表 | JSON |
| POST | `/project/listproject` | 方案列表 | JSON |
| POST | `/project/mkdirgroup` | 创建方案组 | String |
| POST | `/project/editgroup` | 修改方案组 | JSON |
| GET | `/project/addproject` | 新增方案页面 | ModelAndView |
| POST | `/project/verifygroup` | 校验方案组 | JSON |
| GET | `/project/detail` | 方案详情页面 | ModelAndView |
| POST | `/project/commitproject` | 提交新方案 | JSON |
| GET | `/project/editproject` | 修改方案页面 | ModelAndView |
| GET | `/project/getedit` | 获取方案信息 | JSON |
| POST | `/project/commiteditproject` | 提交修改方案 | JSON |
| POST | `/project/getGroupAndProject` | 方案组+方案列表 | JSON |
| POST | `/project/delProject` | 删除方案 | JSON |
| POST | `/project/delProjectDetail` | 删除方案详情 | JSON |
| POST | `/project/batchUpdateProject` | 批量删除 | JSON |
| POST | `/project/keywords` | 获取关键词 | JSON |

### 系统设置模块（SystemController）

| 方法 | 路径 | 功能 | 返回类型 |
|------|------|------|---------|
| GET | `/system/warning` | 预警配置页面 | ModelAndView |
| POST | `/system/listWarning` | 预警列表 | ResultUtil |
| POST | `/system/updateWarningStatusById` | 修改预警开关 | ResultUtil |
| POST | `/system/getwords` | 预警词检查 | ResultUtil |
| GET | `/system/warningmsg` | 预警消息页面 | ModelAndView |
| GET | `/system/warningedit` | 预警编辑页面 | ModelAndView |
| GET | `/system/warningSettingDetail` | 预警详情 | ResultUtil |
| POST | `/system/updateWarning` | 编辑预警 | ResultUtil |
| POST | `/system/getWarningArticle` | 预警文章列表 | ResultUtil |
| GET | `/system/preference` | 偏好设置页面 | ModelAndView |
| GET | `/system/favorite` | 收藏夹页面 | ModelAndView |
| POST | `/system/getFavoriteList` | 收藏列表 | ResultUtil |
| POST | `/system/listProjectByUserId` | 用户方案列表 | JSON |
| POST | `/system/getOpinionConditionByProjectId` | 偏好信息 | JSON |
| POST | `/system/updateOpinionCondition` | 保存偏好 | JSON |
| GET | `/system/feedback` | 反馈页面 | ModelAndView |
| POST | `/system/getSystemTitle` | 系统标题 | ResultUtil |
| POST | `/system/listSolutionGroupByUserId` | 方案组列表 | JSON |
| POST | `/system/listProjectByGroupId` | 方案列表 | JSON |
| GET | `/system/productmanual/online` | 在线手册 | ModelAndView |
| GET | `/system/uploadProductManual` | 下载手册 | 文件流 |

### 全文搜索模块（FullSearchController）
> 约 30+ API，主要针对不同数据类型的列表和详情。全部 GET 请求，多数返回 JSONObject。

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/fullsearch` | 搜索页面 |
| GET | `/fullsearch/result` | 搜索结果页面 |
| GET | `/fullsearch/informationList` | 资讯列表 |
| POST | `/fullsearch/informationListpost` | 资讯列表（POST） |
| GET | `/fullsearch/hotList` | 热点列表 |
| GET | `/fullsearch/companyList` | 工商列表 |
| GET | `/fullsearch/judgmentList` | 法律文书 |
| GET | `/fullsearch/biddingList` | 招标列表 |
| GET | `/fullsearch/inviteList` | 招聘列表 |
| GET | `/fullsearch/reportList` | 研报列表 |
| GET | `/fullsearch/knowLedgeList` | 知识产权 |
| GET | `/fullsearch/investmentList` | 投资融资 |
| GET | `/fullsearch/baiduKnowsList` | 百度知道 |
| GET | `/fullsearch/thesisnList` | 百度学术 |
| GET | `/fullsearch/doctorList` | 医生列表 |
| GET | `/fullsearch/lawyerList` | 律师列表 |
| GET | `/fullsearch/professorList` | 专家人才 |
| GET | `/fullsearch/executionPersonList` | 被执行人 |
| GET | `/fullsearch/complaintList` | 投诉列表 |
| GET | `/fullsearch/announcementList` | 公告列表 |

### 报告模块（ReportController）
| POST | `/report/listReportCustom` | 报告列表 | JSON |
| GET | `/report` | 报告列表页面 | ModelAndView |
| GET | `/report/{id}` | 报告详情页面 | ModelAndView |
| POST | `/report/reportDetail` | 报告详情数据 | JSON |
| POST | `/report/batchUpdateReportCustom` | 批量删除报告 | JSON |
| POST | `/report/batchUpdateReportCustomStatus` | 修改报告状态 | JSON |

### 微信模块（WechatController）
| GET | `/wechat/getQrCode` | 获取二维码 | ResultUtil |
| GET | `/wechat/getBindQrCode` | 绑定二维码 | ResultUtil |
| GET | `/wechat/checkBind` | 检查绑定 | ResultUtil |
| GET | `/wechat/wasBind` | 绑定回调 | ResultUtil |
| POST | `/wechat/handleSubscribe` | 关注事件 | Boolean |
| GET | `/wechat/handleUnsubscribe` | 取消关注 | void |
| POST | `/wechat/handleAuthorize` | 授权事件 | void |
| GET | `/wechat/checkLogin` | 检查登录 | ResultUtil |

### 其他
| POST | `/volume/getproject` | 声量数据 | JSON |
| POST | `/volume/projectname` | 声量方案名 | JSON |
| GET | `/displayboard` | 综合看板页面 | ModelAndView |
| POST | `/user/detail` | 用户详情 | ResultUtil |
| POST | `/user/edit` | 修改密码 | ResultUtil |
| POST | `/user/save` | 新增用户 | JSON |
| GET | `/user/{userid}` | 账号管理页面 | ModelAndView |

## 响应结构

### 统一响应（ResultVO）
```json
{"code": 200, "msg": "success", "data": {}}
```
- code 200 = 成功，500 = 业务错误
- 部分 API 使用 code 0 表示成功（如 ApiController）

### 旧式响应
```json
{"code": 200, "msg": "成功", "data": []}
{"code": 200, "msg": "", "data": "..."} 
```
- code 类型可能是 Integer 或 String

### 直接 JSON
```json
{"code": 200, "msg": "成功", "data": [...]}
{"code": -1, "msg": "用户不存在"}
```
- 格式不统一

## 错误结构

无统一错误结构。常见的错误返回字段：
- `{"code": 500, "msg": "错误描述"}` — 业务错误
- `{"code": -1, "msg": "用户不存在"}` — 登录错误
- `{"code": 203, "msg": "旧密码错误", "data": null}` — 密码错误
- `{"code": 422, "msg": "...", "result": "fail"}` — 限流错误

## 鉴权要求

- **页面访问**：Cookie（token）或 URL 参数（token），由 LoginHandlerInterceptor 拦截
- **API 访问**：请求头 `token`，由 LoginHandlerInterceptor 拦截
- **Token 获取**：POST `/user/getToken`（页面）或 POST `/api/getToken`（API）
- **Token 格式**：JWT（nimbus-jose-jwt HS256）
- **Token 过期**：由 `token.expire-time` 配置控制
- **白名单**：登录相关路径（`/login`、`/api/getToken`、`/user/getToken`）[待确认是否全部豁免]

## 权限要求

详见 [PERMISSION_RULES.md](PERMISSION_RULES.md)

## 分页约定

- 使用 PageHelper（MyBatis 分页插件）
- 参数：`page`（页码，默认1）、`pageSize`（行数，默认10-50不等）
- 响应中可能包含 `total`、`pages`、`pageNum`、`pageSize` 等字段
- 大部分分页结果通过 JSON 字符串返回，字段名不统一

## 排序 / 筛选约定

### 筛选参数
- `emotion`：情感（1正面/2中性/3负面，可多选）
- `match` / `matchingmode`：匹配方式（1全文/2标题/3正文）
- `precise`：精准筛选（0关闭/1开启）
- `similar` / `merge`：相似合并（0不合并/1合并）
- `timePeriod`：时间周期（1=24h/2=3天/3=7天/4=15天）
- `classify`：数据来源分类
- `province` / `city`：地域筛选

### 排序参数
- `sort`：排序方式（1时间降序/2时间升序/3相似数倒序）

## Breaking Change 规则

1. 任何修改 API 参数、返回值、路由路径的操作都是 Breaking Change
2. Breaking Change 必须在文档中标注
3. Breaking Change 必须在 AI_PROGRESS.md 中记录
4. Breaking Change 前必须通知用户/主控

## 新增 / 修改 API 的流程

1. 确定 API 归属模块和路由前缀
2. 确定鉴权方式（token / 无鉴权）
3. 确定请求方式（GET / POST / PUT / DELETE）
4. 新增 API 建议返回 ResultVO<T> 格式
5. 在 API_CONTRACT.md 中记录
6. 如果在 ApiController 中新增 → 添加 Swagger 注解
7. 子线程操作时向主控说明新增 API

### 蒙语/维语舆情搜索模块（MinoritySearchController）

新增独立 REST API，使用 `@RestController` + `/api/minority` 前缀。

| 方法 | 路径 | 功能 | 参数 | 返回格式 |
|------|------|------|------|---------|
| GET | `/api/minority/search` | 蒙/维语舆情搜索+分析 | keyword(必填), engine(默认all), page(默认1), analyze(默认true) | `{"code":0, "data":{list, statistics}}` |

- **keyword**：蒙语或维语关键词原文（UTF-8 编码）
- **engine**：`baidu` / `bing` / `all`
- **analyze**：是否启用 DeepSeek LLM 分析增强
- **鉴权**：请求头 `token`（与现有 API 一致）
- **响应结构**：
```json
{
  "code": 0,
  "data": {
    "keyword": "原文关键词",
    "detectedLang": "mongolian",
    "engine": "all",
    "total": 20,
    "list": [
      {
        "title": "搜索结果标题（原文）",
        "snippet": "摘要（原文）",
        "url": "https://...",
        "source": "来源网站",
        "publishDate": "2025-01-01",
        "engine": "baidu",
        "language": "mongolian",
        "sentiment": "negative",
        "topic": "教育",
        "summary": "中文摘要",
        "duplicateOf": -1
      }
    ],
    "statistics": {
      "sentimentDist": {"positive": 5, "negative": 3, "neutral": 12},
      "topicDist": {"教育": 8, "文化": 6, "民生": 4},
      "sourceDist": {"news.sina.com": 5, "baidu.tieba": 3},
      "topWords": ["ᠮᠣᠩᠭᠣᠯ", "ᠬᠡᠯᠡ"],
      "timeTrend": [{"date": "2025-01-01", "count": 3}]
    }
  }
}
```

### 校园舆情 — 接入配置（新增 Baidu 适配器）

`POST /campus/ingest/task/save` 的 `adapterType` 字段新增可选值：

| adapterType | 说明 | fetchConfig 字段 |
|-------------|------|-----------------|
| `baidu_search` | 百度千帆 Web Search，可选 Jina Reader 正文增强 | query(必填), resourceTypes(选填,默认["web"]), topK(选填,默认20), credentialRef(选填,默认"BAIDU_API_KEY"), readerEnabled(选填,默认false), readerProvider(选填,当前仅jina), maxReaderCalls(选填,默认5), fallbackToSnippet(选填,默认true), readerTimeoutMs(选填,默认15000) |
| `third_party_api` + `provider=tikhub` | TikHub 社媒搜索；小红书、微博、B站支持搜索后详情增强；知乎、微信公众号、快手支持关键词搜索接入 | endpointKey(必填), platform(选填), query(必填), limit(选填,默认20), page(选填), credentialRef(选填,默认"TIKHUB_API_KEY"), timeoutMs(选填,默认10000,最大30000), detailEnabled(小红书/微博/B站选填), maxDetailCalls(选填,默认20) |
| `public_web_pull` | 白名单公开网页；可仅校验或使用 Jina Reader 读取单 URL 正文 | whitelistId(必填), url(必填), mode(metadata_only/jina_reader), readerProvider(选填,当前仅jina), maxDepth(必须0), timeoutMs(选填,默认15000) |

**百度查询语法**：支持布尔运算符 `query: "新疆大学 OR 新大 OR 心大 -录取分数线"`，支持 `site:domain.com` 限定。

**正文增强约定**：Jina Reader 是内容提取器，不负责关键词搜索。百度任务先通过百度千帆发现 URL，开启 `readerEnabled=true` 后最多读取 `maxReaderCalls` 个搜索结果 URL；读取失败且 `fallbackToSnippet=true` 时保留百度摘要入库。公开网页任务使用 `mode=jina_reader` 时必须先通过白名单域名/路径校验，只读取单个公开 URL，不做栏目递归、Cookie、代理、浏览器采集或验证码绕过。

**TikHub 详情增强约定**：小红书搜索结果以 `note_id/externalId` 精确去重，并对同来源、同平台、同标题近似重复做长正文优先合并；微博以搜索返回 `id` 调详情；B站以搜索返回 `aid` 调详情。开启 `detailEnabled=true` 后，详情接口只能增强同一条接入记录的正文、发布时间、原文链接和互动数，不得新增第二条详情记录；详情失败需在 `raw_data` 标记，微信公众号请求成功但 0 条需在运行日志中标明“无可识别文章”。

### 校园舆情 — 线索研判

| 方法 | 路径 | 功能 | 鉴权 |
|------|------|------|------|
| GET | `/campus/clue/list?language=xx&clueStatus=pending_judge` | 研判列表（按语言/状态筛选） | token |
| POST | `/campus/clue/judge?clueId=xx&riskLevel=xx&judgeOpinion=xx` | 人工研判 | token |
| POST | `/campus/alert/create-from-clue` | 从线索创建告警 | token |

**线索语言字段**：`CampusClue.language` 取值 `"zh"` / `"mongolian"` / `"uyghur"`。

**研判路由**（服务端自动）：`zh`/`null`→规则引擎（关键词/负向词/正则），`mongolian`/`uyghur`→AI引擎（DeepSeek 翻译+分析）。

**线索状态流转**：
```
pending_judge → [自动研判] → judged → [人工确认] → archived
                                ↓
                          [产生告警] → CampusAlert
```

## 校园舆情 API（/campus）— P0 核心契约

> 基于 `src/main/java/com/stonedt/intelligence/controller/campus/*Controller.java` 扫描整理。校园模块均为 `@RestController`，统一返回 `ResultVO<T>`，即 `{"code":200,"msg":"success","data":...}`；业务校验失败通常返回 `ResultVO.error(400,msg)`，权限不足由 `CampusPermissionInterceptor` 返回 `{"code":403,"msg":"无校园权限，请联系管理员授权"}`。

### 通用约定

| 项 | 约定 |
|----|------|
| 鉴权 | `/campus/**` 需要登录态 token/cookie，仍由 `LoginHandlerInterceptor` 处理 |
| 权限 | 除 `/campus/system/current-user`、`/campus/system/menu-tree` 外，需通过 `campus_permission_api` + `campus_role_api` 授权 |
| 分页 | 常用 `pageNum`、`pageSize`，响应为 `PageInfo<T>` |
| 日期 | 查询参数日期格式多为 `yyyy-MM-dd` |
| 审计 | 关键写操作调用 `CampusAuditLogService.record(...)` |
| 数据权限 | 当前以校园角色/接口权限为主，学校/部门级数据隔离仍需继续确认 |
| 风险等级 | 统一使用 `normal` 普通关注、`concern` 一般预警、`major` 重大预警、`urgent` 特别重大；服务端兼容历史中文值和 `higher`，写入时归一为统一编码 |

### 工作台与态势

| 方法 | 路径 | 参数 | 响应 | 权限 |
|------|------|------|------|------|
| GET | `/campus/dashboard/overview` | 无 | `ResultVO<Map>` | 登录 + API 权限 |
| GET | `/campus/dashboard/statistics` | 无 | `ResultVO<Map>` | 登录 + API 权限；返回 `overview/monitorOverview/riskDistribution/sourceRiskDistribution/topicRiskDistribution/governanceMetrics` 等态势和治理指标 |
| GET | `/campus/dashboard/word-cloud` | [待补全] | `ResultVO<List>` | 登录 + API 权限 |
| GET | `/campus/dashboard/trend` | [待补全] | `ResultVO<List>` | 登录 + API 权限 |
| GET | `/campus/hot-rank/list` | `days` 等 [待确认] | `ResultVO<List>` | 登录 + API 权限 |
| GET | `/campus/compare/data` | `keyword` 等 [待确认] | `ResultVO<Map>` | 登录 + API 权限 |
| GET | `/campus/spread/data` | `eventId`/`keyword` 等 [待确认] | `ResultVO<Map>` | 登录 + API 权限 |

### 线索库（CampusClueController）

| 方法 | 路径 | 参数/请求体 | 响应 | 说明 |
|------|------|-------------|------|------|
| GET | `/campus/clue/list` | `pageNum,pageSize,keyword,clueSource,sourcePlatform,sourceSubPlatform,riskLevel,clueStatus,language,sentiment,articleStatus,startTime,endTime,publishTimeStart,publishTimeEnd,collectTimeStart,collectTimeEnd,matchScope,similarDedup,sortBy` | `ResultVO<PageInfo<CampusClue>>` | 线索分页查询；日期结束日按整天包含 |
| GET | `/campus/clue/detail` | `clueId` | `ResultVO<CampusClue>` | 线索详情 |
| GET | `/campus/clue/operation-logs` | `clueId` | `ResultVO<List<CampusClueOperationLog>>` | 操作日志 |
| GET | `/campus/clue/count-by-media-type` | 与 `/campus/clue/list` 相同的筛选字段（不含分页/排序） | `ResultVO<List<Map{name,value}>>` | 当前筛选口径下的平台统计；前端合成“全部（年份）”总数 |
| GET | `/campus/clue/count-by-sub-platform` | 与 `/campus/clue/list` 相同的筛选字段（通常包含 `sourcePlatform`，不含分页/排序） | `ResultVO<List<Map{name,value}>>` | 当前筛选口径下的子平台/来源统计 |
| GET | `/campus/clue/suggest` | `keyword` | `ResultVO<List<String>>` | 搜索建议 |
| POST | `/campus/clue/save` | `CampusClue` JSON | `ResultVO<CampusClue>` | 新增/保存，默认 `clueStatus=pending_judge`，缺省时自动补 `schoolRelevance*` 与 `topic*` 字段 |
| POST | `/campus/clue/judge` | `clueId,riskLevel,judgeOpinion?` | `ResultVO<CampusClue>` | 人工研判，状态进入 `judged` |
| POST | `/campus/clue/archive` | `clueId,archiveReason?` | `ResultVO<CampusClue>` | 线索归档 |
| POST | `/campus/clue/delete` | `clueId` | `ResultVO<Void>` | 软删除 |

说明：`articleStatus` 在校园线索库当前表结构中暂无对应持久字段，不能映射到 `clue_source`；后续如需要“已读/已选”筛选，应先补齐状态模型和写入接口。

### 事件与处置（CampusEventController）

| 方法 | 路径 | 参数/请求体 | 响应 | 说明 |
|------|------|-------------|------|------|
| GET | `/campus/event/list` | `pageNum,pageSize,keyword,riskLevel,eventStatus,startTime,endTime` | `ResultVO<PageInfo<CampusEvent>>` | 事件分页 |
| GET | `/campus/event/detail` | `eventId` | `ResultVO<CampusEvent>` | 事件详情 |
| POST | `/campus/event/save` | `CampusEvent` JSON | `ResultVO<CampusEvent>` | 新增/保存事件 |
| POST | `/campus/event/create-from-clue` | `clueId` + 可选 `CampusEvent` JSON | `ResultVO<CampusEvent>` | 线索转事件 |
| POST | `/campus/event/clue/add` | `eventId,clueId` | `ResultVO<CampusEvent>` | 将已有线索加入已有事件，并写入 `campus_event_clue` 与 `campus_clue.event_id` |
| POST | `/campus/event/rate` | `eventId,riskLevel,disposalRequirement?` | `ResultVO<CampusEvent>` | 风险定级 |
| POST | `/campus/event/account/add` | `eventId,accountId` | `ResultVO<CampusEventAccount>` | 关联账号 |
| POST | `/campus/event/assign` | `CampusDisposalTask` JSON | `ResultVO<CampusDisposalTask>` | 分派处置 |
| POST | `/campus/event/feedback` | `disposalTaskId,recordContent,attachmentDesc?` | `ResultVO<CampusDisposalRecord>` | 处置反馈 |
| POST | `/campus/event/return` | `disposalTaskId,recordContent` | `ResultVO<CampusDisposalRecord>` | 退回重办 |
| POST | `/campus/event/confirm` | `disposalTaskId,recordContent` | `ResultVO<CampusDisposalRecord>` | 复核确认 |
| POST | `/campus/event/record/add` | `eventId,recordContent,attachmentDesc?` | `ResultVO<CampusDisposalRecord>` | 单用户模式记录线下处置；服务层生成本地记录任务并把事件置为处理中 |
| POST | `/campus/event/archive` | `eventId,archiveConclusion` | `ResultVO<CampusEvent>` | 事件归档；单用户模式允许任意未归档事件填写结论后归档 |
| GET | `/campus/event/clue/list` | `eventId` | `ResultVO<List<CampusEventClue>>` | 关联线索 |
| GET | `/campus/event/clue/suggest` | `eventId,limit?` | `ResultVO<List<CampusClue>>` | 按事件主题、风险等级和已关联线索排除口径推荐相似线索 |
| GET | `/campus/event/account/list` | `eventId` | `ResultVO<List<CampusEventAccount>>` | 关联账号 |
| GET | `/campus/event/task/list` | `eventId` | `ResultVO<List<CampusDisposalTask>>` | 处置任务 |
| GET | `/campus/event/record/list` | `disposalTaskId` 或 `eventId` | `ResultVO<List<CampusDisposalRecord>>` | 处置记录；事件页按 `eventId` 展示线下处置台账 |

说明：当前校园事件前端按单用户台账模式使用，重点覆盖“线索归集 → 事件定级 → 记录线下处置 → 归档”。`assign/feedback/return/confirm` 多人派单接口保留兼容，但前端不作为主流程暴露。

### 预警中心（CampusAlertController）

| 方法 | 路径 | 参数/请求体 | 响应 | 说明 |
|------|------|-------------|------|------|
| GET | `/campus/alert/sensitive-word/list` | `pageNum,pageSize,keyword,wordCategory,riskLevel,status` | `ResultVO<PageInfo<CampusSensitiveWord>>` | 敏感词列表 |
| POST | `/campus/alert/sensitive-word/save` | `CampusSensitiveWord` JSON | `ResultVO<CampusSensitiveWord>` | 保存敏感词 |
| POST | `/campus/alert/sensitive-word/delete` | `wordId` | `ResultVO<Void>` | 删除敏感词 |
| GET | `/campus/alert/rule/list` | `pageNum,pageSize,keyword,ruleType,enabled` | `ResultVO<PageInfo<CampusAlertRule>>` | 规则列表 |
| POST | `/campus/alert/rule/save` | `CampusAlertRule` JSON | `ResultVO<CampusAlertRule>` | 保存规则 |
| POST | `/campus/alert/rule/delete` | `ruleId` | `ResultVO<Void>` | 删除规则 |
| GET | `/campus/alert/list` | `pageNum,pageSize,keyword,alertSource,riskLevel,alertStatus` | `ResultVO<PageInfo<CampusAlert>>` | 预警列表 |
| POST | `/campus/alert/create` | `CampusAlert` JSON | `ResultVO<CampusAlert>` | 人工创建预警 |
| POST | `/campus/alert/handle` | `alertId,alertStatus,handleOpinion?` | `ResultVO<CampusAlert>` | 处理/忽略预警 |
| POST | `/campus/alert/evaluate-clue` | `clueId` | `ResultVO<List<CampusAlert>>` | 线索触发预警评估 |
| POST | `/campus/alert/create-from-clue` | `clueId` | `ResultVO<List<CampusAlert>>` | 从线索生成预警；兼容舆情研判前端入口，内部复用线索预警评估 |
| POST | `/campus/alert/evaluate-account-content` | `contentId` | `ResultVO<List<CampusAlert>>` | 账号内容触发预警评估 |

说明：`CampusAlert` 新增 `evidenceJson`，用于保存结构化预警依据，包括来源对象、规则、风险等级、风险分、学校相关性、主题分类、命中词和原文链接等；人工预警缺省时生成最小依据快照。

### 数据接入（CampusIngestController）

| 方法 | 路径 | 参数/请求体 | 响应 | 说明 |
|------|------|-------------|------|------|
| GET | `/campus/ingest/source/list` | `pageNum,pageSize,keyword,sourceType,platform,enabled` | `ResultVO<PageInfo<CampusIngestSource>>` | 接入来源 |
| POST | `/campus/ingest/source/save` | `CampusIngestSource` JSON | `ResultVO<CampusIngestSource>` | 保存来源 |
| POST | `/campus/ingest/source/delete` | `sourceId` | `ResultVO<Void>` | 删除来源 |
| GET | `/campus/ingest/task/list` | `pageNum,pageSize,keyword,sourceId,targetType,taskStatus` | `ResultVO<PageInfo<CampusIngestTask>>` | 接入任务 |
| POST | `/campus/ingest/task/save` | `CampusIngestTask` JSON | `ResultVO<CampusIngestTask>` | 保存任务，`adapterType` 支持 `manual_push/third_party_api/public_web_pull/baidu_search` |
| POST | `/campus/ingest/task/update-status` | `taskId,taskStatus` | `ResultVO<CampusIngestTask>` | 启用/暂停/禁用 |
| POST | `/campus/ingest/task/delete` | `taskId` | `ResultVO<Void>` | 删除任务 |
| POST | `/campus/ingest/task/run` | `taskId` | `ResultVO<CampusIngestRunLog>` | 手动运行 |
| GET | `/campus/ingest/record/list` | `pageNum,pageSize,keyword,sourceId,taskId,normalizedStatus,targetType,startTime,endTime` | `ResultVO<PageInfo<CampusIngestRecord>>` | 接入记录 |
| POST | `/campus/ingest/record/submit` | `CampusIngestRecord` JSON | `ResultVO<CampusIngestRecord>` | 手动提交记录 |
| POST | `/campus/ingest/record/convert-clue` | `recordId` | `ResultVO<CampusClue>` | 记录转线索 |
| POST | `/campus/ingest/record/convert-account-content` | `recordId,accountId?` | `ResultVO<CampusAccountContent>` | 记录转账号动态 |
| POST | `/campus/ingest/run/start` | `taskId` | `ResultVO<CampusIngestRunLog>` | 开始运行日志 |
| POST | `/campus/ingest/run/finish` | `runId,runStatus?,fetchedCount?,successCount?,failCount?,errorMessage?` | `ResultVO<CampusIngestRunLog>` | 完成运行日志 |
| GET | `/campus/ingest/run/list` | `taskId` | `ResultVO<List<CampusIngestRunLog>>` | 任务运行日志 |
| GET | `/campus/ingest/run/page` | `pageNum,pageSize,taskId,runStatus,errorType,triggerType` | `ResultVO<PageInfo<CampusIngestRunLog>>` | 运行日志分页 |
| GET | `/campus/ingest/api-call/list` | `taskId,runId,provider,callStatus` | `ResultVO<List<CampusIngestApiCallLog>>` | 外部 API 调用日志；`provider` 包括 `tikhub`、`baidu`、`jina_reader` |
| GET/POST | `/campus/ingest/public-web/whitelist/*` | `list/save/update-status/delete` | `ResultVO` | 公开网页白名单 |

说明：上述运行日志、API 调用日志和 `third_party_api` 兼容接口仍作为后端能力保留；客户后台暂不展示数据接入业务页，`/admin/ingest` 直接访问重定向到 `/admin/monitor-tasks`。接入任务 `targetType=clue` 表示可自动沉淀为线索；监测任务自动维护的内部接入任务使用 `targetType=monitor_scan`，只供 `campus_monitor` 扫描，不在规则命中前自动转线索。运行日志 `runStatus` 支持 `running/success/partial_success/failed`；手动 finish 未显式传状态时，会按成功/失败计数自动归并，任务运行中“有成功也有失败”记录为 `partial_success`。接入记录自动转线索时继承记录侧 `riskLevel`，不再统一降级为普通关注；转换失败会把记录标记为 `normalized_status=failed`。

### 监测与检测任务

| 模块 | 核心路径 | 说明 |
|------|----------|------|
| 监测任务 | `GET /campus/monitor/overview` | 监测概览 |
| 监测任务 | `/campus/monitor/task/list/save/update-status/update-display/delete/run/ai-diagnose` | 监测任务 CRUD、启停、前台展示、手动运行、AI体检；自动维护接入任务 |
| 监测信息 | `/campus/monitor/information/list/count-by-platform` | 只展示已命中监测任务的 `campus_monitor_result` 数据，默认排除暂停、禁用、已隐藏或已删除任务数据 |
| 监测结果 | `/campus/monitor/result/list/alert/ignore/convert-clue` | 监测结果查询、转预警、忽略、转线索 |
| 任务内重点目标 | `/campus/monitor/watch-target/list/save/create-from-result/delete` | 本任务重点账号/指定链接维护 |
| 监测告警 | `/campus/monitor/alert/list/handle` | 监测来源预警查询和处理 |
| 监测日志 | `GET /campus/monitor/task/run-log/list` | 监测运行日志 |
| 检测主题 | `/campus/detection/topic/list/save/delete` | 检测主题维护 |
| 检测规则 | `/campus/detection/rule/list/save/delete` | 检测规则维护 |
| 检测任务 | `/campus/detection/task/list/save/update-status/delete/run` | 检测任务 CRUD、启停、手动运行 |
| 检测命中 | `/campus/detection/hit/list/alert/ignore` | 命中列表、转预警、忽略 |
| 检测日志 | `GET /campus/detection/run-log/list` | 检测运行日志 |

#### 监测任务 P0-P2 补充契约

| 方法 | 路径 | 参数/请求体 | 响应 | 权限 |
|------|------|-------------|------|------|
| GET | `/campus/monitor/task/list` | `pageNum,pageSize,keyword?,taskStatus?,platform?` | `ResultVO<PageInfo<CampusMonitorTask>>` | `campus:monitor:read` |
| POST | `/campus/monitor/task/save` | `CampusMonitorTask` JSON | `ResultVO<CampusMonitorTask>` | `campus:monitor:operate` |
| POST | `/campus/monitor/task/update-status` | `monitorTaskId,taskStatus(active/paused/disabled)` | `ResultVO<CampusMonitorTask>` | `campus:monitor:operate` |
| POST | `/campus/monitor/task/update-display` | `monitorTaskId,displayEnabled(1/0)` | `ResultVO<CampusMonitorTask>` | `campus:monitor:operate` |
| POST | `/campus/monitor/task/delete` | `monitorTaskId` | `ResultVO<Void>` | `campus:monitor:operate` |
| POST | `/campus/monitor/task/run` | `monitorTaskId` | `ResultVO<CampusMonitorRunLog>` | `campus:monitor:operate` |
| POST | `/campus/monitor/task/ai-diagnose` | `monitorTaskId` | `ResultVO<CampusMonitorTaskAiDiagnosis>` | `campus:monitor:operate` |
| GET | `/campus/monitor/task/run-log/list` | `pageNum,pageSize,monitorTaskId` | `ResultVO<PageInfo<CampusMonitorRunLog>>` | `campus:monitor:read` |
| GET | `/campus/monitor/information/list` | `pageNum,pageSize,keyword?,monitorTaskId?,sourcePlatform?,sourceSubPlatform?,riskLevel?,clueStatus?,language?,sentiment?,resultStatus?,publishTimeStart?,publishTimeEnd?,collectTimeStart?,collectTimeEnd?,matchScope?,similarDedup?,hitScope?(risk/all),sortBy?` | `ResultVO<PageInfo<CampusMonitorInformation>>` | `campus:monitor:read` |
| GET | `/campus/monitor/information/count-by-platform` | 同上，不含分页和排序 | `ResultVO<List<{name,value}>>` | `campus:monitor:read` |
| GET | `/campus/monitor/information/count-by-sub-platform` | 同上，不含分页和排序；保留为历史子来源统计兼容接口，前端不再固定展示公开论坛子标签 | `ResultVO<List<{name,value}>>` | `campus:monitor:read` |
| GET | `/campus/monitor/result/list` | `pageNum,pageSize,monitorTaskId?,keyword?,riskLevel?,resultStatus?,platform?,language?,converted?` | `ResultVO<PageInfo<CampusMonitorResult>>` | `campus:monitor:read` 或业务操作权限 |
| POST | `/campus/monitor/result/convert-clue` | `monitorResultId` | `ResultVO<CampusClue>` | `campus:monitor:operate` |
| POST | `/campus/monitor/result/sentiment` | `monitorResultId,sentiment(positive/neutral/negative/none)` | `ResultVO<CampusMonitorResult>` | `campus:monitor:operate` |
| POST | `/campus/monitor/result/ai-analyze` | JSON：`monitorResultIds?`, `monitorTaskId?`, `limit?(默认20,最大20)` | `ResultVO<CampusMonitorAiAnalyzeResponse>` | `campus:monitor:operate` |
| GET | `/campus/monitor/watch-target/list` | `pageNum,pageSize,monitorTaskId?,targetType?,platform?,keyword?,targetStatus?` | `ResultVO<PageInfo<CampusMonitorWatchTarget>>` | `campus:monitor:read` |
| POST | `/campus/monitor/watch-target/save` | `CampusMonitorWatchTarget` JSON | `ResultVO<CampusMonitorWatchTarget>` | `campus:monitor:operate` |
| POST | `/campus/monitor/watch-target/create-from-result` | `monitorResultId,monitorTaskId,targetType(account/link)` | `ResultVO<CampusMonitorWatchTarget>` | `campus:monitor:operate` |
| POST | `/campus/monitor/watch-target/delete` | `targetId` | `ResultVO<Void>` | `campus:monitor:operate` |

监测信息推荐参数：`hitScope=all/risk`，默认 `all`；`sortBy=publishTime/collectTime/relevance/sentiment`，旧值 `value/siteLevel` 后端仅兼容并落到默认发布时间排序；`sentiment=positive/neutral/negative/none`，多选用逗号分隔。

监测信息页允许通过 `/campus/monitor/result/sentiment` 人工校正单条监测命中的情感。后端只写入 `positive/neutral/negative/none`；若该监测命中已关联线索，同步更新 `campus_clue.sentiment` 并记录线索操作日志；若关联线索已归档，接口返回失败，不修改监测结果或线索。修改情感不会自动转预警、不会加入事件、不会改变 `riskMarked/resultStatus/clueStatus`。

监测任务 AI 体检通过 `/campus/monitor/task/ai-diagnose` 手动触发，只读取任务配置和近期聚合统计，返回 `summary/keywordSuggestions/negativeWordSuggestions/excludeWordSuggestions/platformSuggestions/frequencySuggestion/alertModeSuggestion/risks/suggestions`，不写回任务配置，也不展示具体采集内容。

监测命中 AI 分析通过 `/campus/monitor/result/ai-analyze` 手动触发。AI 输出只接受：`sentiment=positive/neutral/negative/none`、`shouldHit=hit/not_hit/uncertain`、`summary`、`hitReason`、`confidence(0-100)`、`schoolRelevanceScore(0-100)`、`matchedSchoolTerms`、`topicCategory/topicSubCategory/topicReason`、`riskLevel=normal/concern`、`riskReason`。成功后写入 `campus_monitor_result.sentiment/ai_summary/ai_hit_recommendation/ai_hit_reason/ai_confidence/ai_analysis_time/ai_provider_code/ai_model_code/risk_level/risk_score/school_relevance*/matched_school_terms/topic*`；若已转线索且线索未归档，同步更新 `campus_clue.sentiment/risk_level/school_relevance*/matched_school_terms/topic*` 并记录线索操作日志；已归档线索关联的监测命中跳过写入。AI 判断“不建议命中”只写 `ai_hit_recommendation=not_hit` 和理由，不自动忽略、不自动删除、不自动转预警、不改变 `resultStatus/alertId`。风险等级口径为：普通关键词只判断是否属于任务，不直接推高风险；只有负面词/风险词、原始非普通风险、负面情感或 AI 明确返回 `riskLevel=concern` 时，才进入“一般预警”风险等级。

监测模块返回的 Snowflake 业务 ID 必须按字符串序列化，避免浏览器把 19 位 Long 当作 `number` 后丢失精度。范围包括 `CampusMonitorInformation`、`CampusMonitorResult`、`CampusMonitorTask`、`CampusMonitorWatchTarget`、`CampusMonitorRunLog`、`CampusAlert` 中会被前端再次提交的 ID 字段，如 `monitorResultId`、`monitorTaskId`、`clueId`、`alertId`、`targetId`、`sourceObjectId`。对应 POST/GET 参数仍按 Long 绑定，前端可提交原样字符串或兼容数字。

`CampusMonitorTask` 新增/扩展字段：`keywordsI18n`、`negativeWordsI18n`、`excludeWordsI18n`、`displayEnabled`、`autoIngestEnabled`、`lastCollectTime`、`lastMatchCount`、`displayResultCount`、`lastErrorMessage`、`ingestCapabilityStatus`。多语言 JSON 示例：`{"zh":"招生,投诉","mongolian":"...","uyghur":"..."}`。旧字段 `keywords`、`negativeWords`、`excludeWords` 继续有效。只有 `taskStatus=active` 且 `displayEnabled=1` 的任务命中会进入 `/campus/monitor/information/**` 展示和平台统计；暂停/禁用任务停止调度并隐藏其历史命中，删除任务会软删除任务、停用调度并自动隐藏前台数据。`autoIngestEnabled=1` 时，保存/运行监测任务会通过 `campus_ingest` Service 自动创建或复用 `targetType=monitor_scan` 的内部接入任务，运行时先触发接入任务，再扫描对应 `campus_ingest_record`。普通前端不再手动提交 `ingestTaskIds`，仅高级诊断展示自动绑定结果。

`ingestCapabilityStatus` 取值：`ready` 可用、`partial` 部分可用、`unsupported` 平台未接入、`failed` 调用失败、`pending` 待运行。当前自动接入优先支持：百度新闻/公开网页（`baidu_search` + Jina Reader 正文增强配置）、TikHub 抖音/小红书/B站/微博/知乎/微信公众号/快手。公开论坛不再作为独立自动接入平台，历史论坛/贴吧/豆瓣数据按新闻/网页口径兼容展示。`alertMode=all_hits` 为兼容旧值保留，当前语义为“风险命中告警”：普通主题词命中不自动预警，只有负面词、风险词、非普通风险等级或高风险分才自动进入预警。

微博自动监测链路：`campus_monitor` 根据任务主体、别名、关键词和负面词构造 TikHub `weibo_search_all` 查询；`campus_ingest` 只接收包含微博帖子 ID 和正文文本的真实帖子候选，过滤搜索页、超话/话题统计卡、账号资料卡等非内容对象；开启 `detailEnabled=true` 后按微博 `id` 调 `weibo_post_detail_v2` 并请求长微博全文，详情只增强同一条接入记录，不生成第二条记录。微博原文链接只接受真实帖子链接（`weibo.com/{userId}/{postId}` 或 `m.weibo.cn/detail/{postId}`），旧 profile/search/external 链接不进入监测信息展示。

`CampusMonitorResult` 新增展示字段：`language`、`clueId`、`likeCount`、`commentCount`、`shareCount`、`collectCount`、`viewCount`、`schoolRelevanceScore`、`schoolRelevanceReason`、`matchedSchoolTerms`、`excludedReason`、`topicCategory`、`topicSubCategory`、`topicReason`。互动数字由接入层尽量解析，平台无返回时保持空值。学校相关性和主题分类由监测扫描时基于主体词、校园语境词、命中词和 `campus_event_topic` 字典生成。`convert-clue` 会优先复用 `campus_ingest_record.target_type=clue` 已绑定线索，避免监测结果和线索库重复生成；新建线索时会携带相关性和主题字段。

`CampusMonitorInformation` 是监测信息工作台的统一返回模型，不新增表。数据源仅为 `campus_monitor_result` 的只读视图，核心字段包括：`infoType,infoId,monitorResultId,clueId,title,content,summary,contentCaptureStatus,contentCaptureLabel,originalUrl,platform,sourcePlatform,sourceSubPlatform,authorName,publishTime,collectTime,publishTimeStatus,infoTime,matchedKeywords,matchedNegativeWords,keywords,sentiment,aiSummary,aiHitRecommendation,aiHitReason,aiConfidence,aiAnalysisTime,aiProviderCode,aiModelCode,riskLevel,riskMarked,resultStatus,clueStatus,likeCount,commentCount,shareCount,collectCount,viewCount,schoolRelevanceScore,schoolRelevanceReason,matchedSchoolTerms,topicCategory,topicSubCategory,topicReason`。`summary` 优先返回 `aiSummary`，无 AI 摘要时回退内容截断。`contentCaptureStatus` 取值为 `full/partial/missing`，用于区分完整正文、摘要/标题和未采集；`publishTimeStatus` 取值为 `known/missing/inferred`，发布时间缺失时前端展示“发布时间未知”并使用 `collectTime` 辅助排序和展示。统一列表默认 `hitScope=all`，只读取 active 且 `displayEnabled=1` 的真实 `campus_monitor_result`，不混入普通线索、搜索页沉淀内容和手工新增线索；`hitScope=risk` 仅返回 `riskMarked=true` 的关键词命中。新命中逻辑只以 `keywords` 作为命中条件，`monitorSubject/subjectAliases/matchedSubjects` 仅保留展示与历史兼容语义；负面词只生成风险标记，不再决定普通命中是否展示。`sentiment` 统一使用 `positive/neutral/negative/none`，后端兼容历史中文“疑似/确认”值。`similarDedup=true` 时按 `content_hash → 有效原文链接 → 标题+平台` 合并相似展示，并影响列表分页和平台统计。前端“详情”必须先展示站内内容详情，只有详情页/弹窗中的“查看原链接”才打开外部原文；非 `http/https` 原文链接不得作为可点击外链；“转线索/转预警/忽略/加重点账号/加指定链接”仅对有 `monitorResultId` 的行开放。

`CampusMonitorWatchTarget` 用于任务内重点账号/链接约束，核心字段：`monitorTaskId,targetType,platform,accountId,accountName,accountUid,linkUrl,sourceObjectType,sourceObjectId,authorizationScope,keywordScope,targetStatus`。DPI 或其它平台推送账号应复用该模型或账号模块保存入口，不能直接写库。

### 教育专题（CampusEducationController）

| 方法 | 路径 | 参数/请求体 | 响应 | 权限 |
|------|------|-------------|------|------|
| GET | `/campus/education/school/list` | `pageNum,pageSize,keyword?,region?,educationStage?,status?` | `ResultVO<PageInfo<CampusSchoolSubject>>` | `campus:education:read` |
| POST | `/campus/education/school/save` | `CampusSchoolSubject` JSON | `ResultVO<CampusSchoolSubject>` | `campus:education:operate` |
| POST | `/campus/education/school/delete` | `schoolId` | `ResultVO<Void>` | `campus:education:operate` |
| GET | `/campus/education/school/template` | 无 | `text/csv` 文件 | `campus:education:read` |
| POST | `/campus/education/school/import` | `multipart/form-data file=.csv` | `ResultVO<Map<String,Integer>>`，含 `inserted/updated/skipped/failed` | `campus:education:operate` |
| GET | `/campus/education/topic/list` | `topicType(education_news/policy/admission),startTime?,endTime?,limit?` | `ResultVO<List<Map>>` | `campus:education:read` |
| GET | `/campus/education/ranking/school-sentiment` | `keyword?,startTime?,endTime?,limit?` | `ResultVO<List<Map>>` | `campus:education:read` |
| POST | `/campus/education/baidu-task/create` | `CampusEducationBaiduTaskRequest` JSON | `ResultVO<CampusIngestTask>` | `campus:education:operate` |
| POST | `/campus/education/baidu-task/create-and-run` | `CampusEducationBaiduTaskRequest` JSON | `ResultVO<{task,runLog}>` | `campus:education:operate` |

教育专题遵守 Odoo 式模块归属：百度搜索只创建或运行 `campus_ingest` 接入任务，不在教育模块直接外呼百度；专题列表和学校排名只统计已进入线索库的数据。前端必须从 `/campus/ingest/source/list` 选择合法百度来源，不能手填不可追溯的来源 ID。

### 重点账号、报告、权限与基础数据

| 模块 | 核心路径 | 说明 |
|------|----------|------|
| AI 能力管理 | `/campus/ai/overview` | AI 供应商、功能和 24 小时调用概览 |
| AI 供应商 | `/campus/ai/provider/list/save/delete/test` | DeepSeek、TikHub、百度千帆、Jina Reader 和历史能力接入点配置；只保存 `credentialRef` |
| AI 模型 | `/campus/ai/model/list/save/delete` | 模型编码、上下文、温度、最大 token、流式支持和启停 |
| AI 功能绑定 | `/campus/ai/feature/list/save` | 报告、自动报告、研判、词云、监测AI分析、接入、正文提取、历史能力与供应商/模型绑定 |
| AI 提示词 | `/campus/ai/prompt/list/save/delete` | 功能级 system/user prompt 和输出格式模板 |
| AI 调用日志 | `/campus/ai/call-log/list` | DeepSeek 等通过 AI 管理层发起的脱敏调用日志 |
| 重点账号 | `/campus/account/list/detail/save/audit/update-status/delete` | 账号库、审核、启停/删除 |
| 账号任务/内容 | `/campus/account/task/add/list`、`/campus/account/content/add/list` | 关注任务和公开动态 |
| 分析任务 | `/campus/analysis/task/create/list/run`、`/campus/analysis/result/list/review` | 规则辅助研判任务和结果复核；当前默认 `modelProvider=local_heuristic` |
| 线索搜索 | `/campus/clue/list` | 前端 `/search` 仅搜索线索库，不搜索未转线索的监测信息或原始接入记录 |
| 报告模板 | `/campus/report/template/list/save/delete` | 报告模板；前端独立页面 `/report-templates`、`/report-templates/create`、`/report-templates/:templateId/edit` 复用该接口 |
| 报告生成 | `/campus/report/list/detail/events/save/generate/generate-ai/generate-ai-stream` | 报告保存、传统生成、AI 生成和 SSE 流；`generate-ai`/`generate-ai-stream` 可选 `aiUserPrompt`，返回 `ResultVO<CampusReport>` 并持久化 `reportContent/reportStatus/fileName/aiUserPrompt`；报告相关 Snowflake ID 响应按字符串序列化，入参兼容 Long 字符串 |
| 报告下载 | `/campus/report/download/download-docx/download-pptx` | Markdown/Word/PPT 导出 |
| 自动报告 | `/campus/auto-report/job/list/save/update-status/delete/run`、`/log/list` | 定时报告任务；任务 ID 和生成日志 ID 按字符串序列化，`generationMode=template/ai` 决定传统或 AI 生成；任务可保存 `eventId` 和长期 `aiUserPrompt` |
| 组织部门 | `/campus/department/list/tree/detail/save/delete` | 部门维护 |
| 字典 | `/campus/dict/type/list/save/delete`、`/item/list/enabled/save/delete` | 字典类型和条目 |
| 审计日志 | `GET /campus/audit/list` | 校园操作审计 |
| 权限 | `/campus/system/current-user/menu-tree/role/*/menu/list/api/list` | 角色、菜单、API 权限 |

AI 能力管理接口统一返回 `ResultVO<T>`。GET 查询使用 `campus:ai:read`，POST 保存、删除和供应商配置测试使用 `campus:ai:operate`；后台菜单权限为 `campus:ai:view`。P2 历史能力只做登记和启停管理，默认不写入当前校园主流程。

报告生成补充：`/campus/report/generate` 和 `/campus/report/generate-ai` 均使用报告配置的统一 scope 聚合线索库数据，支持 `scopeType/scopeKeywords/excludeKeywords/platformScope/riskLevels/departmentScope/monitorTaskIds/analysisProfile`。关联事件报告优先按 `eventId` 统计，事件业务 ID 同样按字符串序列化。AI 输入必须使用无循环引用的结构化快照，`aiUserPrompt` 只改变输出重点和表达，不改变规则统计口径。`/campus/report/generate-ai-stream` 通过 SSE `message` 事件实时返回内容块，完成发送 `done` 事件，失败发送 `error` 事件；前端完成后应重新读取报告详情，以数据库中已持久化的 `reportContent` 为准。

### Breaking Change 补充规则（校园模块）

1. 修改 `/campus/**` 路由、参数名、状态枚举、`ResultVO` 包装结构均视为 Breaking Change。
2. 新增校园 API 后必须同步补 `campus_permission_api` 初始化数据，并在本文档记录。
3. 改动状态字段时必须同步更新 [STATE_MACHINE.md](STATE_MACHINE.md) 和前端字典映射。
4. 改动权限时必须同步更新 [PERMISSION_RULES.md](PERMISSION_RULES.md)，并说明 `campus_admin/operator/viewer` 的影响范围。

## 待补全区域

- 所有页面 API 的完整请求参数和响应结构需要逐步细化记录
- 全文搜索模块约 50+ 方法，本文档仅列出核心部分
- 响应结构字段含义需逐步规范化
- 校园模块仍需逐步为每个 Entity 字段补充字段级说明，尤其是报告、检测、接入配置 JSON 字段
