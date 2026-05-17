# campus_ingest Manifest

## 模块定位

数据接入模块负责统一管理来源、接入任务、接入记录、百度搜索、TikHub 社媒接入、Jina Reader 正文增强和白名单公开网页边界。运行日志、外部调用日志和历史外部接口适配器保留为后端兼容能力，不在后台业务页展示。

## 依赖模块

- `campus_monitor`：监测任务扫描接入记录；自动采集场景下只允许通过 `CampusIngestService` 创建/复用/运行本模块接入来源和接入任务。
- `campus_ai`：TikHub、百度千帆和 Jina Reader 的接入点、密钥引用、超时和启停配置由 AI 能力管理模块统一登记，本模块保留原配置兜底。
- `campus_clue`：接入记录可转线索；只有明确 `target_type=clue` 的接入任务允许运行后自动沉淀线索。
- `campus_account_watch`：接入记录可转重点账号公开动态。

运行日志 `run_status` 支持 `running/success/partial_success/failed`；任务运行中部分成功不得整体回滚成功入库记录。

## 数据模型

- `campus_ingest_source`
- `campus_ingest_task`
- `campus_ingest_record`
- `campus_ingest_run_log`
- `campus_ingest_api_call_log`
- Jina Reader 不新增业务表；调用审计复用 `campus_ingest_api_call_log`，正文结果写入 `campus_ingest_record.content`。
- `target_type=monitor_scan` 为监测任务自动接入的内部目标类型，只供 `campus_monitor` 扫描，不自动转线索。

## API 契约

- `/campus/ingest/source/**`
- `/campus/ingest/task/**`
- `/campus/ingest/record/**`
- `/campus/ingest/run/**`
- `/campus/ingest/api-call/**`
- `/campus/ingest/public-web/whitelist/**`

## 前端入口

- 客户后台暂不展示数据接入业务页；`/admin/ingest` 直接访问会重定向到 `/admin/monitor-tasks`。
- `IngestView.vue` 和 `/campus/ingest/**` 后端能力保留，供内部运维、自动监测采集和后续授权场景复用。
- 运行日志、API 调用日志和历史外部接口供应商配置不作为客户后台默认能力展示。

## 权限

- 菜单：数据接入后台页。
- API：沿用 `campus:ingest:*` 现有授权，新增适配器不应新增绕过权限的入口。

## 测试影响

- 适配器返回内容必须标准化为 `CampusIngestItem`。
- 历史外部接口适配器保持后端兼容；如需正式下线，应另起迁移批次评估存量任务、运行记录和接口权限。
- 通用映射器只接收有正文/标题/描述的内容对象，不能把仅包含账号资料的对象当作舆情内容入库。
- 接入标准化必须输出纯文本，标题、正文、作者和关键词不得保留第三方搜索高亮 HTML（如 `<em class="keyword">`）或控制字符。
- 接入标准化必须过滤平台搜索反馈项、按钮项、举报原因和推荐控件；快手 `search_*`、`/short-video/search_*` 以及“与搜索词无关/内容过时/封面质量差/其他”等 UI 文案不得作为有效内容入库。
- 接入记录情感值统一为 `positive/neutral/negative/none`，历史中文“疑似/确认”值只做兼容迁移。
- 不同平台不一定返回点赞/评论/转发等互动数，字段允许为空。
- 平台返回了发布时间或点赞/评论/转发/收藏/浏览数时应尽量标准化进入 `CampusIngestItem`，供监测信息和线索库复用展示。
- 监测任务自动创建/复用的接入任务必须使用 `target_type=monitor_scan`；宽泛搜索结果进入 `campus_ingest_record` 后，必须由监测规则命中才允许出现在监测信息或转线索。
- `target_type=clue` 自动转线索时必须继承接入记录风险等级；转换失败的记录必须标记为 `normalized_status=failed`，便于后续重试或排查。
- TikHub 自动接入白名单覆盖抖音、小红书、微博、B站、知乎、微信公众号、快手；新增平台必须先补充 allowlist、URL 构造、通用映射测试和契约文档。
- TikHub 微信公众号 `wechat_mp_search_article` 自动接入使用监测主体主词作为查询词，默认 `sortType=_0`；外部接口返回“请重试/status=400/request_failed”类短暂失败时允许有限重试，但不能把失败结果伪造为空成功。
- TikHub 微信公众号请求成功但返回 0 条时，运行日志必须保留“请求成功但无可识别文章”的信息，便于区分接口失败和外部源无结果。
- TikHub 详情增强采用同记录增强：小红书 `xiaohongshu_search_notes` 按 `note_id`，微博 `weibo_search_all` 按 `id`，B站 `bilibili_search_by_type` 按 `aid` 调详情；详情只能增强同一条接入记录，不得作为第二条内容插入；详情失败必须在 raw_data 中标记，不得静默伪装为完整正文。
- 小红书同来源、同平台、同标题的近似重复内容优先合并到已存在记录，保留正文更长的一条，避免不同 note_id 的短摘要重复污染监测信息。
- 微博 `weibo_search_all` 必须过滤非帖子对象：搜索页、超话/话题统计卡、账号资料卡、无帖子 ID 或无正文文本的对象不得进入 `campus_ingest_record`；原文链接只能保存真实微博帖子链接（`weibo.com/{userId}/{postId}` 或 `m.weibo.cn/detail/{postId}`），不能把 `sinaweibo://tabbar`、`s.weibo.com` 搜索页、账号主页或外部博客链接当作正文原文。
- 百度搜索任务必须使用 `credentialRef` 或 Spring 配置，不能写入明文密钥。
- TikHub、百度千帆、Jina Reader 新调用优先读取 `campus_ai_provider`，供应商停用或未配置时回退现有 env/Spring 配置；真实密钥仍只能通过环境变量或受控配置注入。
- 百度搜索正文增强只能作为 `baidu_search` 的可选增强层：百度负责发现公开 URL，Jina Reader 只读取 URL 正文，失败默认保留百度摘要。
- `public_web_pull` 支持 `metadata_only` 和 `jina_reader` 两种模式；`jina_reader` 仍必须先命中公开网页白名单，第一版只允许单 URL，`maxDepth` 必须为 0。
- Jina Reader 外部调用必须写入 `campus_ingest_api_call_log`，并受任务 `daily_quota_limit` 约束；调用代理或官方 Reader 时必须使用固定非 Java 默认 User-Agent，避免 Cloudflare 按 `Java/1.8` 特征返回 403/1010。

## 禁止事项

- 禁止在 `fetch_config` 中保存真实 API Key、Cookie、Token、设备指纹、签名参数。
- 禁止公开网页绕过白名单。
- 禁止用 Jina Reader 读取登录后页面、内网地址、IP 地址、含 Cookie/Token/签名参数的 URL。
- 禁止在 `public_web_pull` 中做栏目递归、自动发现链接、浏览器采集、代理池、账号池或验证码绕过。
- 禁止在后台业务页暴露历史外部接口供应商调用细节。
